package Enginuity.TestDrivers;

import org.nfunk.jep.*;
import org.nfunk.jep.type.*;

public class JEPTestDriver {
    
    private JEP myParser = new JEP();
    
    public JEPTestDriver() {
        
        String expression = "14.7/(1 + (x / 128))";
        double xValue = 255;
        
        myParser.initSymTab(); // clear the contents of the symbol table
        myParser.addStandardConstants();
        myParser.addComplex(); // among other things adds i to the symbol table
        myParser.addVariable("x", xValue);
        myParser.parseExpression(expression);
        double result = myParser.getValue();

        System.out.println(expression);
        System.out.println("x = " + xValue);
        System.out.println(result);
        
    }
    
    public static void main(String[] args) {
        new JEPTestDriver();
    }    
}