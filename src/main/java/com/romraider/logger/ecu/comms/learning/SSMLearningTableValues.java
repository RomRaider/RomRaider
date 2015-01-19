/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.romraider.Settings;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.comms.io.connection.LoggerConnection;
import com.romraider.logger.ecu.comms.learning.flkctable.FlkcTableQueryBuilder;
import com.romraider.logger.ecu.comms.learning.parameter.SSMParameter;
import com.romraider.logger.ecu.comms.learning.parameter.SSMParameterCrossReference;
import com.romraider.logger.ecu.comms.learning.parameter.ParameterIdComparator;
import com.romraider.logger.ecu.comms.learning.tableaxis.TableAxisQueryParameterSet;
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
import com.romraider.logger.ecu.ui.swing.tools.LearningTableValuesResultsPanel;
import com.romraider.util.ParamChecker;

/**
 * This class manages the building of ECU queries and retrieving the data to
 * populate the table models which will be used by the Learning Table Values
 * display panel.
 */
public final class SSMLearningTableValues extends SwingWorker<Void, Void>
                    implements LearningTableValues {

    private static final Logger LOGGER =
            Logger.getLogger(SSMLearningTableValues.class);
    private static final List<String> AF_TABLE_NAMES = Arrays.asList(
            "A/F Learning #1 Airflow Ranges",
            "A/F Learning #1 Airflow Ranges ",
            "A/F Learning Airflow Ranges");
    private static final List<String> FLKC_LOAD_TABLE_NAMES = Arrays.asList(
            "Fine Correction Columns (Load)",
            "Fine Correction Columns (Load) ");
    private static final List<String> FLKC_RPM_TABLE_NAMES = Arrays.asList(
            "Fine Correction Rows (RPM)",
            "Fine Correction Rows (RPM) ");
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

    public SSMLearningTableValues() {}

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
                    "ECU definition file not found or undefined. Learning\n" +
                    "Table Values cannot be properly retrieved until an ECU\n" +
                    "defintion is defined in the Editor's Definition Manager.",
                    "ECU Defintion Missing", WARNING_MESSAGE);
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

        String message = "Retrieving vehicle info & A/F values...";
        messageListener.reportMessage(message);
        buildVehicleInfoMap(ecuDef);

        try {
            LoggerConnection connection = getConnection(
                    settings.getLoggerProtocol(), settings.getLoggerPort(),
                    settings.getLoggerConnectionProperties());
            try {
                Collection<EcuQuery> queries = buildLearningQueries();

                LOGGER.info(message);
                connection.sendAddressReads(
                        queries,
                        settings.getDestinationTarget(),
                        new PollingStateImpl());
                LOGGER.info("Current vehicle info & A/F values retrieved.");

                Collections.sort(
                        (List<EcuQuery>)queries, new ParameterIdComparator());

                processEcuQueryResponses((List<EcuQuery>) queries);

                message = "Retrieving A/F Learning ranges...";
                messageListener.reportMessage(message);
                String[] afRanges = new String[0];
                queries.clear();
                queries = getTableAxisRanges(document, ecuDef, AF_TABLE_NAMES);
                if (queries != null && !queries.isEmpty()) {
                    LOGGER.info(message);
                    connection.sendAddressReads(
                            queries,
                            settings.getDestinationTarget(),
                            new PollingStateImpl());
                    LOGGER.info("A/F Learning ranges retrieved.");
                    afRanges = formatRanges(queries, "%.2f");
                }

                message = "Retrieving FLKC Load ranges...";
                messageListener.reportMessage(message);
                String[] flkcLoad = new String[0];
                queries.clear();
                queries = getTableAxisRanges(document, ecuDef, FLKC_LOAD_TABLE_NAMES);
                if (queries != null && !queries.isEmpty()) {
                    LOGGER.info(message);
                    connection.sendAddressReads(
                            queries,
                            settings.getDestinationTarget(),
                            new PollingStateImpl());
                    LOGGER.info("FLKC Load ranges retrieved.");
                    flkcLoad = formatRanges(queries, "%.2f");
                }

                message = "Retrieving FLKC RPM ranges...";
                messageListener.reportMessage(message);
                String[] flkcRpm = new String[0];
                queries.clear();
                queries = getTableAxisRanges(document, ecuDef, FLKC_RPM_TABLE_NAMES);
                if (queries != null && !queries.isEmpty()) {
                    LOGGER.info(message);
                    connection.sendAddressReads(
                            queries,
                            settings.getDestinationTarget(),
                            new PollingStateImpl());
                    LOGGER.info("FLKC RPM ranges retrieved.");
                    flkcRpm = formatRpmRanges(queries);
                }

                List<List<EcuQuery>> flkcQueryGroups = new ArrayList<List<EcuQuery>>();
                if (flkc != null) {
                    flkcQueryGroups = new FlkcTableQueryBuilder().build(
                                            flkc,
                                            flkcAddr,
                                            flkcRpm.length,
                                            flkcLoad.length - 1);
    
                    for (int i = 0; i < flkcQueryGroups.size(); i++) {
                        queries.clear();
                        for (int j = 0; j < flkcQueryGroups.get(i).size(); j++) {
                            if (flkcQueryGroups.get(i).get(j) != null) {
                                queries.add(flkcQueryGroups.get(i).get(j));
                            }
                        }
                        message = String.format("Retrieving FLKC row %d values...", i);
                        messageListener.reportMessage(message);
                        LOGGER.info(message);
                        connection.sendAddressReads(
                                queries,
                                settings.getDestinationTarget(),
                                new PollingStateImpl());
                        LOGGER.info("FLKC row " + i + " values retrieved.");               
                    }
                }
                else {
                    message = String.format("Error retrieving FLKC data values, missing FLKC reference");
                    messageListener.reportMessage(message);
                    LOGGER.error(message);
                }

                messageListener.reportMessage(
                        "Learning Table Values retrieved successfully.");
                final LearningTableValuesResultsPanel results =
                        new LearningTableValuesResultsPanel(
                                logger, vehicleInfo,
                                afRanges, afLearning,
                                flkcLoad, flkcRpm, flkcQueryGroups);
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
                    "Unable to retrieve current ECU learning values");
            LOGGER.error(message + " Error retrieving values", e);
            showMessageDialog(logger,
                    message +
                    "\nError performing Learning Table Values read.\n" +
                    "Check the following:\n" +
                    "* Logger has successfully conencted to the ECU\n" +
                    "* Correct COM port is selected (if not Openport 2)\n" +
                    "* Cable is connected properly\n* Ignition is ON\n",
                    "Learning Table Values",
                    ERROR_MESSAGE);
        }
        return null;
    }

    /**
     * Build a collection of queries based on the initialized values of
     * parameters defined for this ECU.  Also identify the IAM and FLKC
     * parameters used to locate and calculate the FLKC table.
     * @return the supported parameter list filtered for only the Learning Table
     * Value parameters needed.
     */
    private final Collection<EcuQuery> buildLearningQueries() {
        final Collection<EcuQuery> query = new ArrayList<EcuQuery>();
        final List<ParameterRow> parameterRows = parmeterList.getParameterRows();
        if (!ParamChecker.isNullOrEmpty(parameterRows)) {
            for (ParameterRow parameterRow : parameterRows) {
                final SSMParameter parameterId =
                        SSMParameter.fromValue(parameterRow.getLoggerData().getId());
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
     * Define the start address of the FLKC table in RAM base on a Extended
     * parameter if defined.
     * Also isolate the FLKC extended parameter to use the data converter
     * when building the FLKC table queries.
     */
    private final void setFlkcTableAddress(
            ParameterRow parameterRow,
            SSMParameter parameterId) {

        switch (parameterId) {
            case E1:
                if (flkcAddr == 0) {
                    flkcAddr = getParameterAddr(parameterRow) + 0x02;
                }
                break;
            case E31:
                if (flkcAddr == 0) {
                    flkcAddr = getParameterAddr(parameterRow) + 0x14;
                }
                break;
            case E12:
            case E41:
                if (flkc == null) {
                    flkc = parameterRow;
                }
                break;
            case E173:
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
                ecuDef.getCarString().replaceAll("Subaru ", ""));
    }

    /**
     * Retrieve the table axis values from the ECU definition. First try the
     * 4-cyl table names, if still empty try the 6-cyl table name.
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
        final SSMParameterCrossReference parameterMap = new SSMParameterCrossReference();
        final List<Object> afLearningBank1 = new ArrayList<Object>();
        final List<Object> afLearningBank2 = new ArrayList<Object>();

        for (EcuQuery query : queries) {
            final SSMParameter parameterId =
                    SSMParameter.fromValue(query.getLoggerData().getId());
            final String paramDesc = parameterMap.getValue(parameterId);
            String result = String.format("%.2f %s",
                    query.getResponse(),
                    query.getLoggerData().getSelectedConvertor().getUnits());
            switch (parameterId) {
                case E1:
                    result = String.format("%.0f", query.getResponse());
                    vehicleInfo.put(paramDesc, result);
                    break;
                case E31:
                    result = String.format("%.3f", query.getResponse());
                    vehicleInfo.put(paramDesc, result);
                    break;
                case E12:
                case E41:
                case E173:
                    break;
                case E13:
                case E44:
                    afLearningBank1.add((Object) "#1");
                    afLearningBank1.add((Object) result);
                    break;
                case E14:
                case E45:
                case E15:
                case E46:
                    afLearningBank1.add((Object) result);
                    break;
                case E16:
                case E47:
                    afLearningBank1.add((Object) result);
                    afLearning.add(afLearningBank1);
                    break;
                case E62:
                    afLearningBank2.add((Object) "#2");
                    afLearningBank2.add((Object) result);
                    break;
                case E63:
                case E64:
                    afLearningBank2.add((Object) result);
                    break;
                case E65:
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
            tableAxisQuery = TableAxisQueryParameterSet.build(
                    tableMap.get("storageaddress"),
                    tableMap.get("storagetype"),
                    tableMap.get("expression"),
                    tableMap.get("units"),
                    tableMap.get("sizey")
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
        double rangeMin = 0;
        double rangeMax = 0;
        for (EcuQuery ecuQuery : axisRanges) {
            rangeMax = ecuQuery.getResponse() - 0.01;
            final String range = String.format(
                    numberFormat + " - " + numberFormat,
                    rangeMin,
                    rangeMax);
            ranges.add(range);
            rangeMin = ecuQuery.getResponse();
            rangeMax = ecuQuery.getResponse();
        }
        final String range = String.format(numberFormat + "+", rangeMax);
        ranges.add(range);
        return ranges.toArray(new String[0]);
    }

    /**
     * Format the RPM range data to be used as FLKC table row header values.
     */
    private final String[] formatRpmRanges(Collection<EcuQuery> axisRanges) {

        final List<String> ranges = new ArrayList<String>();
        double rangeMin = 0;
        double rangeMax = 0;
        for (EcuQuery ecuQuery : axisRanges) {
            rangeMax = ecuQuery.getResponse() - 1;
            final String range = String.format(
                    "%.0f - %.0f",
                    rangeMin,
                    rangeMax);
            ranges.add(range);
            rangeMin = ecuQuery.getResponse();
            rangeMax = ecuQuery.getResponse();
        }
        final String range = String.format("%.0f+", rangeMax);
        ranges.add(range);
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
