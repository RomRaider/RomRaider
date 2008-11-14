package com.romraider.util.proxy;

import java.lang.reflect.Constructor;
import static java.lang.reflect.Proxy.newProxyInstance;

public final class Proxifier {
    public static <T> T proxy(T t, Class<? extends Wrapper> cls) {
        Wrapper wrapper = instantiate(cls, t);
        return proxy(t, wrapper);
    }

    private static <T> T proxy(T t, Wrapper wrapper) {
        Class<?> cls = t.getClass();
        ClassLoader loader = cls.getClassLoader();
        Class<?>[] interfaces = cls.getInterfaces();
        return (T) newProxyInstance(loader, interfaces, wrapper);
    }

    private static <T> Wrapper instantiate(Class<? extends Wrapper> wrapper, T t) {
        try {
            Constructor<?> constructor = wrapper.getConstructor(Object.class);
            return (Wrapper) constructor.newInstance(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
