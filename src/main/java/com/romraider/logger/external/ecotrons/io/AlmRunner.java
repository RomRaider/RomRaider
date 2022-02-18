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

package com.romraider.logger.external.ecotrons.io;

import static com.romraider.util.ByteUtil.byteListToBytes;
import static com.romraider.util.HexUtil.asHex;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.ecotrons.plugin.AlmDataItem;
import com.romraider.logger.external.ecotrons.plugin.AlmDataProcessor;
import com.romraider.logger.external.ecotrons.plugin.AlmSensorType;

public final class AlmRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(AlmRunner.class);
    private static final byte[] CONNECT_CMD = {
        (byte) 0x80, (byte) 0x8F, (byte) 0xEA, 0x03, (byte) 0x9C, 0x01, 0x00, (byte) 0x99};
    private static final byte[] START_CMD = {
        (byte) 0x80, (byte) 0x8F, (byte) 0xEA, 0x03, (byte) 0x9C, 0x0D, 0x00, (byte) 0xA5};
    private static final byte[] STOP_CMD = {
        (byte) 0x80, (byte) 0x8F, (byte) 0xEA, 0x03, (byte) 0x9C, 0x09, 0x00, (byte) 0xA5};
    private final AlmConnection connection;
    private final Map<AlmSensorType, AlmDataItem> dataItems;
    private boolean stop;
    private boolean init = true;
    private boolean error;
    private int stage;

    public AlmRunner(String port, Map<AlmSensorType, AlmDataItem> dataItems) {
        connection = new AlmSerialConnection(port);
        this.dataItems = dataItems;
    }

    @Override
    public void run() {
        try {
            int length = 0;
            boolean packetStarted = false;
            final List<Byte> buffer = new ArrayList<Byte>(64);
            while (!stop) {
                if (stage == 0 && init) {
                    connection.write(CONNECT_CMD);
                    init = false;
                }
                if (stage == 1 && init) {
                    connection.write(START_CMD);
                    init = false;
                }
                byte b = connection.readByte();
                if (b == (byte) 0x8F
                        && buffer.size() >= 1
                        && buffer.get(buffer.size() - 1) == (byte) 0x80) {
                    packetStarted = false;
                    buffer.add(b);
                }
                else if (b == (byte) 0xEA
                        && buffer.size() >= 2
                        && buffer.get(buffer.size() - 1) == (byte) 0x8F
                        && buffer.get(buffer.size() - 2) == (byte) 0x80) {
                    packetStarted = true;
                    error = false;
                    buffer.add(b);
                }
                else if (packetStarted && length == 0) {
                    buffer.add(b);
                    length = b;
                    final byte[] bytes = new byte[length + 1];
                    connection.readBytes(bytes);
                    for (byte data : bytes) {
                        buffer.add(data);
                    }
                }
                else if (error && (b != (byte) 0x80)) {
                    buffer.clear();
                }
                else {
                    buffer.add(b);
                }

                if (buffer.size() == (length + 5)) {
                    final byte cs = AlmChecksumCalculator.calculateChecksum(buffer);
                    if (cs == buffer.get(buffer.size() - 1)) {
                        if (stage == 0) {
                            if (buffer.get(4) == (byte) 0xE5
                                    && buffer.get(5) == (byte) 0x01) {
                                if (LOGGER.isTraceEnabled())
                                    LOGGER.trace(String.format(
                                        "Stage:%d, ALM Connect response:%s",
                                        stage, asHex(toArray(buffer))));
                                stage = 1;
                                error = false;
                                init = true;
                            }
                            else {
                                error = true;
                                init = false;
                            }
                        }
                        else if (stage > 0) {
                            if (buffer.get(4) == (byte) 0xE5
                                    && buffer.get(5) == (byte) 0x0D) {
                                if (LOGGER.isTraceEnabled())
                                    LOGGER.trace(String.format(
                                        "Stage:%d, ALM measuring response:%s",
                                        stage, asHex(toArray(buffer))));
                                stage = 2;
                                error = false;
                                init = true;
                                AlmDataProcessor.parseResponse(dataItems, buffer);
                            }
                            else {
                                error = true;
                                init = false;
                            }
                        }
                    }
                    else {
                        error = true;
                        LOGGER.error(String.format(
                                "Stage:%d, ALM checksum failure:%s, expected:%02X",
                                stage, asHex(toArray(buffer)), cs));
                    }
                    buffer.clear();
                    packetStarted = false;
                    length = 0;
                }
            }
        }
        catch (Throwable t) {
            LOGGER.error("Error occurred", t);
        }
        finally {
            if (stage == 2) {
                error = false;
                init = true;
                stage = 0;
                connection.write(STOP_CMD);
                final byte[] response = new byte[8];
                connection.readBytes(response);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace(String.format(
                        "Stage:%d, ALM stop response:%s",
                        stage, asHex(response)));
            }
            connection.close();
        }
    }

    @Override
    public void stop() {
        stop = true;
    }

    private byte[] toArray(List<Byte> buffer) {
        final byte[] response = new byte[buffer.size()];
        byteListToBytes(buffer, response);
        return response;
    }
}
