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

package com.romraider.logger.external.te.io;

import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.te.plugin.TEDataItem;
import com.romraider.logger.external.te.plugin.TESensorType;
import static com.romraider.logger.external.te.plugin.TESensorType.Lambda;
import static com.romraider.logger.external.te.plugin.TESensorType.USR1;
import static com.romraider.logger.external.te.plugin.TESensorType.USR2;
import static com.romraider.logger.external.te.plugin.TESensorType.USR3;
import static com.romraider.logger.external.te.plugin.TESensorType.TC1;
import static com.romraider.logger.external.te.plugin.TESensorType.TC2;
import static com.romraider.logger.external.te.plugin.TESensorType.TC3;
import static com.romraider.logger.external.te.plugin.TESensorType.TorVss;
import static com.romraider.logger.external.te.plugin.TESensorType.RPM;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TERunner implements Stoppable {
    private static final Logger LOGGER = getLogger(TERunner.class);
    private final Map<TESensorType, TEDataItem> dataItems;
    private final TEConnection connection;
    private boolean stop;

    public TERunner(String port, Map<TESensorType, TEDataItem> dataItems) {
        this.connection = new TEConnectionImpl(port);
        this.dataItems = dataItems;
    }

    public void run() {
        try {
            boolean packetStarted = false;
            List<Byte> buffer = new ArrayList<Byte>(28);
            while (!stop) {
                byte b = connection.readByte();
                if (convertAsUnsignedByteToInt(b) == 0xa5
                        && buffer.size() >= 1
                        && buffer.get(buffer.size() - 1) == 0x5a) {
                    packetStarted = true;
                    buffer.clear();
                    buffer.add((byte) 0x5a);
                    buffer.add(b);

                } else if (packetStarted && buffer.size() <= 28) {
                    buffer.add(b);
                    switch (buffer.size()) {
                        case 7:
                            TEDataItem lambdaDataItem = dataItems.get(Lambda);
                            if (lambdaDataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(5));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(6));
                                lambdaDataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 11:
                            TEDataItem usr1DataItem = dataItems.get(USR1);
                            if (usr1DataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(9));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(10));
                                usr1DataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 13:
                            TEDataItem usr2DataItem = dataItems.get(USR2);
                            if (usr2DataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(11));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(12));
                                usr2DataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 15:
                            TEDataItem usr3DataItem = dataItems.get(USR3);
                            if (usr3DataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(13));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(14));
                                usr3DataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 17:
                            TEDataItem tc1DataItem = dataItems.get(TC1);
                            if (tc1DataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(15));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(16));
                                tc1DataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 19:
                            TEDataItem tc2DataItem = dataItems.get(TC2);
                            if (tc2DataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(17));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(18));
                                tc2DataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 21:
                            TEDataItem tc3DataItem = dataItems.get(TC3);
                            if (tc3DataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(19));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(20));
                                tc3DataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 23:
                            TEDataItem tOrVssDataItem = dataItems.get(TorVss);
                            if (tOrVssDataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(21));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(22));
                                tOrVssDataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 25:
                            TEDataItem rpmDataItem = dataItems.get(RPM);
                            if (rpmDataItem != null) {
                                int raw1 = convertAsUnsignedByteToInt(buffer.get(23));
                                int raw2 = convertAsUnsignedByteToInt(buffer.get(24));
                                rpmDataItem.setRaw(raw1, raw2);
                            }
                            break;
                        case 28:
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
