/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

package com.romraider.logger.ecu.comms.learning;

import static com.romraider.logger.ecu.comms.io.connection.LoggerConnectionFactory.getConnection;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.romraider.Settings;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.comms.io.connection.DS2LoggerConnection;
import com.romraider.logger.ecu.comms.learning.parameter.DS2Parameter;
import com.romraider.logger.ecu.comms.learning.parameter.DS2ParameterCrossReference;
import com.romraider.logger.ecu.comms.learning.parameter.ParameterIdComparator;
import com.romraider.logger.ecu.comms.learning.tableaxis.DS2TableAxisQueryParameterSet;
import com.romraider.logger.ecu.comms.learning.tables.DS2FlkcTableQueryBuilder;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryImpl;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.EcuDefinition;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.definition.Transport;
import com.romraider.logger.ecu.definition.xml.EcuDefinitionDocumentLoader;
import com.romraider.logger.ecu.definition.xml.EcuDefinitionInheritanceList;
import com.romraider.logger.ecu.definition.xml.EcuTableDefinitionHandler;
import com.romraider.logger.ecu.ui.MessageListener;
import com.romraider.logger.ecu.ui.paramlist.ParameterListTableModel;
import com.romraider.logger.ecu.ui.paramlist.ParameterRow;
import com.romraider.logger.ecu.ui.swing.tools.DS2LearningTableValuesResultsPanel;
import com.romraider.util.ParamChecker;
import com.romraider.util.ResourceUtil;

/**
 * This class manages the building of ECU queries and retrieving the data to
 * populate the table models which will be used by the Learning Table Values
 * display panel.
 */
