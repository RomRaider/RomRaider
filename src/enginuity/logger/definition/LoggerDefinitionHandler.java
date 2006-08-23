package enginuity.logger.definition;

import enginuity.util.ParamChecker;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

//TODO: Add metric and imperial units and conversions to logger.xml and parse them out. This will need a generic EcuParameterConvertor class.

public final class LoggerDefinitionHandler extends DefaultHandler {
    private static final String YES = "yes";
    private static final String TAG_PROTOCOL = "protocol";
    private static final String TAG_PARAMETER = "parameter";
    private static final String TAG_METRIC = "metric";
    private static final String TAG_BYTE = "byte";
    private static final String TAG_SWITCH = "switch";
    private static final String ATTR_ID = "id";
    private static final String ATTR_DESC = "desc";
    private static final String ATTR_UNITS = "units";
    private static final String ATTR_EXPRESSION = "expr";
    private static final String ATTR_FORMAT = "format";
    private static final String ATTR_BYTE = "byte";
    private static final String ATTR_BIT = "bit";
    private static final String ATTR_FILELOGCONTROLLER = "filelogcontroller";
    private List<EcuParameter> params;
    private List<EcuSwitch> switches;
    private String name;
    private String desc;
    private List<String> addressList;
    private int bit;
    private boolean fileLogController;
    private EcuDataConvertor convertor;
    private StringBuilder charBuffer;
    private String protocol;
    private boolean parseProtocol;

    public LoggerDefinitionHandler(String protocol) {
        ParamChecker.checkNotNullOrEmpty(protocol, "protocol");
        this.protocol = protocol;
    }

    public void startDocument() {
        params = new ArrayList<EcuParameter>();
        switches = new ArrayList<EcuSwitch>();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_PROTOCOL.equals(qName)) {
            parseProtocol = protocol.equalsIgnoreCase(attributes.getValue(ATTR_ID));
        } else if (parseProtocol) {
            if (TAG_PARAMETER.equals(qName)) {
                name = attributes.getValue(ATTR_ID);
                desc = attributes.getValue(ATTR_DESC);
                addressList = new ArrayList<String>();
            } else if (TAG_METRIC.equals(qName)) {
                convertor = new EcuParameterConvertorImpl(attributes.getValue(ATTR_UNITS), attributes.getValue(ATTR_EXPRESSION),
                        attributes.getValue(ATTR_FORMAT));
            } else if (TAG_SWITCH.equals(qName)) {
                name = attributes.getValue(ATTR_ID);
                desc = attributes.getValue(ATTR_DESC);
                addressList = new ArrayList<String>();
                addressList.add(attributes.getValue(ATTR_BYTE));
                bit = Integer.valueOf(attributes.getValue(ATTR_BIT));
                fileLogController = YES.equals(attributes.getValue(ATTR_FILELOGCONTROLLER));
            }
        }
        charBuffer = new StringBuilder();
    }

    public void characters(char[] ch, int start, int length) {
        if (parseProtocol) {
            charBuffer.append(ch, start, length);
        }
    }

    public void endElement(String uri, String localName, String qName) {
        if (TAG_PROTOCOL.equals(qName)) {
            parseProtocol = false;
        } else if (parseProtocol) {
            if (TAG_BYTE.equals(qName)) {
                addressList.add(charBuffer.toString());
            } else if (TAG_PARAMETER.equals(qName)) {
                String[] addresses = new String[addressList.size()];
                addressList.toArray(addresses);
                EcuParameter param = new EcuParameterImpl(name, desc, addresses, convertor);
                params.add(param);
            } else if (TAG_SWITCH.equals(qName)) {
                String[] addresses = new String[addressList.size()];
                addressList.toArray(addresses);
                EcuSwitch ecuSwitch = new EcuSwitchImpl(name, desc, addresses, new EcuSwitchConvertorImpl(bit), fileLogController);
                switches.add(ecuSwitch);
            }
        }
    }

    public List<EcuParameter> getEcuParameters() {
        return params;
    }

    public List<EcuSwitch> getEcuSwitches() {
        return switches;
    }
}
