package edu.hm.cs.fwp.cloud.common.test.internal.config.convert;

import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.Converter;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class Converters {

    public static final Map<Type, Converter> ALL_CONVERTERS = new HashMap<>();
    @SuppressWarnings("unchecked")
    static final Converter<String> STRING_CONVERTER = (Converter & Serializable) value -> value;
    @SuppressWarnings("unchecked")
    static final Converter<Boolean> BOOLEAN_CONVERTER = (Converter & Serializable) value -> {
        if (value != null) {
            return "TRUE".equalsIgnoreCase(value)
                    || "1".equalsIgnoreCase(value)
                    || "YES".equalsIgnoreCase(value)
                    || "Y".equalsIgnoreCase(value)
                    || "ON".equalsIgnoreCase(value)
                    || "JA".equalsIgnoreCase(value)
                    || "J".equalsIgnoreCase(value)
                    || "OUI".equalsIgnoreCase(value);
        }
        return null;
    };
    @SuppressWarnings("unchecked")
    static final Converter<Double> DOUBLE_CONVERTER = (Converter & Serializable) value -> value != null
            ? Double.valueOf(value)
            : null;
    @SuppressWarnings("unchecked")
    static final Converter<Float> FLOAT_CONVERTER = (Converter & Serializable) value -> value != null
            ? Float.valueOf(value)
            : null;
    @SuppressWarnings("unchecked")
    static final Converter<Long> LONG_CONVERTER = (Converter & Serializable) value -> value != null
            ? Long.valueOf(value)
            : null;
    @SuppressWarnings("unchecked")
    static final Converter<Integer> INTEGER_CONVERTER = (Converter & Serializable) value -> value != null
            ? Integer.valueOf(value)
            : null;
    @SuppressWarnings("unchecked")
    static final Converter<Class<?>> CLASS_CONVERTER = (Converter & Serializable) value -> {
        try {
            return value != null ? Class.forName(value) : null;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    };

    static {
        ALL_CONVERTERS.put(String.class, STRING_CONVERTER);

        ALL_CONVERTERS.put(Boolean.class, BOOLEAN_CONVERTER);
        ALL_CONVERTERS.put(Boolean.TYPE, BOOLEAN_CONVERTER);

        ALL_CONVERTERS.put(Double.class, DOUBLE_CONVERTER);
        ALL_CONVERTERS.put(Double.TYPE, DOUBLE_CONVERTER);

        ALL_CONVERTERS.put(Float.class, FLOAT_CONVERTER);
        ALL_CONVERTERS.put(Float.TYPE, FLOAT_CONVERTER);

        ALL_CONVERTERS.put(Long.class, LONG_CONVERTER);
        ALL_CONVERTERS.put(Long.TYPE, LONG_CONVERTER);

        ALL_CONVERTERS.put(Integer.class, INTEGER_CONVERTER);
        ALL_CONVERTERS.put(Integer.TYPE, INTEGER_CONVERTER);

        ALL_CONVERTERS.put(Class.class, CLASS_CONVERTER);
    }
}
