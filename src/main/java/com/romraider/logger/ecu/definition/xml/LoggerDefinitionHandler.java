/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2018 RomRaider.com
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

package com.romraider.logger.ecu.definition.xml;

import static com.romraider.Settings.COMMA;
import static com.romraider.logger.ecu.definition.xml.ConverterMaxMinDefaults.getMax;
import static com.romraider.logger.ecu.definition.xml.ConverterMaxMinDefaults.getMin;
import static com.romraider.logger.ecu.definition.xml.ConverterMaxMinDefaults.getStep;
import static com.romraider.util.HexUtil.asBytes;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static java.lang.Double.parseDouble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.romraider.Settings;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.connection.KwpSerialConnectionProperties;
import com.romraider.io.connection.SerialConnectionProperties;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.definition.EcuAddress;
import com.romraider.logger.ecu.definition.EcuAddressImpl;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.EcuDerivedParameterConvertor;
import com.romraider.logger.ecu.definition.EcuDerivedParameterConvertorImpl;
import com.romraider.logger.ecu.definition.EcuDerivedParameterImpl;
import com.romraider.logger.ecu.definition.EcuDtcConvertorImpl;
import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuParameterConvertorImpl;
import com.romraider.logger.ecu.definition.EcuParameterImpl;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.EcuSwitchConvertorImpl;
import com.romraider.logger.ecu.definition.EcuSwitchImpl;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.definition.Transport;
import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;

public final class LoggerDefinitionHandler extends DefaultHandler {
    private static final String TAG_LOGGER = "logger";
    private static final String TAG_PROTOCOL = "protocol";
    private static final String TAG_PARAMETER = "parameter";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_DEPENDS = "depends";
    private static final String TAG_CONVERSION = "conversion";
    private static final String TAG_REPLACE = "replace";
    private static final String TAG_REF = "ref";
    private static final String TAG_SWITCH = "switch";
    private static final String TAG_ECUPARAM = "ecuparam";
    private static final String TAG_ECU = "ecu";
    private static final String TAG_DTCODE = "dtcode";
    private static final String TAG_TRANSPORT = "transport";
    private static final String TAG_MODULE = "module";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_ID = "id";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_DESC = "desc";
    private static final String ATTR_ECUBYTEINDEX = "ecubyteindex";
    private static final String ATTR_LENGTH = "length";
    private static final String ATTR_ECUBIT = "ecubit";
    private static final String ATTR_UNITS = "units";
    private static final String ATTR_GROUP = "group";
    private static final String ATTR_SUBGROUP = "subgroup";
    private static final String ATTR_GROUPSIZE = "groupsize";
    private static final String ATTR_EXPRESSION = "expr";
    private static final String ATTR_FORMAT = "format";
    private static final String ATTR_BYTE = "byte";
    private static final String ATTR_BIT = "bit";
    private static final String ATTR_PARAMETER = "parameter";
    private static final String ATTR_STORAGETYPE = "storagetype";
    private static final String ATTR_ENDIAN = "endian";
    private static final String ATTR_BAUD = "baud";
    private static final String ATTR_CANID = "canid";
    private static final String ATTR_SAE = "sae";
    private static final String ATTR_KWP = "kwp";
    private static final String ATTR_DATABITS = "databits";
    private static final String ATTR_STOPBITS = "stopbits";
    private static final String ATTR_PARITY = "parity";
    private static final String ATTR_CONNECT_TIMEOUT = "connect_timeout";
    private static final String ATTR_SEND_TIMEOUT = "send_timeout";
    private static final String ATTR_VALUE = "value";
    private static final String ATTR_WITH = "with";
    private static final String ATTR_GAUGE_MIN = "gauge_min";
    private static final String ATTR_GAUGE_MAX = "gauge_max";
    private static final String ATTR_GAUGE_STEP = "gauge_step";
    private static final String ATTR_TARGET = "target";
    private static final String ATTR_TMPADDR = "tmpaddr";
    private static final String ATTR_MEMADDR = "memaddr";
    private static final String ATTR_ADDRESS = "address";
    private static final String ATTR_TESTER = "tester";
    private static final String ATTR_FASTPOLL = "fastpoll";
    private static final String ATTR_P1_MAX = "p1_max";
    private static final String ATTR_P3_MIN = "p3_min";
    private static final String ATTR_P4_MIN = "p4_min";
    private final String protocol;
    private final String fileLoggingControllerSwitchId;
    private final EcuInit ecuInit;
    private List<EcuParameter> params = new ArrayList<EcuParameter>();
    private List<EcuSwitch> switches = new ArrayList<EcuSwitch>();
    private List<EcuSwitch> dtcodes = new ArrayList<EcuSwitch>();
    private EcuSwitch fileLoggingControllerSwitch;
    private ConnectionProperties connectionProperties;
    private Map<String, EcuData> ecuDataMap;
    private Map<String, String> replaceMap;
    private String id;
    private String name;
    private String desc;
    private String ecuByteIndex;
    private String ecuBit;
    private String group;
    private String subgroup;
    private String groupsize;
    private String ecuIds;
    private List<String> addrStrings = new ArrayList<String>();
    private boolean EcuAddressCreated;
    private EcuAddress address;
    private Set<String> dependsList;
    private Map<String, EcuAddress> ecuAddressMap;
    private boolean derived;
    private int addressBit;
    private int addressLength;
    private Set<EcuDataConvertor> convertorList;
    private Set<EcuDerivedParameterConvertor> derivedConvertorList;
    private StringBuilder charBuffer;
    private boolean parseProtocol;
    private String conversionUnits;
    private String conversionExpression;
    private String conversionFormat;
    private String conversionStorageType;
    private Settings.Endian conversionEndian;
    private GaugeMinMax conversionGauge;
    private String target;
    private String version;
    private String protocolId;
    private Transport transport;
    private Collection<Module> moduleList;
    private Map<Transport, Collection<Module>> transportMap;
    private Map<String, Map<Transport, Collection<Module>>> protocolMap;

