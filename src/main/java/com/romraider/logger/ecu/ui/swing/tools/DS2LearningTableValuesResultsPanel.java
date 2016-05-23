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

package com.romraider.logger.ecu.ui.swing.tools;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.ui.swing.tools.tablemodels.AirFuelLearningTableModel;
import com.romraider.logger.ecu.ui.swing.tools.tablemodels.FineLearningKnockCorrectionTableModel;
import com.romraider.logger.ecu.ui.swing.tools.tablemodels.VehicleInformationTableModel;
import com.romraider.logger.ecu.ui.swing.tools.tablemodels.renderers.CentreRenderer;
import com.romraider.logger.ecu.ui.swing.tools.tablemodels.renderers.LtvCellRenderer;
import com.romraider.logger.ecu.ui.swing.vertical.VerticalLabelUI;
import com.romraider.swing.SetFont;
import com.romraider.util.FormatFilename;
import com.romraider.util.SettingsManager;

/**
 * This class is used to build and display the Adaptation Table Values
 * retrieved from the ECU.
 */
public class DS2LearningTableValuesResultsPanel extends JDialog {
    private static final long serialVersionUID = 6716454294436022709L;
    private final String DIALOG_TITLE = "Adaptation Table Values";
    private final String DT_FORMAT = "%1$tY%1$tm%1$td-%1$tH%1$tM%1$tS";
    private final int LTV_WIDTH = 720;
    private final int LTV_HEIGHT = 595;
    private final JPanel contentPanel = new JPanel();
    private JTable vehicleInfoTable;
    private JTable afLearningTable;
    private List<JTable> knockTables = new ArrayList<JTable>();
    private final String FLKC_NAME = "Knock Adaptation (\u00B0 of correction)";

