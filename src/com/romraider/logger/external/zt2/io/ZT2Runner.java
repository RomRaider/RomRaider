/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

package com.romraider.logger.external.zt2.io;

import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.zt2.plugin.ZT2DataItem;
import com.romraider.logger.external.zt2.plugin.ZT2SensorType;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorType.AFR;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorType.EGT;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorType.MAP;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorType.RPM;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorType.TPS;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ZT2Runner implements Stoppable {
    private static final Logger LOGGER = getLogger(ZT2Runner.class);
    private final Map<ZT2SensorType, ZT2DataItem> dataItems;
    private final ZT2Connection connection;
    private boolean stop;

    public ZT2Runner(String port, Map<ZT2SensorType, ZT2DataItem> dataItems) {
        this.connection = new ZT2ConnectionImpl(port);
        this.dataItems = dataItems;
    }

    public void run() {
        try {
            boolean packetStarted = false;
            List<Byte> buffer = new ArrayList<Byte>(14);
            while (!stop) {
                byte b = connection.readByte();
                if (b == 0x02
                        && buffer.size() >= 2
                        && buffer.get(buffer.size() - 1) == 0x01
                        && buffer.get(buffer.size() - 2) == 0x00) {
                    packetStarted = true;
                    buffer.clear();
                    buffer.add((byte) 0x00);
                    buffer.add((byte) 0x01);
                    buffer.add(b);

                } else if (packetStarted && buffer.size() <= 14) {
                    buffer.add(b);
                    switch (buffer.size()) {
                        case 4:
                            ZT2DataItem afrDataItem = dataItems.get(AFR);
                            if (afrDataItem != null) {
                                int raw = convertAsUnsignedByteToInt(buffer.get(3));
                                afrDataItem.setRaw(raw);
                            }
                            break;
                        case 6:
                            ZT2DataItem egtDataItem = dataItems.get(EGT);
                            if (egtDataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(4));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(5));
                                egtDataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 8:
                            ZT2DataItem rpmDataItem = dataItems.get(RPM);
                            if (rpmDataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(6));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(7));
                                rpmDataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 10:
                            ZT2DataItem mapDataItem = dataItems.get(MAP);
                            if (mapDataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(8));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(9));
                                mapDataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 11:
                            ZT2DataItem tpsDataItem = dataItems.get(TPS);
                            if (tpsDataItem != null) {
                                int raw = convertAsUnsignedByteToInt(buffer.get(10));
                                tpsDataItem.setRaw(raw);
                            }
                            break;
                        case 14:
                            buffer.clear();
                            packetStarted = false;
                            break;
                    }
                } else {
                    buffer.add(b);
                    packetStarted = false;
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Error occurred", t);
        } finally {
            connection.close();
        }
    }

    public void stop() {
        stop = true;
        connection.close();
    }

    private int convertAsUnsignedByteToInt(byte aByte) {
        // A byte in java is signed, so -128 to 128
        // unlike in other platforms where it's
        // normally unsigned, so 0-255
        return (int) aByte & 0xFF;
    }
}
