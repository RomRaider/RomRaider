/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.romraider.Settings;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.comms.io.connection.LoggerConnection;
import com.romraider.logger.ecu.comms.learning.parameter.NCSParameter;
import com.romraider.logger.ecu.comms.learning.parameter.NCSParameterCrossReference;
import com.romraider.logger.ecu.comms.learning.parameter.ParameterIdComparator;
import com.romraider.logger.ecu.comms.learning.tableaxis.NCSTableAxisQueryParameterSet;
import com.romraider.logger.ecu.comms.learning.tables.NCSLtftTableQueryBuilder;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryImpl;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.EcuDefinition;
import com.romraider.logger.ecu.definition.xml.EcuDefinitionDocumentLoader;
import com.romraider.logger.ecu.definition.xml.EcuDefinitionInheritanceList;
import com.romraider.logger.ecu.definition.xml.EcuTableDefinitionHandler;
import com.romraider.logger.ecu.ui.MessageListener;
import com.romraider.logger.ecu.ui.paramlist.ParameterListTableModel;
import com.romraider.logger.ecu.ui.paramlist.ParameterRow;
import com.romraider.logger.ecu.ui.swing.tools.NCSLearningTableValuesResultsPanel;
import com.romraider.util.ParamChecker;
import com.romraider.util.ResourceUtil;


/**
 * This class manages the building of ECU queries and retrieving the data to
 * populate the table models which will be used by the Learning Table Values
 * display panel.
 */
