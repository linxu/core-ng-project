package core.framework.api;

import core.framework.impl.module.ModuleContext;
import core.framework.test.inject.TestBeanFactory;
import core.framework.test.module.InitDBConfig;
import core.framework.test.module.InitSearchConfig;
import core.framework.test.module.MockFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * @author neo
 */
public abstract class AbstractTestModule extends Module {
    private final Logger logger = LoggerFactory.getLogger(AbstractTestModule.class);

    public final void configure() {
        logger.info("initialize test context");
        context = new ModuleContext(new TestBeanFactory(), new MockFactoryImpl());

        logger.info("initialize application");
        initialize();
        context.config.validate();
        ((TestBeanFactory) context.beanFactory).validateOverrideBindings();
    }

    public <T> T overrideBinding(Class<? super T> type, T instance) {
        return overrideBinding(type, null, instance);
    }

    public <T> T overrideBinding(Type type, String name, T instance) {
        return ((TestBeanFactory) context.beanFactory).overrideBinding(type, name, instance);
    }

    public <T> T create(Class<T> instanceClass) {
        return context.beanFactory.create(instanceClass);
    }

    public InitDBConfig initDB() {
        return initDB(null);
    }

    public InitDBConfig initDB(String name) {
        return new InitDBConfig(context, name);
    }

    public InitSearchConfig initSearch() {
        return new InitSearchConfig(context);
    }
}