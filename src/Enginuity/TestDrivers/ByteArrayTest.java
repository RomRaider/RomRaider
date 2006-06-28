package Enginuity.TestDrivers;

import Enginuity.Maps.Table;
import Enginuity.Maps.XML.RomAttributeParser;

public class ByteArrayTest {
   
    public ByteArrayTest() {
    }
    
    public static void main(String args[]) {        
        int testValue = 2230;
        System.out.println("Test value: " + testValue);
        byte[] output = RomAttributeParser.parseIntegerValue(testValue, Table.ENDIAN_LITTLE, 2);
        System.out.println((output[0] & 0xff) + " " + (output[1] & 0xff));
        System.out.println(RomAttributeParser.parseByteValue(output, Table.ENDIAN_LITTLE, 0, 2));
    }    
}