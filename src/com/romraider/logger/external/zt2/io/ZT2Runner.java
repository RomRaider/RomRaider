/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

import static com.romraider.logger.external.core.ExternalSensorType.EGT;
import static com.romraider.logger.external.core.ExternalSensorType.ENGINE_SPEED;
import static com.romraider.logger.external.core.ExternalSensorType.MAP;
import static com.romraider.logger.external.core.ExternalSensorType.TPS;
import static com.romraider.logger.external.core.ExternalSensorType.USER1;
import static com.romraider.logger.external.core.ExternalSensorType.WIDEBAND;
import static com.romraider.util.ByteUtil.asUnsignedInt;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.romraider.logger.external.core.ExternalSensorType;
import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.zt2.plugin.ZT2DataItem;

public final class ZT2Runner implements Stoppable {
    private static final Logger LOGGER = getLogger(ZT2Runner.class);
    private final Map<ExternalSensorType, ZT2DataItem> dataItems;
    private final ZT2Connection connection;
    private boolean stop;

    public ZT2Runner(String port, Map<ExternalSensorType, ZT2DataItem> dataItems) {
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
                    ZT2DataItem dataItem = dataItems.get(WIDEBAND);
                    switch (buffer.size()) {
                        case 4:
                            if (dataItem != null) {
                                int raw = asUnsignedInt(buffer.get(3));
                                dataItem.setRaw(raw);
                            }
                            break;
                        case 6:
                            dataItem = dataItems.get(EGT);
                            if (dataItem != null) {
                                int raw1 = asUnsignedInt(buffer.get(4));
                                int raw2 = asUnsignedInt(buffer.get(5));
                                dataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 8:
                            dataItem = dataItems.get(ENGINE_SPEED);
                            if (dataItem != null) {
                                int raw1 = asUnsignedInt(buffer.get(6));
                                int raw2 = asUnsignedInt(buffer.get(7));
                                dataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 10:
                            dataItem = dataItems.get(MAP);
                            if (dataItem != null) {
                                int raw1 = asUnsignedInt(buffer.get(8));
                                int raw2 = asUnsignedInt(buffer.get(9));
                                dataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 11:
                            dataItem = dataItems.get(TPS);
                            if (dataItem != null) {
                                int raw = asUnsignedInt(buffer.get(10));
                                dataItem.setRaw(raw);
                            }
                            break;
                        case 12:
                            dataItem = dataItems.get(USER1);
                            if (dataItem != null) {
                                int raw = asUnsignedInt(buffer.get(11));
                                dataItem.setRaw(raw);
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
            connection.close();
        } catch (Throwable t) {
            LOGGER.error("ZT2 error occurred", t);
        } finally {
            connection.close();
        }
    }

    public void stop() {
        stop = true;
    }
}
