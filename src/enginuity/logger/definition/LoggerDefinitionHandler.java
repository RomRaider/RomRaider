package enginuity.logger.definition;

import enginuity.util.ParamChecker;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LoggerDefinitionHandler extends DefaultHandler {
    private static final String YES = "yes";
    private static final String TAG_PROTOCOL = "protocol";
    private static final String TAG_PARAMETER = "parameter";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_DEPENDS = "depends";
    private static final String TAG_CONVERSION = "conversion";
    private static final String TAG_CONVERSIONS = "conversions";
    private static final String TAG_BYTE = "byte";
    private static final String TAG_REF = "ref";
    private static final String TAG_SWITCH = "switch";
    private static final String ATTR_ID = "id";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_DESC = "desc";
    private static final String ATTR_UNITS = "units";
    private static final String ATTR_EXPRESSION = "expr";
    private static final String ATTR_FORMAT = "format";
    private static final String ATTR_BYTE = "byte";
    private static final String ATTR_BIT = "bit";
    private static final String ATTR_FILELOGCONTROLLER = "filelogcontroller";
    private List<EcuParameter> params;
    private List<EcuSwitch> switches;
    private Map<String, EcuData> ecuDataMap;
    private String id;
    private String name;
    private String desc;
    private Set<String> addressList;
    private Set<String> dependsList;
    private boolean derived;
    private int bit;
    private boolean fileLogController;
    private Set<EcuDataConvertor> convertorList;
    private Set<EcuDerivedParameterConvertor> derivedConvertorList;
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
        ecuDataMap = new HashMap<String, EcuData>();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_PROTOCOL.equals(qName)) {
            parseProtocol = protocol.equalsIgnoreCase(attributes.getValue(ATTR_ID));
        } else if (parseProtocol) {
            if (TAG_PARAMETER.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
            } else if (TAG_ADDRESS.equals(qName)) {
                addressList = new LinkedHashSet<String>();
                derived = false;
            } else if (TAG_DEPENDS.equals(qName)) {
                dependsList = new LinkedHashSet<String>();
                derived = true;
            } else if (TAG_REF.equals(qName)) {
                dependsList.add(attributes.getValue(ATTR_ID));
            } else if (TAG_CONVERSIONS.equals(qName)) {
                convertorList = new LinkedHashSet<EcuDataConvertor>();
                derivedConvertorList = new LinkedHashSet<EcuDerivedParameterConvertor>();
            } else if (TAG_CONVERSION.equals(qName)) {
                if (derived) {
                    derivedConvertorList.add(new EcuDerivedParameterConvertorImpl(attributes.getValue(ATTR_UNITS), attributes.getValue(ATTR_EXPRESSION),
                            attributes.getValue(ATTR_FORMAT)));
                } else {
                    convertorList.add(new EcuParameterConvertorImpl(attributes.getValue(ATTR_UNITS), attributes.getValue(ATTR_EXPRESSION),
                            attributes.getValue(ATTR_FORMAT)));
                }
            } else if (TAG_SWITCH.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
                addressList = new HashSet<String>();
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
                EcuParameter param;
                if (derived) {
                    Set<EcuData> dependencies = new HashSet<EcuData>();
                    for (String refid : dependsList) {
                        dependencies.add(ecuDataMap.get(refid));
                    }
                    param = new EcuDerivedParameterImpl(id, name, desc, dependencies.toArray(new EcuData[dependencies.size()]),
                            derivedConvertorList.toArray(new EcuDerivedParameterConvertor[derivedConvertorList.size()]));
                } else {
                    String[] addresses = new String[addressList.size()];
                    addressList.toArray(addresses);
                    param = new EcuParameterImpl(id, name, desc, addresses, convertorList.toArray(new EcuDataConvertor[convertorList.size()]));
                }
                params.add(param);
                ecuDataMap.put(param.getId(), param);
            } else if (TAG_SWITCH.equals(qName)) {
                String[] addresses = new String[addressList.size()];
                addressList.toArray(addresses);
                EcuSwitch ecuSwitch = new EcuSwitchImpl(id, name, desc, addresses, new EcuDataConvertor[]{new EcuSwitchConvertorImpl(bit)}, fileLogController);
                switches.add(ecuSwitch);
                ecuDataMap.put(ecuSwitch.getId(), ecuSwitch);
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
