package edu.hm.cs.fwp.cloud.common.test.internal.config.spi;

import edu.hm.cs.fwp.cloud.common.test.internal.config.Config;
import edu.hm.cs.fwp.cloud.common.test.internal.config.ConfigProviderResolverImpl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ServiceLoader;

public abstract class ConfigProviderResolver {
    private static ConfigProviderResolver instance = new ConfigProviderResolverImpl();

    protected ConfigProviderResolver() {
    }

    /**
     * Creates a ConfigProviderResolver object
     * Only used internally from within {@link edu.hm.cs.fwp.cloud.common.test.internal.config.ConfigProvider}
     *
     * @return ConfigProviderResolver an instance of ConfigProviderResolver
     */
    public static ConfigProviderResolver instance() {
        if (instance == null) {
            synchronized (ConfigProviderResolver.class) {
                if (instance != null) {
                    return instance;
                }

                ClassLoader cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                    @Override
                    public ClassLoader run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
                if (cl == null) {
                    cl = ConfigProviderResolver.class.getClassLoader();
                }

                ConfigProviderResolver newInstance = loadSpi(cl);

                if (newInstance == null) {
                    throw new IllegalStateException(
                            "No ConfigProviderResolver implementation found!");
                }

                instance = newInstance;
            }
        }

        return instance;
    }

    private static ConfigProviderResolver loadSpi(ClassLoader cl) {
        if (cl == null) {
            return null;
        }

        // start from the root CL and go back down to the TCCL
        ClassLoader parentcl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return cl.getParent();
            }
        });
        ConfigProviderResolver instance = loadSpi(parentcl);

        if (instance == null) {
            ServiceLoader<ConfigProviderResolver> sl = ServiceLoader.load(
                    ConfigProviderResolver.class, cl);
            for (ConfigProviderResolver spi : sl) {
                if (instance != null) {
                    throw new IllegalStateException(
                            "Multiple ConfigResolverProvider implementations found: "
                                    + spi.getClass().getName() + " and "
                                    + instance.getClass().getName());
                } else {
                    instance = spi;
                }
            }
        }
        return instance;
    }

    /**
     * Set the instance. It is used by OSGi environment while service loader
     * pattern is not supported.
     *
     * @param resolver set the instance.
     */
    public static void setInstance(ConfigProviderResolver resolver) {
        instance = resolver;
    }

    /**
     * @return config the config object for the Thread Context Classloader
     * @see edu.hm.cs.fwp.cloud.common.test.internal.config.ConfigProvider#getConfig()
     */
    public abstract Config getConfig();

    /**
     * @param loader the classloader
     * @return config the config object for the specified classloader
     * @see edu.hm.cs.fwp.cloud.common.test.internal.config.ConfigProvider#getConfig(ClassLoader)
     */
    public abstract Config getConfig(ClassLoader loader);

    /**
     * Create a fresh {@link ConfigBuilder} instance.
     * <p>
     * This ConfigBuilder will initially contain no {@link ConfigSource}. The other {@link ConfigSource} will have
     * to be added manually or discovered by calling {@link ConfigBuilder#addDiscoveredSources()}.
     * <p>
     * This ConfigBuilder will initially contain default {@link Converter Converters}. Any other converters will need to
     * be added manually.
     * <p>
     * The ConfigProvider will not manage the Config instance internally
     *
     * @return a fresh ConfigBuilder
     */
    public abstract ConfigBuilder getBuilder();

    /**
     * Register a given {@link Config} within the Application (or Module) identified by the given ClassLoader.
     * If the ClassLoader is {@code null} then the current Application will be used.
     *
     * @param config      which should get registered
     * @param classLoader which identifies the Application or Module the given Config should get associated with.
     * @throws IllegalStateException if there is already a Config registered within the Application.
     *                               A user could explicitly use {@link #releaseConfig(Config)} for this case.
     */
    public abstract void registerConfig(Config config, ClassLoader classLoader);

    /**
     * A {@link Config} normally gets released if the Application it is associated with gets destroyed.
     * Invoke this method if you like to destroy the Config prematurely.
     * <p>
     * If the given Config is associated within an Application then it will be unregistered.
     *
     * @param config the config to be released
     */
    public abstract void releaseConfig(Config config);
}