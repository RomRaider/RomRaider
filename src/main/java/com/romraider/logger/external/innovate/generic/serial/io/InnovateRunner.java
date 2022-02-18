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

package com.romraider.logger.external.innovate.generic.serial.io;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.external.core.DataListener;
import com.romraider.logger.external.core.Stoppable;
import static com.romraider.util.ByteUtil.matchOnes;
import static com.romraider.util.ByteUtil.matchZeroes;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static java.lang.System.arraycopy;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class InnovateRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(InnovateRunner.class);
    private static final double MAX_AFR = 20.33;
    private final SerialConnection connection;
    private final DataListener listener;
    private boolean stop;

    public InnovateRunner(String port, DataListener listener) {
        checkNotNullOrEmpty(port, "port");
        this.connection = serialConnection(port);
        // LC-1 & LM-2
//        this.connection = new TestInnovateConnection("13036B00000000000000000000B2874313036B00000000000000000000B28743");
        // LM-1
//        this.connection = new TestInnovateConnection("8113037C1E66012600720049003B003B");
//        this.connection = new TestInnovateConnection("B28242310024B28242310000"); // bad data?
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                byte b0 = nextByte();
                if (isHeaderHighByte(b0)) {
                    byte b1 = nextByte();
                    if (isHeaderLowByte(b1)) {
                        int numWords = numWords(b0, b1);
                        byte[] bytes = new byte[numWords * 2];
                        connection.read(bytes);
                        if (LOGGER.isTraceEnabled())
                            LOGGER.trace("Innovate response: " + packet(b0, b1, bytes));
                        process(bytes);
                    } else {
                        if (LOGGER.isTraceEnabled())
                            LOGGER.trace("Innovate discarded: " + asHex(b1));
                    }
                } else if (isLm1HighByte(b0)) {
                    byte b1 = nextByte();
                    if (isLm1LowByte(b1)) {
                        byte[] rest = new byte[14];
                        connection.read(rest);
                        byte[] bytes = new byte[16];
                        bytes[0] = b0;
                        bytes[1] = b1;
                        arraycopy(rest, 0, bytes, 2, rest.length);
                        if (LOGGER.isTraceEnabled())
                            LOGGER.trace("Innovate response: " + asHex(bytes));
                        process(bytes);
                    } else {
                        if (LOGGER.isTraceEnabled())
                            LOGGER.trace("Innovate discarded: " + asHex(b1));
                    }
                } else {
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("Innovate discarded: " + asHex(b0));
                }
            }
            connection.close();
        } catch (Throwable t) {
            LOGGER.error("Error occurred", t);
        } finally {
            connection.close();
        }
    }

    @Override
    public void stop() {
        stop = true;
    }

    private void process(byte[] bytes) {
        if (isError(bytes)) {
            double error = -1d * getLambda(bytes);
            LOGGER.error("Innovate error: " + error);
            listener.setData(error);
        } else if (isOk(bytes)) {
            double afr = getAfr(bytes);
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("Innovate AFR: " + afr);
            listener.setData(afr > MAX_AFR ? MAX_AFR : afr);
        }
    }

    private SerialConnectionImpl serialConnection(String port) {
        ConnectionProperties properties = new InnovateConnectionProperties();
        return new SerialConnectionImpl(port, properties);
    }

    private byte nextByte() {
        return (byte) connection.read();
    }

    // 1x11xx1x
    private boolean isHeaderHighByte(byte b) {
        return matchOnes(b, 178);
    }

    // 1xxxxxxx
    private boolean isHeaderLowByte(byte b) {
        return matchOnes(b, 128);
    }

    // 1x0xxx0x
    private boolean isLm1HighByte(byte b) {
        return matchOnes(b, 128) && matchZeroes(b, 34);
    }

    // 0xxxxxxx
    private boolean isLm1LowByte(byte b) {
        return matchZeroes(b, 128);
    }

    private double getAfr(byte[] bytes) {
        return (getLambda(bytes) + 500) * getAf(bytes) / 10000.0;
    }

    private int getAf(byte[] bytes) {
        return ((bytes[0] & 1) << 7) | bytes[1];
    }

    // xxx000xx
    private boolean isOk(byte[] bytes) {
        return matchZeroes(bytes[0], 28);
    }

    // xxx110xx
    private boolean isError(byte[] bytes) {
        return matchOnes(bytes[0], 24) && matchZeroes(bytes[0], 4);
    }

    // 01xxxxxx 0xxxxxxx
    private int getLambda(byte[] bytes) {
        return ((bytes[2] & 63) << 7) | bytes[3];
    }

    private int numWords(byte b0, byte b1) {
        int result = 0;
        if (matchOnes(b0, 1)) result |= 128;
        if (matchOnes(b1, 64)) result |= 64;
        if (matchOnes(b1, 32)) result |= 32;
        if (matchOnes(b1, 16)) result |= 16;
        if (matchOnes(b1, 8)) result |= 8;
        if (matchOnes(b1, 4)) result |= 4;
        if (matchOnes(b1, 2)) result |= 2;
        if (matchOnes(b1, 1)) result |= 1;
        return result;
    }

    private String packet(byte b0, byte b1, byte[] bytes) {
        byte[] result = new byte[bytes.length + 2];
        result[0] = b0;
        result[1] = b1;
        arraycopy(bytes, 0, result, 2, bytes.length);
        return asHex(result);
    }
}
