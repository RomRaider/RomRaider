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

import static java.lang.System.arraycopy;
import static java.lang.System.currentTimeMillis;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.romraider.io.j2534.api.J2534_v0404.PASSTHRU_MSG;
import com.romraider.io.j2534.api.J2534_v0404.SCONFIG;
import com.romraider.io.j2534.api.J2534_v0404.SCONFIG.ByReference;
import com.romraider.io.j2534.api.J2534_v0404.SCONFIG_LIST;
import com.romraider.util.HexUtil;
import com.romraider.util.ThreadUtil;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

/**
 * J2534 Implementation of the Native library wrapper <b>J2534 v0404</b>
 */
public final class J2534Impl implements J2534 {
    private static final Logger LOGGER = Logger.getLogger(J2534Impl.class);
    private final NativeLong protocolID;
    private boolean loopback;
    private static J2534_v0404 lib;

    /**
    *    Enum class representing the J2534-1 protocols with methods to
    *    translate the mnemonic and numerical values.
    */
    public enum Protocol {
        J1850VPW        (0x01),
        J1850PWM        (0x02),
        ISO9141            (0x03),
        ISO14230        (0x04),
        CAN                (0x05),
        ISO15765        (0x06),
        SCI_A_ENGINE     (0x07),    // OP2.0: Not supported
        SCI_A_TRANS         (0x08),    // OP2.0: Not supported
        SCI_B_ENGINE    (0x09),    // OP2.0: Not supported
        SCI_B_TRANS        (0x0A),    // OP2.0: Not supported
        UNDEFINED (0xFFFFFFFF); // Returned when no match is found for get()

        private static final Map<Integer, Protocol> lookup
                    = new HashMap<Integer, Protocol>();

        static {
            for(Protocol s : EnumSet.allOf(Protocol.class))
                lookup.put(s.getValue(), s);
        }
        
        private int value;
        
        private Protocol(int value) {
            this.value = value;
        }
        
        /**
        * @return    the numeric value associated with the <b>Protocol</b>
        *            mnemonic string.
        */
        public int getValue() {
            return value;
        }
        
        /**
        * @param    value - numeric value to be translated.
        * @return    the <b>Protocol</b> mnemonic mapped to the numeric
        *            value or UNDEFINED if value is undefined.
        */
        public static Protocol get(int value) {
            if (lookup.containsKey(value)) {
                return lookup.get(value);
            }
            else return UNDEFINED;
        }
    }

    /**
    *    Enum class representing the J2534-1 protocol flags with methods to
    *    translate the mnemonic and numerical values.
    */
    public enum Flag {
        ISO9141_NO_CHECKSUM    (0x0200),
        UNDEFINED        (0xFFFFFFFF);    // Returned when no match is found for get()

        private static final Map<Integer, Flag> lookup
                = new HashMap<Integer, Flag>();

        static {
            for(Flag s : EnumSet.allOf(Flag.class))
                lookup.put(s.getValue(), s);
        }
        
        private int value;
        
        private Flag(int value) {
            this.value = value;
        }
        
        /**
        * @return    the numeric value associated with the <b>Flag</b>
        *            mnemonic string.
        */
        public int getValue() {
            return value;
        }
        
        /**
        * @param    value - numeric value to be translated.
        * @return    the <b>Flag</b> mnemonic mapped to the numeric
        *            value or UNDEFINED if value is undefined.
        */
        public static Flag get(int value) {
            if (lookup.containsKey(value)) {
                return lookup.get(value);
            }
            else return UNDEFINED;
        }
    }

