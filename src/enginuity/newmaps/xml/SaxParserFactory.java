package enginuity.newmaps.xml;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


// Jared, this is the same as the SaxParserFactory that I used for the logger which I've moved to the enginuity.util package. You should probably use the same one..
public final class SaxParserFactory {

    private SaxParserFactory() {
    }

    public static SAXParser getSaxParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(false);
        parserFactory.setValidating(true);
        parserFactory.setXIncludeAware(false);
        return parserFactory.newSAXParser();
    }
}
