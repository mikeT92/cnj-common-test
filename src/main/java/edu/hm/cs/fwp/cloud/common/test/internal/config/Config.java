package edu.hm.cs.fwp.cloud.common.test.internal.config;

import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigSource;

import java.util.Optional;

public interface Config {

    /**
     * Return the resolved property value with the specified type for the
     * specified property name from the underlying {@link ConfigSource ConfigSources}.
     * <p>
     * If this method gets used very often then consider to locally store the configured value.
     *
     * @param <T>          The property type
     * @param propertyName The configuration propertyName.
     * @param propertyType The type into which the resolve property value should get converted
     * @return the resolved property value as an object of the requested type.
     * @throws java.lang.IllegalArgumentException if the property cannot be converted to the specified type.
     * @throws java.util.NoSuchElementException   if the property isn't present in the configuration.
     */
    <T> T getValue(String propertyName, Class<T> propertyType);

    /**
     * Return the resolved property value with the specified type for the
     * specified property name from the underlying {@link ConfigSource ConfigSources}.
     * <p>
     * If this method is used very often then consider to locally store the configured value.
     *
     * @param <T>          The property type
     * @param propertyName The configuration propertyName.
     * @param propertyType The type into which the resolve property value should be converted
     * @return The resolved property value as an Optional of the requested type.
     * @throws java.lang.IllegalArgumentException if the property cannot be converted to the specified type.
     */
    <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType);

    /**
     * Return a collection of property names.
     *
     * @return the names of all configured keys of the underlying configuration.
     */
    Iterable<String> getPropertyNames();

    /**
     * @return all currently registered {@link ConfigSource configsources} sorted with descending ordinal and ConfigSource name
     */
    Iterable<ConfigSource> getConfigSources();
}