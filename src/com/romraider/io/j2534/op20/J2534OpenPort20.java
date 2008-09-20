package com.romraider.io.j2534.op20;

import com.romraider.io.j2534.api.ConfigItem;
import com.romraider.io.j2534.api.J2534;
import com.romraider.io.j2534.api.J2534Exception;
import com.romraider.io.j2534.api.Version;
import static com.romraider.io.j2534.op20.OpenPort20.FILTER_PASS;
import static com.romraider.io.j2534.op20.OpenPort20.FLAG_NONE;
import static com.romraider.io.j2534.op20.OpenPort20.IOCTL_GET_CONFIG;
import static com.romraider.io.j2534.op20.OpenPort20.IOCTL_SET_CONFIG;
import static com.romraider.io.j2534.op20.OpenPort20.PROTOCOL_ISO9141;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruClose;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruConnect;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruDisconnect;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruGetLastError;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruIoctl;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruOpen;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruReadMsgs;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruReadVersion;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruStartMsgFilter;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruStopMsgFilter;
import static com.romraider.io.j2534.op20.OpenPort20.PassThruWriteMsgs;
import static com.romraider.io.j2534.op20.OpenPort20.STATUS_NOERROR;

public final class J2534OpenPort20 implements J2534 {
    private final boolean supported = OpenPort20.isSupported();

    public boolean isSupported() {
        return supported;
    }

    public int open() {
        int[] deviceId = {0};
        int status = PassThruOpen(null, deviceId);
        if (status != STATUS_NOERROR) handleError(status);
        return deviceId[0];
    }

    public Version readVersion(int deviceId) {
        byte[] firmware = new byte[80];
        byte[] dll = new byte[80];
        byte[] api = new byte[80];
        int status = PassThruReadVersion(deviceId, firmware, dll, api);
        if (status != STATUS_NOERROR) handleError(status);
        return new Version(toString(firmware), toString(dll), toString(api));
    }

    public int connect(int deviceId) {
        int[] channelId = {0};
        int status = PassThruConnect(deviceId, PROTOCOL_ISO9141, FLAG_NONE, 4800, channelId);
        if (status != STATUS_NOERROR) handleError(status);
        return channelId[0];
    }

    public void setConfig(int channelId, ConfigItem... items) {
        if (items.length == 0) return;
        SConfig[] sConfigs = sConfigs(items);
        SConfigList list = sConfigList(sConfigs);
        int status = PassThruIoctl(channelId, IOCTL_SET_CONFIG, list, null);
        if (status != STATUS_NOERROR) handleError(status);
    }

    public ConfigItem[] getConfig(int channelId, int... parameters) {
        if (parameters.length == 0) return configItems();
        SConfig[] sConfigs = sConfigs(parameters);
        SConfigList input = sConfigList(sConfigs);
        int status = PassThruIoctl(channelId, IOCTL_GET_CONFIG, input, null);
        if (status != STATUS_NOERROR) handleError(status);
        return configItems(input.ConfigPtr);
    }

    public int startPassMsgFilter(int channelId, byte mask, byte pattern) {
        PassThruMessage maskMsg = passThruMessage(mask);
        PassThruMessage patternMsg = passThruMessage(pattern);
        int[] msgId = {0};
        int status = PassThruStartMsgFilter(channelId, FILTER_PASS, maskMsg, patternMsg, null, msgId);
        if (status != STATUS_NOERROR) handleError(status);
        return msgId[0];
    }

    public void writeMsgs(int channelId, byte[] data) {
        PassThruMessage msg = passThruMessage(data);
        int[] pNumMsgs = {1};
        int status = PassThruWriteMsgs(channelId, msg, pNumMsgs, 55);
        if (status != STATUS_NOERROR) handleError(status);
    }

    public byte[] readMsgs(int channelId) {
        PassThruMessage msg = passThruMessage();
        int[] pNumMsgs = {1};
        int status = PassThruReadMsgs(channelId, msg, pNumMsgs, 0);
        if (status != STATUS_NOERROR) handleError(status);
        return data(msg);
    }

    public void stopMsgFilter(int channelId, int msgId) {
        int status = PassThruStopMsgFilter(channelId, msgId);
        if (status != STATUS_NOERROR) handleError(status);
    }

    public void disconnect(int channelId) {
        int status = PassThruDisconnect(channelId);
        if (status != STATUS_NOERROR) handleError(status);
    }

    public void close(int deviceId) {
        int status = PassThruClose(deviceId);
        if (status != STATUS_NOERROR) handleError(status);
    }

    private ConfigItem[] configItems(SConfig... sConfigs) {
        ConfigItem[] items = new ConfigItem[sConfigs.length];
        for (int i = 0; i < sConfigs.length; i++) {
            SConfig sConfig = sConfigs[i];
            items[i] = new ConfigItem(sConfig.Parameter, sConfig.Value);
        }
        return items;
    }

    private SConfig[] sConfigs(ConfigItem... items) {
        SConfig[] sConfigs = new SConfig[items.length];
        for (int i = 0; i < items.length; i++) {
            SConfig sconfig = new SConfig();
            sconfig.Parameter = items[i].parameter;
            sconfig.Value = items[i].value;
            sConfigs[i] = sconfig;
        }
        return sConfigs;
    }

    private SConfig[] sConfigs(int... parameters) {
        SConfig[] sConfigs = new SConfig[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            SConfig sConfig = new SConfig();
            sConfig.Parameter = parameters[i];
            sConfigs[i] = sConfig;
        }
        return sConfigs;
    }

    private SConfigList sConfigList(SConfig[] sconfigs) {
        SConfigList input = new SConfigList();
        input.NumOfParams = sconfigs.length;
        input.ConfigPtr = sconfigs;
        return input;
    }

    private static PassThruMessage passThruMessage(byte... data) {
        PassThruMessage msg = passThruMessage();
        System.arraycopy(data, 0, msg.Data, 0, data.length);
        msg.DataSize = data.length;
        return msg;
    }

    private static PassThruMessage passThruMessage() {
        PassThruMessage msg = new PassThruMessage();
        msg.ProtocolID = PROTOCOL_ISO9141;
        return msg;
    }

    private static byte[] data(PassThruMessage msg) {
        int length = msg.DataSize;
        byte[] data = new byte[length];
        System.arraycopy(msg.Data, 0, data, 0, length);
        return data;
    }

    private static void handleError(int status) {
        byte[] error = new byte[80];
        PassThruGetLastError(error);
        throw new J2534Exception("Error: [" + status + "] " + toString(error));
    }

    private static String toString(byte[] bytes) {
        String msg = "";
        for (int b : bytes) {
            if (b == 0x00) continue;
            msg += (char) b;
        }
        return msg;
    }
}
