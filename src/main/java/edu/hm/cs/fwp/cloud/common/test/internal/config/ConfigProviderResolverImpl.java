package edu.hm.cs.fwp.cloud.common.test.internal.config;

import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigBuilder;
import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigProviderResolver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ConfigProviderResolverImpl extends ConfigProviderResolver {

    public static final ConfigProviderResolverImpl INSTANCE = new ConfigProviderResolverImpl();

    private Map<ClassLoader, Config> configsForClassLoader = new HashMap<>();

    @Override
    public Config getConfig() {
        return getConfig(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Config getConfig(ClassLoader classLoader) {
        Config result = this.configsForClassLoader.get(classLoader);
        if (result == null) {
            result = getBuilder().forClassLoader(classLoader)
                    .addDefaultSources()
                    .addDiscoveredSources()
                    .addDiscoveredConverters()
                    .build();
            registerConfig(result, classLoader);
        }
        return result;
    }

    @Override
    public ConfigBuilder getBuilder() {
        return new ConfigBuilderImpl();
    }

    @Override
    public void registerConfig(Config config, ClassLoader classLoader) {
        this.configsForClassLoader.put(classLoader, config);
    }

    @Override
    public void releaseConfig(Config config) {
        Iterator<Map.Entry<ClassLoader, Config>> iterator = this.configsForClassLoader.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ClassLoader, Config> entry = iterator.next();
            if (entry.getValue() == config) {
                iterator.remove();
                return;
            }

        }
    }
}