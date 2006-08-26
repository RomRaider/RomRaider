package enginuity.logger.io.serial;

import enginuity.logger.LoggerController;
import static enginuity.util.ParamChecker.checkNotNull;

public final class SerialPortRefresher implements Runnable {
    private SerialPortRefreshListener listener;
    private LoggerController controller;

    public SerialPortRefresher(SerialPortRefreshListener listener, LoggerController controller) {
        checkNotNull(listener, controller);
        this.listener = listener;
        this.controller = controller;
    }

    public void run() {
        while (true) {
            listener.refreshPortList(controller.listSerialPorts());
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
