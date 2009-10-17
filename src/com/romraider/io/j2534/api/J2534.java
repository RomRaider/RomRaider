/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

public interface J2534 {
    int open();

    Version readVersion(int deviceId);

    int connect(int deviceId, int flags, int baud);

    void setConfig(int channelId, ConfigItem... items);

    ConfigItem[] getConfig(int channelId, int... parameters);

    int startPassMsgFilter(int channelId, byte mask, byte pattern);

    void writeMsg(int channelId, byte[] data, long timeout);

    byte[] readMsg(int channelId, long maxWait);

    void readMsg(int channelId, byte[] response, long timeout);

    void stopMsgFilter(int channelId, int msgId);

    void disconnect(int channelId);

    void close(int deviceId);
}
