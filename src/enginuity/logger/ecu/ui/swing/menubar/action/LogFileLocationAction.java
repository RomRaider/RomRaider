package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.logger.ecu.EcuLogger;
import static enginuity.logger.ecu.ui.swing.menubar.util.FileHelper.getFile;
import static enginuity.logger.ecu.ui.swing.menubar.util.FileHelper.getLoggerOutputDirFileChooser;

import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import java.awt.event.ActionEvent;
import java.io.File;

public final class LogFileLocationAction extends AbstractAction {

    public LogFileLocationAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            setLogFileLocationDialog();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }

    private void setLogFileLocationDialog() throws Exception {
        File lastLoggerOutputDir = getFile(logger.getSettings().getLoggerOutputDirPath());
        JFileChooser fc = getLoggerOutputDirFileChooser(lastLoggerOutputDir);
        if (fc.showOpenDialog(logger) == APPROVE_OPTION) {
            String loggerOutputDirPath = fc.getSelectedFile().getAbsolutePath();
            logger.getSettings().setLoggerOutputDirPath(loggerOutputDirPath);
            logger.reportMessage("Log file output location successfully updated: " + loggerOutputDirPath);
        }
    }
}
