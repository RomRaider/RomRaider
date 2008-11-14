package com.romraider.util.proxy;

import static com.romraider.util.ParamChecker.checkNotNull;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import static java.lang.System.currentTimeMillis;
import java.lang.reflect.Method;

public final class TimerWrapper implements Wrapper {
    private static final Logger LOGGER = getLogger(TimerWrapper.class);
    private final Object delegate;

    public TimerWrapper(Object delegate) {
        checkNotNull(delegate);
        this.delegate = delegate;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long start = currentTimeMillis();
        try {
            return method.invoke(delegate, args);
        } finally {
            long time = currentTimeMillis() - start;
            log(method, time);
        }
    }

    private void log(Method method, long time) {
        String c = delegate.getClass().getSimpleName();
        String m = method.getName();
        LOGGER.trace("[TIMER] - " + c + "." + m + ": " + time + "ms");
    }
}