    /**
    *    Enum class representing the J2534/2 extension types with methods to
    *    translate the mnemonic and numerical values.
    */
    public enum Extension {
           CAN_CH1            (0x00009000),
        J1850VPW_CH1    (0x00009080),
        J1850PWM_CH1    (0x00009160),
        ISO9141_CH1        (0x00009240),
        ISO9141_CH2        (0x00009241),
        ISO9141_CH3        (0x00009242),
        ISO9141_K        (0x00009240),
        ISO9141_L        (0x00009241),    // OP2.0: ISO9141 communications over the L line
        ISO9141_INNO    (0x00009242),    // OP2.0: RS-232 receive-only via the 2.5mm jack
        ISO14230_CH1    (0x00009320),
        ISO14230_CH2    (0x00009321),
        ISO14230_K        (0x00009320),
        ISO14230_L        (0x00009321),    // OP2.0: ISO14230 communications over the L line
        ISO15765_CH1    (0x00009400),
        UNDEFINED        (0xFFFFFFFF);    // Returned when no match is found for get()
    
        private static final Map<Integer, Extension> lookup
                    = new HashMap<Integer, Extension>();
    
        static {
        for(Extension s : EnumSet.allOf(Extension.class))
            lookup.put(s.getValue(), s);
        }
        
        private int value;
        
        private Extension(int value) {
            this.value = value;
        }
        
        /**
        * @return    the numeric value associated with the <b>Extension</b>
        *            mnemonic string.
        */
        public int getValue() {
            return value;
        }
        
        /**
        * @param    value - numeric value to be translated.
        * @return    the <b>Extension</b> mnemonic mapped to the numeric
        *            value or UNDEFINED if value is undefined.
        */
        public static Extension get(int value) {
            if (lookup.containsKey(value)) {
                return lookup.get(value);
            }
            else return UNDEFINED;
        }
    }

    /**
    *    Enum class representing the J2534-1 filter types with methods to
    *    translate the mnemonic and numerical values.
    */
    public enum Filter {
        PASS_FILTER            (0x01),
        BLOCK_FILTER        (0x02),
        FLOW_CONTROL_FILTER    (0x03),
        UNDEFINED      (0xFFFFFFFF);    // Returned when no match is found for get()
        
        private static final Map<Integer, Filter> lookup
                    = new HashMap<Integer, Filter>();

        static {
            for(Filter s : EnumSet.allOf(Filter.class))
                lookup.put(s.getValue(), s);
        }
        
        private int value;
        
        private Filter(int value) {
            this.value = value;
        }
        
        /**
        * @return    the numeric value associated with the <b>Filter</b>
        *            mnemonic string.
        */
        public int getValue() {
            return value;
        }
        
        /**
        * @param    value - numeric value to be translated.
        * @return    the <b>Filter</b> mnemonic mapped to the numeric
        *            value or UNDEFINED if value is undefined.
        */
        public static Filter get(int value) {
            if (lookup.containsKey(value)) {
                return lookup.get(value);
            }
            else return UNDEFINED;
        }
    }

    // IOCTL IDs
    // J2534-1
    /**
    *    Enum class representing the J2534-1 IOCTL types with methods to
    *    translate the mnemonic and numerical values.
    */
    public enum IOCtl {
        GET_CONFIG                        (0x01),
        SET_CONFIG                        (0x02),
        READ_VBATT                        (0x03),
        FIVE_BAUD_INIT                    (0x04),
        FAST_INIT                         (0x05),
        CLEAR_TX_BUFFER                   (0x07),
        CLEAR_RX_BUFFER                   (0x08),
        CLEAR_PERIODIC_MSGS               (0x09),
        CLEAR_MSG_FILTERS                 (0x0A),
        CLEAR_FUNCT_MSG_LOOKUP_TABLE      (0x0B),
        ADD_TO_FUNCT_MSG_LOOKUP_TABLE     (0x0C),
        DELETE_FROM_FUNCT_MSG_LOOUP_TABLE (0x0D),
        READ_PROG_VOLTAGE                 (0x0E),
        UNDEFINED                    (0xFFFFFFFF);
                                        // Returned when no match is found for get()
        
        private static final Map<Integer, IOCtl> lookup
                    = new HashMap<Integer, IOCtl>();
    
