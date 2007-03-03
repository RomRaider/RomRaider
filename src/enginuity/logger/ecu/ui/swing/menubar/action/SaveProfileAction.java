package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.logger.ecu.EcuLogger;
import static enginuity.logger.ecu.ui.swing.menubar.util.FileHelper.saveProfileToFile;

import java.awt.event.ActionEvent;
import java.io.File;

public final class SaveProfileAction extends AbstractAction {

    public SaveProfileAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            saveProfile();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }

    private void saveProfile() throws Exception {
        File lastProfileFile = new File(logger.getSettings().getLoggerProfileFilePath());
        String profileFilePath = saveProfileToFile(logger.getCurrentProfile(), lastProfileFile);
        logger.getSettings().setLoggerProfileFilePath(profileFilePath);
        logger.reportMessageInTitleBar("Profile: " + profileFilePath);
        logger.reportMessage("Profile succesfully saved: " + profileFilePath);
    }
}
