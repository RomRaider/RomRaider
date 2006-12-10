package enginuity.logger.profile;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

public final class UserProfileHandler extends DefaultHandler {
    private static final String SELECTED = "selected";
    private static final String TAG_PARAMETER = "parameter";
    private static final String TAG_SWITCH = "switch";
    private static final String ATTR_ID = "id";
    private static final String ATTR_UNITS = "units";
    private static final String ATTR_LIVE_DATA = "livedata";
    private static final String ATTR_GRAPH = "graph";
    private static final String ATTR_DASH = "dash";
    private UserProfile profile;
    private HashMap<String, UserProfileItem> params;
    private HashMap<String, UserProfileItem> switches;

    public void startDocument() {
        params = new HashMap<String, UserProfileItem>();
        switches = new HashMap<String, UserProfileItem>();
        profile = new UserProfileImpl(params, switches);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_PARAMETER.equals(qName)) {
            params.put(attributes.getValue(ATTR_ID), getUserProfileItem(attributes));
        } else if (TAG_SWITCH.equals(qName)) {
            switches.put(attributes.getValue(ATTR_ID), getUserProfileItem(attributes));
        }
    }

    public UserProfile getUserProfile() {
        return profile;
    }

    private UserProfileItem getUserProfileItem(Attributes attributes) {
        return new UserProfileItemImpl(
                attributes.getValue(ATTR_UNITS),
                SELECTED.equalsIgnoreCase(attributes.getValue(ATTR_LIVE_DATA)),
                SELECTED.equalsIgnoreCase(attributes.getValue(ATTR_GRAPH)),
                SELECTED.equalsIgnoreCase(attributes.getValue(ATTR_DASH))
        );
    }

}
