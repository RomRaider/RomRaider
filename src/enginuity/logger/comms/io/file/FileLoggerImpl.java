package enginuity.logger.comms.io.file;

import enginuity.Settings;
import enginuity.logger.exception.FileLoggerException;
import static enginuity.util.ParamChecker.checkNotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class FileLoggerImpl implements FileLogger {
    private static final String NEW_LINE = "\n";
    private final Settings settings;
    private boolean started = false;
    private OutputStream os = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public FileLoggerImpl(Settings settings) {
        checkNotNull(settings);
        this.settings = settings;
    }

    public void start() {
        if (!started) {
            stop();
            try {
                String filePath = buildFilePath();
                os = new BufferedOutputStream(new FileOutputStream(filePath));
                System.out.println("Started logging to file: " + filePath);
            } catch (Exception e) {
                stop();
                throw new FileLoggerException(e);
            }
            started = true;
        }
    }

    public void stop() {
        if (os != null) {
            try {
                os.close();
                System.out.println("Stopped logging to file.");
            } catch (Exception e) {
                throw new FileLoggerException(e);
            }
        }
        started = false;
    }

    public void writeLine(String line) {
        try {
            os.write(line.getBytes());
            if (!line.endsWith(NEW_LINE)) {
                os.write(NEW_LINE.getBytes());
            }
        } catch (Exception e) {
            stop();
            throw new FileLoggerException(e);
        }
    }

    public boolean isStarted() {
        return started;
    }

    private String buildFilePath() {
        String logDir = settings.getLoggerOutputDirPath();
        if (!logDir.endsWith(File.separator)) {
            logDir += File.separator;
        }
        logDir += "enginuitylog_" + dateFormat.format(new Date()) + ".csv";
        return logDir;
    }

}
