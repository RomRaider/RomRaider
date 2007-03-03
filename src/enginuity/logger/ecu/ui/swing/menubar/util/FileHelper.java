package enginuity.logger.ecu.ui.swing.menubar.util;

import enginuity.logger.ecu.profile.UserProfile;

import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileHelper {
    private static final String USER_HOME_DIR = System.getProperty("user.home");

    private FileHelper() {
        throw new UnsupportedOperationException();
    }

    public static File getFile(String filePath) {
        return filePath == null ? new File(USER_HOME_DIR) : new File(filePath);
    }

    public static JFileChooser getProfileFileChooser(File lastProfileFile) {
        JFileChooser fc;
        if (lastProfileFile.exists() && lastProfileFile.isFile() && lastProfileFile.getParentFile() != null) {
            fc = new JFileChooser(lastProfileFile.getParentFile().getAbsolutePath());
        } else {
            fc = new JFileChooser();
        }
        fc.setFileFilter(new UserProfileFileFilter());
        return fc;
    }

    public static String saveProfileToFile(UserProfile profile, File destinationFile) throws IOException {
        String profileFilePath = destinationFile.getAbsolutePath();
        if (!profileFilePath.endsWith(".xml")) {
            profileFilePath += ".xml";
            destinationFile = new File(profileFilePath);
        }
        FileOutputStream fos = new FileOutputStream(destinationFile);
        try {
            fos.write(profile.getBytes());
        } finally {
            fos.close();
        }
        return profileFilePath;
    }

    public static JFileChooser getLoggerOutputDirFileChooser(File lastLoggerOutputDir) {
        JFileChooser fc;
        if (lastLoggerOutputDir.exists() && lastLoggerOutputDir.isDirectory()) {
            fc = new JFileChooser(lastLoggerOutputDir.getAbsolutePath());
        } else {
            fc = new JFileChooser();
        }
        fc.setFileSelectionMode(DIRECTORIES_ONLY);
        return fc;
    }
}
