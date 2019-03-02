package edu.hm.cs.fwp.cloud.common.test.internal.config.spi;

import java.util.Map;
import java.util.Set;

public interface ConfigSource {
    String CONFIG_ORDINAL = "config_ordinal";
    int DEFAULT_ORDINAL = 100;

    /**
     * Return the properties in this config source
     *
     * @return the map containing the properties in this config source
     */
    Map<String, String> getProperties();

    /**
     * Gets all property names known to this config source, without evaluating the values.
     * <p>
     * For backwards compatibility, there is a default implementation that just returns the keys of {@code getProperties()}
     * slower ConfigSource implementations should replace this with a more performant implementation
     *
     * @return the set of property keys that are known to this ConfigSource
     */
    default Set<String> getPropertyNames() {
        return getProperties().keySet();
    }

    /**
     * Return the ordinal for this config source. If a property is specified in multiple config sources, the value
     * in the config source with the highest ordinal takes precedence.
     * For the config sources with the same ordinal value, the config source names will
     * be used for sorting according to string sorting criteria.
     * Note that this property only gets evaluated during ConfigSource discovery.
     * <p>
     * The default ordinals for the default config sources:
     * <ol>
     * <li>System properties (ordinal=400)</li>
     * <li>Environment properties (ordinal=300)
     * <p>Some operating systems allow only alphabetic characters or an underscore(_), in environment variables.
     * Other characters such as ., /, etc may be disallowed.
     * In order to set a value for a config property that has a name containing such disallowed characters from an environment variable,
     * the following rules are used.
     * This ConfigSource searches for potentially 3 environment variables with a given property name (e.g. {@code "com.ACME.size"}):</p>
     * <ol>
     * <li>Exact match (i.e. {@code "com.ACME.size"})</li>
     * <li>Replace the character that is neither alphanumeric nor '_' with '_' (i.e. {@code "com_ACME_size"})</li>
     * <li>Replace the character that is neither alphanumeric nor '_' with '_' and convert to upper case
     * (i.e. {@code "COM_ACME_SIZE"})</li>
     * </ol>
     * <p>The first environment variable that is found is returned by this ConfigSource.</p>
     * </li>
     * <li>/META-INF/microprofile-config.properties (default ordinal=100)</li>
     * </ol>
     * <p>
     * <p>
     * Any ConfigSource part of an application will typically use an ordinal between 0 and 200.
     * ConfigSource provided by the container or 'environment' typically use an ordinal higher than 200.
     * A framework which intends have values overwritten by the application will use ordinals between 0 and 100.
     * The property "config_ordinal" can be specified to override the default value.
     *
     * @return the ordinal value
     */
    default int getOrdinal() {
        String configOrdinal = getValue(CONFIG_ORDINAL);
        if (configOrdinal != null) {
            try {
                return Integer.parseInt(configOrdinal);
            } catch (NumberFormatException ignored) {

            }
        }
        return DEFAULT_ORDINAL;
    }

    /**
     * Return the value for the specified property in this config source.
     *
     * @param propertyName the property name
     * @return the property value
     */
    String getValue(String propertyName);

    /**
     * The name of the config might be used for logging or analysis of configured values.
     *
     * @return the 'name' of the configuration source, e.g. 'property-file mylocation/myproperty.properties'
     */
    String getName();

}