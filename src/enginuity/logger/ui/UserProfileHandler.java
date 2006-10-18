package enginuity.logger.ui;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashSet;
import java.util.Set;

public final class UserProfileHandler extends DefaultHandler {
    private static final String TAG_PARAMETER = "parameter";
    private static final String TAG_SWITCH = "switch";
    private static final String ATTR_ID = "id";
    private UserProfile profile;
    private Set<String> params;
    private Set<String> switches;

    public void startDocument() {
        params = new HashSet<String>();
        switches = new HashSet<String>();
        profile = new UserProfileImpl(params, switches);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_PARAMETER.equals(qName)) {
            params.add(attributes.getValue(ATTR_ID));
        } else if (TAG_SWITCH.equals(qName)) {
            switches.add(attributes.getValue(ATTR_ID));
        }
    }

    public UserProfile getUserProfile() {
        return profile;
    }
}
