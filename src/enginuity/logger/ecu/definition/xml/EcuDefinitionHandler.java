package enginuity.logger.ecu.definition.xml;

import enginuity.logger.ecu.definition.EcuDefinition;
import enginuity.logger.ecu.definition.EcuDefinitionImpl;
import static enginuity.util.ParamChecker.isNullOrEmpty;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/*
<romid>
    <xmlid>CAL ID:A4TC300K</xmlid>
    <internalidaddress>200</internalidaddress>
    <internalidstring>A4TC300K</internalidstring>
    <year>03</year>
    <transmission>AT</transmission>
    <ecuid>3614446205</ecuid>
</romid>
*/
public final class EcuDefinitionHandler extends DefaultHandler {
    private static final String TAG_ROMID = "romid";
    private static final String TAG_CALID = "internalidstring";
    private static final String TAG_ECUID = "ecuid";
    private Map<String, EcuDefinition> ecuDefinitionMap = new HashMap<String, EcuDefinition>();
    private String calId;
    private String ecuId;
    private StringBuilder charBuffer;

    public void startDocument() {
        ecuDefinitionMap = new HashMap<String, EcuDefinition>();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_ROMID.equals(qName)) {
            calId = "";
            ecuId = "";
        }
        charBuffer = new StringBuilder();
    }

    public void characters(char[] ch, int start, int length) {
        charBuffer.append(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) {
        if (TAG_ROMID.equals(qName)) {
            if (!isNullOrEmpty(ecuId) && !isNullOrEmpty(calId)) {
                ecuDefinitionMap.put(ecuId, new EcuDefinitionImpl(ecuId, calId));
            }
        } else if (TAG_CALID.equals(qName)) {
            calId = charBuffer.toString();
        } else if (TAG_ECUID.equals(qName)) {
            ecuId = charBuffer.toString();
        }
    }

    public Map<String, EcuDefinition> getEcuDefinitionMap() {
        return ecuDefinitionMap;
    }
}
