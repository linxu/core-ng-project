package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import core.framework.api.log.ActionLogContext;
import core.framework.api.log.Markers;
import core.framework.api.util.StopWatch;
import core.framework.impl.async.ThreadPools;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public final class RabbitMQImpl implements RabbitMQ {
    public final Pool<Channel> pool;
    private final Logger logger = LoggerFactory.getLogger(RabbitMQImpl.class);
    private final ConnectionFactory connectionFactory = new ConnectionFactory();
    private final ExecutorService workerExecutor;
    private final ScheduledExecutorService heartbeatExecutor;
    private final Lock lock = new ReentrantLock();
    private List<Address> addresses;
    private long slowOperationThresholdInNanos = Duration.ofMillis(100).toNanos();
    private volatile Connection connection;

    public RabbitMQImpl() {
        workerExecutor = ThreadPools.fixedThreadPool(1, "rabbitMQ-worker-");
        connectionFactory.setSharedExecutor(workerExecutor);
        heartbeatExecutor = ThreadPools.singleThreadScheduler("rabbitMQ-heartbeat-");
        connectionFactory.setHeartbeatExecutor(heartbeatExecutor);
        connectionFactory.setAutomaticRecoveryEnabled(true);
        user("rabbitmq");       // default user/password
        password("rabbitmq");
        pool = new Pool<>(this::createChannel, Channel::close);
        pool.name("rabbitmq");
        pool.size(1, 50);
        pool.maxIdleTime(Duration.ofMinutes(30));
        timeout(Duration.ofSeconds(5));
    }

    public void close() {
        if (connection != null) {
            logger.info("close rabbitMQ client, hosts={}", addresses);
            try {
                pool.close();
                connection.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        workerExecutor.shutdown();
        heartbeatExecutor.shutdown();
    }

    public void user(String user) {
        connectionFactory.setUsername(user);
    }

    public void password(String password) {
        connectionFactory.setPassword(password);
    }

    public void hosts(String... hosts) {
        logger.info("set rabbitMQ hosts, hosts={}", Arrays.toString(hosts));
        addresses = Arrays.stream(hosts).map(Address::new).collect(Collectors.toList());
    }

    public void timeout(Duration timeout) {
        connectionFactory.setConnectionTimeout((int) timeout.toMillis());
        pool.checkoutTimeout(timeout);
    }

    public void slowOperationThreshold(Duration threshold) {
        slowOperationThresholdInNanos = threshold.toNanos();
    }

    @Override
    public RabbitMQConsumer consumer(String queue, int prefetchCount) {
        Channel channel = createChannel();
        return new RabbitMQConsumer(channel, queue, prefetchCount, slowOperationThresholdInNanos);
    }

    @Override
    public void publish(String exchange, String routingKey, byte[] message, AMQP.BasicProperties properties) {
        StopWatch watch = new StopWatch();
        PoolItem<Channel> item = pool.borrowItem();
        try {
            item.resource.basicPublish(exchange, routingKey, properties, message);
        } catch (AlreadyClosedException e) {    // rabbitmq throws AlreadyClosedException for channel error, e.g. channel is not configured correctly or not exists
            item.broken = true;
            throw e;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("rabbitMQ", elapsedTime);
            logger.debug("publish, exchange={}, routingKey={}, elapsedTime={}", exchange, routingKey, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    private void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_RABBITMQ"), "slow rabbitMQ operation, elapsedTime={}", elapsedTime);
        }
    }

    public Channel createChannel() {
        try {
            if (connection == null) {
                createConnection();
            }
            return connection.createChannel();
        } catch (IOException | TimeoutException e) {
            throw new Error(e);
        }
    }

    private void createConnection() throws IOException, TimeoutException {
        if (addresses == null || addresses.isEmpty()) throw new Error("addresses must not be empty");
        lock.lock();
        try {
            if (connection == null)
                connection = connectionFactory.newConnection(addresses);
        } finally {
            lock.unlock();
        }
    }
}
