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

package com.romraider.io.j2534.op20;

import com.jinvoke.JInvoke;
import com.jinvoke.NativeImport;

public final class OpenPort20 {
    private static final String OP20PT32_DLL = "op20pt32";

    // FIX - Split out and separate these
    public static final int STATUS_NOERROR = 0x00;
    public static final int STATUS_ERR_TIMEOUT = 0x09;
    public static final int PROTOCOL_ISO9141 = 3;
    public static final int FLAG_NONE = 0x00;
    public static final int FLAG_ISO9141_NO_CHECKSUM = 0x00000200;
    public static final int FILTER_PASS = 0x00000001;
    public static final int IOCTL_GET_CONFIG = 0x01;
    public static final int IOCTL_SET_CONFIG = 0x02;
    public static final int CONFIG_DATA_RATE = 0x01;
    public static final int CONFIG_LOOPBACK = 0x03;
    public static final int CONFIG_P1_MAX = 0x07;
    public static final int CONFIG_P3_MIN = 0x0A;
    public static final int CONFIG_P4_MIN = 0x0C;

    static {
        JInvoke.initialize();
    }

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruOpen(String pName, int[] pDeviceID);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruClose(int DeviceID);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruReadVersion(int DeviceID, byte[] pFirmwareVersion, byte[] pDllVersion, byte[] pApiVersion);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruGetLastError(byte[] pErrorDescription);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruConnect(int DeviceID, int ProtocolID, int Flags, int BaudRate, int[] pChannelID);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruDisconnect(int ChannelID);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruStartMsgFilter(int ChannelID, int FilterType, PassThruMessage pMaskMsg, PassThruMessage pPatternMsg, PassThruMessage pFlowControlMsg, int[] pMsgID);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruStopMsgFilter(int ChannelID, int MsgID);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruIoctl(int ChannelID, int IoctlID, SConfigList pInput, SConfigList pOutput);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruIoctl(int ChannelID, int IoctlID, SByteArray pInput, SByteArray pOutput);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruWriteMsgs(int ChannelID, PassThruMessage pMsg, int[] pNumMsgs, int Timeout);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruReadMsgs(int ChannelID, PassThruMessage pMsg, int[] pNumMsgs, int Timeout);

    @NativeImport(library = OP20PT32_DLL)
    public static native int PassThruReadMsgs(int ChannelID, PassThruMessage[] pMsgs, int[] pNumMsgs, int Timeout);

    // FIX - Add support for these too...
//    @NativeImport(library = OP20PT32_DLL)
//    public static native int PassThruSetProgrammingVoltage(int DeviceID, int PinNumber, int Voltage);

//    @NativeImport(library = OP20PT32_DLL)
//    public static native int PassThruStartPeriodicMsg(int ChannelID, PassThruMessage pMsg, int[] pMsgID, int TimeInterval);

//    @NativeImport(library = OP20PT32_DLL)
//    public static native int PassThruStopPeriodicMsg(int ChannelID, int MsgID);
}
