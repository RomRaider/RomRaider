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

package com.romraider.logger.ecu.definition.xml;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.connection.ConnectionPropertiesImpl;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.definition.EcuAddress;
import com.romraider.logger.ecu.definition.EcuAddressImpl;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.EcuDerivedParameterConvertor;
import com.romraider.logger.ecu.definition.EcuDerivedParameterConvertorImpl;
import com.romraider.logger.ecu.definition.EcuDerivedParameterImpl;
import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuParameterConvertorImpl;
import com.romraider.logger.ecu.definition.EcuParameterImpl;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.EcuSwitchConvertorImpl;
import com.romraider.logger.ecu.definition.EcuSwitchImpl;
import static com.romraider.logger.ecu.definition.xml.ConverterMaxMinDefaults.getMax;
import static com.romraider.logger.ecu.definition.xml.ConverterMaxMinDefaults.getMin;
import static com.romraider.logger.ecu.definition.xml.ConverterMaxMinDefaults.getStep;
import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static java.lang.Double.parseDouble;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LoggerDefinitionHandler extends DefaultHandler {
    private static final String FLOAT = "float";
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
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_ID = "id";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_DESC = "desc";
    private static final String ATTR_ECUBYTEINDEX = "ecubyteindex";
    private static final String ATTR_LENGTH = "length";
    private static final String ATTR_ECUBIT = "ecubit";
    private static final String ATTR_UNITS = "units";
    private static final String ATTR_EXPRESSION = "expr";
    private static final String ATTR_FORMAT = "format";
    private static final String ATTR_BYTE = "byte";
    private static final String ATTR_BIT = "bit";
    private static final String ATTR_PARAMETER = "parameter";
    private static final String ATTR_STORAGETYPE = "storagetype";
    private static final String ATTR_BAUD = "baud";
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
    private final String protocol;
    private final String fileLoggingControllerSwitchId;
    private final EcuInit ecuInit;
    private List<EcuParameter> params = new ArrayList<EcuParameter>();
    private List<EcuSwitch> switches = new ArrayList<EcuSwitch>();
    private EcuSwitch fileLoggingControllerSwitch;
    private ConnectionProperties connectionProperties;
    private Map<String, EcuData> ecuDataMap;
    private Map<String, String> replaceMap;
    private String id;
    private String name;
    private String desc;
    private String ecuByteIndex;
    private String ecuBit;
    private String ecuIds;
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
    private GaugeMinMax conversionGauge;
    private String target;
    private String version;

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
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_LOGGER.equals(qName)) {
            version = attributes.getValue(ATTR_VERSION);
        } else if (TAG_PROTOCOL.equals(qName)) {
            parseProtocol = protocol.equalsIgnoreCase(attributes.getValue(ATTR_ID));
            if (parseProtocol) {
                connectionProperties = new ConnectionPropertiesImpl(Integer.parseInt(attributes.getValue(ATTR_BAUD)),
                        Integer.parseInt(attributes.getValue(ATTR_DATABITS)), Integer.parseInt(attributes.getValue(ATTR_STOPBITS)),
                        Integer.parseInt(attributes.getValue(ATTR_PARITY)), Integer.parseInt(attributes.getValue(ATTR_CONNECT_TIMEOUT)),
                        Integer.parseInt(attributes.getValue(ATTR_SEND_TIMEOUT)));
            }
        } else if (parseProtocol) {
            if (TAG_PARAMETER.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
                ecuByteIndex = attributes.getValue(ATTR_ECUBYTEINDEX);
                ecuBit = attributes.getValue(ATTR_ECUBIT);
                target = attributes.getValue(ATTR_TARGET);
                resetConvertorLists();
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
                double gaugeMin = getConversionMin(attributes, conversionUnits);
                double gaugeMax = getConversionMax(attributes, conversionUnits);
                double gaugeStep = getConversionStep(attributes, conversionUnits);
                conversionGauge = new GaugeMinMax(gaugeMin, gaugeMax, gaugeStep);
                replaceMap = new HashMap<String, String>();
            } else if (TAG_REPLACE.equals(qName)) {
                replaceMap.put(attributes.getValue(ATTR_VALUE), attributes.getValue(ATTR_WITH));
            } else if (TAG_SWITCH.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
                ecuByteIndex = attributes.getValue(ATTR_ECUBYTEINDEX);
                ecuBit = attributes.getValue(ATTR_BIT);
                target = attributes.getValue(ATTR_TARGET);
                address = new EcuAddressImpl(attributes.getValue(ATTR_BYTE), 1, Integer.valueOf(attributes.getValue(ATTR_BIT)));
                resetConvertorLists();
            } else if (TAG_ECUPARAM.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
                target = attributes.getValue(ATTR_TARGET);
                resetConvertorLists();
                ecuAddressMap = new HashMap<String, EcuAddress>();
                derived = false;
            } else if (TAG_ECU.equals(qName)) {
                ecuIds = attributes.getValue(ATTR_ID);
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
        } else if (parseProtocol) {
            if (TAG_ADDRESS.equals(qName)) {
                address = new EcuAddressImpl(charBuffer.toString(), addressLength, addressBit);
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
                        EcuParameter param = new EcuParameterImpl(id, name, desc, address,
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
                    convertorList.add(new EcuParameterConvertorImpl(conversionUnits, conversionExpression, conversionFormat, address.getBit(),
                            FLOAT.equalsIgnoreCase(conversionStorageType), replaceMap, conversionGauge));
                }
            } else if (TAG_ECUPARAM.equals(qName)) {
                if (ecuInit != null && ecuAddressMap.containsKey(ecuInit.getEcuId())) {
                    if (convertorList.isEmpty()) {
                        convertorList.add(new EcuParameterConvertorImpl());
                    }
                    EcuParameter param = new EcuParameterImpl(id, name, desc, ecuAddressMap.get(ecuInit.getEcuId()),
                            convertorList.toArray(new EcuDataConvertor[convertorList.size()]));
                    params.add(param);
                    ecuDataMap.put(param.getId(), param);
                }
            } else if (TAG_ECU.equals(qName)) {
                for (String ecuId : ecuIds.split(",")) {
                    ecuAddressMap.put(ecuId, address);
                }
            } else if (TAG_SWITCH.equals(qName)) {
                if (ecuByteIndex == null || ecuBit == null || ecuInit == null || isSupportedParameter(ecuInit,
                        ecuByteIndex, ecuBit)) {
                    EcuDataConvertor[] convertors = new EcuDataConvertor[]{new EcuSwitchConvertorImpl(address.getBit())};
                    EcuSwitch ecuSwitch = new EcuSwitchImpl(id, name, desc, address, convertors);
                    switches.add(ecuSwitch);
                    ecuDataMap.put(ecuSwitch.getId(), ecuSwitch);
                    if (id.equalsIgnoreCase(fileLoggingControllerSwitchId)) {
                        fileLoggingControllerSwitch = new EcuSwitchImpl(id, name, desc, address, convertors);
                    }
                }
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

    private void resetConvertorLists() {
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
