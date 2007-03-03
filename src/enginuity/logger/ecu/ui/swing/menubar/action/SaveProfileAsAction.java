package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.logger.ecu.EcuLogger;
import static enginuity.logger.ecu.ui.swing.menubar.util.FileHelper.getFile;
import static enginuity.logger.ecu.ui.swing.menubar.util.FileHelper.getProfileFileChooser;
import static enginuity.logger.ecu.ui.swing.menubar.util.FileHelper.saveProfileToFile;

import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import java.awt.event.ActionEvent;
import java.io.File;

public final class SaveProfileAsAction extends AbstractAction {

    public SaveProfileAsAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            saveProfileAs();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }

    private void saveProfileAs() throws Exception {
        File lastProfileFile = getFile(logger.getSettings().getLoggerProfileFilePath());
        JFileChooser fc = getProfileFileChooser(lastProfileFile);
        if (fc.showSaveDialog(logger) == APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            if (!selectedFile.exists() || showConfirmDialog(logger, selectedFile.getName() + " already exists! Overwrite?") == OK_OPTION) {
                saveProfileToFile(logger.getCurrentProfile(), selectedFile);
            }
        }
    }
}