public final class NCSLearningTableValues extends SwingWorker<Void, Void>
                    implements LearningTableValues {

    private static final Logger LOGGER =
            Logger.getLogger(NCSLearningTableValues.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            NCSLearningTableValues.class.getName());
    private static final List<String> AF_TABLE_NAMES = Arrays.asList(
            "A/F Learning #1 Airflow Ranges",
            "A/F Learning #1 Airflow Ranges ",
            "A/F Learning Airflow Ranges");
    // LTFT table column and row names can be overridden in the
    // ./customize/ncslearning.properties file
    private static List<String> LTFT_TABLE_COLUMN_NAMES = Arrays.asList(
            "Learning map TP shaft lattice point table");
    private static List<String> LTFT_TABLE_ROW_NAMES = Arrays.asList(
            "Learning map N shaft lattice point table");
    private final Map<String, Object> vehicleInfo =
            new LinkedHashMap<String, Object>();
    private final List<List<Object>> afLearning = new ArrayList<List<Object>>();
    private EcuLogger logger;
    private Settings settings;
    private MessageListener messageListener;
    private ParameterListTableModel parmeterList;
    private EcuDefinition ecuDef;
    private ParameterRow ltftTrim;
    private int ltftTrimAddr;
    private ParameterRow ltftCnt;
    private int ltftCntAddr;

    public NCSLearningTableValues() {}

    @Override
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
        this.ltftTrim = null;
        this.ltftTrimAddr = 0;
        this.ltftCnt = null;
        this.ltftCntAddr = 0;
        loadProperties();
    }

    @Override
    public final Void doInBackground() {
        Document document = null;
        if (ecuDef.getEcuDefFile() == null) {
            showMessageDialog(logger,
                    rb.getString("DEFNOTFOUND"),
                    rb.getString("DEFMISSING"), WARNING_MESSAGE);
            return null;
        }
        else {
            document = EcuDefinitionDocumentLoader.getDocument(ecuDef);
        }

        final boolean logging = logger.isLogging();
        if (logging) logger.stopLogging();

        String message = rb.getString("GETLTFTVALUES");
        messageListener.reportMessage(message);
        buildVehicleInfoMap(ecuDef);

        try {
            LoggerConnection connection = getConnection(
                    settings.getLoggerProtocol(), settings.getLoggerPort(),
                    settings.getLoggerConnectionProperties());
            try {
                Collection<EcuQuery> queries = buildLearningQueries();
                // Break queries into two sets to avoid the ECU packet limit
                int setSize = queries.size() / 2;
                Collection<EcuQuery> querySet1 = new ArrayList<EcuQuery>();
                Collection<EcuQuery> querySet2 = new ArrayList<EcuQuery>();
                int s = 0;
                for (EcuQuery q : queries) {
                    if (s < setSize) {
                        querySet1.add(q);
                    }
                    else {
                        querySet2.add(q);
                    }
                    s++;
                }
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace(
                    String.format(
                            "Queries:%d, Set size:%d, Set 1 size:%d, Set 2 size:%d",
                            queries.size(), setSize, querySet1.size(), querySet2.size()));

                LOGGER.info("Retrieving vehicle info & LTFT values ...");
                connection.sendAddressReads(
                        querySet1,
                        settings.getDestinationTarget(),
                        new PollingStateImpl());
                connection.sendAddressReads(
                        querySet2,
                        settings.getDestinationTarget(),
                        new PollingStateImpl());
                LOGGER.info("Current vehicle info & LTFT values retrieved.");

                Collections.sort(
                        (List<EcuQuery>)queries, new ParameterIdComparator());

                processEcuQueryResponses((List<EcuQuery>) queries);

                //TODO: afRanges to be replaced with Knock tables once known how they operate
                String[] afRanges = new String[0];

                message = rb.getString("GETCOLRANGES");
                messageListener.reportMessage(message);
                String[] ltftCol = new String[0];
                queries.clear();
                queries = getTableAxisRanges(document, ecuDef, LTFT_TABLE_COLUMN_NAMES);
                if (queries != null && !queries.isEmpty()) {
                    LOGGER.info("Retrieving LTFT column ranges ...");
                    connection.sendAddressReads(
                            queries,
                            settings.getDestinationTarget(),
                            new PollingStateImpl());
                    LOGGER.info("LTFT column ranges retrieved.");
                    ltftCol = formatRanges(queries, "%.2f");
                }

                message = rb.getString("GETROWRANGES");
                messageListener.reportMessage(message);
                String[] ltftRow = new String[0];
                queries.clear();
                queries = getTableAxisRanges(document, ecuDef, LTFT_TABLE_ROW_NAMES);
                if (queries != null && !queries.isEmpty()) {
                    LOGGER.info("Retrieving LTFT row ranges ...");
                    connection.sendAddressReads(
                            queries,
                            settings.getDestinationTarget(),
                            new PollingStateImpl());
                    LOGGER.info("LTFT row ranges retrieved.");
                    ltftRow = formatRpmRanges(queries);
                }

                List<List<List<EcuQuery>>> ltftQueryTables = new ArrayList<List<List<EcuQuery>>>();
                List<List<EcuQuery>> ltftQueryGroups = new ArrayList<List<EcuQuery>>();
                if (ltftTrim != null) {
                    for (int k = 0; k < 256; k += 128) {
                        ltftQueryGroups = new NCSLtftTableQueryBuilder().build(
                                                    ltftTrim,
                                                    ltftTrimAddr + k,
                                                    ltftRow.length,
                                                    ltftCol.length - 1);
                        ltftQueryTables.add(ltftQueryGroups);

                        for (int i = 0; i < ltftQueryGroups.size(); i++) {
                            queries.clear();
                            for (int j = 0; j < ltftQueryGroups.get(i).size(); j++) {
                                if (ltftQueryGroups.get(i).get(j) != null) {
                                    queries.add(ltftQueryGroups.get(i).get(j));
                                }
                            }
                            message = MessageFormat.format(
                                    rb.getString("GETTABLELTFT"),
                                    (k/128+1), i);
                            messageListener.reportMessage(message);
                            LOGGER.info(String.format(
                                    "Retrieving Table %d LTFT row %d values ...",
                                    (k/128+1), i));
                            setSize = queries.size() / 2;
                            querySet1 = new ArrayList<EcuQuery>();
                            querySet2 = new ArrayList<EcuQuery>();
                            s = 0;
                            for (EcuQuery q : queries) {
                                if (s < setSize) {
                                    querySet1.add(q);
                                }
                                else {
                                    querySet2.add(q);
                                }
                                s++;
                            }
                            if (LOGGER.isTraceEnabled())
                                LOGGER.trace(
                                String.format(
                                        "Queries:%d, Set size:%d, Set 1 size:%d, Set 2 size:%d",
                                        queries.size(), setSize, querySet1.size(), querySet2.size()));

                            connection.sendAddressReads(
                                    querySet1,
                                    settings.getDestinationTarget(),
                                    new PollingStateImpl());
                            connection.sendAddressReads(
                                    querySet2,
                                    settings.getDestinationTarget(),
                                    new PollingStateImpl());
                            LOGGER.info(String.format(
                                    "Table %d row %d LTFT values retrieved.",
                                    (k/128+1), i));
                            queries.clear();
                        }
                    }
                }
                else {
                    message = rb.getString("ERRTABLELTFT");
                    messageListener.reportMessage(message);
                    LOGGER.error("Error retrieving LTFT data values, missing LTFT reference");
                }
                if (ltftCnt != null) {
                    for (int k = 0; k < 128; k += 64) {
                        ltftQueryGroups = new NCSLtftTableQueryBuilder().build(
                                                    ltftCnt,
                                                    ltftCntAddr + k,
                                                    ltftRow.length,
                                                    ltftCol.length - 1);
                        ltftQueryTables.add(ltftQueryGroups);

                        for (int i = 0; i < ltftQueryGroups.size(); i++) {
                            queries.clear();
                            for (int j = 0; j < ltftQueryGroups.get(i).size(); j++) {
                                if (ltftQueryGroups.get(i).get(j) != null) {
                                    queries.add(ltftQueryGroups.get(i).get(j));
                                }
                            }
                            message = MessageFormat.format(
                                    rb.getString("GETTABLELTFT"),
                                    (k/64+3), i);
                            messageListener.reportMessage(message);
                            LOGGER.info(String.format("Retrieving Table %d LTFT row %d values...",
                                    (k/64+3), i));
                            setSize = queries.size() / 2;
                            querySet1 = new ArrayList<EcuQuery>();
                            querySet2 = new ArrayList<EcuQuery>();
                            s = 0;
                            for (EcuQuery q : queries) {
                                if (s < setSize) {
                                    querySet1.add(q);
                                }
                                else {
                                    querySet2.add(q);
                                }
                                s++;
                            }
                            if (LOGGER.isTraceEnabled())
                                LOGGER.trace(
                                String.format("Queries:%d, Set size:%d, Set 1 size:%d, Set 2 size:%d",
                                    queries.size(), setSize, querySet1.size(), querySet2.size()));

                            connection.sendAddressReads(
                                    querySet1,
                                    settings.getDestinationTarget(),
                                    new PollingStateImpl());
                            connection.sendAddressReads(
                                    querySet2,
                                    settings.getDestinationTarget(),
                                    new PollingStateImpl());
                            LOGGER.info(String.format("Table %d row %d LTFT values retrieved.",
                                    (k/64+3), i));
                            queries.clear();
                        }
                    }
                }
                else {
                    message = rb.getString("ERRTABLELTFT");
                    messageListener.reportMessage(message);
                    LOGGER.error("Error retrieving LTFT data values, missing LTFT reference");
                }

                messageListener.reportMessage(
                        rb.getString("LTFTSUCCESS"));
                final NCSLearningTableValuesResultsPanel results =
                        new NCSLearningTableValuesResultsPanel(
                                logger, vehicleInfo,
                                afRanges, afLearning,
                                ltftCol, ltftRow, ltftQueryTables);
                results.displayLearningResultsPanel();
            }
            finally {
                connection.close();
//                settings.setTransportProtocol(transport);
//                settings.setDestinationTarget(module);
                if (logging) logger.startLogging();
            }
        }
        catch (Exception e) {
            messageListener.reportError(
                    rb.getString("ERRLTFT"));
            LOGGER.error(message + " Error retrieving values", e);
            showMessageDialog(logger,
                    MessageFormat.format(
                            rb.getString("ERRCONNECT"), message),
                    rb.getString("LTV"),
                    ERROR_MESSAGE);
        }
        return null;
    }

    /**
     * Build a collection of queries based on the initialized values of
     * parameters defined for this ECU.  Also identify the LTFT Trim and Count
     * parameters used to locate the LTFT tables.
     * @return the supported parameter list filtered for only the Learning Table
     * Value parameters needed.
     */
    private final Collection<EcuQuery> buildLearningQueries() {
        final Collection<EcuQuery> query = new ArrayList<EcuQuery>();
        final List<ParameterRow> parameterRows = parmeterList.getParameterRows();
        if (!ParamChecker.isNullOrEmpty(parameterRows)) {
            for (ParameterRow parameterRow : parameterRows) {
                final NCSParameter parameterId =
                        NCSParameter.fromValue(parameterRow.getLoggerData().getId());
                if (parameterId != null) {
                    switch (parameterId) {
                        case E173:
                            ltftTrimAddr = getParameterAddr(parameterRow);
                            ltftTrim = parameterRow;
                            break;
                        case E174:
                            ltftCntAddr = getParameterAddr(parameterRow);
                            ltftCnt = parameterRow;
                            break;
                        default:
                            query.add(buildEcuQuery(parameterRow));
                            break;
                    }
                }
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
                ecuDef.getCarString().replaceAll("Nissan ", ""));
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
        final NCSParameterCrossReference parameterMap = new NCSParameterCrossReference();

        for (EcuQuery query : queries) {
            final NCSParameter parameterId =
                    NCSParameter.fromValue(query.getLoggerData().getId());
            final String paramDesc = parameterMap.getValue(parameterId);
            String result = String.format("%.2f %s",
                    query.getResponse(),
                    query.getLoggerData().getSelectedConvertor().getUnits());
            switch (parameterId) {
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
            tableAxisQuery = NCSTableAxisQueryParameterSet.build(
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
     * Format the RPM range data to be used as FLKC table row header values.
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
     * Load ECU def table names from a user customized properties file.
     * The file will replace the Class defined names if it is present.
     * Names in the file should be separated by the : character
     * @exception    FileNotFoundException if the directory or file is not present
     * @exception    IOException if there's some kind of IO error
     */
    private void loadProperties() {
        final Properties learning = new Properties();
        FileInputStream propFile;
        try {
            propFile = new FileInputStream("./customize/ncslearning.properties");
            learning.load(propFile);
            final String ltft_table_column_names =
                    learning.getProperty("ltft_table_column_names");
            String[] names = ltft_table_column_names.split(":", 0);
            LTFT_TABLE_COLUMN_NAMES = new ArrayList<String>();
            for (String name : names) {
                if (ParamChecker.isNullOrEmpty(name)) continue;
                LTFT_TABLE_COLUMN_NAMES.add(name);
            }
            final String ltft_table_row_names =
                    learning.getProperty("ltft_table_row_names");
            names = ltft_table_row_names.split(":", 0);
            LTFT_TABLE_ROW_NAMES = new ArrayList<String>();
            for (String name : names) {
                if (ParamChecker.isNullOrEmpty(name)) continue;
                LTFT_TABLE_ROW_NAMES.add(name);
            }
            propFile.close();
            LOGGER.info("NCSLearningTableValues loaded table names from file: ./customize/ncslearning.properties");
        } catch (FileNotFoundException e) {
            LOGGER.error("NCSLearningTableValues properties file: " + e.getLocalizedMessage());
        } catch (IOException e) {
            LOGGER.error("NCSLearningTableValues IOException: " + e.getLocalizedMessage());
        }
    }
}
