package enginuity.util;

@SuppressWarnings({"UnnecessaryBoxing"})
public final class ByteUtil {

    private ByteUtil() {
    }

    public static int asInt(byte[] bytes) {
        int i = 0;
        for (int j = 0; j < bytes.length; j++) {
            if (j > 0) {
                i <<= 8;
            }
            i |= bytes[j] & 0xFF;
        }
        return i;
    }

    public static byte asByte(int i) {
        return Integer.valueOf(i).byteValue();
    }

    public static int asInt(byte b) {
        return Byte.valueOf(b).intValue();
    }

    public static byte[] asBytes(int i) {
        byte[] b = new byte[4];
        for (int j = 0; j < 4; j++) {
            int offset = (b.length - 1 - j) * 8;
            b[j] = (byte) ((i >>> offset) & 0xFF);
        }
        return b;
    }
}
