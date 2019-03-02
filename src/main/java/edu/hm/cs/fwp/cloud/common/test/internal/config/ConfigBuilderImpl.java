package edu.hm.cs.fwp.cloud.common.test.internal.config;

import edu.hm.cs.fwp.cloud.common.test.internal.config.source.EnvConfigSource;
import edu.hm.cs.fwp.cloud.common.test.internal.config.source.PropertiesConfigSourceProvider;
import edu.hm.cs.fwp.cloud.common.test.internal.config.source.SysPropConfigSource;
import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigBuilder;
import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigSource;
import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigSourceProvider;
import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.Converter;

import javax.annotation.Priority;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ConfigBuilderImpl implements ConfigBuilder {

    private static final String META_INF_TEST_CONFIG_PROPERTIES = "META-INF/test-config.properties";

    // sources are not sorted by their ordinals
    private List<ConfigSource> sources = new ArrayList<>();
    private Map<Type, ConverterWithPriority> converters = new HashMap<>();
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private boolean addDefaultSources = false;
    private boolean addDiscoveredSources = false;
    private boolean addDiscoveredConverters = false;

    public ConfigBuilderImpl() {
    }

    @Override
    public ConfigBuilder addDiscoveredSources() {
        this.addDiscoveredSources = true;
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredConverters() {
        this.addDiscoveredConverters = true;
        return this;
    }

    private List<ConfigSource> discoverSources() {
        List<ConfigSource> discoveredSources = new ArrayList<>();
        ServiceLoader<ConfigSource> configSourceLoader = ServiceLoader.load(ConfigSource.class, this.classLoader);
        configSourceLoader.forEach(configSource -> {
            discoveredSources.add(configSource);
        });

        // load all ConfigSources from ConfigSourceProviders
        ServiceLoader<ConfigSourceProvider> configSourceProviderLoader = ServiceLoader.load(ConfigSourceProvider.class,
                this.classLoader);
        configSourceProviderLoader.forEach(configSourceProvider -> {
            configSourceProvider.getConfigSources(this.classLoader)
                    .forEach(configSource -> {
                        discoveredSources.add(configSource);
                    });
        });
        return discoveredSources;
    }

    private List<Converter> discoverConverters() {
        List<Converter> converters = new ArrayList<>();
        ServiceLoader<Converter> converterLoader = ServiceLoader.load(Converter.class, this.classLoader);
        converterLoader.forEach(converter -> {
            converters.add(converter);
        });
        return converters;
    }

    @Override
    public ConfigBuilder addDefaultSources() {
        this.addDefaultSources = true;
        return this;
    }

    private List<ConfigSource> getDefaultSources() {
        List<ConfigSource> defaultSources = new ArrayList<>();

        defaultSources.add(new EnvConfigSource());
        defaultSources.add(new SysPropConfigSource());
        defaultSources.addAll(
                new PropertiesConfigSourceProvider(META_INF_TEST_CONFIG_PROPERTIES, true, this.classLoader)
                        .getConfigSources(this.classLoader));

        return defaultSources;
    }

    @Override
    public ConfigBuilder forClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    @Override
    public ConfigBuilder withSources(ConfigSource... configSources) {
        for (ConfigSource source : configSources) {
            this.sources.add(source);
        }
        return this;
    }

    @Override
    public ConfigBuilder withConverters(Converter[] converters) {
        for (Converter converter : converters) {
            Type type = getConverterType(converter.getClass());
            if (type == null) {
                throw new IllegalStateException(
                        "Can not add converter " + converter + " that is not parameterized with a type");
            }
            addConverter(type, getPriority(converter), converter);
        }
        return this;
    }

    @Override
    public <T> ConfigBuilder withConverter(Class<T> type, int priority, Converter<T> converter) {
        addConverter(type, priority, converter);
        return this;
    }

    private void addConverter(Type type, int priority, Converter converter) {
        // add the converter only if it has a higher priority than another converter for
        // the same type
        ConverterWithPriority oldConverter = this.converters.get(type);
        int newPriority = getPriority(converter);
        if (oldConverter == null || priority > oldConverter.priority) {
            this.converters.put(type, new ConverterWithPriority(converter, newPriority));
        }
    }

    private Type getConverterType(Class clazz) {
        if (clazz.equals(Object.class)) {
            return null;
        }

        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                if (pt.getRawType().equals(Converter.class)) {
                    Type[] typeArguments = pt.getActualTypeArguments();
                    if (typeArguments.length != 1) {
                        throw new IllegalStateException(
                                "Converter " + clazz + " must be parameterized with a single type");
                    }
                    return typeArguments[0];
                }
            }
        }

        return getConverterType(clazz.getSuperclass());
    }

    private int getPriority(Converter converter) {
        int priority = 100;
        Priority priorityAnnotation = converter.getClass().getAnnotation(Priority.class);
        if (priorityAnnotation != null) {
            priority = priorityAnnotation.value();
        }
        return priority;
    }

    @Override
    public Config build() {
        if (this.addDiscoveredSources) {
            this.sources.addAll(discoverSources());
        }
        if (this.addDefaultSources) {
            this.sources.addAll(getDefaultSources());
        }

        if (this.addDiscoveredConverters) {
            for (Converter converter : discoverConverters()) {
                Type type = getConverterType(converter.getClass());
                if (type == null) {
                    throw new IllegalStateException(
                            "Can not add converter " + converter + " that is not parameterized with a type");
                }
                addConverter(type, getPriority(converter), converter);
            }
        }

        Collections.sort(this.sources, new Comparator<ConfigSource>() {
            @Override
            public int compare(ConfigSource o1, ConfigSource o2) {
                return o2.getOrdinal() - o1.getOrdinal();
            }
        });

        Map<Type, Converter> configConverters = new HashMap<>();
        this.converters
                .forEach((type, converterWithPriority) -> configConverters.put(type, converterWithPriority.converter));
        return new ConfigImpl(this.sources, configConverters);
    }

    private static class ConverterWithPriority {
        private final Converter converter;
        private final int priority;

        private ConverterWithPriority(Converter converter, int priority) {
            this.converter = converter;
            this.priority = priority;
        }
    }

}
