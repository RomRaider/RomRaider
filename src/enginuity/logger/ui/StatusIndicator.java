package enginuity.logger.ui;

import javax.swing.*;
import java.awt.*;
import static java.awt.BorderLayout.WEST;

public final class StatusIndicator extends JPanel implements ControllerListener, FileLoggerListener {
    private final JLabel statusLabel = new JLabel();
    private static final String TEXT_LOGGING = "Reading data...";
    private static final String TEXT_STOPPED = "Stopped.";
    private static final String TEXT_FILE_LOGGING = "Logging to file...";
    private static final ImageIcon ICON_FILE_LOGGING = new ImageIcon("./graphics/logger_green.png");
    private static final ImageIcon ICON_STOPPED = new ImageIcon("./graphics/logger_red.png");
    private static final ImageIcon ICON_LOGGING = new ImageIcon("./graphics/logger_blue.png");

    public StatusIndicator() {
        setLayout(new BorderLayout());
        add(statusLabel, WEST);
        stop();
    }

    public void start() {
        statusLabel.setText(TEXT_LOGGING);
        statusLabel.setIcon(ICON_LOGGING);
    }

    public void stop() {
        statusLabel.setText(TEXT_STOPPED);
        statusLabel.setIcon(ICON_STOPPED);
    }

    public void setLoggingToFile(boolean loggingToFile) {
        if (loggingToFile) {
            fileLogging();
        } else {
            start();
        }
    }

    private void fileLogging() {
        statusLabel.setText(TEXT_FILE_LOGGING);
        statusLabel.setIcon(ICON_FILE_LOGGING);
    }
}
