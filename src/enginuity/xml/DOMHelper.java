package enginuity.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DOMHelper {

    private DOMHelper() {
    }

    public static String unmarshallText(Node textNode) {
        StringBuffer buf = new StringBuffer();

        Node n;
        NodeList nodes = textNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == Node.TEXT_NODE) {
                buf.append(n.getNodeValue());
            } else {
                // expected a text-only node (skip)
            }
        }
        return buf.toString();
    }

    public static String unmarshallAttribute(Node node, String name, String defaultValue) {
        Node n = node.getAttributes().getNamedItem(name);
        return (n != null) ? (n.getNodeValue()) : (defaultValue);
    }

    public static Double unmarshallAttribute(Node node, String name, double defaultValue) {
        return Double.parseDouble(unmarshallAttribute(node, name, String.valueOf(defaultValue)));
    }

    public static int unmarshallAttribute(Node node, String name, int defaultValue) {
        return Integer.parseInt(unmarshallAttribute(node, name, String.valueOf(defaultValue)));
    }

    public static boolean unmarshallAttribute(Node node, String name, boolean defaultValue) {
        return Boolean.parseBoolean(unmarshallAttribute(node, name, String.valueOf(defaultValue)));
    }

}
