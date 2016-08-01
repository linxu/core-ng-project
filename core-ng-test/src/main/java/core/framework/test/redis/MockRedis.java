package core.framework.test.redis;

import core.framework.api.redis.Redis;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public final class MockRedis implements Redis {
    private final Map<String, Value> store = Maps.newConcurrentHashMap();

    @Override
    public String get(String key) {
        Value value = store.get(key);
        if (value == null) return null;
        if (value.type != ValueType.VALUE) throw Exceptions.error("invalid type, key={}, type={}", key, value.type);
        if (value.expired(System.currentTimeMillis())) {
            store.remove(key);
            return null;
        }
        return value.value;
    }

    @Override
    public void set(String key, String value) {
        store.put(key, new Value(value));
    }

    @Override
    public void set(String key, String value, Duration expiration) {
        Value redisValue = new Value(value);
        redisValue.expirationTime = System.currentTimeMillis() + expiration.toMillis();
        store.put(key, redisValue);
    }

    @Override
    public boolean setIfAbsent(String key, String value, Duration expiration) {
        Value redisValue = new Value(value);
        redisValue.expirationTime = System.currentTimeMillis() + expiration.toMillis();
        Object previous = store.putIfAbsent(key, redisValue);
        return previous == null;
    }

    @Override
    public void expire(String key, Duration duration) {
        Value value = store.get(key);
        if (value != null) {
            value.expirationTime = System.currentTimeMillis() + duration.toMillis();
        }
    }

    @Override
    public void del(String key) {
        store.remove(key);
    }

    @Override
    public Map<String, String> mget(String... keys) {
        Map<String, String> results = Maps.newHashMapWithExpectedSize(keys.length);
        for (String key : keys) {
            String value = get(key);
            if (value != null) results.put(key, value);
        }
        return results;
    }

    @Override
    public void mset(Map<String, String> values) {
        values.forEach(this::set);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        Value value = store.get(key);
        if (value == null) return Maps.newHashMap();
        if (value.type != ValueType.HASH) throw Exceptions.error("invalid type, key={}, type={}", key, value.type);
        return new HashMap<>(value.hash);
    }

    @Override
    public String hget(String key, String field) {
        Value value = store.get(key);
        if (value == null) return null;
        if (value.type != ValueType.HASH) throw Exceptions.error("invalid type, key={}, type={}", key, value.type);
        return value.hash.get(field);
    }

    @Override
    public void hset(String key, String field, String value) {
        Value hashValue = store.computeIfAbsent(key, k -> new Value(Maps.newHashMap()));
        if (hashValue.type != ValueType.HASH) throw Exceptions.error("invalid type, key={}, type={}", key, hashValue.type);
        hashValue.hash.put(field, value);
    }

    @Override
    public void hmset(String key, Map<String, String> values) {
        Value hashValue = store.computeIfAbsent(key, k -> new Value(Maps.newHashMap()));
        if (hashValue.type != ValueType.HASH) throw Exceptions.error("invalid type, key={}, type={}", key, hashValue.type);
        hashValue.hash.putAll(values);
    }

    enum ValueType {
        VALUE, HASH
    }

    static class Value {
        ValueType type;
        String value;
        Map<String, String> hash;
        Long expirationTime;

        Value(String value) {
            type = ValueType.VALUE;
            this.value = value;
        }

        public Value(Map<String, String> hash) {
            type = ValueType.HASH;
            this.hash = hash;
        }

        boolean expired(long now) {
            return expirationTime != null && now >= expirationTime;
        }
    }
}
