package enginuity.newmaps.definition;

import enginuity.newmaps.ecudata.Scale;

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

}