        static {
            for(IOCtl s : EnumSet.allOf(IOCtl.class))
                lookup.put(s.getValue(), s);
        }
        
        private int value;
        
        private IOCtl(int value) {
            this.value = value;
        }
        
        /**
        * @return    the numeric value associated with the <b>IOCtl</b>
        *            mnemonic string.
        */
        public int getValue() {
            return value;
        }
        
        /**
        * @param    value - numeric value to be translated.
        * @return    the <b>IOCtl</b> mnemonic mapped to the numeric
        *            value or UNDEFINED if value is undefined.
        */
        public static IOCtl get(int value) {
            if (lookup.containsKey(value)) {
                return lookup.get(value);
            }
            else return UNDEFINED;
        }
    }

    /**
    *    Enum class representing the J2534-1 Get/Set config parameters with methods to
    *    translate the mnemonic and numerical values.
    */
    public enum Config {
        DATA_RATE        (0x01),
        LOOPBACK        (0x03),
        NODE_ADDRESS    (0x04), // OP2.0: Not yet supported
        NETWORK_LINE    (0x05), // OP2.0: Not yet supported
        P1_MIN            (0x06), // J2534 says this may not be changed
        P1_MAX            (0x07),
        P2_MIN            (0x08), // J2534 says this may not be changed
        P2_MAX            (0x09), // J2534 says this may not be changed
        P3_MIN            (0x0A),
        P3_MAX            (0x0B), // J2534 says this may not be changed
        P4_MIN            (0x0C),
        P4_MAX            (0x0D), // J2534 says this may not be changed
        W0                (0x19),
        W1                (0x0E),
        W2                (0x0F),
        W3                (0x10),
        W4                (0x11),
        W5                (0x12),
        TIDLE            (0x13),
        TINIL            (0x14),
        TWUP            (0x15),
        PARITY            (0x16),
        BIT_SAMPLE_POINT(0x17), // OP2.0: Not yet supported
        SYNC_JUMP_WIDTH    (0x18), // OP2.0: Not yet supported
        T1_MAX            (0x1A),
        T2_MAX            (0x1B),
        T3_MAX            (0x24),
        T4_MAX            (0x1C),
        T5_MAX            (0x1D),
        ISO15765_BS        (0x1E),
        ISO15765_STMIN    (0x1F),
        DATA_BITS        (0x20),
        FIVE_BAUD_MOD    (0x21),
        BS_TX            (0x22),
        STMIN_TX        (0x23),
        ISO15765_WFT_MAX(0x25),
        UNDEFINED (0xFFFFFFFF);    // Returned when no match is found for get()
        
        private static final Map<Integer, Config> lookup
                    = new HashMap<Integer, Config>();
    
        static {
            for(Config s : EnumSet.allOf(Config.class))
                lookup.put(s.getValue(), s);
        }
        
        private int value;
        
        private Config(int value) {
            this.value = value;
        }
        
        /**
        * @return    the numeric value associated with the <b>Config</b>
        *            mnemonic string.
        */
        public int getValue() {
            return value;
        }
        
        /**
        * @param    value - numeric value to be translated.
        * @return    the <b>Config</b> mnemonic mapped to the numeric
        *            value or RESERVED if value is undefined.
        */
        public static Config get(int value) {
            if (lookup.containsKey(value)) {
                return lookup.get(value);
            }
            else return UNDEFINED;
        }
    }