    public DS2LearningTableValuesResultsPanel(
            EcuLogger logger,
            Map<String, Object> vehicleInfo,
            String[] afRanges,
            List<List<Object>> afLearning,
            String[] flkcLoad,
            String[] flkcRpm,
            List<List<List<EcuQuery>>> flkcQueryTables) {

        super(logger, false);
        setIconImage(logger.getIconImage());
        setTitle(DIALOG_TITLE);
        setBounds(
                (logger.getWidth() > LTV_WIDTH) ?
                        logger.getX() + (logger.getWidth() - LTV_WIDTH) / 2 : 0,
                (logger.getHeight() > LTV_HEIGHT) ?
                        logger.getY() + ((logger.getHeight() - LTV_HEIGHT) / 2) : 0,
                LTV_WIDTH,
                LTV_HEIGHT);
        getContentPane().setLayout(new BorderLayout());

        contentPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        contentPanel.setLayout(null);
        contentPanel.add(buildVehicleInfoPanel(vehicleInfo));
        contentPanel.add(buildAfLearningPanel(afRanges, afLearning));
        contentPanel.add(buildFlkcPanel(flkcLoad, flkcRpm, flkcQueryTables));

        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buildSaveReultsPanel(), BorderLayout.SOUTH);
    }

    /**
     * This method is called to display the Adaptation Table Values
     * retrieved from the ECU.
     */
    public final void displayLearningResultsPanel() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private final JPanel buildVehicleInfoPanel(
            Map<String, Object> vehicleInfo) {

        final JPanel vehicleInfoTitlePanel = new JPanel();
        vehicleInfoTitlePanel.setBorder(
                BorderFactory.createTitledBorder("Vehicle Information"));
        vehicleInfoTitlePanel.setBounds(10, 2, 692, 70);
        vehicleInfoTitlePanel.setLayout(new BorderLayout(0, 0));

        JPanel vehicleInfoTablePanel = new JPanel();
        vehicleInfoTablePanel.setBorder(
                new EtchedBorder(EtchedBorder.LOWERED, null, null));
        vehicleInfoTablePanel.setLayout(new BorderLayout(0, 0));

        final VehicleInformationTableModel tableModel =
                new VehicleInformationTableModel();
        tableModel.setVehicleInfo(vehicleInfo);
        vehicleInfoTable = new JTable(tableModel);
        setTableBehaviour(vehicleInfoTable);

        TableColumn column = null;
        for (int i = 0; i < vehicleInfoTable.getColumnCount(); i++) {
            column = vehicleInfoTable.getColumnModel().getColumn(i);
            if (i == 0 || i ==1) {
                column.setPreferredWidth(85);
            }
            else if (i == 2) {
                column.setPreferredWidth(200);
            }
        }

        vehicleInfoTablePanel.add(
                formatTableHeader(vehicleInfoTable),
                BorderLayout.PAGE_START);
        vehicleInfoTablePanel.add(vehicleInfoTable);

        vehicleInfoTitlePanel.add(vehicleInfoTablePanel, BorderLayout.CENTER);
        return vehicleInfoTitlePanel;
    }

    private final JPanel buildAfLearningPanel(
            String[] afRanges,
            List<List<Object>> afLearning) {

        final JPanel afLearningTitlePanel = new JPanel();
        afLearningTitlePanel.setBorder(
                new TitledBorder(null,
                        "A/F Adaptation (Stored)",
                        TitledBorder.LEADING,
                        TitledBorder.TOP, null, null));
        afLearningTitlePanel.setBounds(10, 72, 450, 100);
        afLearningTitlePanel.setLayout(new BorderLayout(0, 0));

        final JLabel xLabel = new JLabel("Applied Adaptations");
        SetFont.plain(xLabel);
        xLabel.setHorizontalAlignment(SwingConstants.CENTER);
        afLearningTitlePanel.add(xLabel, BorderLayout.NORTH);

        final JLabel yLabel = new JLabel("Bank");
        SetFont.plain(yLabel);
        yLabel.setUI(new VerticalLabelUI(false));
        afLearningTitlePanel.add(yLabel, BorderLayout.WEST);

        final JPanel afLearningTablePanel = new JPanel();
        afLearningTablePanel.setBorder(
                new EtchedBorder(EtchedBorder.LOWERED, null, null));
        afLearningTablePanel.setLayout(new BorderLayout(0, 0));

        final AirFuelLearningTableModel tableModel =
                new AirFuelLearningTableModel();
        tableModel.setColumnHeadings(afRanges);
        tableModel.setAfLearningInfo(afLearning);
        afLearningTable = new JTable(tableModel);
        setTableBehaviour(afLearningTable);

        TableColumn column = null;
        for (int i = 0; i < afLearningTable.getColumnCount(); i++) {
            column = afLearningTable.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(40);
            }
            else {
                column.setPreferredWidth(120);
            }
        }

        afLearningTablePanel.add(
                formatTableHeader(afLearningTable),
                BorderLayout.PAGE_START);
        if (afLearning.size() > 0) {
            afLearningTablePanel.add(afLearningTable);
        }
        else {
            afLearningTablePanel.add(new JLabel(
                    " No data - A/F Adaptation parameter IDs not defined"));
        }

        afLearningTitlePanel.add(afLearningTablePanel, BorderLayout.CENTER);
        return afLearningTitlePanel;
    }

    private final JPanel buildFlkcPanel(
            String[] flkcLoad,
            String[] flkcRpm,
            List<List<List<EcuQuery>>> flkcQueryTables) {

        final JPanel flkcTitlePanel = new JPanel();
        flkcTitlePanel.setBorder(
                new TitledBorder(null,
                        FLKC_NAME,
                        TitledBorder.LEADING,
                        TitledBorder.TOP, null, null));
        flkcTitlePanel.setBounds(10, 172, 692, 354);
        flkcTitlePanel.setLayout(new BorderLayout(0, 0));

        final JLabel xLabel = new JLabel("Engine Load (mg/stroke)");
        SetFont.plain(xLabel);
        xLabel.setHorizontalAlignment(SwingConstants.CENTER);
        flkcTitlePanel.add(xLabel, BorderLayout.NORTH);

        final JLabel yLabel = new JLabel("Engine Speed (RPM)");
        SetFont.plain(yLabel);
        yLabel.setUI(new VerticalLabelUI(false));
        flkcTitlePanel.add(yLabel, BorderLayout.WEST);

        final JTabbedPane tabs = new JTabbedPane();
        for (int i = 0; i < flkcQueryTables.size(); i++) {
            final FineLearningKnockCorrectionTableModel tableModel =
                    new FineLearningKnockCorrectionTableModel();
            tableModel.setColumnHeadings(flkcLoad);
            tableModel.setRomHeadings(flkcRpm);
            tableModel.setFlkcData(flkcQueryTables.get(i));
            final JTable knockTable = new JTable(tableModel);
            knockTables.add(knockTable);
            setTableBehaviour(knockTable);
            formatTableHeader(knockTable);
            final JScrollPane flkcTablePanel = new JScrollPane(knockTable);
            flkcTablePanel.setBorder(
                    new EtchedBorder(EtchedBorder.LOWERED, null, null));
            tabs.addTab("Table " + (i + 1), flkcTablePanel);
        }
        if (flkcQueryTables.size() > 0) {
            flkcTitlePanel.add(tabs, BorderLayout.CENTER);
        }
        else {
            flkcTitlePanel.removeAll();
            flkcTitlePanel.add(new JLabel(
                    " No data - Knock Adaptation parameter ID not defined"),
                    BorderLayout.CENTER);
        }
        return flkcTitlePanel;
    }

    private final void setTableBehaviour(JTable table) {
        table.setBorder(null);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        table.setFont(new Font("Tahoma", Font.PLAIN, 11));
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Double.class, new LtvCellRenderer());
        table.setDefaultRenderer(String.class, new LtvCellRenderer());
    }

    private final JTableHeader formatTableHeader(JTable table) {
        final JTableHeader th = table.getTableHeader();
        th.setReorderingAllowed(false);
        th.setDefaultRenderer(new CentreRenderer(table));
        SetFont.bold(th, 11);
        return th;
    }

    private final JPanel buildSaveReultsPanel() {

        final JPanel controlPanel = new JPanel();
        final JButton toFile = new JButton("Save to File");
        toFile.setToolTipText("Save tables to a text file");
        toFile.setMnemonic(KeyEvent.VK_F);
        toFile.addActionListener(new ActionListener() {
            @Override
            public final void actionPerformed(ActionEvent actionEvent) {
                saveTableText();
            }
        });
        final JButton toImage = new JButton("Save as Image");
        toImage.setToolTipText("Save tables as an image");
        toImage.setMnemonic(KeyEvent.VK_I);
        toImage.addActionListener(new ActionListener() {
            @Override
            public final void actionPerformed(ActionEvent actionEvent) {
                saveTableImage();
            }
        });
        controlPanel.add(toFile);
        controlPanel.add(toImage);
        return controlPanel;
    }

    private final void saveTableText() {
        final String nowStr = String.format(DT_FORMAT, System.currentTimeMillis());
        final String fileName = String.format("%s%sromraiderADPT_%s.csv",
                SettingsManager.getSettings().getLoggerOutputDirPath(),
                File.separator,
                nowStr);
        try {
            final File csvFile = new File(fileName);
            final String EOL = System.getProperty("line.separator");
            final BufferedWriter bw = new BufferedWriter(
                    new FileWriter(csvFile));
            bw.write("Adaptation Table Values" + EOL);
            Object result = 0;
            int columnCount = vehicleInfoTable.getColumnCount();
            for (int i = 0; i < columnCount; i++ ) {
                result = vehicleInfoTable.getTableHeader().getColumnModel().
                        getColumn(i).getHeaderValue();
                bw.append(result.toString() + ",");
            }
            bw.append(EOL);
            for (int i = 0; i < columnCount; i++ ) {
                result = vehicleInfoTable.getValueAt(0, i);
                bw.append(result + ",");
            }
            bw.append(EOL + EOL);
            bw.write("A/F Adaptation (Stored)" + EOL);
            columnCount = afLearningTable.getColumnCount();
            int rowCount = afLearningTable.getRowCount();
            for (int i = 0; i < columnCount; i++) {
                result = afLearningTable.getTableHeader().getColumnModel().
                        getColumn(i).getHeaderValue();
                bw.append(result.toString() + ",");
            }
            bw.append(EOL);
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    result = afLearningTable.getValueAt(i, j);
                    bw.append(result + ",");
                }
                bw.append(EOL);
            }
            bw.append(EOL);
            bw.write(FLKC_NAME + EOL);
            int k = 1;
            for (JTable knockTable : knockTables) {
                columnCount = knockTable.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    result = knockTable.getTableHeader().getColumnModel().
                            getColumn(i).getHeaderValue();
                    if (result.toString().equals(" ")) {
                        result = String.format("Table %d", k);
                    }
                    bw.append(result.toString() + ",");
                }
                k++;
            }
            bw.append(EOL);
            rowCount = knockTables.get(0).getRowCount();
            for (int i = 0; i < rowCount; i++) {
                for (JTable knockTable : knockTables) {
                    columnCount = knockTable.getColumnCount();
                    for (int j = 0; j < columnCount; j++) {
                        result = knockTable.getValueAt(i, j);
                        bw.append(result + ",");
                    }
                }
                bw.append(EOL);
            }
            bw.close();
            final String shortName = FormatFilename.getShortName(fileName);
            showMessageDialog(
                    null,
                    "Table's text saved to: " + shortName,
                    "Save Success",
                    INFORMATION_MESSAGE);
        }
        catch (Exception e) {
            showMessageDialog(
                    null,
                    "Failed to save tables, check path:\n" + fileName,
                    "Save Failed",
                    ERROR_MESSAGE);
        }
    }

    private final void saveTableImage() {
        final BufferedImage resultsImage = new BufferedImage(
                contentPanel.getWidth(),
                contentPanel.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        contentPanel.paint(resultsImage.createGraphics());
        final String nowStr = String.format(DT_FORMAT, System.currentTimeMillis());
        final String fileName = String.format("%s%sromraiderADPT_%s.png",
                SettingsManager.getSettings().getLoggerOutputDirPath(),
                File.separator,
                nowStr);
        final String shortName = FormatFilename.getShortName(fileName);
        try {
            final File imageFile = new File(fileName);
            ImageIO.write(
                    resultsImage,
                    "png",
                    imageFile);
            showMessageDialog(
                    null,
                    "Adaptation Table Values image saved to: " + shortName,
                    "Save Success",
                    INFORMATION_MESSAGE);
        }
        catch (Exception e) {
            showMessageDialog(
                    null,
                    "Failed to save image, check path:\n" + fileName,
                    "Save Failed",
                    ERROR_MESSAGE);
        }
    }
}
