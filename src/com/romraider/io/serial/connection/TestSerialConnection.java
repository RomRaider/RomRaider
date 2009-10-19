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

package com.romraider.io.serial.connection;

import com.romraider.io.connection.ConnectionProperties;
import static com.romraider.io.protocol.ssm.SSMChecksumCalculator.calculateChecksum;
import static com.romraider.io.protocol.ssm.SSMProtocol.ADDRESS_SIZE;
import static com.romraider.io.protocol.ssm.SSMProtocol.DATA_SIZE;
import static com.romraider.io.protocol.ssm.SSMProtocol.DIAGNOSTIC_TOOL_ID;
import static com.romraider.io.protocol.ssm.SSMProtocol.ECU_ID;
import static com.romraider.io.protocol.ssm.SSMProtocol.ECU_INIT_COMMAND;
import static com.romraider.io.protocol.ssm.SSMProtocol.HEADER;
import static com.romraider.io.protocol.ssm.SSMProtocol.READ_ADDRESS_COMMAND;
import static com.romraider.io.protocol.ssm.SSMProtocol.READ_ADDRESS_RESPONSE;
import static com.romraider.io.protocol.ssm.SSMProtocol.READ_MEMORY_COMMAND;
import static com.romraider.io.protocol.ssm.SSMProtocol.READ_MEMORY_RESPONSE;
import static com.romraider.io.protocol.ssm.SSMProtocol.REQUEST_NON_DATA_BYTES;
import static com.romraider.io.protocol.ssm.SSMProtocol.RESPONSE_NON_DATA_BYTES;
import static com.romraider.io.protocol.ssm.SSMProtocol.WRITE_MEMORY_COMMAND;
import static com.romraider.io.protocol.ssm.SSMProtocol.WRITE_MEMORY_RESPONSE;
import com.romraider.logger.ecu.exception.SerialCommunicationException;
import static com.romraider.util.ByteUtil.asInt;
import static com.romraider.util.HexUtil.asBytes;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ThreadUtil.sleep;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import static java.lang.System.currentTimeMillis;
import java.util.Random;

final class TestSerialConnection implements SerialConnection {
    private static final Logger LOGGER = getLogger(TestSerialConnection.class);
    private static final Random RANDOM = new Random(currentTimeMillis());
    private static final String ECU_INIT_RESPONSE_01_UP = "8010F001BF4080F01039FFA21011315258400673FACB842B83FEA800000060CED4FDB060000F200000000000DC0000551E30C0F222000040FB00E1000000000000000059";
    private static final String ECU_INIT_RESPONSE_PRE_01 = "8010F001BF4080F01029FFA1100B195458050561C4EB800808000000000070CE64F8BA080000E00000000000DC0000108000007B";
    private static final String ECU_INIT_RESPONSE = ECU_INIT_RESPONSE_01_UP;
    //    private static final String ECU_INIT_RESPONSE = ECU_INIT_RESPONSE_PRE_01;
    private byte[] request = new byte[0];

    public TestSerialConnection(String portName, ConnectionProperties connectionProperties) {
        checkNotNullOrEmpty(portName, "portName");
        checkNotNull(connectionProperties, "connectionProperties");
        LOGGER.info("*** TEST *** Opening connection: " + portName);
    }

    public void write(byte[] bytes) {
        //LOGGER.("*** TEST *** Write bytes = " + asHex(bytes));
        request = bytes;
    }

    public int available() {
        if (isEcuInitRequest()) {
            return asBytes(ECU_INIT_RESPONSE).length;
        } else if (isReadAddressRequest()) {
            return request.length + (RESPONSE_NON_DATA_BYTES + calculateNumResponseDataBytes());
        } else if (isReadMemoryRequest()) {
            return request.length + (RESPONSE_NON_DATA_BYTES + asInt(request[9]) + 1);
        } else if (isWriteMemoryRequest()) {
            return request.length + (RESPONSE_NON_DATA_BYTES + (request.length - 6 - ADDRESS_SIZE));
        } else {
            throw new SerialCommunicationException("*** TEST *** Unsupported request: " + asHex(request));
        }
    }

