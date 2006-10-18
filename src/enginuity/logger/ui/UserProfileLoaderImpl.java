package enginuity.logger.ui;

import enginuity.logger.exception.ConfigurationException;
import static enginuity.logger.xml.SaxParserFactory.getSaxParser;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import javax.xml.parsers.SAXParser;
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
                SAXParser parser = getSaxParser();
                UserProfileHandler handler = new UserProfileHandler();
                parser.parse(inputStream, handler);
                return handler.getUserProfile();
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }
}
