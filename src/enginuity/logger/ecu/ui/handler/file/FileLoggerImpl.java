/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package enginuity.logger.ecu.ui.handler.file;

import enginuity.Settings;
import enginuity.logger.ecu.exception.FileLoggerException;
import enginuity.logger.ecu.ui.MessageListener;
import static enginuity.util.ParamChecker.checkNotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class FileLoggerImpl implements FileLogger {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private final Settings settings;
    private final MessageListener messageListener;
    private boolean started;
    private OutputStream os;
    private long startTimestamp;
    private boolean zero;

    public FileLoggerImpl(Settings settings, MessageListener messageListener) {
        checkNotNull(settings, messageListener);
        this.settings = settings;
        this.messageListener = messageListener;
    }

    public void start() {
        if (!started) {
            stop();
            try {
                String filePath = buildFilePath();
                os = new BufferedOutputStream(new FileOutputStream(filePath));
                messageListener.reportMessageInTitleBar("Started logging to file: " + filePath);
                zero = true;
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
                messageListener.reportMessageInTitleBar("Stopped logging to file.");
            } catch (Exception e) {
                throw new FileLoggerException(e);
            }
        }
        started = false;
    }

    public boolean isStarted() {
        return started;
    }

    public void writeHeaders(String headers) {
        writeText("Time" + headers);
    }

    public void writeLine(String line, long timestamp) {
        writeText(prependTimestamp(line, timestamp));
    }

    private void writeText(String text) {
        try {
            os.write(text.getBytes());
            if (!text.endsWith(NEW_LINE)) {
                os.write(NEW_LINE.getBytes());
            }
        } catch (Exception e) {
            stop();
            throw new FileLoggerException(e);
        }
    }

    private String prependTimestamp(String line, long timestamp) {
        String formattedTimestamp;
        if (settings.isFileLoggingAbsoluteTimestamp()) {
            formattedTimestamp = timestampFormat.format(new Date(timestamp));
        } else {
            if (zero) {
                formattedTimestamp = "0";
                startTimestamp = System.currentTimeMillis();
                zero = false;
            } else {
                formattedTimestamp = String.valueOf(System.currentTimeMillis() - startTimestamp);
            }
        }
        return new StringBuilder(formattedTimestamp).append(line).toString();
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
