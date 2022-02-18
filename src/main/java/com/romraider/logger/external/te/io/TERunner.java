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

package com.romraider.logger.external.te.io;

import com.romraider.logger.external.core.ExternalSensorType;
import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.te.plugin.TEDataItem;
import static com.romraider.logger.external.core.ExternalSensorType.WIDEBAND;
import static com.romraider.logger.external.core.ExternalSensorType.USER1;
import static com.romraider.logger.external.core.ExternalSensorType.USER2;
import static com.romraider.logger.external.core.ExternalSensorType.USER3;
import static com.romraider.logger.external.core.ExternalSensorType.THERMACOUPLE1;
import static com.romraider.logger.external.core.ExternalSensorType.THERMACOUPLE2;
import static com.romraider.logger.external.core.ExternalSensorType.THERMACOUPLE3;
import static com.romraider.logger.external.core.ExternalSensorType.TorVss;
import static com.romraider.logger.external.core.ExternalSensorType.ENGINE_SPEED;
import static com.romraider.util.ByteUtil.asUnsignedInt;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TERunner implements Stoppable {
    private static final Logger LOGGER = getLogger(TERunner.class);
    private final Map<ExternalSensorType, TEDataItem> dataItems;
    private final TEConnection connection;
    private boolean stop;
    private byte byteSum;
    private int sequenceNo;
    private int lastSequenceNo = -1;

    public TERunner(String port, Map<ExternalSensorType, TEDataItem> dataItems) {
        this.connection = new TEConnectionImpl(port);
        this.dataItems = dataItems;
    }

    @Override
    public void run() {
        try {
            boolean packetStarted = false;
            List<Byte> buffer = new ArrayList<Byte>(28);
            while (!stop) {
                byte b = connection.readByte();
                if (b == ((byte)0xa5)
                        && buffer.size() >= 1
                        && buffer.get(buffer.size() - 1) == ((byte)0x5a)) {
                    packetStarted = true;
                    buffer.clear();
                    buffer.add((byte) 0x5a);
                    buffer.add(b);
                }
                else if (packetStarted && buffer.size() <= 28) {
                    buffer.add(b);
                    switch (buffer.size()) {
                        case 3:
                            sequenceNo = asUnsignedInt(buffer.get(2));
                            break;
                        case 27:
                            byteSum = 0;
                            for (byte b1 : buffer) {
                                byteSum = (byte) (byteSum + b1);
                            }
                            byteSum = (byte) ~byteSum; // 1's complement of sum
                            break;
                        case 28:
                            if (LOGGER.isTraceEnabled())
                                LOGGER.trace("Tech Edge (data 2.0): LastSeq:" + lastSequenceNo + " seq:" + sequenceNo + " data:" + buffer);
                            if (byteSum != b) {
                                LOGGER.error("Tech Edge (data 2.0): CheckSum Failed, calculated:" + byteSum + ", received:" + b);
                            }
                            if (lastSequenceNo == -1) {
                                lastSequenceNo = sequenceNo;
                            }
                            else {
                                if (lastSequenceNo == 0xff) {
                                    if (sequenceNo != 0x00) {
                                           LOGGER.error("Tech Edge (data 2.0): Packet Drop: expected sequence number:0" + ", received:" + sequenceNo);
                                        lastSequenceNo = sequenceNo;
                                    }
                                    else {
                                        lastSequenceNo = sequenceNo;
                                    }
                                }
                                else {
                                    if ((lastSequenceNo + 1) != sequenceNo) {
                                        LOGGER.error("Tech Edge (data 2.0): Packet Drop: expected sequence number:" + (lastSequenceNo + 1) + ", received:" + sequenceNo);
                                        lastSequenceNo = sequenceNo;
                                    }
                                    else {
                                        TEDataItem dataItem = dataItems.get(WIDEBAND);
                                        if (dataItem != null) {
                                            int raw1 = asUnsignedInt(buffer.get(5));
                                            int raw2 = asUnsignedInt(buffer.get(6));
                                            dataItem.setRaw(raw1, raw2);
                                        }
                                        dataItem = dataItems.get(USER1);
                                        if (dataItem != null) {
                                            int raw1 = asUnsignedInt(buffer.get(9));
                                            int raw2 = asUnsignedInt(buffer.get(10));
                                            dataItem.setRaw(raw1, raw2);
                                        }
                                        dataItem = dataItems.get(USER2);
                                        if (dataItem != null) {
                                            int raw1 = asUnsignedInt(buffer.get(11));
                                            int raw2 = asUnsignedInt(buffer.get(12));
                                            dataItem.setRaw(raw1, raw2);
                                        }
                                        dataItem = dataItems.get(USER3);
                                        if (dataItem != null) {
                                            int raw1 = asUnsignedInt(buffer.get(13));
                                            int raw2 = asUnsignedInt(buffer.get(14));
                                            dataItem.setRaw(raw1, raw2);
                                        }
                                        dataItem = dataItems.get(THERMACOUPLE1);
                                        if (dataItem != null) {
                                            int raw1 = asUnsignedInt(buffer.get(15));
                                            int raw2 = asUnsignedInt(buffer.get(16));
                                            dataItem.setRaw(raw1, raw2);
                                        }
                                        dataItem = dataItems.get(THERMACOUPLE2);
                                        if (dataItem != null) {
                                            int raw1 = asUnsignedInt(buffer.get(17));
                                            int raw2 = asUnsignedInt(buffer.get(18));
                                            dataItem.setRaw(raw1, raw2);
                                        }
                                        dataItem = dataItems.get(THERMACOUPLE3);
                                        if (dataItem != null) {
                                            int raw1 = asUnsignedInt(buffer.get(19));
                                            int raw2 = asUnsignedInt(buffer.get(20));
                                            dataItem.setRaw(raw1, raw2);
                                        }
                                        dataItem = dataItems.get(TorVss);
                                        if (dataItem != null) {
                                            int raw1 = asUnsignedInt(buffer.get(21));
                                            int raw2 = asUnsignedInt(buffer.get(22));
                                            dataItem.setRaw(raw1, raw2);
                                        }
                                        dataItem = dataItems.get(ENGINE_SPEED);
                                        if (dataItem != null) {
                                            int raw1 = asUnsignedInt(buffer.get(23));
                                            int raw2 = asUnsignedInt(buffer.get(24));
                                            dataItem.setRaw(raw1, raw2);
                                        }
                                        lastSequenceNo++;
                                    }
                                }
                            }
                            buffer.clear();
                            packetStarted = false;
                            break;
                    }
                }
                else {
                    buffer.add(b);
                    packetStarted = false;
                }
            }
            connection.close();
        }
        catch (Throwable t) {
            LOGGER.error("Error occurred", t);
        }
        finally {
            connection.close();
        }
    }

    @Override
    public void stop() {
        stop = true;
    }
}
