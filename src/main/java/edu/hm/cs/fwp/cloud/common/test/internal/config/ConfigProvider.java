package edu.hm.cs.fwp.cloud.common.test.internal.config;

import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigProviderResolver;

public final class ConfigProvider {
    private static final ConfigProviderResolver INSTANCE = ConfigProviderResolver.instance();

    private ConfigProvider() {
    }

    /**
     * Provide a {@link Config} based on all {@link edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigSource ConfigSources} of the
     * current Thread Context ClassLoader (TCCL)
     * <p>
     *
     * <p>
     * <p>
     * The {@link Config} will be stored for future retrieval.
     * <p>
     * There is exactly a single Config instance per ClassLoader
     *
     * @return the config object for the thread context classloader
     */
    public static Config getConfig() {
        return INSTANCE.getConfig();
    }

    /**
     * Provide a {@link Config} based on all {@link edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigSource ConfigSources} of the
     * specified ClassLoader
     *
     * <p>
     * There is exactly a single Config instance per ClassLoader
     *
     * @param cl the specified classloader
     * @return the config for the specified classloader
     */
    public static Config getConfig(ClassLoader cl) {
        return INSTANCE.getConfig(cl);
    }
}