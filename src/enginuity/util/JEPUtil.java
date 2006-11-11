package enginuity.util;

import org.nfunk.jep.JEP;

import java.util.Map;

public final class JEPUtil {

    private JEPUtil() {
    }

    public static double evaluate(String expression, double value) {
        JEP parser = new JEP();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addVariable("x", value);
        parser.parseExpression(expression);
        return parser.getValue();
    }

    public static double evaluate(String expression, Map<String, Double> valueMap) {
        JEP parser = new JEP();
        parser.initSymTab(); // clear the contents of the symbol table
        for (String id : valueMap.keySet()) {
            parser.addVariable(id, valueMap.get(id));
        }
        parser.parseExpression(expression);
        return parser.getValue();
    }
}
