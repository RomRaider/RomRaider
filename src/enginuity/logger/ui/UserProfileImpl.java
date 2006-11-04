package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;
import enginuity.logger.definition.EcuParameter;
import enginuity.logger.definition.EcuSwitch;
import enginuity.logger.exception.ConfigurationException;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.isNullOrEmpty;

import java.util.Map;

public final class UserProfileImpl implements UserProfile {
    private final Map<String, UserProfileItem> params;
    private final Map<String, UserProfileItem> switches;

    public UserProfileImpl(Map<String, UserProfileItem> params, Map<String, UserProfileItem> switches) {
        checkNotNull(params, "params");
        checkNotNull(switches, "switches");
        this.params = params;
        this.switches = switches;
    }

    public boolean contains(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        return getMap(ecuData).keySet().contains(ecuData.getId());
    }

    public boolean isSelectedOnLiveDataTab(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        return getUserProfileItem(ecuData).isLiveDataSelected();
    }

    public boolean isSelectedOnGraphTab(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        return getUserProfileItem(ecuData).isGraphSelected();
    }

    public boolean isSelectedOnDashTab(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        return getUserProfileItem(ecuData).isDashSelected();
    }

    public EcuDataConvertor getSelectedConvertor(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        String defaultUnits = getUserProfileItem(ecuData).getUnits();
        if (defaultUnits != null && ecuData.getConvertors().length > 1) {
            for (EcuDataConvertor convertor : ecuData.getConvertors()) {
                if (defaultUnits.equals(convertor.getUnits())) {
                    return convertor;
                }
            }
            throw new ConfigurationException("Unknown default units, '" + defaultUnits + "', specified for " + ecuData.getName());
        } else {
            return ecuData.getSelectedConvertor();
        }
    }

    public byte[] getBytes() {
        return buildXml().getBytes();
    }

    private String buildXml() {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
        builder.append("<!DOCTYPE profile SYSTEM \"profile.dtd\">\n\n");
        builder.append("<profile>\n");
        if (!params.isEmpty()) {
            builder.append("    <parameters>\n");
            appendEcuDataElements(builder, "parameter", params, true);
            builder.append("    </parameters>\n");
        }
        if (!switches.isEmpty()) {
            builder.append("    <switches>\n");
            appendEcuDataElements(builder, "switch", switches, false);
            builder.append("    </switches>\n");
        }
        builder.append("</profile>\n");
        return builder.toString();
    }

    private void appendEcuDataElements(StringBuilder builder, String dataType, Map<String, UserProfileItem> dataMap, boolean showUnits) {
        for (String id : dataMap.keySet()) {
            UserProfileItem item = dataMap.get(id);
            builder.append("        <").append(dataType).append(" id=\"").append(id).append("\"");
            if (item.isLiveDataSelected()) {
                builder.append(" livedata=\"selected\"");
            }
            if (item.isGraphSelected()) {
                builder.append(" graph=\"selected\"");
            }
            if (item.isLiveDataSelected()) {
                builder.append(" dash=\"selected\"");
            }
            if (showUnits && !isNullOrEmpty(item.getUnits())) {
                builder.append(" units=\"").append(item.getUnits()).append("\"");
            }
            builder.append("/>\n");
        }
    }

    private UserProfileItem getUserProfileItem(EcuData ecuData) {
        return getMap(ecuData).get(ecuData.getId());
    }

    private Map<String, UserProfileItem> getMap(EcuData ecuData) {
        if (ecuData instanceof EcuParameter) {
            return params;
        } else if (ecuData instanceof EcuSwitch) {
            return switches;
        } else {
            throw new UnsupportedOperationException("Unknown EcuData type: " + ecuData.getClass());
        }
    }

}
