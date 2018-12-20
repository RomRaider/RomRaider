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

package com.romraider.io.j2534.api;

import java.nio.ByteBuffer;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.NativeLongByReference;
/**
 * JNA Wrapper for Native library <b>J2534 v0404</b><br>
 */
public class J2534_v0404 implements Library {
            
    public J2534_v0404(String library) {
        NativeLibrary.getInstance(library);
        Native.register(library);
    }

    public native NativeLong PassThruOpen(
            Pointer pName,
            NativeLongByReference pDeviceID
            );
    public native NativeLong PassThruClose(
            NativeLong DeviceID
            );
    public native NativeLong PassThruConnect(
            NativeLong DeviceID,
            NativeLong protocolID,
            NativeLong flags,
            NativeLong baud,
            NativeLongByReference pChannelID
            );
    public native NativeLong PassThruDisconnect(
            NativeLong channelID
            );
    public native NativeLong PassThruReadMsgs(
            NativeLong ChannelID,
            Pointer pMsg,
            NativeLongByReference pNumMsgs,
            NativeLong Timeout
            );
    public native NativeLong PassThruWriteMsgs(
            NativeLong ChannelID,
            Pointer pMsg,
            NativeLongByReference pNumMsgs,
            NativeLong Timeout
            );
    public native NativeLong PassThruStartPeriodicMsg(
            NativeLong channelID,
            Pointer pMsg,
            NativeLongByReference pMsgID,
            NativeLong timeInterval
            );
    public native NativeLong PassThruStopPeriodicMsg(
            NativeLong channelID,
            NativeLong msgID
            );
    public native NativeLong PassThruStartMsgFilter(
            NativeLong ChannelID,
            NativeLong FilterType,
            Pointer pMaskMsg,
            Pointer pPatternMsg,
            Pointer pFlowControlMsg,
            NativeLongByReference pMsgID
            );
    public native NativeLong PassThruStopMsgFilter(
            NativeLong channelID,
            NativeLong msgID
            );
    public native NativeLong PassThruSetProgrammingVoltage(
            NativeLong pinNumber,
            NativeLong voltage
            );
    public native NativeLong PassThruReadVersion(
            NativeLong DeviceID,
            ByteBuffer pFirmwareVersion,
            ByteBuffer pDllVersion,
            ByteBuffer pApiVersion
            );
    public native NativeLong PassThruGetLastError(
            ByteBuffer pErrorDescription
            );
    public native NativeLong PassThruIoctl(
            NativeLong channelID,
            NativeLong ioctlID,
            Pointer pInput,
            Pointer pOutput
            );

    public static class SCONFIG extends Structure {
        public NativeLong parameter;
        public NativeLong value;
        public SCONFIG() {
            initFieldOrder();
        }
        protected void initFieldOrder() {
            setFieldOrder(new String[]{"parameter", "value"});
        }
        public static class ByReference
            extends SCONFIG
            implements Structure.ByReference {
        }
        public static class ByValue
            extends SCONFIG
            implements Structure.ByValue {
        }
    }

    public static class SCONFIG_LIST extends Structure {
        public NativeLong numOfParams;
        public SCONFIG.ByReference configPtr;
        public SCONFIG_LIST() {
            initFieldOrder();
        }
        protected void initFieldOrder() {
            setFieldOrder(new String[]{"numOfParams", "configPtr"});
        }
        public static class ByReference
            extends SCONFIG_LIST
            implements Structure.ByReference {
        }
        public static class ByValue
            extends SCONFIG_LIST
            implements Structure.ByValue {
        }
    }

    public static class PASSTHRU_MSG extends Structure {
        public NativeLong protocolID;
        public NativeLong rxStatus;
        public NativeLong txFlags;
        public NativeLong timestamp;
        public NativeLong dataSize;
        public NativeLong extraDataIndex;
        public byte[] data = new byte[4128];
        public PASSTHRU_MSG() {
            super();
            initFieldOrder();
        }
        protected void initFieldOrder() {
            setFieldOrder(new String[]{
                    "protocolID", "rxStatus", "txFlags",
                    "timestamp", "dataSize", "extraDataIndex", "data"});
        }
        public static class ByReference
            extends PASSTHRU_MSG
            implements Structure.ByReference {
        }
        public static class ByValue
            extends PASSTHRU_MSG
            implements Structure.ByValue {
        }
    }

    public static class SBYTE_ARRAY extends Structure {
        public NativeLong numOfBytes;
        public Pointer bytePtr;
        public SBYTE_ARRAY() {
            super();
            initFieldOrder();
        }
        protected void initFieldOrder() {
            setFieldOrder(new String[]{"numOfBytes", "bytePtr"});
        }
        public static class ByReference
            extends SBYTE_ARRAY
            implements Structure.ByReference {
        }
        public static class ByValue
            extends SBYTE_ARRAY
            implements Structure.ByValue {
        }
    }
}
