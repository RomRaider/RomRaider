package com.romraider.io.j2534.op20;

import static com.jinvoke.JInvoke.initialize;
import com.jinvoke.NativeImport;
import java.io.File;

public final class OpenPort20 {
    private static final String OP20PT32_DLL_LOCATION = "C:/windows/system32/op20pt32.dll";

    // FIX - Split out and separate these
    public static final int STATUS_NOERROR = 0x00;
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
        if (isSupported()) initialize();
    }

    public static boolean isSupported() {
        File dll = new File(OP20PT32_DLL_LOCATION);
        return dll.exists();
    }

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruOpen(String pName, int[] pDeviceID);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruClose(int DeviceID);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruReadVersion(int DeviceID, byte[] pFirmwareVersion, byte[] pDllVersion, byte[] pApiVersion);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruGetLastError(byte[] pErrorDescription);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruConnect(int DeviceID, int ProtocolID, int Flags, int BaudRate, int[] pChannelID);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruDisconnect(int ChannelID);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruStartMsgFilter(int ChannelID, int FilterType, PassThruMessage pMaskMsg, PassThruMessage pPatternMsg, PassThruMessage pFlowControlMsg, int[] pMsgID);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruStopMsgFilter(int ChannelID, int MsgID);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruIoctl(int ChannelID, int IoctlID, SConfigList pInput, SConfigList pOutput);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruIoctl(int ChannelID, int IoctlID, SByteArray pInput, SByteArray pOutput);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruWriteMsgs(int ChannelID, PassThruMessage pMsg, int[] pNumMsgs, int Timeout);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruReadMsgs(int ChannelID, PassThruMessage pMsg, int[] pNumMsgs, int Timeout);

    @NativeImport(library = OP20PT32_DLL_LOCATION)
    public static native int PassThruReadMsgs(int ChannelID, PassThruMessage[] pMsgs, int[] pNumMsgs, int Timeout);

    // FIX - Add support for these too...
//    @NativeImport(library = OP20PT32_DLL_LOCATION)
//    public static native int PassThruSetProgrammingVoltage(int DeviceID, int PinNumber, int Voltage);

//    @NativeImport(library = OP20PT32_DLL_LOCATION)
//    public static native int PassThruStartPeriodicMsg(int ChannelID, PassThruMessage pMsg, int[] pMsgID, int TimeInterval);

//    @NativeImport(library = OP20PT32_DLL_LOCATION)
//    public static native int PassThruStopPeriodicMsg(int ChannelID, int MsgID);
}
