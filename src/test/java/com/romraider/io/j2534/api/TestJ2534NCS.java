/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.romraider.io.j2534.api.J2534Impl.Config;
import com.romraider.io.j2534.api.J2534Impl.Protocol;
import com.romraider.io.j2534.api.J2534Impl.TxFlags;
import com.romraider.logger.ecu.exception.InvalidResponseException;
import com.romraider.maps.checksum.NcsCoDec;
import com.romraider.util.HexUtil;
import com.romraider.util.LogManager;

/**
 * This class is used to exercise the J2534 API against a real J2534 device and
 * an active ECU using the ISO15765-2 protocol.
 * Using command line options the ECU can be read and written.
 */
public final class TestJ2534NCS {
    private static J2534 api ;
    private static final int LOOPBACK = 0;
    private static final byte[] mask1 = {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    private static final byte[] match1 = {
            (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xe8};
    private static final byte[] tester = {
            (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xe0};
    private static final byte ECU_NRC = (byte) 0x7F;
    private static final byte READ_MODE9_RESPONSE = (byte) 0x49;
    private static final byte READ_MODE9_COMMAND = (byte) 0x09;
    private static final byte[] READ_MODE9_PIDS = {(byte) 0x00,
            (byte) 0x02, (byte) 0x04, (byte) 0x06, (byte) 0x08};
    private static final byte READ_MODE3_RESPONSE = (byte) 0x43;
    private static final byte READ_MODE3_COMMAND = (byte) 0x03;
    private static final byte READ_MODE21_COMMAND = (byte) 0x21;
    private static final byte READ_MODE22_COMMAND = (byte) 0x22;
    private static final byte ECU_INIT_COMMAND = (byte) 0x10;
    private static final StringBuilder sb = new StringBuilder();
    private static String calid = "";


    public TestJ2534NCS(String[] args) throws InterruptedException {
        final int deviceId = api.open();
        sb.delete(0, sb.capacity());
        String report = "";
        try {
            version(deviceId);
            final int channelId = api.connect(deviceId, 0, 500000);

            final double vBatt = api.getVbattery(deviceId);
            report = String.format("J2534 Interface Pin 16: %sVDC%n", vBatt);
            System.out.print(report);
            sb.append(report);

            final int msgId = api.startFlowCntrlFilter(
                    channelId, mask1, match1, tester, TxFlags.ISO15765_FRAME_PAD);

            try {
                setConfig(channelId);
                getConfig(channelId);
                report = String.format("%n--- Vehicle Information ---%n");
                System.out.print(report);
                sb.append(report);

                for (byte pid : READ_MODE9_PIDS) {
                    final byte[] mode9 = buildRequest(
                            READ_MODE9_COMMAND,     // mode
                            new byte[]{pid},        // pid
                            true,                   // pid valid
                            tester);                // source
                    api.writeMsg(channelId,
                            mode9,
                            1000L,
                            TxFlags.ISO15765_FRAME_PAD);

                    final byte[] response;
                    response = api.readMsg(channelId, 1, 1000L);
                    handleResponse(response);
                }
                final byte[] mode3 = buildRequest(
                        READ_MODE3_COMMAND,     // mode
                        new byte[]{0},          // pid
                        false,                  // pid valid
                        tester);                // source
                api.writeMsg(channelId,
                        mode3,
                        1000L,
                        TxFlags.ISO15765_FRAME_PAD);

                byte[] response;
                response = api.readMsg(channelId, 1, 1000L);
                handleResponse(response);

                // UDS 22 1101 - this will fail
                final byte[] mode22xx = buildRequest(
                        (byte) 0x22,    // mode
                        new byte[]{
                                (byte) 0x11, //CID high byte VDC
                                (byte) 0x03,//CID low byte VDC
//                                (byte) 0x11, //CID high byte ECT
//                                (byte) 0x01, //CID low byte ECU
                        },
                        true,                // pid valid
                        tester);             // source
                System.out.println("UDS 22 1103 BattVDC request  = " +
                        HexUtil.asHex(mode22xx));
                api.writeMsg(channelId,
                        mode22xx,
                        1000L,
                        TxFlags.ISO15765_FRAME_PAD);
                response = api.readMsg(channelId, 1, 1000L);
                System.out.println("UDS 22 1103 BattVDC response = " +
                        HexUtil.asHex(response));

// NCS init
                final byte[] init = buildRequest(
                        ECU_INIT_COMMAND,       // mode
                        new byte[]{(byte) 0xC0},// pid
                        true,                   // pid valid
                        tester);                // source
                System.out.println("Init request = " +
                        HexUtil.asHex(init));
                api.writeMsg(channelId,
                        init,
                        1000L,
                        TxFlags.ISO15765_FRAME_PAD);
                response = api.readMsg(channelId, 1, 1000L);
                System.out.println("Init response = " +
                        HexUtil.asHex(response));

                // UDS 22 1101 - try again
                System.out.println("UDS 22 1103 BattVDC request  = " +
                        HexUtil.asHex(mode22xx));
                api.writeMsg(channelId,
                        mode22xx,
                        1000L,
                        TxFlags.ISO15765_FRAME_PAD);
                response = api.readMsg(channelId, 1, 1000L);
                System.out.println("UDS 22 1103 BattVDC response = " +
                        HexUtil.asHex(response));

                // NCS ident
                final byte[] id = buildRequest(
                        READ_MODE21_COMMAND,    // mode
                        new byte[]{(byte) 0x10},// pid
                        true,                   // pid valid
                        tester);                // source
                System.out.println("Ident request = " +
                        HexUtil.asHex(id));
                api.writeMsg(channelId,
                        id,
                        1000L,
                        TxFlags.ISO15765_FRAME_PAD);
                response = api.readMsg(channelId, 1, 1000L);
                System.out.println("Ident response = " +
                        HexUtil.asHex(response));

                // UDS 21 01
                final byte[] mode2101 = buildRequest(
                        READ_MODE21_COMMAND,    // mode
                        new byte[]{(byte) 0x01},// pid
                        true,                   // pid valid
                        tester);                // source
                System.out.println("UDS 21 01 request  = " +
                        HexUtil.asHex(mode2101));
                api.writeMsg(channelId,
                        mode2101,
                        1000L,
                        TxFlags.ISO15765_FRAME_PAD);
                response = api.readMsg(channelId, 1, 1000L);
                System.out.println("UDS 21 01 response = " +
                        HexUtil.asHex(response));

                // NCS Query PID support
                final byte[] supportedPidsPid = {
                        (byte) 0x00, (byte) 0x20, (byte) 0x40, (byte) 0x60,
                        (byte) 0x80, (byte) 0xA0, (byte) 0xC0, (byte) 0xE0};
                boolean test_grp = true;
                for (byte pid : supportedPidsPid) {
                    if (test_grp) {
                        final byte[] mode21 = buildRequest(
                                READ_MODE21_COMMAND,    // mode
                                new byte[]{pid},        // pid
                                true,                   // pid valid
                                tester);                // source
                        System.out.println("UDS 21 request  = " +
                                HexUtil.asHex(mode21));
                        api.writeMsg(channelId,
                                mode21,
                                1000L,
                                TxFlags.ISO15765_FRAME_PAD);
                        response = api.readMsg(channelId, 1, 1000L);
                        System.out.println("UDS 21 response = " +
                                HexUtil.asHex(response));
                        // Check lsb to see if next PID group is supported
                        if ((response[response.length-1] & 0x01) == 0) {
                            test_grp = false;
                        }
                    }
                }
                final byte[] highBytes = {
                        (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14,
                        (byte) 0x15, (byte) 0x16};
                for (byte hb : highBytes) {
                    test_grp = true;
                    for (byte pid : supportedPidsPid) {
                        if (test_grp) {
                            final byte[] mode22 = buildRequest(
                                    READ_MODE22_COMMAND,    // mode
                                    new byte[]{hb, pid},    // pid
                                    true,                   // pid valid
                                    tester);                // source
                            System.out.println("UDS 22 request  = " +
                                    HexUtil.asHex(mode22));
                            api.writeMsg(channelId,
                                    mode22,
                                    1000L,
                                    TxFlags.ISO15765_FRAME_PAD);
                            response = api.readMsg(channelId, 1, 1000L);
                            System.out.println("UDS 22 response = " +
                                    HexUtil.asHex(response));
                            // Check lsb to see if next PID group is supported
                            if ((response[response.length-1] & 0x01) == 0) {
                                test_grp = false;
                            }
                        }
                    }
                }

                // UDS 2C E0
                final byte[] mode2Cxx = buildRequest(
                        (byte) 0x2c,    // mode
                        new byte[]{(byte) 0xE0,
//                              (byte) 0x01, //definitionMode
//                              (byte) 0x01, //positionIn
//                              (byte) 0x01, //size
//                              (byte) 0x01,
//                              (byte) 0x01,

                                (byte) 0x02, //definitionMode
                                (byte) 0x01, //positionIn doesn't see to matter
                                (byte) 0x01, //size
                                (byte) 0x11, //CID high byte ECT
                                (byte) 0x01, //CID low byte ECU
                                (byte) 0x01, //positionInRecord doesn't see to matter
                                (byte) 0x02, //definitionMode
                                (byte) 0x02, //positionIn doesn't see to matter
                                (byte) 0x01, //size
                                (byte) 0x11, //CID high byte VDC
                                (byte) 0x03, //CID low byte VDC
                                (byte) 0x01, //positionInRecord doesn't see to matter
                                (byte) 0x02, //definitionMode
                                (byte) 0x03, //positionIn doesn't see to matter
                                (byte) 0x02, //size
                                (byte) 0x12, //CID high byte RPM
                                (byte) 0x01, //CID low byte RPM
                                (byte) 0x01, //positionInRecord doesn't see to matter
                                (byte) 0x02, //definitionMode
                                (byte) 0x04, //positionIn doesn't see to matter
                                (byte) 0x02, //size
                                (byte) 0x12, //CID high byte TPS1
                                (byte) 0x0F, //CID low byte TPS1
                                (byte) 0x01, //positionInRecord doesn't see to matter
                                (byte) 0x02, //definitionMode
                                (byte) 0x05, //positionIn doesn't see to matter
                                (byte) 0x02, //size
                                (byte) 0x12, //CID high byte TPS2
                                (byte) 0x10, //CID low byte TPS2
                                (byte) 0x01, //positionInRecord doesn't see to matter

//                              (byte) 0x03, //definitionMode
//                              (byte) 0x01, //positionIn
//                              (byte) 0x01, //size
//                              (byte) 0xff, //RAM high byte
//                              (byte) 0xff, //RAM high byte
//                              (byte) 0x12, //RAM mid byte
//                              (byte) 0x34, //RAM low byte
//                              (byte) 0x03, //definitionMode
//                              (byte) 0x02, //positionIn
//                              (byte) 0x01, //size
//                              (byte) 0xff, //RAM high byte
//                              (byte) 0xff, //RAM high byte
//                              (byte) 0xab, //RAM mid byte
//                              (byte) 0xcd, //RAM low byte
                                },
                        true,                // pid valid
                        tester);             // source
                System.out.println("UDS 2C request  = " +
                        HexUtil.asHex(mode2Cxx));
                api.writeMsg(channelId,
                        mode2Cxx,
                        1000L,
                        TxFlags.ISO15765_FRAME_PAD);
                response = api.readMsg(channelId, 1, 1000L);
                System.out.println("UDS 2C response = " +
                        HexUtil.asHex(response));

                final byte[] _21e0 = buildRequest(
                        (byte) 0x21,            // mode
                        new byte[]{(byte) 0xe0},
                        true,                   // pid valid
                        tester);                // source
                System.out.println("UDS 21 request = " +
                        HexUtil.asHex(_21e0));
                api.writeMsg(channelId,
                        _21e0,
                        1000L,
                        TxFlags.ISO15765_FRAME_PAD);
                response = api.readMsg(channelId, 1, 1000L);
                System.out.println("UDS 21 reponse = " +
                HexUtil.asHex(response));

                FileOutputStream stream = null;
                try {
                    if (args.length == 3 || args.length == 4) {
                        byte[] uds3483_d = new byte[0x30];
                        final byte[] _10fb = buildRequest(
                                ECU_INIT_COMMAND,       // mode
                                new byte[]{(byte) 0xfb},// pid
                                true,                   // pid valid
                                tester);                // source
                        System.out.println("Init request = " +
                                HexUtil.asHex(_10fb));
                        api.writeMsg(channelId,
                                _10fb,
                                1000L,
                                TxFlags.ISO15765_FRAME_PAD);
                        response = api.readMsg(channelId, 1, 1000L);
                        System.out.println("Init response = " +
                                HexUtil.asHex(response));

                        final byte[] _2110 = buildRequest(
                                READ_MODE21_COMMAND,    // mode
                                new byte[]{(byte) 0x10},// pid
                                true,                   // pid valid
                                tester);                // source
                        System.out.println("ID request = " +
                                HexUtil.asHex(_2110));
                        api.writeMsg(channelId,
                                _2110,
                                1000L,
                                TxFlags.ISO15765_FRAME_PAD);
                        response = api.readMsg(channelId, 1, 1000L);
                        System.out.println("ID response = " +
                                HexUtil.asHex(response));

                        final byte[] _2181 = buildRequest(
                                READ_MODE21_COMMAND,    // mode
                                new byte[]{(byte) 0x81},// pid
                                true,                   // pid valid
                                tester);                // source
                        System.out.println("VIN request = " +
                                HexUtil.asHex(_2181));
                        api.writeMsg(channelId,
                                _2181,
                                1000L,
                                TxFlags.ISO15765_FRAME_PAD);
                        response = api.readMsg(channelId, 1, 1000L);
                        System.out.println("VIN response = " +
                                HexUtil.asHex(response));

                        final byte[] _2180 = buildRequest(
                            READ_MODE21_COMMAND,    // mode
                            new byte[]{(byte) 0x80},// pid
                            true,                   // pid valid
                            tester);                // source
                        System.out.println("UDS 2180 request = " +
                                HexUtil.asHex(_2180));
                        api.writeMsg(channelId,
                            _2180,
                            1000L,
                            TxFlags.ISO15765_FRAME_PAD);
                        response = api.readMsg(channelId, 1, 1000L);
                        System.out.println("UDS 2180 response = " +
                            HexUtil.asHex(response));

                        final byte[] _21f0 = buildRequest(
                                READ_MODE21_COMMAND,    // mode
                                new byte[]{(byte) 0xf0},// pid
                                true,                   // pid valid
                                tester);                // source
                        System.out.println("Prod Num request long = " +
                                HexUtil.asHex(_21f0));
                        api.writeMsg(channelId,
                                _21f0,
                                1000L,
                                TxFlags.ISO15765_FRAME_PAD);
                        response = api.readMsg(channelId, 1, 1000L);
                        System.out.println("Prod Num response long = " +
                                HexUtil.asHex(response));

                        final byte[] _21fe = buildRequest(
                                READ_MODE21_COMMAND,    // mode
                                new byte[]{(byte) 0xfe},// pid
                                true,                   // pid valid
                                tester);                // source
                        System.out.println("Prod Num request = " +
                                HexUtil.asHex(_21fe));
                        api.writeMsg(channelId,
                                _21fe,
                                1000L,
                                TxFlags.ISO15765_FRAME_PAD);
                        response = api.readMsg(channelId, 1, 1000L);
                        System.out.println("Prod Num response = " +
                                HexUtil.asHex(response));
                        if (response[4] != 0x7f) {
                            int j = 6;
                            while (response[j] != 0 && j < response.length) { j++; }
                            if (j == response.length) { j--; }
                            System.arraycopy(response, 6, uds3483_d, 0, j - 6);
                        }

                        final byte[] _21ff = buildRequest(
                                READ_MODE21_COMMAND,    // mode
                                new byte[]{(byte) 0xff},// pid
                                true,                   // pid valid
                                tester);                // source
                        System.out.println("HW Num request = " +
                                HexUtil.asHex(_21ff));
                        api.writeMsg(channelId,
                                _21ff,
                                1000L,
                                TxFlags.ISO15765_FRAME_PAD);
                        response = api.readMsg(channelId, 1, 1000L);
                        System.out.println("HW Num response = " +
                                HexUtil.asHex(response));
                        if (response[4] != 0x7f) {
                            System.arraycopy(response, 10, uds3483_d, 28, 10);
                        }
                        // Read from start address for length bytes
                        if (args[1].equalsIgnoreCase("r")) {
                            long start;
                            int size;
                            if (args[2].startsWith("0x")) {
                                start = Long.valueOf(args[2].substring(2), 16);
                            }
                            else {
                                start = Long.parseLong(args[2]);
                            }
                            if (args[3].startsWith("0x")) {
                                size = Integer.valueOf(args[3].substring(2), 16);
                            }
                            else {
                                size = Integer.parseInt(args[3]);
                            }

                            final String filename = String.format("%s\\%s_read.bin",
                                    System.getProperty("user.home"), calid);
                            System.out.println(String.format(
                                    "Writing file: %s%nRead start: 0x%X, length: %s",
                                    filename, start, size));
                            stream = new FileOutputStream(filename);
                            final long end = start + size;
                            long i = start;
                            final int frame_len = 0x3F;
                            int len = 0x3F;
                            while (i < end) {
                                if ((i + frame_len) > end) {
                                    len = size % frame_len;
                                }
                                final byte[] frame = new byte[6];
                                final byte[] addr = ByteBuffer.allocate(8).putLong(i).array();
                                System.arraycopy(addr, 4, frame, 0, 4); // copy last 4 (integer) bytes
                                frame[5] = (byte) len;
                                final byte[] _2300 = buildRequest(
                                        (byte) 0x23,
                                        frame,
                                        true,
                                        tester);
                                System.out.println(String.format("Reading addr: 0x%06X, len: %d", i, len));
                                api.writeMsg(channelId,
                                        _2300,
                                        1000L,
                                        TxFlags.ISO15765_FRAME_PAD);
                                response = api.readMsg(channelId, 1, 1000L);
                                final byte[] out = new byte[response.length-5];
                                System.arraycopy(response, 5, out, 0, out.length);
                                stream.write(out);
                                i+=frame_len;
                            }
                        }
                        // Program ECU with or test given binary file
                        if (args[1].equals("P") || args[1].equalsIgnoreCase("t")) {
                            boolean test = false;
                            if (args[1].equalsIgnoreCase("t")) {
                                test = true;
                                System.out.println("Test programming without actually doing it");
                            }
                            final File binFile = new File(args[2]);
                            if ((binFile.length() % 4) > 0) {
                                System.out.println(String.format(
                                        "Error: file length %d is not a multiple of 4 bytes", binFile.length()));
                                return;
                            }
                            final NcsCoDec codec = new NcsCoDec();
                            final int size = 0x80;
                            int scode = 0;
                            int idx = 0x8200;
                            try {
                                List<ByteBuffer> inChunks = readFile(binFile, size);
                                final int filesize = inChunks.size() * size;
                                System.out.println("file: " + binFile.getName());
                                System.out.println(String.format(
                                        "file size: %d, chunk size: %d, chunks: %d, chunks length: %d",
                                        binFile.length(), size, inChunks.size(), filesize));
                                if (binFile.length() != filesize) {
                                    System.out.println("Error: file and chunked file are not equal byte length");
                                    return;
                                }
                                if ((binFile.length() == 524288) || (binFile.length() == 1048576) || (binFile.length() == 1310720)
                                        || (binFile.length() == 1572864) || (binFile.length() == 2097152)) {
                                    scode = inChunks.get(191).getInt(96); // 191*size+96=24544 (0x5FE0)
                                }
                                if (scode == 0) {
                                    System.out.println("Error: scode not found in file");
                                    return;
                                }
                                System.out.println(String.format("using scode from file: %08X", scode));
                                if (test) System.out.println("input starting at 0x8200:");
                                // remove the chunks that we won't be flashing
                                final int skip = idx / size;
                                for (int i = 0; i < skip; i++) {
                                    inChunks.remove(0);
                                }
                                List<byte[]> encChunks = new ArrayList<byte[]>();
                                for (ByteBuffer chunk : inChunks) {
                                    if (test) System.out.println(String.format("0x%06X : %s",
                                            idx, HexUtil.asHex(chunk.array())));
                                    encChunks.add(codec.nisEncode(chunk, scode));
                                    idx += size;
                                }
                                if (test) System.out.println("encoded output:");
                                idx = 0x8200;
                                List<byte[]> prgChunks = new ArrayList<byte[]>();
                                for (byte[] chunk : encChunks) {
                                    //System.out.println(HexUtil.asHex(chunk));
                                    final ByteBuffer crcChunk = ByteBuffer.allocate(134).order(ByteOrder.BIG_ENDIAN);
                                    crcChunk.putInt(1, idx);
                                    crcChunk.putShort(0, (short) 0x3482);
                                    crcChunk.put(5, (byte) size);
                                    crcChunk.position(6);
                                    crcChunk.put(chunk);
                                    //System.out.println(HexUtil.asHex(crcChunk.array()));
                                    final short crc = codec.calcCrc(crcChunk.array());
                                    //System.out.println(String.format("CRC:%04x", crc));
                                    final short invert = (short) ~crc;
                                    //System.out.println(String.format("~CRC:%04x", invert));
                                    final short flip = (short) (((invert << 8) | ((invert & 0xffff) >>> 8)) & 0xffff);
                                    //System.out.println(String.format("Flip:%04x", flip));
                                    final ByteBuffer finalChunk = ByteBuffer.allocate(136).order(ByteOrder.BIG_ENDIAN);
                                    finalChunk.put(crcChunk.array());
                                    finalChunk.putShort(flip);
                                    if (test) System.out.println(String.format("%04X : %s",
                                            codec.calcCrc(finalChunk.array()),
                                            HexUtil.asHex(finalChunk.array())));
                                    prgChunks.add(finalChunk.array());
                                    idx += size;
                                }

                                // Start programming
                                try {
                                    uds1085(channelId);
                                    uds2781(channelId);
                                    uds2782(channelId);
                                    if (!test) {
                                        uds3181(channelId);
                                        for (byte[] chunk : prgChunks) {
                                            uds34(channelId, chunk);
                                        }
                                        uds3182(channelId);
                                    }
                                    else {
                                        System.out.println("If not a Test, programming messages would appear here ...");
                                    }
                                    //String uds3483_t = "3132333435014106043954524B454D564E32000000000080010203040544414243444E4F4445780000010110115CFFFF";
                                    //String uds3483_t = "30303030303041060431434D43375150443430310000000000000000005230303030434F4E332B0619022000005CAD04"; // CRC = 312C
                                    String uds3483_t = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"; // !~CRC = B9BC
                                    uds3483_d = HexUtil.asBytes(uds3483_t);
                                    final ByteBuffer uds3483_bb = ByteBuffer.allocate(uds3483_d.length + 6).order(ByteOrder.BIG_ENDIAN);
                                    uds3483_bb.putShort((short) 0x3483);
                                    uds3483_bb.putInt(0x00000030);
                                    uds3483_bb.put(uds3483_d);
                                    //System.out.println(HexUtil.asHex(uds3483_bb.array()));
                                    short crc = codec.calcCrc(uds3483_bb.array());
                                    short invert = (short) ~crc;
                                    short flip = (short) (((invert << 8) | ((invert & 0xffff) >>> 8)) & 0xffff);
                                    final ByteBuffer uds3483_data = ByteBuffer.allocate(uds3483_bb.capacity() + 2).order(ByteOrder.BIG_ENDIAN);
                                    uds3483_data.put(uds3483_bb.array());
                                    uds3483_data.putShort(flip);
                                    if (test) System.out.println(String.format("UDS 3483 data: %s",
                                            HexUtil.asHex(uds3483_data.array())));
                                    if (!test) uds3483(channelId, uds3483_data.array());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    if (stream != null) stream.close();
                }
                uds1081(channelId);
            }
            catch (Exception e) {
                System.out.println(e);
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

//    public final String toString() {
//        return sb.toString();
//    }

    private final void handleResponse(byte[] response) {
        String report = "";
        //validateResponse(response);
        final int responseLen = response.length;
        if (response[4] == READ_MODE9_RESPONSE) {
            final byte[] data = new byte[responseLen - 7];
            System.arraycopy(response, 7, data, 0, data.length);
            if (response[5] == 0x00) {
                final byte[] rsp = new byte[responseLen - 6];
                System.arraycopy(response, 6, rsp, 0, data.length);
                report = String.format(String.format(
                        "Mode 9 PIDs: %s%n", HexUtil.asHex(rsp)));
                System.out.print(report);
                sb.append(report);
            }
            if (response[5] == 0x02) {
                report = String.format(String.format(
                        "VIN: %s%n", new String(data)));
                System.out.print(report);
                sb.append(report);
            }
            if (response[5] == 0x04) {
                int i;
                for (i = 0; i < data.length && data[i] != 0; i++) { }
                final String str = new String(data, 0, i);
                report = String.format(String.format(
                        "CAL ID: %s%n", str));
                System.out.print(report);
                sb.append(report);
                calid = str;
            }
            if (response[5] == 0x06) {
                report = String.format(String.format(
                        "CVN: %s%n", HexUtil.asHex(data)));
                System.out.print(report);
                sb.append(report);
            }
            if (response[5] == 0x08) {
                report = String.format("PID_8: %s%n", HexUtil.asHex(data));
                System.out.print(report);
                sb.append(report);
            }
            if (response[5] == 0x0A) {
                int i;
                for (i = 0; i < data.length && data[i] != 0; i++) { }
                final String str = new String(data, 0, i);
                report = String.format(String.format(
                        "Module: %s%n", str));
                System.out.print(report);
                sb.append(report);
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

                    report = String.format(String.format(
                            "DTC %d: %s%s%s%s%s%n",
                            j,
                            moduleTxt,
                            Character.forDigit(dtcB1, 16),
                            Character.forDigit(dtcB2, 16),
                            Character.forDigit(dtcB3, 16),
                            Character.forDigit(dtcB4, 16)));
                    System.out.print(report);
                    sb.append(report);
                    j++;
                }
            }
        }
    }

    private final void version(final int deviceId) {
        final Version version = api.readVersion(deviceId);
        String report = String.format(
                "J2534 Firmware:[%s], DLL:[%s], API:[%s]%n",
                version.firmware, version.dll, version.api);
        System.out.print(report);
        sb.append(report);
    }

    private final void setConfig(int channelId) {
        final ConfigItem loopback = new ConfigItem(Config.LOOPBACK.getValue(), LOOPBACK);
        final ConfigItem bs = new ConfigItem(Config.ISO15765_BS.getValue(), 0);
        final ConfigItem stMin = new ConfigItem(Config.ISO15765_STMIN.getValue(), 0);
        final ConfigItem bs_tx = new ConfigItem(Config.BS_TX.getValue(), 0xffff);
        final ConfigItem st_tx = new ConfigItem(Config.STMIN_TX.getValue(), 0xffff);
        final ConfigItem wMax = new ConfigItem(Config.ISO15765_WFT_MAX.getValue(), 0);
        api.setConfig(channelId, loopback, bs, stMin, bs_tx, st_tx, wMax);
    }

    private final void getConfig(int channelId) {
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
        String report = "";
        for (ConfigItem item : configs) {
            report = String.format(
                    "J2534 Config item %d: Parameter: %s, value:%d%n",
                    i, Config.get(item.parameter), item.value);
            System.out.print(report);
            sb.append(report);
            i++;
        }
    }

    private final void validateResponse(byte[] response) {
        assertEquals(match1, response, "Invalid ECU id");
        if (response.length == 7 && response[4] == 0x7f) {
            assertNrc(ECU_NRC, response[4], response[5], response[6], "Command: ");
        }
        //assertOneOf(new byte[]{ECU_INIT_RESPONSE, READ_MODE3_RESPONSE, READ_MODE9_RESPONSE}, response[4], "Invalid response code");
    }

    private final void assertNrc(byte expected, byte actual, byte command, byte code, String msg) {
        if (actual == expected) {
            String ec = " unsupported";
            switch(code) {
                case 0x10:
                    ec = " generalReject";
                    break;
                case 0x11:
                    ec = " serviceNotSupported";
                    break;
                case 0x12:
                    ec = " subFunctionNotSupported-invalidFormat";
                    break;
                case 0x13:
                    ec = " invalid format or length";
                    break;
                case 0x21:
                    ec = " busy-RepeatRequest";
                    break;
                case 0x22:
                    ec = " conditionsNotCorrect or requestSequenceError";
                    break;
                case 0x23:
                    ec = " routineNotComplete";
                    break;
                case 0x31:
                    ec = " requestOutOfRange";
                    break;
                case 0x33:
                    ec = " securityAccessDenied";
                    break;
                case 0x35:
                    ec = " invalidKey";
                    break;
                case 0x36:
                    ec = " exceedNumberOfAttempts";
                    break;
                case 0x37:
                    ec = " requiredTimeDelayNotExpired";
                    break;
                case 0x40:
                    ec = " downloadNotAccepted";
                    break;
                case 0x41:
                    ec = " improperDownloadType";
                    break;
                case 0x42:
                    ec = " can'tDownloadToSpecifiedAddress";
                    break;
                case 0x43:
                    ec = " can'tDownloadNumberOfBytesRequested";
                    break;
                case 0x50:
                    ec = " uploadNotAccepted";
                    break;
                case 0x51:
                    ec = " improperUploadType";
                    break;
                case 0x52:
                    ec = " can'tUploadFromSpecifiedAddress";
                    break;
                case 0x53:
                    ec = " can'tUploadNumberOfBytesRequested";
                    break;
                case 0x71:
                    ec = " transferSuspended";
                    break;
                case 0x72:
                    ec = " transferAborted";
                    break;
                case 0x74:
                    ec = " illegalAddressInBlockTransfer";
                    break;
                case 0x75:
                    ec = " illegalByteCountInBlockTransfer";
                    break;
                case 0x76:
                    ec = " illegalBlockTransferType";
                    break;
                case 0x77:
                    ec = " blockTransferDataChecksumError";
                    break;
                case 0x78:
                    ec = " reqCorrectlyRcvd-RspPending (requestCorrectlyReceived-ResponsePending)";
                    break;
                case 0x79:
                    ec = " incorrectByteCountDuringBlockTransfer";
                    break;
            }
            if (code > 0x7f) {
                ec = " manufacturerSpecificCode";
            }
            throw new InvalidResponseException(
                    msg + asHex(new byte[]{command}) + ec);
        }
    }

    private final void assertEquals(byte[] expected, byte[] actual, String msg) {
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

    private final void assertOneOf(byte[] validOptions, byte actual, String msg) {
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

    private final byte[] buildRequest(
            byte mode,
            byte[] pid,
            boolean pidValid,
            byte[] address) {

        final ByteArrayOutputStream bb = new ByteArrayOutputStream(6);
        try {
            bb.write(address);
            bb.write(mode);
            if (pidValid) {
                bb.write(pid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bb.toByteArray();
    }


    private final byte[] buildBareRequest(
            byte[] data,
            byte[] address) {

        final ByteArrayOutputStream bb = new ByteArrayOutputStream(6);
        try {
            bb.write(address);
            bb.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bb.toByteArray();
    }

    private final void uds1081(int channelId) {
        final byte[] _1081 = buildRequest(
                (byte) 0x10,
                new byte[]{(byte) 0x81},
                true,
                tester);
        System.out.println("Ending session: " +
                HexUtil.asHex(_1081));
        api.writeMsg(channelId,
                _1081,
                1000L,
                TxFlags.ISO15765_FRAME_PAD);
        final byte[] response = api.readMsg(channelId, 1, 1000L);
        validateResponse(response);
        System.out.println("End session ACK: " +
                HexUtil.asHex(response));
    }

    private final void uds1085(int channelId) {
        final byte[] _1085 = buildRequest(
                (byte) 0x10,
                new byte[]{(byte) 0x85},
                true,
                tester);
        System.out.println("Starting programming session: " +
                HexUtil.asHex(_1085));
        api.writeMsg(channelId,
                _1085,
                1000L,
                TxFlags.ISO15765_FRAME_PAD);
        final byte[] response = api.readMsg(channelId, 1, 1000L);
        validateResponse(response);
        System.out.println("Programming session ACK: " +
                HexUtil.asHex(response));
    }

    private final void uds2781(int channelId) {
        final byte[] _2781 = buildRequest(
                (byte) 0x27,
                new byte[]{(byte) 0x81},
                true,
                tester);
        System.out.println("Requesting security access: " +
                HexUtil.asHex(_2781));
        api.writeMsg(channelId,
                _2781,
                1000L,
                TxFlags.ISO15765_FRAME_PAD);
        final byte[] response = api.readMsg(channelId, 1, 1000L);
        validateResponse(response);
        System.out.println("Security access seed: " +
                HexUtil.asHex(response));
    }

    private final void uds2782(int channelId) {
        final byte[] _2782 = buildRequest(
                (byte) 0x27,
                new byte[]{(byte) 0x82,(byte) 0x11,(byte) 0x22,(byte) 0x33,(byte) 0x44},// pid
                true,
                tester);
        System.out.println("Sending security access key: " +
                HexUtil.asHex(_2782));
        api.writeMsg(channelId,
                _2782,
                1000L,
                TxFlags.ISO15765_FRAME_PAD);
        final byte[] response = api.readMsg(channelId, 1, 1000L);
        validateResponse(response);
        System.out.println("Security access granted: " +
                HexUtil.asHex(response));
    }

    private final void uds3181(int channelId) {
          byte[] _3181 = buildRequest(
          (byte) 0x31,
          new byte[]{(byte) 0x81,(byte) 0x82,(byte) 0xf0,(byte) 0x5a},
          true,
          tester);
        System.out.println("Requesting download service: " +
          HexUtil.asHex(_3181));
        api.writeMsg(channelId,
          _3181,
          1000L,
          TxFlags.ISO15765_FRAME_PAD);
        byte[] response = api.readMsg(channelId, 1, 1000L);
        validateResponse(response);
        System.out.println("Preparing ECU for download: " +
                HexUtil.asHex(response));

        // wait for "ready" response
        while (response[response.length-1] == 1) {
            _3181 = buildRequest(
                  (byte) 0x31,
                  new byte[]{(byte) 0x81,(byte) 0x01},
                  true,
                  tester);
            //System.out.println("UDS 31 request = " +
            //      HexUtil.asHex(_3181));
            api.writeMsg(channelId,
                  _3181,
                  1000L,
                  TxFlags.ISO15765_FRAME_PAD);
            response = api.readMsg(channelId, 1, 1000L);
            validateResponse(response);
            System.out.print(".");
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(".");
        System.out.println("ECU ready for download: " +
                HexUtil.asHex(response));
    }

    private final void uds3182(int channelId) {
        byte[] _3182 = buildRequest(
        (byte) 0x31,
        new byte[]{(byte) 0x82,(byte) 0x00},
        true,
        tester);
      System.out.println("Closing download service: " +
        HexUtil.asHex(_3182));
      api.writeMsg(channelId,
              _3182,
              1000L,
              TxFlags.ISO15765_FRAME_PAD);
      byte[] response = api.readMsg(channelId, 1, 1000L);
      validateResponse(response);
      if (response[response.length-1] == 1) {
          System.out.println("Download service closed: " +
                  HexUtil.asHex(response));
      }
      else {
          throw new InvalidResponseException("Download secrive close failed");
      }

      System.out.println("Verifying download:");
      // wait for "ready" response
      while (response[response.length-1] == 1) {
          _3182 = buildRequest(
                (byte) 0x31,
                new byte[]{(byte) 0x82,(byte) 0x01},
                true,
                tester);
          //System.out.println("UDS 31 request = " +
          //      HexUtil.asHex(_3181));
          api.writeMsg(channelId,
                  _3182,
                  1000L,
                  TxFlags.ISO15765_FRAME_PAD);
          response = api.readMsg(channelId, 1, 1000L);
          validateResponse(response);
          System.out.print(".");
          try {
              Thread.sleep(400);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
      }
      System.out.println(".");
      System.out.println("Verification response: " +
              HexUtil.asHex(response));
      if (response[response.length-1] == 2) {
          System.out.println("Verification successful!");
      }
      else {
          throw new InvalidResponseException("Verification of download data failed");
      }
  }

    private final void uds34(int channelId, byte[] data) {
        final byte[] _34 = buildBareRequest(
                data,
                tester);
        System.out.print(String.format("%d: writing address 0x%02X%02X%02X : ",
                System.currentTimeMillis(), data[2], data[3], data[4]));
        api.writeMsg(channelId,
                _34,
                1000L,
                TxFlags.ISO15765_FRAME_PAD);
        final byte[] response = api.readMsg(channelId, 1, 1000L);
        validateResponse(response);
        if (response[response.length-1] == 2) {
            System.out.println("ok");
        }
        else {
            System.out.println("NAK : " +
                    HexUtil.asHex(response));
            throw new InvalidResponseException("Programming failed at last address block");
        }
    }

    private final void uds3483(int channelId, byte[] data) {
        final byte[] _3483 = buildBareRequest(
                data,
                tester);
        System.out.println("Finalizing ECU: " +
                HexUtil.asHex(_3483));
        api.writeMsg(channelId,
                _3483,
                1000L,
                TxFlags.ISO15765_FRAME_PAD);
        final byte[] response = api.readMsg(channelId, 1, 1000L);
        System.out.println("Finalizing ECU response: " +
                HexUtil.asHex(response));
        validateResponse(response);
        if (response[response.length-1] == 2) {
            System.out.println("Complete! ");
        }
    }

    /**
     * For the given inputFile, read data into chunks of size bytes
     * and return a List of read chunks
     */
    public final List<ByteBuffer> readFile(File inputFile, int size) throws IOException {
        final List<ByteBuffer> chunkList = new ArrayList<ByteBuffer>();
        final FileInputStream fis = new FileInputStream(inputFile);
        try {
            final byte[] buf = new byte[size];
            int readBytes = 0;
            int bytesRead = 0;
            while ((readBytes = fis.read(buf)) != -1) {
                bytesRead += readBytes;
                final ByteBuffer chunk = ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN).put(buf);
                chunkList.add(chunk);
            }
            if ((bytesRead % size) != 0) {
                System.out.println("WARNING: less than a chunck size of data left over : " + (bytesRead % size));
            }
        } finally {
            fis.close();
        }
        return chunkList;
    }

    public final static void main(String[] args) throws InterruptedException{
        LogManager.initDebugLogging();
        if (args.length < 1) {
            System.out.printf("Provide \"J2534_library_name\" cmdline arg.");
            return;
        }
        else {
            /**
             * Argument options:
             * arg0: J2534 library [required] - op20pt32 | MONGI432 | /usr/local/lib/j2534.so
             * arg1: Command [optional] - one of: r | P | t
             * Command arguments:
             * r = read from address for length bytes:
             *      example, read from start for 128kB:
             *      r 0 0x2000
             *      (start and length can be provided in decimal or hexadecimal)
             * P = program ECU from given ROM image file:
             *      example:
             *      P <path/filename>
             *      (the file must be the exact size of the ROM in bytes)
             * t = test programming ECU from given ROM image file:
             *      example:
             *      t <path/filename>
             *      (the file must be the exact size of the ROM in bytes)
             */
            api = new J2534Impl(
                    Protocol.ISO15765, args[0]);
        }
        new TestJ2534NCS(args);
    }
}