public final class DS2LearningTableValues extends SwingWorker<Void, Void>
                    implements LearningTableValues {

    private static final Logger LOGGER =
            Logger.getLogger(DS2LearningTableValues.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            DS2LearningTableValues.class.getName());
    private static final String[] AF_RANGE_NAMES = new String[]{" ",
            "Additive Adaptation",
            "Multiplicative Adaptation"};
    private static final List<String> KNK_LOAD_TABLE_NAMES = Arrays.asList(
            "Knock Tables X Axis (Load)");
    private static final List<String> KNK_RPM_TABLE_NAMES = Arrays.asList(
            "Knock Tables Y Axis (Engine Speed)");
    private final Map<String, Object> vehicleInfo =
            new LinkedHashMap<String, Object>();
    private final List<List<Object>> afLearning = new ArrayList<List<Object>>();
    private EcuLogger logger;
    private Settings settings;
    private MessageListener messageListener;
    private ParameterListTableModel parmeterList;
    private EcuDefinition ecuDef;
    private ParameterRow flkc;
    private int flkcAddr;

    public DS2LearningTableValues() {}

    public void init(
            EcuLogger logger,
            ParameterListTableModel dataTabParamListTableModel,
            EcuDefinition ecuDef) {

        ParamChecker.checkNotNull(logger, "EcuLogger");
        ParamChecker.checkNotNull(dataTabParamListTableModel,
                "ParameterListTableModel");
        this.logger = logger;
        this.settings = logger.getSettings();
        this.messageListener = logger;
        this.parmeterList = dataTabParamListTableModel;
        this.ecuDef = ecuDef;
        this.flkc = null;
        this.flkcAddr = 0;
    }

    @Override
    public final Void doInBackground() {
        Document document = null;
        if (ecuDef.getEcuDefFile() == null) {
            showMessageDialog(logger,
                    rb.getString("DEFNOTFOUND"),
                    rb.getString("DEFMISSING"),
                    WARNING_MESSAGE);
            return null;
        }
        else {
            document = EcuDefinitionDocumentLoader.getDocument(ecuDef);
        }

        final String transport = settings.getTransportProtocol();
        final Module module = settings.getDestinationTarget();
        if (settings.isCanBus()) {
            settings.setTransportProtocol("ISO9141");
            final Module ecuModule = getModule("ECU");
            settings.setDestinationTarget(ecuModule);
        }
        final boolean logging = logger.isLogging();
        if (logging) logger.stopLogging();

        String message = rb.getString("GETAFVALUES");
        messageListener.reportMessage(message);
        buildVehicleInfoMap(ecuDef);

        try {
            final DS2LoggerConnection connection = (DS2LoggerConnection) getConnection(
                    settings.getLoggerProtocol(), settings.getLoggerPort(),
                    settings.getLoggerConnectionProperties());
            try {
                Collection<EcuQuery> queries = buildLearningQueries();

                try {
                    LOGGER.info("Retrieving vehicle info & A/F values ...");
                    connection.sendAddressReads(
                            queries,
                            settings.getDestinationTarget(),
                            new PollingStateImpl());
                    LOGGER.info("Current vehicle info & A/F values retrieved.");
                }
                catch (Exception e) {
                    LOGGER.error(message + " Error retrieving values", e);
                }

                Collections.sort(
                        (List<EcuQuery>)queries, new ParameterIdComparator());

                processEcuQueryResponses((List<EcuQuery>) queries);

                message = rb.getString("GETKNOCKRANGES");
                messageListener.reportMessage(message);
                String[] flkcLoad = new String[0];
                queries.clear();
                queries = getTableAxisRanges(document, ecuDef, KNK_LOAD_TABLE_NAMES);
                if (queries != null && !queries.isEmpty()) {
                    LOGGER.info("Retrieving Knock Load ranges ...");
                    connection.sendAddressReads(
                            queries,
                            settings.getDestinationTarget(),
                            new PollingStateImpl());
                    LOGGER.info("Knock Load ranges retrieved.");
                    flkcLoad = formatRanges(queries, "%.0f");
                }

                message = rb.getString("GETRPMRANGES");
                messageListener.reportMessage(message);
                String[] flkcRpm = new String[0];
                queries.clear();
                queries = getTableAxisRanges(document, ecuDef, KNK_RPM_TABLE_NAMES);
                if (queries != null && !queries.isEmpty()) {
                    LOGGER.info("Retrieving Knock RPM ranges ...");
                    connection.sendAddressReads(
                            queries,
                            settings.getDestinationTarget(),
                            new PollingStateImpl());
                    LOGGER.info("Knock RPM ranges retrieved.");
                    flkcRpm = formatRpmRanges(queries);
                }
                queries.clear();

                List<List<List<EcuQuery>>> flkcQueryTables = new ArrayList<List<List<EcuQuery>>>();
                List<List<EcuQuery>> flkcQueryGroups = new ArrayList<List<EcuQuery>>();
                if (flkc != null) {
                    for (int k = 0; k < 384; k += 64) {
                        flkcQueryGroups = new DS2FlkcTableQueryBuilder().build(
                                                flkc,
                                                flkcAddr + k,
                                                flkcRpm.length,
                                                flkcLoad.length - 1);
                        flkcQueryTables.add(flkcQueryGroups);

                        for (int i = 0; i < flkcQueryGroups.size(); i++) {
                            for (int j = 0; j < flkcQueryGroups.get(i).size(); j++) {
                                if (flkcQueryGroups.get(i).get(j) != null) {
                                    queries.add(flkcQueryGroups.get(i).get(j));
                                }
                            }
                        }
                        message = MessageFormat.format(
                                rb.getString("GETTABLEKNOCK"),
                                (k/64+1));
                        messageListener.reportMessage(message);
                        LOGGER.info(String.format("Retrieving Table %d Knock values ...",
                                (k/64+1)));
                        connection.sendAddressReads(
                                queries,
                                settings.getDestinationTarget(),
                                new PollingStateImpl());
                        LOGGER.info(String.format("Table %d Knock values retrieved.",
                                (k/64+1)));               
                        queries.clear();
                    }
                }
                else {
                    message = rb.getString("ERRTABLEKNOCK");
                    messageListener.reportMessage(message);
                    LOGGER.error("Error retrieving Knock data values, missing Knock reference");
                }

                messageListener.reportMessage(
                        rb.getString("ADAPTSUCCESS"));
                final DS2LearningTableValuesResultsPanel results =
                        new DS2LearningTableValuesResultsPanel(
                                logger, vehicleInfo,
                                AF_RANGE_NAMES, afLearning,
                                flkcLoad, flkcRpm, flkcQueryTables);
                results.displayLearningResultsPanel();
            }
            finally {
                connection.close();
                settings.setTransportProtocol(transport);
                settings.setDestinationTarget(module);
                if (logging) logger.startLogging();
            }
        }
        catch (Exception e) {
            messageListener.reportError(
                    rb.getString("ERRADAPT"));
            LOGGER.error(message + " Error retrieving values", e);
            showMessageDialog(logger,
                    MessageFormat.format(
                            rb.getString("ERRCONNECT"), message),
                    rb.getString("ATV"),
                    ERROR_MESSAGE);
        }
        return null;
    }

    /**
     * Build a collection of queries based on the initialized values of
     * parameters defined for this ECU.  Also identify the Knock
     * parameters used to locate and calculate the Knock table.
     * @return the supported parameter list filtered for only the Learning Table
     * Value parameters needed.
     */
    private final Collection<EcuQuery> buildLearningQueries() {
        final Collection<EcuQuery> query = new ArrayList<EcuQuery>();
        final List<ParameterRow> parameterRows = parmeterList.getParameterRows();
        if (!ParamChecker.isNullOrEmpty(parameterRows)) {
            for (ParameterRow parameterRow : parameterRows) {
                final DS2Parameter parameterId =
                        DS2Parameter.fromValue(parameterRow.getLoggerData().getId());
                if (parameterId != null) {
                    query.add(buildEcuQuery(parameterRow));
                    setFlkcTableAddress(parameterRow, parameterId);                    }
            }
        }
        return query;
    }

    /**
     * Build a query object for a parameter item.
     * @return a new EcuQuery.
     */
    private final EcuQuery buildEcuQuery(ParameterRow parameterRow) {
        final EcuQuery ecuQuery =
                new EcuQueryImpl((EcuData) parameterRow.getLoggerData());
        return ecuQuery;
    }

    /**
     * Define the start address of the Knock table in RAM base on a Extended
     * parameter if defined.
     * Also isolate the Knock extended parameter to use the data converter
     * when building the Knock table queries.
     */
    private final void setFlkcTableAddress(
            ParameterRow parameterRow,
            DS2Parameter parameterId) {

        switch (parameterId) {
            case E99:
                flkcAddr = getParameterAddr(parameterRow);
                flkc = parameterRow;
                break;
            default:
                break;
        }
    }

    /**
     * Return the parameter's integer address.
     */
    private final int getParameterAddr(ParameterRow parameterRow) {
        final EcuData ecudata = (EcuData) parameterRow.getLoggerData();
        final String addrStr = ecudata.getAddress().getAddresses()[0];
        final String addrHexStr = addrStr.replaceAll("0x", "");
        return Integer.parseInt(addrHexStr, 16);
    }

    /**
     * Start populating the vehicle information map with passed values.
     */
    private final void buildVehicleInfoMap(EcuDefinition ecuDef) {
        vehicleInfo.put("CAL ID", ecuDef.getCalId());
        vehicleInfo.put("ECU ID", ecuDef.getEcuId());
        vehicleInfo.put("Description",
                ecuDef.getCarString().replaceAll("BMW ", ""));
    }

    /**
     * Retrieve the table axis values from the ECU definition.
     */
    private final List<EcuQuery> getTableAxisRanges(
            Document document,
            EcuDefinition ecuDef,
            List<String> tableNames) {

        List<EcuQuery> tableAxis = new ArrayList<EcuQuery>();
        for (String tableName : tableNames) {
            tableAxis = loadTable(document, ecuDef, tableName);
            if (!tableAxis.isEmpty()) {
                break;
            }
        }
        return tableAxis;
    }

    /**
     * Once values from the ECU have been populated add the values to the
     * table models datasets.
     */
    private final void processEcuQueryResponses(List<EcuQuery> queries) {
        final DS2ParameterCrossReference parameterMap = new DS2ParameterCrossReference();
        final List<Object> afLearningBank1 = new ArrayList<Object>();
        final List<Object> afLearningBank2 = new ArrayList<Object>();

        for (EcuQuery query : queries) {
            final DS2Parameter parameterId =
                    DS2Parameter.fromValue(query.getLoggerData().getId());
            final String paramDesc = parameterMap.getValue(parameterId);
            String result = String.format("%.2f %s",
                    query.getResponse(),
                    query.getLoggerData().getSelectedConvertor().getUnits());
            switch (parameterId) {
                case E99:
                    break;
                case E19:
                    afLearningBank1.add((Object) "#1");
                    afLearningBank1.add((Object) result);
                    break;
                case E21:
                    afLearningBank1.add((Object) result);
                    afLearning.add(afLearningBank1);
                    break;
                case E20:
                    afLearningBank2.add((Object) "#2");
                    afLearningBank2.add((Object) result);
                    break;
                case E22:
                    afLearningBank2.add((Object) result);
                    afLearning.add(afLearningBank2);
                    break;
                default:
                    vehicleInfo.put(paramDesc, result);
                    break;
            }
        }
    }

    /**
     * Build a List of EcuQueries to retrieve the axis and scaling of a table.
     * A table is found when the storageaddress parameter has been identified.
     */
    private final List<EcuQuery> loadTable(
            Document document,
            EcuDefinition ecuDef,
            String tableName) {

        final List<Node> inheritanceList =
                EcuDefinitionInheritanceList.getInheritanceList(document, ecuDef);
        final Map<String, String> tableMap =
                EcuTableDefinitionHandler.getTableDefinition(
                        document,
                        inheritanceList,
                        tableName);
        List<EcuQuery> tableAxisQuery = new ArrayList<EcuQuery>();
        if (tableMap.containsKey("storageaddress")) {
            tableAxisQuery = DS2TableAxisQueryParameterSet.build(
                    tableMap.get("storageaddress"),
                    tableMap.get("storagetype"),
                    tableMap.get("expression"),
                    tableMap.get("units"),
                    tableMap.get("sizey"),
                    "0x06", "0x00", null,
                    tableMap.get("endian")
            );
        }
        return tableAxisQuery;
    }

    /**
     * Format the range data to be used as table column header values.
     */
    private final String[] formatRanges(
            Collection<EcuQuery> axisRanges,
            String numberFormat) {

        final List<String> ranges = new ArrayList<String>();
        ranges.add(" ");
        double value = 0;
        for (EcuQuery ecuQuery : axisRanges) {
            value = ecuQuery.getResponse();
            final String range = String.format(
                    numberFormat,
                    value);
            ranges.add(range);
        }
        return ranges.toArray(new String[0]);
    }

    /**
     * Format the RPM range data to be used as Knock table row header values.
     */
    private final String[] formatRpmRanges(Collection<EcuQuery> axisRanges) {

        final List<String> ranges = new ArrayList<String>();
        double value = 0;
        for (EcuQuery ecuQuery : axisRanges) {
            value = ecuQuery.getResponse();
            final String range = String.format(
                    "%.0f", value);
            ranges.add(range);
        }
        return ranges.toArray(new String[0]);
    }

    /**
     * Return a Transport based on its String ID.
     */
    private Transport getTransportById(String id) {
        for (Transport transport : getTransportMap().keySet()) {
            if (transport.getId().equalsIgnoreCase(id))
                return transport;
        }
        return null;
    }

    /**
     * Return a Map of Transport and associated Modules for the current protocol.
     */
    private Map<Transport, Collection<Module>> getTransportMap() {
        return logger.getProtocolList().get(settings.getLoggerProtocol());
    }

    /**
     * Return a Module based on its String name.
     */
    private Module getModule(String name) {
        final Collection<Module> modules = getTransportMap().get(
                getTransportById(settings.getTransportProtocol()));
        for (Module module: modules) {
            if (module.getName().equalsIgnoreCase(name))
                return module;
        }
        return null;
    }
}
