package enginuity.logger;

import enginuity.Settings;
import enginuity.logger.query.LoggerCallback;
import enginuity.util.HexUtil;

public final class TestEcuLogger {

    private TestEcuLogger() {
    }

    public static void main(String... args) {
        try {
            testLoggerController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testLoggerController() {
        LoggerController controller = new DefaultLoggerController(new Settings());
        try {
            controller.start();
            addLogger(controller, "0x291C8");
            addLogger(controller, "0x291F9");
            addLogger(controller, "0x286BB");
            sleep(60000);
            controller.removeLogger("0x291F9");
            controller.removeLogger("0x291C8");
            controller.removeLogger("0x286BB");
            addLogger(controller, "0x299C8");
            sleep(1000);
        } finally {
            controller.stop();
        }

    }

    private static void addLogger(LoggerController controller, String address) {
        controller.addLogger(address, new LoggerCallback() {
            public void callback(byte[] value) {
                printResponse(value);
            }
        });
    }

    private static void printResponse(byte[] value) {
        System.out.println("Response: " + HexUtil.asHex(value));
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
