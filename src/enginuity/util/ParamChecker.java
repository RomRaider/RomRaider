package enginuity.util;

import java.util.Collection;

public final class ParamChecker {

    private ParamChecker() {
    }

    public static void checkNotNull(Object param, String paramName) {
        if (param == null) {
            throw new IllegalArgumentException("Parameter " + paramName + " must not be null");
        }
    }

    public static void checkNotNull(Object... params) {
        for (int i = 0; i < params.length; i++) {
            checkNotNull(params[i], "arg" + i);
        }
    }

    public static void checkNotNullOrEmpty(String param, String paramName) {
        if (isNullOrEmpty(param)) {
            throw new IllegalArgumentException("Parameter " + paramName + " must not be null or empty");
        }
    }

    public static void checkNotNullOrEmpty(Object[] param, String paramName) {
        if (param == null || param.length == 0) {
            throw new IllegalArgumentException("Parameter " + paramName + " must not be null or empty");
        }
    }

    public static void checkNotNullOrEmpty(Collection<?> param, String paramName) {
        if (param == null || param.isEmpty()) {
            throw new IllegalArgumentException("Parameter " + paramName + " must not be null or empty");
        }
    }

    public static void checkGreaterThanZero(int param, String paramName) {
        if (param <= 0) {
            throw new IllegalArgumentException("Parameter " + paramName + " must be > 0");
        }
    }

    public static void checkNotNullOrEmpty(byte[] param, String paramName) {
        if (param == null || param.length == 0) {
            throw new IllegalArgumentException("Parameter " + paramName + " must not be null or empty");
        }
    }

    public static boolean isNullOrEmpty(String param) {
        return param == null || param.length() == 0;
    }
}