    public void read(byte[] bytes) {
        if (isEcuInitRequest()) {
            System.arraycopy(asBytes(ECU_INIT_RESPONSE), 0, bytes, 0, bytes.length);
        } else if (isIamRequest()) {
            byte[] response = asBytes("0x80F01006E83F600000000D");
            System.arraycopy(response, 0, bytes, request.length, response.length);
        } else if (isEngineLoadRequest()) {
            byte[] response = asBytes("0x80F01006E83EC74A760033");
            System.arraycopy(response, 0, bytes, request.length, response.length);
        } else if (isReadAddressRequest()) {
            byte[] responseData = generateResponseData(calculateNumResponseDataBytes());
            int i = 0;
            byte[] response = new byte[RESPONSE_NON_DATA_BYTES + calculateNumResponseDataBytes()];
            response[i++] = HEADER;
            response[i++] = DIAGNOSTIC_TOOL_ID;
            response[i++] = ECU_ID;
            response[i++] = (byte) (1 + responseData.length);
            response[i++] = READ_ADDRESS_RESPONSE;
            System.arraycopy(responseData, 0, response, i, responseData.length);
            response[i += responseData.length] = calculateChecksum(response);
            System.arraycopy(request, 0, bytes, 0, request.length);
            System.arraycopy(response, 0, bytes, request.length, response.length);
        } else if (isReadMemoryRequest()) {
            byte[] responseData = generateResponseData(asInt(request[9]) + 1);
            int i = 0;
            byte[] response = new byte[RESPONSE_NON_DATA_BYTES + responseData.length];
            response[i++] = HEADER;
            response[i++] = DIAGNOSTIC_TOOL_ID;
            response[i++] = ECU_ID;
            response[i++] = (byte) (1 + responseData.length);
            response[i++] = READ_MEMORY_RESPONSE;
            System.arraycopy(responseData, 0, response, i, responseData.length);
            response[i += responseData.length] = calculateChecksum(response);
            System.arraycopy(request, 0, bytes, 0, request.length);
            System.arraycopy(response, 0, bytes, request.length, response.length);
        } else if (isWriteMemoryRequest()) {
            int numDataBytes = request.length - 6 - ADDRESS_SIZE;
            byte[] response = new byte[RESPONSE_NON_DATA_BYTES + numDataBytes];
            int i = 0;
            response[i++] = HEADER;
            response[i++] = DIAGNOSTIC_TOOL_ID;
            response[i++] = ECU_ID;
            response[i++] = (byte) (numDataBytes + 1);
            response[i++] = WRITE_MEMORY_RESPONSE;
            System.arraycopy(request, 8, response, i, numDataBytes);
            response[i += numDataBytes] = calculateChecksum(response);
            System.arraycopy(request, 0, bytes, 0, request.length);
            System.arraycopy(response, 0, bytes, request.length, response.length);
        } else {
            throw new SerialCommunicationException("*** TEST *** Unsupported request: " + asHex(request));
        }
        //LOGGER.("*** TEST *** Read bytes  = " + asHex(bytes));
        sleep(200);
    }

    public byte[] readAvailable() {
        byte[] response = new byte[available()];
        read(response);
        return response;
    }

    public void readStaleData() {
    }

    public void close() {
        LOGGER.info("*** TEST *** Connection closed.");
    }

    public String readLine() {
        throw new UnsupportedOperationException();
    }

    public int read() {
        throw new UnsupportedOperationException();
    }

    private int calculateNumResponseDataBytes() {
        return ((request.length - REQUEST_NON_DATA_BYTES) / ADDRESS_SIZE) * DATA_SIZE;
    }

    private boolean isIamRequest() {
        String hex = asHex(request);
        return hex.startsWith("8010F011A8") && hex.contains("FF8228FF8229FF822AFF822B");
    }

    private boolean isEngineLoadRequest() {
        String hex = asHex(request);
        return hex.startsWith("8010F011A8") && hex.contains("FFA6FCFFA6FDFFA6FEFFA6FF");
    }

    private byte[] generateResponseData(int dataLength) {
        byte[] responseData = new byte[dataLength];
        for (int i = 0; i < responseData.length; i++) {
            responseData[i] = (byte) RANDOM.nextInt(255);
        }
        return responseData;
    }

    private boolean isEcuInitRequest() {
        byte command = ECU_INIT_COMMAND;
        return isCommand(command);
    }

    private boolean isReadAddressRequest() {
        return isCommand(READ_ADDRESS_COMMAND);
    }

    private boolean isReadMemoryRequest() {
        return isCommand(READ_MEMORY_COMMAND);
    }

    private boolean isWriteMemoryRequest() {
        return isCommand(WRITE_MEMORY_COMMAND);
    }

    private boolean isCommand(byte command) {
        return request[4] == command;
    }

}
