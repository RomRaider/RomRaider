package enginuity.newmaps.definition;

import enginuity.newmaps.ecudata.Scale;
import enginuity.newmaps.ecudata.Unit;
import java.util.StringTokenizer;
import java.util.Vector;

public final class AttributeParser {
    
    public static int parseEndian(String s) {        
        if (s.equalsIgnoreCase("big")) return Scale.ENDIAN_BIG;
        else return Scale.ENDIAN_LITTLE;
    }
    
    public static int parseStorageType(String s) {
        if (s.equalsIgnoreCase("int8")) {
            return Scale.STORAGE_TYPE_INT8;
        } else if (s.equalsIgnoreCase("uint8")) {
            return Scale.STORAGE_TYPE_UINT8;
        } else if (s.equalsIgnoreCase("int16")) {
            return Scale.STORAGE_TYPE_INT16;
        } else if (s.equalsIgnoreCase("uint16")) {
            return Scale.STORAGE_TYPE_UINT16;
        } else if (s.equalsIgnoreCase("float")) {
            return Scale.STORAGE_TYPE_FLOAT;
        } else if (s.equalsIgnoreCase("hex")) {
            return Scale.STORAGE_TYPE_HEX;
        } else {
            return Scale.STORAGE_TYPE_CHAR;
        }
    }
    
    public static String[] parseValueString(String s, String delim) {
        StringTokenizer t = new StringTokenizer(s, delim);
        Vector<String> values = new Vector<String>();
        while (t.hasMoreTokens()) {
            values.add(t.nextToken());
        }
        return (String[])values.toArray();
    }   
    
    public static int parseUnitSystem(String s) {
        if (s.equalsIgnoreCase("metric")) return Unit.SYSTEM_METRIC;
        else if (s.equalsIgnoreCase("standard")) return Unit.SYSTEM_STANDARD;
        else return Unit.SYSTEM_UNIVERSAL;
    }

}