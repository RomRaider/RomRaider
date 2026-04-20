/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2018 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.io.j2534.api;

import static com.romraider.util.ByteUtil.asUnsignedInt;
import static com.romraider.util.HexUtil.asHex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.romraider.io.j2534.api.J2534Impl.Config;
import com.romraider.io.j2534.api.J2534Impl.Protocol;
import com.romraider.io.j2534.api.J2534Impl.TxFlags;
import com.romraider.logger.ecu.exception.InvalidResponseException;
import com.romraider.util.HexUtil;
import com.romraider.util.LogManager;

/**
 * This class is used to exercise the J2534 API against a real J2534 device and
 * an active ECU using the ISO15765-2 protocol.
 */
public final class TestJ2534IsoTp {
    private static J2534 api ;
    private static final int LOOPBACK = 0;
    private static final byte[] mask1 = {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    private static final byte[] match1 = {
            (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xe8};
    private static final byte[] fc1 = {
            (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xe0};
    private static final byte ECU_NRC = (byte) 0x7F;
    private static final byte READ_MODE9_RESPONSE = (byte) 0x49;
    private static final byte READ_MODE9_COMMAND = (byte) 0x09;
    private static final byte[] READ_MODE9_PIDS = {
            (byte) 0x02, (byte) 0x04, (byte) 0x06};
    private static final byte READ_MODE3_RESPONSE = (byte) 0x43;
    private static final byte READ_MODE3_COMMAND = (byte) 0x03;
    private static final byte ECU_INIT_COMMAND = (byte) 0x01;
    private static final byte ECU_INIT_RESPONSE = (byte) 0x41;
    private static final StringBuilder sb = new StringBuilder(); 


    public TestJ2534IsoTp() throws InterruptedException {
        final int deviceId = api.open();
        sb.delete(0, sb.capacity());
        try {
            version(deviceId);
            final int channelId = api.connect(deviceId, 0, 500000);
            
            final double vBatt = api.getVbattery(deviceId);
//            System.out.println("Pin 16: " + vBatt + " VDC");
            sb.append(String.format(
                    "J2534 Interface Pin 16: %sVDC%n", vBatt));

            final byte[] mask2 = {
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            final byte[] match2 = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xdf};
            final byte[] fc2 = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xdf}; //Tester
            
            final byte[] mask3 = {
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            final byte[] match3 = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xe0};
            final byte[] fc3 = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xe0};
            
            final int msgId = api.startFlowCntrlFilter(
                    channelId, mask1, match1, fc1, TxFlags.ISO15765_FRAME_PAD);
//            final int msgId1 = api.startFlowCntrlFilter(
//                    channelId, mask2, match2, fc2, TxFlags.ISO15765_FRAME_PAD);
//            final int msgId2 = api.startFlowCntrlFilter(
//                    channelId, mask3, match3, fc3, TxFlags.ISO15765_FRAME_PAD);

            try {
                setConfig(channelId);
                getConfig(channelId);
                sb.append(String.format("%n--- Vehicle Information ---%n"));

                for (byte pid : READ_MODE9_PIDS) {
                    final byte[] mode9 = buildRequest(
                            READ_MODE9_COMMAND,     // mode
                            pid,                    // pid
                            true,                   // pid valid
                            fc1);                   // source
                    api.writeMsg(channelId, 
                            mode9,
                            1000L, 
                            TxFlags.ISO15765_FRAME_PAD);
    
                    final byte[] response;
                    response = api.readMsg(channelId, 1, 1000L);
//                    System.out.println("Response = " +
//                            HexUtil.asHex(response));
                    handleResponse(response);
                }
                final byte[] mode3 = buildRequest(
                        READ_MODE3_COMMAND,     // mode
                        (byte) 0x00,            // pid
                        false,                  // pid valid
                        fc1);                   // source
                api.writeMsg(channelId, 
                        mode3,
                        1000L, 
                        TxFlags.ISO15765_FRAME_PAD);

                final byte[] response;
                response = api.readMsg(channelId, 1, 1000L);
//                System.out.println("Response = " +
//                        HexUtil.asHex(response));
                handleResponse(response);
            }
            catch (Exception e) {
//                System.out.println(e);
                sb.append(e);
            }
            finally {
                api.stopMsgFilter(channelId, msgId);
//                api.stopMsgFilter(channelId, msgId1);
//                api.stopMsgFilter(channelId, msgId2);
                api.disconnect(channelId);
            }
        } finally {
            api.close(deviceId);
        }
    }

    public final String toString() {
        return sb.toString();
    }

    private final static void handleResponse(byte[] response) {
        validateResponse(response);
        final int responseLen = response.length;
        if (response[4] == READ_MODE9_RESPONSE) {
            final byte[] data = new byte[responseLen - 7];
            System.arraycopy(response, 7, data, 0, data.length);
            if (response[5] == 0x02) {
//                System.out.println("VIN: " + new String(data));
                sb.append(String.format(
                        "VIN: %s%n", new String(data)));
            }
            if (response[5] == 0x04) {
                int i;
                for (i = 0; i < data.length && data[i] != 0; i++) { }
                final String str = new String(data, 0, i);
//                System.out.println("CAL ID: " + str);
                sb.append(String.format(
                        "CAL ID: %s%n", str));
            }
            if (response[5] == 0x06) {
//                System.out.println("CVN: " + HexUtil.asHex(data));
                sb.append(String.format(
                        "CVN: %s%n", HexUtil.asHex(data)));
            }
            if (response[5] == 0x08) {
//                System.out.println("PID_8: " + HexUtil.asHex(data));
            }
            if (response[5] == 0x0A) {
                int i;
                for (i = 0; i < data.length && data[i] != 0; i++) { }
                final String str = new String(data, 0, i);
//                System.out.println("Module: " + str);
                sb.append(String.format(
                        "Module: %s%n", str));
            }
        }
        if (response[4] == READ_MODE3_RESPONSE) {
            if (response[5] > 0x00) {
                final byte[] data = new byte[responseLen - 6];
                System.arraycopy(response, 6, data, 0, data.length);
                int i;
                int j = 1;
                final byte[] codeHex = new byte[2];
                for (i = 0; i < data.length; i = i + 2) {
                    System.arraycopy(data, i, codeHex, 0, 2);
                    final byte module = (byte) ((codeHex[0] & 0xC0) >> 6);
                    String moduleTxt = null;
                    switch (module) {
                        case 0:
                            moduleTxt = "P";
                            break;
                        case 1:
                            moduleTxt = "C";
                            break;
                        case 2:
                            moduleTxt = "B";
                            break;
                        case 3:
                            moduleTxt = "U";
                            break;
                    }

                    final byte dtcB1 = (byte) ((codeHex[0] & 0x30) >> 4);
                    final byte dtcB2 = (byte) (codeHex[0] & 0x0F);
                    final byte dtcB3 = (byte) ((codeHex[1] & 0xF0) >> 4);
                    final byte dtcB4 = (byte) (codeHex[1] & 0x0F);

//                    System.out.print(
//                            String.format("DTC %d: %s%s%s%s%s%n",
//                                    j,
//                                    moduleTxt,
//                                    Character.forDigit(dtcB1, 16),
//                                    Character.forDigit(dtcB2, 16),
//                                    Character.forDigit(dtcB3, 16),
//                                    Character.forDigit(dtcB4, 16)));
                    sb.append(String.format(
                            "DTC %d: %s%s%s%s%s%n",
                            j,
                            moduleTxt,
                            Character.forDigit(dtcB1, 16),
                            Character.forDigit(dtcB2, 16),
                            Character.forDigit(dtcB3, 16),
                            Character.forDigit(dtcB4, 16)));
                    j++;
                }
            }
        }
    }

    private final static void version(final int deviceId) {
        final Version version = api.readVersion(deviceId);
//        System.out.printf("Version => Firmware:[%s], DLL:[%s], API:[%s]%n",
//                version.firmware, version.dll, version.api);
        sb.append(String.format(
                "J2534 Firmware:[%s], DLL:[%s], API:[%s]%n",
                version.firmware, version.dll, version.api));
    }

    private final static void setConfig(int channelId) {
        final ConfigItem loopback = new ConfigItem(Config.LOOPBACK.getValue(), LOOPBACK);
        final ConfigItem bs = new ConfigItem(Config.ISO15765_BS.getValue(), 0);
        final ConfigItem stMin = new ConfigItem(Config.ISO15765_STMIN.getValue(), 0);
        final ConfigItem bs_tx = new ConfigItem(Config.BS_TX.getValue(), 0xffff);
        final ConfigItem st_tx = new ConfigItem(Config.STMIN_TX.getValue(), 0xffff);
        final ConfigItem wMax = new ConfigItem(Config.ISO15765_WFT_MAX.getValue(), 0);
        api.setConfig(channelId, loopback, bs, stMin, bs_tx, st_tx, wMax);
    }

    private final static void getConfig(int channelId) {
        final ConfigItem[] configs = api.getConfig(
                channelId,
                Config.LOOPBACK.getValue(),
                Config.ISO15765_BS.getValue(),
                Config.ISO15765_STMIN.getValue(),
                Config.BS_TX.getValue(),
                Config.STMIN_TX.getValue(),
                Config.ISO15765_WFT_MAX.getValue()
                );
        int i = 1;
        for (ConfigItem item : configs) {
//            System.out.printf("J2534 Config item %d: Parameter: %s, value:%d%n",
//                    i, Config.get(item.parameter), item.value);
            sb.append(String.format(
                    "J2534 Config item %d: Parameter: %s, value:%d%n",
                    i, Config.get(item.parameter), item.value));
            i++;
        }
    }

    private final static void validateResponse(byte[] response) {
        assertEquals(match1, response, "Invalid ECU id");
        if (response.length == 7) {
            assertNrc(ECU_NRC, response[4], response[5], response[6],"Request type not supported");
        }
        assertOneOf(new byte[]{ECU_INIT_RESPONSE, READ_MODE3_RESPONSE, READ_MODE9_RESPONSE}, response[4], "Invalid response code");
    }

    private final static void assertNrc(byte expected, byte actual, byte command, byte code, String msg) {
        if (actual == expected) {
            String ec = " unsupported.";
            if (code == 0x13) {
                ec = " invalid format or length.";
            }
            throw new InvalidResponseException(
                    msg + ". Command: " + asHex(new byte[]{command}) + ec);
        }
    }

    private final static void assertEquals(byte[] expected, byte[] actual, String msg) {
        final byte[] idBytes = new byte[4];
        System.arraycopy(actual, 0, idBytes, 0, 4);
        final int idExpected = asUnsignedInt(expected);
        final int idActual = asUnsignedInt(idBytes);
        if (idActual != idExpected) {
            throw new InvalidResponseException(
                    msg + ". Expected: " + asHex(expected) + 
                    ". Actual: " + asHex(idBytes) + ".");
        }
    }

    private final static void assertOneOf(byte[] validOptions, byte actual, String msg) {
        for (byte option : validOptions) {
            if (option == actual) {
                return;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < validOptions.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(asHex(new byte[]{validOptions[i]}));
        }
        throw new InvalidResponseException(msg + ". Expected one of [" + builder.toString() + "]. Actual: " + asHex(new byte[]{actual}) + ".");
    }

    private final static byte[] buildRequest(
            byte mode, 
            byte pid,
            boolean pidValid,
            byte[]... content) {

        final ByteArrayOutputStream bb = new ByteArrayOutputStream(6);
        try {
            for (byte[] tmp : content) {
                bb.write(tmp);
            }
            bb.write(mode);
            if (pidValid) {
                bb.write(pid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bb.toByteArray();
    }

    public final static void main(String args[]) throws InterruptedException{
        LogManager.initDebugLogging();
        if (args.length < 1) {
            System.out.printf("Provide \"library_name\" cmdline arg.");
            return;
        }
        else {
            api = new J2534Impl(  //op20pt32 MONGI432 /usr/local/lib/j2534.so
                    Protocol.ISO15765, args[0]);
        }

        final TestJ2534IsoTp test1 = new TestJ2534IsoTp();
        System.out.print(test1.toString());
    }
}