    /**
    *    Enum class representing the J2534-1 return error values with methods to
    *    translate the mnemonic and numerical values.
    */
    public enum Status {
        NOERROR                     (0x00),
        ERR_NOT_SUPPORTED         (0x01),
        ERR_INVALID_CHANNEL_ID     (0x02),
        ERR_INVALID_PROTOCOL_ID     (0x03),
        ERR_NULL_PARAMETER         (0x04),
        ERR_INVALID_IOCTL_VALUE     (0x05),
        ERR_INVALID_FLAGS         (0x06),
        ERR_FAILED                 (0x07),
        ERR_DEVICE_NOT_CONNECTED (0x08),
        ERR_TIMEOUT                 (0x09),
        ERR_INVALID_MSG             (0x0A),
        ERR_INVALID_TIME_INTERVAL(0x0B),
        ERR_EXCEEDED_LIMIT         (0x0C),
        ERR_INVALID_MSG_ID         (0x0D),
        ERR_DEVICE_IN_USE         (0x0E),
        ERR_INVALID_IOCTL_ID     (0x0F),
        ERR_BUFFER_EMPTY         (0x10),
        ERR_BUFFER_FULL             (0x11),
        ERR_BUFFER_OVERFLOW         (0x12),
        ERR_PIN_INVALID             (0x13),
        ERR_CHANNEL_IN_USE         (0x14),
        ERR_MSG_PROTOCOL_ID         (0x15),
        ERR_INVALID_FILTER_ID     (0x16),
        ERR_NO_FLOW_CONTROL         (0x17),
        ERR_NOT_UNIQUE             (0x18),
        ERR_INVALID_BAUDRATE     (0x19),
        ERR_INVALID_DEVICE_ID     (0x1A),
        ERR_INVALID_DEVICE_ID_OP2(0x20),    // OP2.0 Tactrix specific
        ERR_OEM_VOLTAGE_TOO_LOW     (0x78),    // OP2.0 Tactrix specific
        ERR_OEM_VOLTAGE_TOO_HIGH (0x77),    // OP2.0 Tactrix specific
        UNDEFINED           (0xFFFFFFFF);    // Returned when no match is found for get()
        
        private static final Map<Integer, Status> lookup
                    = new HashMap<Integer, Status>();
    
        static {
            for(Status s : EnumSet.allOf(Status.class))
                lookup.put(s.getValue(), s);
        }
        
        private int value;
        
        private Status(int value) {
            this.value = value;
        }
        
        /**
        * @return    the numeric value associated with the <b>Status</b>
        *            mnemonic string.
        */
        public int getValue() {
            return value;
        }
        
        /**
        * @param    value - numeric value to be translated.
        * @return    the <b>Status</b> mnemonic mapped to the numeric
        *            value or UNDEFINED if value is undefined.
        */
        public static Status get(int value) {
            if (lookup.containsKey(value)) {
                return lookup.get(value);
            }
            else return UNDEFINED;
        }
    }


    /**
     * Constructor declaration
     * @param         protocolID - numeric ID specified by J2534-1
     * @exception    J2534Exception on various non-zero return status
     * @deprecated
     */
    public J2534Impl(int protocolID) {
        this.protocolID = new NativeLong(protocolID);
    }

    /**
     * Constructor declaration
     * @param         protocol - <b>Protocol</b> enum specified by J2534-1
     * @param        library  - native library of the J2534 device
     * @exception    J2534Exception on various non-zero return status
     */
    public J2534Impl(Protocol protocol, String library) {
        this.protocolID = new NativeLong(protocol.getValue());
        lib = new J2534_v0404(library);
    }

