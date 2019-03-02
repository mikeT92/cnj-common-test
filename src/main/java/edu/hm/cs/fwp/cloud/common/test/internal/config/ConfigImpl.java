package edu.hm.cs.fwp.cloud.common.test.internal.config;

import edu.hm.cs.fwp.cloud.common.test.internal.config.convert.Converters;
import edu.hm.cs.fwp.cloud.common.test.internal.config.convert.ImplicitConverters;
import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigSource;
import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.Converter;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;

public class ConfigImpl implements Config, Serializable {

    private static final long serialVersionUID = 8349556270741215206L;
    // delimiter is a comma that is not preceded by a \
    private static final String DELIMITER = "(?<!\\\\),";
    private final List<ConfigSource> configSources;
    private Map<Type, Converter> converters;

    ConfigImpl(List<ConfigSource> configSources, Map<Type, Converter> converters) {
        this.configSources = configSources;
        this.converters = new HashMap<>(Converters.ALL_CONVERTERS);
        this.converters.putAll(converters);
    }

    private static String[] split(String text) {
        if (text == null) {
            return new String[0];
        }
        String[] split = text.split(DELIMITER);
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].replace("\\,", ",");
        }
        return split;
    }

    @Override
    public <T> T getValue(String name, Class<T> aClass) {
        for (ConfigSource configSource : this.configSources) {
            String value = configSource.getValue(name);
            if (value != null) {
                return convert(value, aClass);
            }
        }
        throw new NoSuchElementException("Property " + name + " not found");
    }

    @Override
    public <T> Optional<T> getOptionalValue(String name, Class<T> aClass) {
        for (ConfigSource configSource : this.configSources) {
            String value = configSource.getValue(name);
            // treat empty value as null
            if (value != null && value.length() > 0) {
                return Optional.of(convert(value, aClass));
            }
        }
        return Optional.empty();
    }

    @Override
    public Iterable<String> getPropertyNames() {
        Set<String> names = new HashSet<>();
        for (ConfigSource configSource : this.configSources) {
            names.addAll(configSource.getProperties().keySet());
        }
        return names;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return this.configSources;
    }

    public <T> T convert(String value, Class<T> asType) {
        T result = null;
        if (value != null) {
            boolean isArray = asType.isArray();
            if (isArray) {
                String[] split = split(value);
                Class<?> componentType = asType.getComponentType();
                T array = (T) Array.newInstance(componentType, split.length);
                Converter<T> converter = getConverter(asType);
                for (int i = 0; i < split.length; i++) {
                    T s = converter.convert(split[i]);
                    Array.set(array, i, s);
                }
                result = array;
            } else {
                Converter<T> converter = getConverter(asType);
                result = converter.convert(value);
            }
        }
        return result;
    }

    private <T> Converter getConverter(Class<T> asType) {
        Converter result = null;
        if (asType.isArray()) {
            result = getConverter(asType.getComponentType());
        } else {
            Converter<T> converter = this.converters.get(asType);
            if (converter == null) {
                // look for implicit converters
                synchronized (this.converters) {
                    converter = ImplicitConverters.getConverter(asType);
                    this.converters.putIfAbsent(asType, converter);
                }
            }
            if (converter == null) {
                throw new IllegalArgumentException("No Converter registered for class " + asType);
            }
            result = converter;
        }
        return result;
    }

}
