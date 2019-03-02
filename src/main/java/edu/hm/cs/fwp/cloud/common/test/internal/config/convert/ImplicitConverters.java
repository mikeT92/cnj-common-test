package edu.hm.cs.fwp.cloud.common.test.internal.config.convert;

import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.Converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ImplicitConverters {

    public static Converter getConverter(Class<?> clazz) {
        for (Converter converter : new Converter[]{
                getConverterFromConstructor(clazz, String.class),
                getConverterFromConstructor(clazz, CharSequence.class),
                getConverterFromStaticMethod(clazz, "valueOf", String.class),
                getConverterFromStaticMethod(clazz, "valueOf", CharSequence.class),
                getConverterFromStaticMethod(clazz, "parse", String.class),
                getConverterFromStaticMethod(clazz, "parse", CharSequence.class)
        }) {
            if (converter != null) {
                return converter;
            }
        }
        return null;
    }

    private static Converter getConverterFromConstructor(Class<?> clazz, Class<?> paramType) {
        try {
            final Constructor<?> declaredConstructor = clazz.getDeclaredConstructor(paramType);
            if (!declaredConstructor.isAccessible()) {
                declaredConstructor.setAccessible(true);
            }
            return value -> {
                try {
                    return declaredConstructor.newInstance(value);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            };
        } catch (NoSuchMethodException e) {
        }
        return null;
    }

    private static Converter getConverterFromStaticMethod(Class<?> clazz, String methodName, Class<?> paramType) {
        try {
            final Method method = clazz.getMethod(methodName, paramType);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            if (Modifier.isStatic(method.getModifiers())) {
                return value -> {
                    try {
                        return method.invoke(null, value);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                };
            }
        } catch (NoSuchMethodException e) {
        }
        return null;
    }
}
