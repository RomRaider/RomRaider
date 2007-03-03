package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.logger.ecu.EcuLogger;
import static enginuity.logger.ecu.ui.swing.menubar.util.FileHelper.getFile;
import static enginuity.logger.ecu.ui.swing.menubar.util.FileHelper.getProfileFileChooser;

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.io.File;

public final class LoadProfileAction extends AbstractAction {

    public LoadProfileAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            loadProfileDialog();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }

    private void loadProfileDialog() throws Exception {
        File lastProfileFile = getFile(logger.getSettings().getLoggerProfileFilePath());
        JFileChooser fc = getProfileFileChooser(lastProfileFile);
        if (fc.showOpenDialog(logger) == JFileChooser.APPROVE_OPTION) {
            String profileFilePath = fc.getSelectedFile().getAbsolutePath();
            logger.loadUserProfile(profileFilePath);
            logger.getSettings().setLoggerProfileFilePath(profileFilePath);
            logger.reportMessageInTitleBar("Profile: " + profileFilePath);
            logger.reportMessage("Profile succesfully loaded: " + profileFilePath);
        }
    }

}