    public LoggerDefinitionHandler(String protocol, String fileLoggingControllerSwitchId, EcuInit ecuInit) {
        checkNotNullOrEmpty(protocol, "protocol");
        checkNotNullOrEmpty(fileLoggingControllerSwitchId, "fileLoggingControllerSwitchId");
        this.protocol = protocol;
        this.fileLoggingControllerSwitchId = fileLoggingControllerSwitchId;
        this.ecuInit = ecuInit;
    }

    public void startDocument() {
        params = new ArrayList<EcuParameter>();
        switches = new ArrayList<EcuSwitch>();
        ecuDataMap = new HashMap<String, EcuData>();
        dtcodes = new ArrayList<EcuSwitch>();
        protocolMap = new HashMap<String, Map<Transport, Collection<Module>>>();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_LOGGER.equals(qName)) {
            version = attributes.getValue(ATTR_VERSION);
        } else if (TAG_PROTOCOL.equals(qName)) {
            protocolId = attributes.getValue(ATTR_ID);
            parseProtocol = protocol.equalsIgnoreCase(protocolId);
            if (parseProtocol) {
                if ("NCS".equalsIgnoreCase(protocolId)) {
                    connectionProperties = new KwpSerialConnectionProperties(Integer.parseInt(attributes.getValue(ATTR_BAUD)),
                            Integer.parseInt(attributes.getValue(ATTR_DATABITS)), Integer.parseInt(attributes.getValue(ATTR_STOPBITS)),
                            Integer.parseInt(attributes.getValue(ATTR_PARITY)), Integer.parseInt(attributes.getValue(ATTR_CONNECT_TIMEOUT)),
                            Integer.parseInt(attributes.getValue(ATTR_SEND_TIMEOUT)), Integer.parseInt(attributes.getValue(ATTR_P1_MAX)),
                            Integer.parseInt(attributes.getValue(ATTR_P3_MIN)), Integer.parseInt(attributes.getValue(ATTR_P4_MIN)));
                }
                else {
                connectionProperties = new SerialConnectionProperties(Integer.parseInt(attributes.getValue(ATTR_BAUD)),
                        Integer.parseInt(attributes.getValue(ATTR_DATABITS)), Integer.parseInt(attributes.getValue(ATTR_STOPBITS)),
                        Integer.parseInt(attributes.getValue(ATTR_PARITY)), Integer.parseInt(attributes.getValue(ATTR_CONNECT_TIMEOUT)),
                        Integer.parseInt(attributes.getValue(ATTR_SEND_TIMEOUT)));
                }
            }
            transportMap = new HashMap<Transport, Collection<Module>>();
        } else if (TAG_TRANSPORT.equals(qName)) {
            id = attributes.getValue(ATTR_ID);
            name = attributes.getValue(ATTR_NAME);
            desc = attributes.getValue(ATTR_DESC);
            transport = new Transport(id, name, desc);
            moduleList = new ArrayList<Module>();
        } else if (TAG_MODULE.equals(qName)) {
            id = attributes.getValue(ATTR_ID);
            final String modAddr = attributes.getValue(ATTR_ADDRESS);
            desc = attributes.getValue(ATTR_DESC);
            final String testerAddr = attributes.getValue(ATTR_TESTER);
            final String fastpoll = attributes.getValue(ATTR_FASTPOLL);
            boolean fp = false;
            if (fastpoll != null && fastpoll.equalsIgnoreCase("true")) {
                fp = true;
            }
            final Module module = new Module(
                    id, asBytes(modAddr), desc, asBytes(testerAddr), fp);
            moduleList.add(module);
        } else if (parseProtocol) {
            if (TAG_PARAMETER.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
                ecuByteIndex = attributes.getValue(ATTR_ECUBYTEINDEX);
                ecuBit = attributes.getValue(ATTR_ECUBIT);
                group = attributes.getValue(ATTR_GROUP);
                subgroup = attributes.getValue(ATTR_SUBGROUP);
                groupsize = attributes.getValue(ATTR_GROUPSIZE);
                target = attributes.getValue(ATTR_TARGET);
                resetLists();
            } else if (TAG_ADDRESS.equals(qName)) {
                String length = attributes.getValue(ATTR_LENGTH);
                addressLength = length == null ? 1 : Integer.valueOf(length);
                String bit = attributes.getValue(ATTR_BIT);
                addressBit = bit == null ? -1 : Integer.valueOf(bit);
                derived = false;
            } else if (TAG_DEPENDS.equals(qName)) {
                dependsList = new LinkedHashSet<String>();
                derived = true;
            } else if (TAG_REF.equals(qName)) {
                dependsList.add(attributes.getValue(ATTR_PARAMETER));
            } else if (TAG_CONVERSION.equals(qName)) {
                conversionUnits = attributes.getValue(ATTR_UNITS);
                conversionExpression = attributes.getValue(ATTR_EXPRESSION);
                conversionFormat = attributes.getValue(ATTR_FORMAT);
                conversionStorageType = attributes.getValue(ATTR_STORAGETYPE);
                String endian = attributes.getValue(ATTR_ENDIAN);
                if (endian != null) {
                    conversionEndian = endian.equalsIgnoreCase("little") ? Settings.Endian.LITTLE : Settings.Endian.BIG;
                }
                else {
                    conversionEndian = Settings.Endian.BIG;
                }
                double gaugeMin = getConversionMin(attributes, conversionUnits);
                double gaugeMax = getConversionMax(attributes, conversionUnits);
                double gaugeStep = getConversionStep(attributes, conversionUnits);
                conversionGauge = new GaugeMinMax(gaugeMin, gaugeMax, gaugeStep);
                replaceMap = new HashMap<String, String>();
                if (!derived && !EcuAddressCreated && addrStrings.size() > 0) {
                    address = new EcuAddressImpl(addrStrings.toArray(new String[0]), addressLength, addressBit);
                    EcuAddressCreated = true;
                }
            } else if (TAG_REPLACE.equals(qName)) {
                replaceMap.put(attributes.getValue(ATTR_VALUE), attributes.getValue(ATTR_WITH));
            } else if (TAG_SWITCH.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
                ecuByteIndex = attributes.getValue(ATTR_ECUBYTEINDEX);
                ecuBit = attributes.getValue(ATTR_BIT);
                group = attributes.getValue(ATTR_GROUP);
                subgroup = attributes.getValue(ATTR_SUBGROUP);
                groupsize = attributes.getValue(ATTR_GROUPSIZE);
                conversionStorageType = attributes.getValue(ATTR_STORAGETYPE);
                conversionUnits = attributes.getValue(ATTR_UNITS);
                target = attributes.getValue(ATTR_TARGET);
                address = new EcuAddressImpl(attributes.getValue(ATTR_BYTE), 1, Integer.valueOf(attributes.getValue(ATTR_BIT)));
                resetLists();
            } else if (TAG_ECUPARAM.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
                group = attributes.getValue(ATTR_GROUP);
                subgroup = attributes.getValue(ATTR_SUBGROUP);
                groupsize = attributes.getValue(ATTR_GROUPSIZE);
                target = attributes.getValue(ATTR_TARGET);
                resetLists();
                ecuAddressMap = new HashMap<String, EcuAddress>();
                derived = false;
            } else if (TAG_ECU.equals(qName)) {
                ecuIds = attributes.getValue(ATTR_ID);
            } else if (TAG_DTCODE.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
                address = new EcuAddressImpl(
                        new String[] {attributes.getValue(ATTR_TMPADDR),
                                      attributes.getValue(ATTR_MEMADDR)
                                     },
                        1,
                        Integer.valueOf(attributes.getValue(ATTR_BIT)));
                resetLists();
            }
        }
        charBuffer = new StringBuilder();
    }

    public void characters(char[] ch, int start, int length) {
        if (parseProtocol) {
            charBuffer.append(ch, start, length);
        }
    }

    public void endElement(String uri, String localName, String qName) {
        if (TAG_PROTOCOL.equals(qName)) {
            parseProtocol = false;
            protocolMap.put(protocolId, transportMap);
        } else if (TAG_TRANSPORT.equals(qName)) {
            transportMap.put(transport, moduleList);
        } else if (parseProtocol) {
            if (TAG_ADDRESS.equals(qName)) {
                addrStrings.add(charBuffer.toString());
            } else if (TAG_PARAMETER.equals(qName)) {
                if (derived) {
                    Set<EcuData> dependencies = new HashSet<EcuData>();
                    for (String refid : dependsList) {
                        if (ecuDataMap.containsKey(refid)) {
                            dependencies.add(ecuDataMap.get(refid));
                        }
                    }
                    if (dependsList.size() == dependencies.size()) {
                        EcuParameter param = new EcuDerivedParameterImpl(id, name, desc,
                                dependencies.toArray(new EcuData[dependencies.size()]),
                                derivedConvertorList.toArray(new EcuDerivedParameterConvertor[derivedConvertorList.size()]));
                        params.add(param);
                        ecuDataMap.put(param.getId(), param);
                    }
                } else {
                    if (ecuByteIndex == null || ecuBit == null || ecuInit == null || isSupportedParameter(ecuInit,
                            ecuByteIndex, ecuBit)) {
                        if (convertorList.isEmpty()) {
                            convertorList.add(new EcuParameterConvertorImpl());
                        }
                        EcuParameter param = new EcuParameterImpl(
                                id, name, desc, address, group, subgroup, groupsize,
                                convertorList.toArray(new EcuDataConvertor[convertorList.size()]));
                        params.add(param);
                        ecuDataMap.put(param.getId(), param);
                    }
                }
            } else if (TAG_CONVERSION.equals(qName)) {
                if (derived) {
                    derivedConvertorList.add(new EcuDerivedParameterConvertorImpl(conversionUnits,
                            conversionExpression, conversionFormat, replaceMap, conversionGauge));
                } else {
                    convertorList.add(new EcuParameterConvertorImpl(
                            conversionUnits, conversionExpression, conversionFormat,
                            address.getBit(), conversionStorageType, conversionEndian,
                            replaceMap, conversionGauge));
                }
            } else if (TAG_ECUPARAM.equals(qName)) {
                if (ecuInit != null && ecuAddressMap.containsKey(ecuInit.getEcuId())) {
                    if (convertorList.isEmpty()) {
                        convertorList.add(new EcuParameterConvertorImpl());
                    }
                    EcuParameter param = new EcuParameterImpl(
                            id, name, desc, ecuAddressMap.get(ecuInit.getEcuId()),
                            group, subgroup, groupsize,
                            convertorList.toArray(new EcuDataConvertor[convertorList.size()]));
                    params.add(param);
                    ecuDataMap.put(param.getId(), param);
                }
            } else if (TAG_ECU.equals(qName)) {
                address = new EcuAddressImpl(addrStrings.toArray(new String[0]), addressLength, addressBit);
                EcuAddressCreated = true;
                for (String ecuId : ecuIds.split(COMMA)) {
                    ecuAddressMap.put(ecuId, address);
                }
                addrStrings.clear();
            } else if (TAG_SWITCH.equals(qName)) {
                if (ecuByteIndex == null || ecuBit == null || ecuInit == null || isSupportedParameter(ecuInit,
                        ecuByteIndex, ecuBit)) {
                    EcuDataConvertor[] convertors =
                            new EcuDataConvertor[]{new EcuSwitchConvertorImpl(
                                    address.getBit(),
                                    conversionStorageType,
                                    conversionUnits)};
                    EcuSwitch ecuSwitch = new EcuSwitchImpl(
                            id, name, desc, address,
                            group, subgroup, groupsize, convertors);
                    switches.add(ecuSwitch);
                    ecuDataMap.put(ecuSwitch.getId(), ecuSwitch);
                    if (id.equalsIgnoreCase(fileLoggingControllerSwitchId)) {
                        fileLoggingControllerSwitch = new EcuSwitchImpl(
                                id, name, desc, address,
                                group, subgroup, groupsize, convertors);
                    }
                }
            } else if (TAG_DTCODE.equals(qName)) {
                final EcuDataConvertor[] convertors = new EcuDataConvertor[]{new EcuDtcConvertorImpl(address.getBit())};
                final EcuSwitch ecuSwitch = new EcuSwitchImpl(
                        id, name, desc, address,
                        group, subgroup, groupsize, convertors);
                dtcodes.add(ecuSwitch);
                ecuDataMap.put(ecuSwitch.getId(), ecuSwitch);
            }
        }
    }

    public List<EcuParameter> getEcuParameters() {
        return params;
    }

    public List<EcuSwitch> getEcuSwitches() {
        return switches;
    }

    public EcuSwitch getFileLoggingControllerSwitch() {
        return fileLoggingControllerSwitch;
    }

    public ConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    public String getVersion() {
        return version;
    }

    public List<EcuSwitch> getEcuCodes() {
        return dtcodes;
    }

    public Map<String, Map<Transport, Collection<Module>>> getProtocols() {
    	/*
        if (protocolMap.get(protocol).isEmpty()) {
            final Module module = new Module(
                    "ecu", new byte[]{0x10}, "Engine Control Unit",
                    new byte[]{(byte)0xF0}, true);
            moduleList = new ArrayList<Module>();
            moduleList.add(module);
            transport = new Transport("iso9141", "K-Line", "Low speed serial");
            transportMap = new HashMap<Transport, Collection<Module>>();
            transportMap.put(transport, moduleList);
            protocolMap.put(protocol, transportMap);
        }*/
        return protocolMap;
    }

    private void resetLists() {
        addrStrings.clear();
        EcuAddressCreated = false;
        convertorList = new LinkedHashSet<EcuDataConvertor>();
        derivedConvertorList = new LinkedHashSet<EcuDerivedParameterConvertor>();
    }

    private boolean isSupportedParameter(EcuInit ecuInit, String ecuByteIndex, String ecuBit) {
        byte[] ecuInitBytes = ecuInit.getEcuInitBytes();
        int index = Integer.parseInt(ecuByteIndex);
        if (index < ecuInitBytes.length) {
            byte[] bytes = new byte[1];
            System.arraycopy(ecuInitBytes, index, bytes, 0, 1);
            return (bytes[0] & 1 << Integer.parseInt(ecuBit)) > 0;
        } else {
            return false;
        }
    }

    private double getConversionMin(Attributes attributes, String units) {
        String value = attributes.getValue(ATTR_GAUGE_MIN);
        if (!isNullOrEmpty(value)) return parseDouble(value);
        return getMin(units);
    }

    private double getConversionMax(Attributes attributes, String units) {
        String value = attributes.getValue(ATTR_GAUGE_MAX);
        if (!isNullOrEmpty(value)) return parseDouble(value);
        return getMax(units);
    }

    private double getConversionStep(Attributes attributes, String units) {
        String value = attributes.getValue(ATTR_GAUGE_STEP);
        if (!isNullOrEmpty(value)) return parseDouble(value);
        return getStep(units);
    }
}
