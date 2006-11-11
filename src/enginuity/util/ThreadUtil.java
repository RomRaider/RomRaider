package enginuity.util;

import java.util.concurrent.TimeUnit;

public final class ThreadUtil {

    private ThreadUtil() {
    }

    public static void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