    /**
     * Establish a connection and initialize the PassThru device.
     * @return DeviceID of PassThru device
     */
    public int open() {
        NativeLongByReference pDeviceID = new NativeLongByReference();
        NativeLong ret = lib.PassThruOpen(null, pDeviceID);
        if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                "PassThruOpen", ret.intValue());
        return pDeviceID.getValue().intValue();
    }

    /**
     * Retrieve the PassThru device firmware version,
     * DLL version, and the J2534 implementation version.
     * @param    deviceId - of PassThru device
     * @return    an instance of <b>Version</b>
     * @see        Version
     */
    public Version readVersion(int deviceId) {
        ByteBuffer pFirmwareVersion = ByteBuffer.allocate(80);
        ByteBuffer pDllVersion = ByteBuffer.allocate(80);
        ByteBuffer pApiVersion = ByteBuffer.allocate(80);
        NativeLong ret = lib.PassThruReadVersion(
                new NativeLong(deviceId),
                pFirmwareVersion,
                pDllVersion,
                pApiVersion
            );
        if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                "PassThruReadVersion", ret.intValue());
        return new Version(
                Native.toString(pFirmwareVersion.array()),
                Native.toString(pDllVersion.array()),
                Native.toString(pApiVersion.array()));
    }

    /**
     * Establish a logical connection with a protocol channel of the specified
     * device.
     * @param    deviceId - of PassThru device
     * @param    flags     - protocol specific options
     * @param    baud     - vehicle network communication rate
     * @return    a handle to the open communications channel
     */
    public int connect(int deviceId, int flags, int baud) {
        NativeLongByReference pChannelID = new  NativeLongByReference();
        NativeLong ret = lib.PassThruConnect(
                new NativeLong(deviceId),
                protocolID,
                new NativeLong(flags),
                new NativeLong(baud),
                pChannelID
            );
        if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                "PassThruConnect", ret.intValue());
        return pChannelID.getValue().intValue();
    }

    /**
     * Configures various PassThru parameters.
     * @param    channelId - handle to the open communications channel
     * @param    items      - values of multiple parameters can be set
     *                         in an array of ConfigItem
     */
    public void setConfig(int channelId, ConfigItem... items) {
        if (items.length == 0) return;
        SCONFIG[] sConfigs = sConfigs(items);
        SCONFIG_LIST list = sConfigList(sConfigs);
        NativeLong ioctlID = new NativeLong();
        ioctlID.setValue(IOCtl.SET_CONFIG.getValue());
        NativeLong ret = lib.PassThruIoctl(
                new NativeLong(channelId),
                ioctlID,
                list.getPointer(),
                null
            );
           if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                   "PassThruIoctl (SET_CONFIG)", ret.intValue());
    }

    /**
     * Retrieve various PassThru configuration parameters.
     * @param    channelId  - handle to the open communications channel
     * @param    parameters - values of multiple parameters can be retrieved
     *                          by setting an array of integer parameter IDs
     * @return    an array of <b>ConfigItem</b>
     * @see        ConfigItem
     */
    public ConfigItem[] getConfig(int channelId, int... parameters) {
        if (parameters.length == 0) return new ConfigItem[0];
        SCONFIG[] sConfigs = sConfigs(parameters);
        SCONFIG_LIST input = sConfigList(sConfigs);
        NativeLong ioctlID = new NativeLong();
        ioctlID.setValue(IOCtl.GET_CONFIG.getValue());
        NativeLong ret = lib.PassThruIoctl(
                new NativeLong(channelId),
                ioctlID,
                input.getPointer(),
                null
            );
           if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                   "PassThruIoctl (GET_CONFIG)", ret.intValue());
        return configItems(input);
    }

    /**
     * Setup network protocol filter(s) to selectively restrict or limit
     * messages received.  Then purge the PassThru device's receive buffer.
     * @param    channelId  - handle to the open communications channel
     * @param    mask       - used to isolate the receive message
     *                          header section(s) of interest
     * @param    pattern       - a message pattern to compare with the receive messages
     * @return    the message filter ID to be used to later stop the filter
     */
    public int startPassMsgFilter(int channelId, byte mask, byte pattern) {
        PASSTHRU_MSG maskMsg    = passThruMessage(mask);
        PASSTHRU_MSG patternMsg = passThruMessage(pattern);

        NativeLongByReference msgId = new NativeLongByReference();
        msgId.setValue(new NativeLong(0));
        NativeLong ret = lib.PassThruStartMsgFilter(
                new NativeLong(channelId),
                new NativeLong(Filter.PASS_FILTER.getValue()),
                maskMsg.getPointer(),
                patternMsg.getPointer(),
                null,
                msgId
            );
           if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                   "PassThruStartMsgFilter", ret.intValue());

        ret = lib.PassThruIoctl(
                new NativeLong(channelId),
                new NativeLong(IOCtl.CLEAR_RX_BUFFER.getValue()),
                null,
                null
            );
           if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                   "PassThruIoctl (CLEAR_RX_BUFFER)", ret.intValue());
        return msgId.getValue().intValue();
    }

    /**
     * This function performs an ISO14230 fast initialization sequence.
     * @param    channelId - handle to the open communications channel
     * @param    input      - start message to be transmitted to the vehicle network
     * @return    response  - response upon a successful initialization
     */
    public byte[] fastInit(int channelId, byte[] input) {
        PASSTHRU_MSG inMsg = passThruMessage(input);
        PASSTHRU_MSG outMsg = passThruMessage();
        LOGGER.trace("Ioctl inMsg: " + toString(inMsg));
        NativeLong ret = lib.PassThruIoctl(
                new NativeLong(channelId),
                new NativeLong(IOCtl.FAST_INIT.value),
                inMsg.getPointer(),
                outMsg.getPointer()
            );
           if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                   "PassThruIoctl", ret.intValue());
           outMsg.read();
        LOGGER.trace("Ioctl outMsg: " + toString(outMsg));
        byte[] response = new byte[outMsg.dataSize.intValue()];
        arraycopy(outMsg.data, 0, response, 0, outMsg.dataSize.intValue());
           return response;
    }

    /**
     * This function reads the battery voltage on pin 16 of the J2534 interface.
     * @param  channelId - handle to the open communications channel
     * @return battery voltage in VDC
     */
    public double getVbattery(int channelId) {
        NativeLongByReference vBatt = new NativeLongByReference();
        NativeLong ret = lib.PassThruIoctl(
                new NativeLong(channelId),
                new NativeLong(IOCtl.READ_VBATT.getValue()),
                null,
                vBatt.getPointer()
            );
           if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                   "PassThruIoctl", ret.intValue());
        LOGGER.trace("Ioctl result: " + vBatt.getValue().longValue());
        double response = vBatt.getValue().doubleValue() / 1000;
        return response;
    }

    /**
     * Send a message through the existing communication channel to the vehicle.
     * @param    channelId - handle to the open communications channel
     * @param    data      - data bytes to be transmitted to the vehicle network
     * @param    timeout      - maximum time (in milliseconds) for write completion
     */
    public void writeMsg(int channelId, byte[] data, long timeout) {
        PASSTHRU_MSG msg = passThruMessage(data);
        LOGGER.trace("Write Msg: " + toString(msg));
        NativeLongByReference numMsg = new NativeLongByReference();
        numMsg.setValue(new NativeLong(1));
        NativeLong ret = lib.PassThruWriteMsgs(
                new NativeLong(channelId),
                msg.getPointer(),
                numMsg,
                new NativeLong(timeout)
            );
           if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                   "PassThruWriteMsgs", ret.intValue());
    }

    /**
     * Retrieve a message through the existing communication channel from the vehicle.
     * @param    channelId - handle to the open communications channel
     * @param    response  - data array to be populated with the vehicle network message
     * @param    timeout      - maximum time (in milliseconds) for read completion
     */
    public void readMsg(int channelId, byte[] response, long timeout) {
        int index = 0;
        long end = currentTimeMillis() + timeout;
        do {
            PASSTHRU_MSG msg = doReadMsg(channelId);
            LOGGER.trace("Read Msg: " + toString(msg));
            if (!isResponse(msg)) continue;
            arraycopy(msg.data, 0, response, index, msg.dataSize.intValue());
            index += msg.dataSize.intValue();
        } while (currentTimeMillis() <= end && index < response.length - 1);
    }

    /**
     * Retrieve a message through the existing communication channel from the vehicle.
     * @param    channelId - handle to the open communications channel
     * @param    maxWait      - maximum time (in milliseconds) for read completion
     * @return    bytes read from the vehicle network
     */
    public byte[] readMsg(int channelId, long maxWait) {
        List<byte[]> responses = new ArrayList<byte[]>();
        long end = currentTimeMillis() + maxWait;
        do {
            PASSTHRU_MSG msg = doReadMsg(channelId);
            LOGGER.trace("Read Msg: " + toString(msg));
            if (isResponse(msg)) responses.add(data(msg));
            ThreadUtil.sleep(2);
        } while (currentTimeMillis() <= end);
        return concat(responses);
    }

    /**
     * Retrieve the indicated number of messages through the existing communication
     * channel from the vehicle. If the number of messages can not be read before the
     * timeout expires, throw an exception.
     * @param    channelId - handle to the open communications channel
     * @param    numMsg      - number of valid messages to retrieve
     * @return    bytes read from the vehicle network
     * @throws    J2534Exception
     */
    public byte[] readMsg(int channelId, int numMsg, long timeout) {
        if (loopback) {
            numMsg++;
        }
        List<byte[]> responses = new ArrayList<byte[]>();
        long end = currentTimeMillis() + timeout;
        do {
            if (currentTimeMillis() >= end) {
                String errString = String.format(
                    "readMsg error: timeout expired waiting for %d more message(s)",
                    numMsg);
                throw new J2534Exception(errString);
            }
            PASSTHRU_MSG msg = doReadMsg(channelId);
            LOGGER.trace("Read Msg: " + toString(msg));
            if (isResponse(msg)) {
                responses.add(data(msg));
                numMsg--;
            }
            ThreadUtil.sleep(2);
        } while (numMsg != 0);
        return concat(responses);
    }

    /**
     * Stop the previously defined message filter by filter ID.
     * @param    channelId  - handle to the open communications channel
     * @param    msgId       - ID of the filter to stop
     */
    public void stopMsgFilter(int channelId, int msgId) {
        NativeLong ret = lib.PassThruStopMsgFilter(
                new NativeLong(channelId),
                new NativeLong(msgId)
            );
        if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                "PassThruStopMsgFilter", ret.intValue());
    }

    /**
     * Disconnect a previously opened communications channel.
     * @param    channelId  - handle to the open communications channel
     */
    public void disconnect(int channelId) {
        NativeLong ret = lib.PassThruDisconnect(new NativeLong(channelId));
        if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                "PassThruDisconnect", ret.intValue());
    }

    /**
     * Close the PassThru device by ID.
     * @param deviceId of PassThru device
     */
    public void close(int deviceId) {
        NativeLong ret = lib.PassThruClose(new NativeLong(deviceId));
        if (ret.intValue() != Status.NOERROR.getValue()) handleError(
                "PassThruClose", ret.intValue());
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

    private String toString(PASSTHRU_MSG msg) {
        byte[] bytes = new byte[msg.dataSize.intValue()];
        arraycopy(msg.data, 0, bytes, 0, bytes.length);
        String str = String.format(
                "[protocolID=%d | rxStatus=%d | txFlags=%d | timestamp=%x |" +
                " dataSize=%d | extraDataIndex=%d | data=%s]",
                msg.protocolID.intValue(),
                msg.rxStatus.intValue(),
                msg.txFlags.intValue(),
                msg.timestamp.intValue(),
                msg.dataSize.intValue(),
                msg.extraDataIndex.intValue(),
                HexUtil.asHex(bytes));
        return str;
    }

    private boolean isResponse(PASSTHRU_MSG msg) {
        if (msg.timestamp.intValue() != 0) {
            switch (msg.rxStatus.intValue()) {
                case 0x00:        // Normal message
                    return true;
    
                case 0x01:        // Loopback message
                    return loopback;

                case 0x02:        // Receive start indication
                    return false;
    
                case 0x04:        // Receive break indication
                    return false;
                    
                case 0x09:        // Transmit done indication 
                    return false;

                case 0x10:        // Receive pad error 
                    return false;
            }
        }
        return false;
    }

    private PASSTHRU_MSG doReadMsg(int channelId) {
        PASSTHRU_MSG msg = passThruMessage();
        NativeLongByReference pNumMsgs = new NativeLongByReference();
        pNumMsgs.setValue(new NativeLong(1));
        NativeLong status = lib.PassThruReadMsgs(
                new NativeLong(channelId),
                msg.getPointer(),
                pNumMsgs,
                new NativeLong(50)
            );
        if (status.intValue() != Status.NOERROR.getValue() &&
            status.intValue() != Status.ERR_TIMEOUT.getValue() &&
            status.intValue() != Status.ERR_BUFFER_EMPTY.getValue())
                handleError("PassThruReadMsgs", status.intValue());
        msg.read();
        return msg;
    }

    private ConfigItem[] configItems(SCONFIG_LIST sConfigs) {
        SCONFIG.ByReference[] configs =
            (ByReference[]) sConfigs.configPtr.toArray(
                    sConfigs.numOfParams.intValue());
        ConfigItem[] items = new ConfigItem[configs.length];
        for (int i = 0; i < configs.length; i++) {
            configs[i].read();
            items[i] = new ConfigItem(
                    configs[i].parameter.intValue(),
                    configs[i].value.intValue());
        }
        return items;
    }

    private SCONFIG[] sConfigs(ConfigItem... items) {
        SCONFIG[] sConfigs =
                (SCONFIG[]) new SCONFIG.ByReference().toArray(items.length);
        for (int i = 0; i < items.length; i++) {
            sConfigs[i].parameter = new NativeLong(items[i].parameter);
            sConfigs[i].value = new NativeLong(items[i].value);
            if (items[i].parameter == Config.LOOPBACK.value) {
                if (items[i].value == 1) {
                    loopback = true;
                }
            }
        }
//        for (SCONFIG sc : sConfigs) {
//            sc.write();
//            System.out.printf("%s%n", sc);
//        }
        return sConfigs;
    }

    private SCONFIG[] sConfigs(int... parameters) {
        SCONFIG[] sConfigs =
                (SCONFIG[]) new SCONFIG.ByReference().toArray(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            sConfigs[i].parameter = new NativeLong(parameters[i]);
            sConfigs[i].value = new NativeLong(-1);
        }
        return sConfigs;
    }

    private SCONFIG_LIST sConfigList(SCONFIG[] sConfigs) {
        SCONFIG_LIST list = new SCONFIG_LIST();
        list.numOfParams = new NativeLong(sConfigs.length);
        list.configPtr = (SCONFIG.ByReference) sConfigs[0];
        list.write();
//        System.out.printf("list:%n%s%n", list);
        return list;
    }

    private PASSTHRU_MSG passThruMessage(byte... data) {
        PASSTHRU_MSG msg = passThruMessage();
        msg.dataSize = new NativeLong(data.length);
        arraycopy(data, 0, msg.data, 0, data.length);
        msg.write();
        return msg;
    }

    private PASSTHRU_MSG passThruMessage() {
        PASSTHRU_MSG msg = new PASSTHRU_MSG();
        msg.txFlags = new NativeLong(0);
        msg.protocolID = protocolID;
        return msg;
    }

    private byte[] data(PASSTHRU_MSG msg) {
        int length = msg.dataSize.intValue();
        byte[] data = new byte[length];
        arraycopy(msg.data, 0, data, 0, length);
        return data;
    }

    /**
     * Retrieve the text description for the most recent non-zero error
     * and throw an exception.
     * @param operation - string containing the name of the method for which
     *                       the error occurred
     * @param status    - the method's numeric error value
     * @exception    J2534Exception on various non-zero return status
     * @see         J2534_v0404
     */
    private static void handleError(String operation, int status) {
        ByteBuffer error = ByteBuffer.allocate(255);
        lib.PassThruGetLastError(error);
        String errString = String.format("%s error [%d:%s], %s",
                operation, status, Status.get(status), Native.toString(error.array()));
        throw new J2534Exception(errString);
    }
}
