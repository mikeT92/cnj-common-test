package edu.hm.cs.fwp.cloud.common.test.internal.config.spi;

public interface ConfigSourceProvider {

    /**
     * Return the collection of {@link ConfigSource}s.
     * For each e.g. property file, we return a single ConfigSource or an empty list if no ConfigSource exists.
     *
     * @param forClassLoader the classloader which should be used if any is needed
     * @return the {@link ConfigSource ConfigSources} to register within the {@link edu.hm.cs.fwp.cloud.common.test.internal.config.Config}.
     */
    Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader);
}