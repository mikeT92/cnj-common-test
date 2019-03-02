package edu.hm.cs.fwp.cloud.common.test.internal.config.spi;

import edu.hm.cs.fwp.cloud.common.test.internal.config.Config;

public interface ConfigBuilder {
    /**
     * Add the default config sources appearing on the builder's classpath
     * including:
     * <ol>
     * <li>System properties</li>
     * <li>Environment properties</li>
     * <li>/META-INF/microprofile-config.properties</li>
     * </ol>
     *
     * @return the ConfigBuilder with the default config sources
     */
    ConfigBuilder addDefaultSources();

    /**
     * Add the config sources appearing to be loaded via service loader pattern
     *
     * @return the ConfigBuilder with the autodiscovered config sources
     */
    ConfigBuilder addDiscoveredSources();

    /**
     * Add the converters to be loaded via service loader pattern
     *
     * @return the ConfigBuilder with the autodiscovered converters
     */
    ConfigBuilder addDiscoveredConverters();

    /**
     * Return the ConfigBuilder for a given classloader
     *
     * @param loader the specified classloader
     * @return the ConfigureBuilder for the given classloader
     */
    ConfigBuilder forClassLoader(ClassLoader loader);

    /**
     * Add the specified {@link ConfigSource}.
     *
     * @param sources the config sources
     * @return the ConfigBuilder with the configured sources
     */
    ConfigBuilder withSources(ConfigSource... sources);

    /**
     * Add the specified {@link Converter}.
     * This method uses reflection to determine what type the converter is for.
     * When using lambda expressions for custom converters you should use
     * {@link #withConverter(Class, int, Converter)} and pass the target type explicitly
     * as lambda expressions do not offer enough type information to the reflection API.
     *
     * @param converters the converters
     * @return the ConfigBuilder with the added converters
     */
    ConfigBuilder withConverters(Converter<?>... converters);


    /**
     * Add the specified {@link Converter} for the given type.
     * This method does not rely on reflection to determine what type the converter is for
     * therefore also lambda expressions can be used.
     *
     * @param type      the Class of type to convert
     * @param priority  the priority of the converter (custom converters have a default priority of 100).
     * @param converter the converter (can not be {@code null})
     * @param <T>       the type to convert
     * @return the ConfigBuilder with the added converters
     */
    <T> ConfigBuilder withConverter(Class<T> type, int priority, Converter<T> converter);

    /**
     * Build the {@link Config} object.
     *
     * @return the Config object
     */
    Config build();
}