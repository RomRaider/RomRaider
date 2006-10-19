package enginuity.logger.ui;

import static enginuity.logger.xml.SaxParserFactory.getSaxParser;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public final class UserProfileLoaderImpl implements UserProfileLoader {

    public UserProfile loadProfile(String userProfileFilePath) {
        checkNotNullOrEmpty(userProfileFilePath, "userProfileFilePath");
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(new File(userProfileFilePath)));
            try {
                UserProfileHandler handler = new UserProfileHandler();
                getSaxParser().parse(inputStream, handler);
                return handler.getUserProfile();
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            System.out.println("Error loading user profile file: " + userProfileFilePath);
            return null;
        }
    }
}
