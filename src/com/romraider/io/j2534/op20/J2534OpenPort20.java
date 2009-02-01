/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

import com.romraider.io.j2534.api.ConfigItem;
import com.romraider.io.j2534.api.J2534;
import com.romraider.io.j2534.api.J2534Exception;
import com.romraider.io.j2534.api.Version;
import static com.romraider.io.j2534.op20.OpenPort20.FILTER_PASS;
import static com.romraider.io.j2534.op20.OpenPort20.IOCTL_GET_CONFIG;
import static com.romraider.io.j2534.op20.OpenPort20.IOCTL_SET_CONFIG;
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
import static com.romraider.io.j2534.op20.OpenPort20.STATUS_ERR_TIMEOUT;
import static com.romraider.io.j2534.op20.OpenPort20.STATUS_NOERROR;
import static com.romraider.util.HexUtil.asHex;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import static java.lang.System.arraycopy;
import java.util.ArrayList;
import java.util.List;

public final class J2534OpenPort20 implements J2534 {
    private static final Logger LOGGER = getLogger(J2534OpenPort20.class);
    private final int protocol;

    public J2534OpenPort20(int protocol) {
        this.protocol = protocol;
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

    public int connect(int deviceId, int flags, int baud) {
        int[] channelId = {0};
        int status = PassThruConnect(deviceId, protocol, flags, baud, channelId);
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

    public void writeMsg(int channelId, byte[] data, long timeout) {
        PassThruMessage msg = passThruMessage(data);
        LOGGER.trace("Write Msg: " + toString(msg));
        int[] pNumMsgs = {1};
        int status = PassThruWriteMsgs(channelId, msg, pNumMsgs, (int) timeout);
        if (status != STATUS_NOERROR) handleError(status);
    }

    public void readMsg(int channelId, byte[] response, long timeout) {
        byte[] data = readMsg(channelId, timeout);
        arraycopy(data, 0, response, 0, response.length);
    }

    public byte[] readMsg(int channelId, long maxWait) {
        byte[] data = doReadMsg(channelId, (int) maxWait);
        LOGGER.trace("Response: " + asHex(data));
        return data;
    }

    // FIX - Optimise & cleanup!!!
    private byte[] doReadMsg(int channelId, int timeout) {
        PassThruMessage msg1 = passThruMessage();
        PassThruMessage msg2 = passThruMessage();
        PassThruMessage msg3 = passThruMessage();
        PassThruMessage msg4 = passThruMessage();
        PassThruMessage[] msgs = {msg1, msg2, msg3, msg4};
        int[] pNumMsgs = {4};
        int status = PassThruReadMsgs(channelId, msgs, pNumMsgs, timeout);
        if (status != STATUS_NOERROR && status != STATUS_ERR_TIMEOUT) handleError(status);
        LOGGER.trace("Read Msg: " + toString(msg2) + toString(msg4));
        List<byte[]> responses = new ArrayList<byte[]>();
        responses.add(data(msg2));
        responses.add(data(msg4));
        return concat(responses);
    }

    private byte[] concat(List<byte[]> responses) {
        int length = 0;
        for (byte[] response : responses) length += response.length;
        byte[] result = new byte[length];
        int index = 0;
        for (byte[] response : responses) {
            if (response.length == 0) continue;
            System.arraycopy(response, 0, result, index, response.length);
            index += response.length;
        }
        return result;
    }

    private String toString(PassThruMessage msg) {
        byte[] bytes = new byte[msg.DataSize];
        arraycopy(msg.Data, 0, bytes, 0, bytes.length);
        return "[ProtocolID=" + msg.ProtocolID + "|RxStatus=" + msg.RxStatus + "|TxFlags=" + msg.TxFlags + "|Timestamp=" + msg.Timestamp + "|ExtraDataIndex=" + msg.ExtraDataIndex + "|DataSize=" + msg.DataSize + "|Data=" + asHex(bytes) + "]";
    }

    private boolean isResponse(PassThruMessage msg) {
        if (msg.RxStatus != 0x00) return false;
        return msg.Timestamp != 0;
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

    private PassThruMessage passThruMessage(byte... data) {
        PassThruMessage msg = passThruMessage();
        arraycopy(data, 0, msg.Data, 0, data.length);
        msg.DataSize = data.length;
        return msg;
    }

    private PassThruMessage passThruMessage() {
        PassThruMessage msg = new PassThruMessage();
        msg.ProtocolID = protocol;
        return msg;
    }

    private byte[] data(PassThruMessage msg) {
        int length = msg.DataSize;
        byte[] data = new byte[length];
        arraycopy(msg.Data, 0, data, 0, length);
        return data;
    }

    private void handleError(int status) {
        byte[] error = new byte[80];
        PassThruGetLastError(error);
        throw new J2534Exception("Error: [" + status + "] " + toString(error));
    }

    private String toString(byte[] bytes) {
        String msg = "";
        for (int b : bytes) {
            if (b == 0x00) continue;
            msg += (char) b;
        }
        return msg;
    }
}
