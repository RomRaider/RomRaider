package enginuity.logger.ecu.definition;

import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.HexUtil.hexToInt;
import static enginuity.util.HexUtil.intToHexString;
import static enginuity.util.ParamChecker.checkGreaterThanZero;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.util.LinkedList;
import java.util.List;

public final class EcuAddressImpl implements EcuAddress {
    private final String[] addresses;
    private final byte[] bytes;
    private final int bit;


    public EcuAddressImpl(String address, int length, int bit) {
        checkNotNullOrEmpty(address, "address");
        checkGreaterThanZero(length, "length");
        this.addresses = buildAddresses(address, length);
        this.bytes = getAddressBytes(addresses);
        this.bit = bit;
    }

    public EcuAddressImpl(String[] addresses) {
        checkNotNullOrEmpty(addresses, "addresses");
        this.addresses = addresses;
        this.bytes = getAddressBytes(addresses);
        this.bit = -1;
    }

    public String[] getAddresses() {
        return addresses;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getBit() {
        return bit;
    }

    public int getLength() {
        return addresses.length;
    }

    private String[] buildAddresses(String startAddress, int addressLength) {
        List<String> addresses = new LinkedList<String>();
        int start = hexToInt(startAddress);
        for (int i = 0; i < addressLength; i++) {
            addresses.add(padAddress(intToHexString(start + i), startAddress.length()));
        }
//        System.out.println(startAddress + ":" + addressLength + " => " + addresses);
        return addresses.toArray(new String[addresses.size()]);
    }

    private String padAddress(String address, int length) {
        if (address.length() == length) {
            return address;
        } else {
            StringBuilder builder = new StringBuilder(length);
            builder.append("0x");
            String s = address.substring(2);
            for (int i = 0; i < length - s.length() - 2; i++) {
                builder.append('0');
            }
            builder.append(s);
            return builder.toString();
        }
    }

    private byte[] getAddressBytes(String[] addresses) {
        byte[] bytes = new byte[0];
        for (String address : addresses) {
            byte[] tmp1 = asBytes(address);
            byte[] tmp2 = new byte[bytes.length + tmp1.length];
            System.arraycopy(bytes, 0, tmp2, 0, bytes.length);
            System.arraycopy(tmp1, 0, tmp2, bytes.length, tmp1.length);
            bytes = tmp2;
        }
        return bytes;
    }
}
