package enginuity.logger.definition;

import enginuity.logger.definition.convertor.EcuParameterConvertor;
import enginuity.logger.exception.ConfigurationException;
import enginuity.util.ParamChecker;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

//TODO: Add metric and imperial units and conversions to logger.xml and parse them out. This will need a generic EcuParameterConvertor class.

public final class LoggerDefinitionHandler extends DefaultHandler {
    private static final String TAG_PROTOCOL = "protocol";
    private static final String TAG_PARAMETER = "parameter";
    private static final String TAG_CONVERTOR = "convertor";
    private static final String TAG_BYTE = "byte";
    private static final String ATTR_ID = "id";
    private static final String ATTR_DESC = "desc";
    private List<EcuParameter> params;
    private String paramName;
    private String paramDesc;
    private List<String> addressList;
    private EcuParameterConvertor convertor;
    private StringBuilder charBuffer;
    private String protocol;
    private boolean parseProtocol;

    public LoggerDefinitionHandler(String protocol) {
        ParamChecker.checkNotNullOrEmpty(protocol, "protocol");
        this.protocol = protocol;
    }

    public void startDocument() {
        params = new ArrayList<EcuParameter>();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_PROTOCOL.equals(qName) && protocol.equalsIgnoreCase(attributes.getValue(ATTR_ID))) {
            parseProtocol = true;
        } else if (parseProtocol) {
            if (TAG_PARAMETER.equals(qName)) {
                paramName = attributes.getValue(ATTR_ID);
                paramDesc = attributes.getValue(ATTR_DESC);
                addressList = new ArrayList<String>();
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
            } else if (TAG_CONVERTOR.equals(qName)) {
                try {
                    convertor = (EcuParameterConvertor) Class.forName(charBuffer.toString().trim()).newInstance();
                } catch (Exception e) {
                    throw new ConfigurationException(e);
                }
            } else if (TAG_PARAMETER.equals(qName)) {
                String[] addresses = new String[addressList.size()];
                addressList.toArray(addresses);
                EcuParameter param = new EcuParameterImpl(paramName, paramDesc, addresses, convertor);
                params.add(param);
            }
        }
    }

    public List<EcuParameter> getEcuParameters() {
        return params;
    }

}
