package enginuity.util;

import org.nfunk.jep.JEP;

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
}
