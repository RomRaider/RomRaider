/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2019 RomRaider.com
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

import static com.romraider.Settings.COMMA;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
import com.romraider.logger.ecu.ui.swing.tools.tablemodels.FineLearningKnockCorrectionTableModel;
import com.romraider.logger.ecu.ui.swing.tools.tablemodels.VehicleInformationTableModel;
import com.romraider.logger.ecu.ui.swing.tools.tablemodels.renderers.CentreRenderer;
import com.romraider.logger.ecu.ui.swing.tools.tablemodels.renderers.LtvCellRenderer;
import com.romraider.logger.ecu.ui.swing.vertical.VerticalLabelUI;
import com.romraider.swing.SetFont;
import com.romraider.util.FormatFilename;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

/**
 * This class is used to build and display the Learning Table Values
 * retrieved from the ECU.
 */
public class NCSLearningTableValuesResultsPanel extends JDialog {
    private static final long serialVersionUID = 6716454297236022709L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            NCSLearningTableValuesResultsPanel.class.getName());
    private final String DIALOG_TITLE = rb.getString("DIALOGTITLE");
    private final String DT_FORMAT = "%1$tY%1$tm%1$td-%1$tH%1$tM%1$tS";
    private final int LTV_WIDTH = 720;
    private final int LTV_HEIGHT = 450;
    private final JPanel contentPanel = new JPanel();
    private JTable vehicleInfoTable;
    //TODO: replace AF learning with a knock table
    private JTable afLearningTable;
    private List<JTable> ltftTables = new ArrayList<JTable>();
    private final String LTFT_NAME = rb.getString("LTFTNAME");

    public NCSLearningTableValuesResultsPanel(
            EcuLogger logger,
            Map<String, Object> vehicleInfo,
            String[] afRanges,
            List<List<Object>> afLearning,
            String[] ltftCol,
            String[] ltftRow,
            List<List<List<EcuQuery>>> ltftData) {

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
//        contentPanel.add(buildAfLearningPanel(afRanges, afLearning));
        contentPanel.add(buildLtftPanel(ltftCol, ltftRow, ltftData));

        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buildSaveReultsPanel(), BorderLayout.SOUTH);
    }

    /**
     * This method is called to display the Learning Table Values
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
                BorderFactory.createTitledBorder(rb.getString("VITITLE")));
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

    private final JPanel buildLtftPanel(
            String[] ltftLoad,
            String[] ltftRpm,
            List<List<List<EcuQuery>>> ltftQueryTables) {

        final JPanel ltftTitlePanel = new JPanel();
        ltftTitlePanel.setBorder(
                new TitledBorder(null,
                        LTFT_NAME,
                        TitledBorder.LEADING,
                        TitledBorder.TOP, null, null));
        ltftTitlePanel.setBounds(10, 72, 692, 226);
        ltftTitlePanel.setLayout(new BorderLayout(0, 0));

        final JLabel xLabel = new JLabel(rb.getString("INJPWLABEL"));
        SetFont.plain(xLabel);
        xLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ltftTitlePanel.add(xLabel, BorderLayout.NORTH);

        final JLabel yLabel = new JLabel(rb.getString("ENGINESPEED"));
        SetFont.plain(yLabel);
        yLabel.setUI(new VerticalLabelUI(false));
        ltftTitlePanel.add(yLabel, BorderLayout.WEST);

        final JTabbedPane tabs = new JTabbedPane();
        for (int i = 0; i < ltftQueryTables.size(); i++) {
            final FineLearningKnockCorrectionTableModel tableModel =
                    new FineLearningKnockCorrectionTableModel();
            tableModel.setColumnHeadings(ltftLoad);
            tableModel.setRomHeadings(ltftRpm);
            tableModel.setFlkcData(ltftQueryTables.get(i));
            final JTable ltftTable = new JTable(tableModel);
            ltftTables.add(ltftTable);
            setTableBehaviour(ltftTable);
            formatTableHeader(ltftTable);
            final JScrollPane ltftTablePanel = new JScrollPane(ltftTable);
            ltftTablePanel.setBorder(
                    new EtchedBorder(EtchedBorder.LOWERED, null, null));
            switch (i) {
                case 0:
                    tabs.addTab(rb.getString("LBTRIM"), ltftTablePanel);
                    break;
                case 1:
                    tabs.addTab(rb.getString("RBTRIM"), ltftTablePanel);
                    break;
                case 2:
                    tabs.addTab(rb.getString("LBCOUNT"), ltftTablePanel);
                    break;
                case 3:
                    tabs.addTab(rb.getString("RBCOUNT"), ltftTablePanel);
                    break;
            }
        }
        if (ltftQueryTables.size() > 0) {
            ltftTitlePanel.add(tabs, BorderLayout.CENTER);
        }
        else {
            ltftTitlePanel.removeAll();
            ltftTitlePanel.add(new JLabel(
                    rb.getString("NOKNDATA")),
                    BorderLayout.CENTER);
        }
        return ltftTitlePanel;
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
        final JButton toFile = new JButton(rb.getString("SAVETOFILE"));
        toFile.setToolTipText(rb.getString("SAVETOFILETT"));
        toFile.setMnemonic(KeyEvent.VK_F);
        toFile.addActionListener(new ActionListener() {
            @Override
            public final void actionPerformed(ActionEvent actionEvent) {
                saveTableText();
            }
        });
        final JButton toImage = new JButton(rb.getString("SAVEASIMAGE"));
        toImage.setToolTipText(rb.getString("SAVEASIMAGETT"));
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
        final String fileName = String.format("%s%sromraiderLTV_%s.csv",
                SettingsManager.getSettings().getLoggerOutputDirPath(),
                File.separator,
                nowStr);
        try {
            final File csvFile = new File(fileName);
            final String EOL = System.getProperty("line.separator");
            final BufferedWriter bw = new BufferedWriter(
                    new FileWriter(csvFile));
            bw.write(rb.getString("DIALOGTITLE") + EOL);
            Object result = 0;
            int columnCount = vehicleInfoTable.getColumnCount();
            for (int i = 0; i < columnCount; i++ ) {
                result = vehicleInfoTable.getTableHeader().getColumnModel().
                        getColumn(i).getHeaderValue();
                bw.append(result.toString());
                bw.append(COMMA);
            }
            bw.append(EOL);
            for (int i = 0; i < columnCount; i++ ) {
                result = vehicleInfoTable.getValueAt(0, i);
                bw.append(result.toString());
                bw.append(COMMA);
            }
            bw.append(EOL + EOL);
//            bw.write("A/F Adaptation (Stored)" + EOL);
//            columnCount = afLearningTable.getColumnCount();
//            int rowCount = afLearningTable.getRowCount();
//            for (int i = 0; i < columnCount; i++) {
//                result = afLearningTable.getTableHeader().getColumnModel().
//                        getColumn(i).getHeaderValue();
//                bw.append(result.toString());
//                bw.append(COMMA);
//            }
//            bw.append(EOL);
//            for (int i = 0; i < rowCount; i++) {
//                for (int j = 0; j < columnCount; j++) {
//                    result = afLearningTable.getValueAt(i, j);
//                    bw.append(result.toString());
//                    bw.append(COMMA);
//                }
//                bw.append(EOL);
//            }
            bw.append(EOL);
            bw.write(LTFT_NAME + EOL);
            int k = 1;
            for (JTable ltftTable : ltftTables) {
                columnCount = ltftTable.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    result = ltftTable.getTableHeader().getColumnModel().
                            getColumn(i).getHeaderValue();
                    if (result.toString().equals(" ")) {
                        switch (k) {
                            case 1:
                                result = rb.getObject("LBTRIM");
                                break;
                            case 2:
                                result = rb.getObject("RBTRIM");
                                break;
                            case 3:
                                result = rb.getObject("LBCOUNT");
                                break;
                            case 4:
                                result = rb.getObject("RBCOUNT");
                                break;
                        }
                    }
                    bw.append(result.toString());
                    bw.append(COMMA);
                }
                k++;
            }
            bw.append(EOL);
            int rowCount = ltftTables.get(0).getRowCount();
            for (int i = 0; i < rowCount; i++) {
                for (JTable ltftTable : ltftTables) {
                    columnCount = ltftTable.getColumnCount();
                    for (int j = 0; j < columnCount; j++) {
                        result = ltftTable.getValueAt(i, j);
                        bw.append(result.toString());
                        bw.append(COMMA);
                    }
                }
                bw.append(EOL);
            }
            bw.close();
            final String shortName = FormatFilename.getShortName(fileName);
            showMessageDialog(
                    null,
                    MessageFormat.format(
                            rb.getString("TABLESAVED"), shortName),
                    rb.getString("SUCCESS"),
                    INFORMATION_MESSAGE);
        }
        catch (Exception e) {
            showMessageDialog(
                    null,
                    MessageFormat.format(
                            rb.getString("SAVEFAILED"), fileName),
                    rb.getString("FAILED"),
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
        final String fileName = String.format("%s%sromraiderLTV_%s.png",
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
                    MessageFormat.format(
                            rb.getString("IMAGESAVED"), shortName),
                    rb.getString("SUCCESS"),
                    INFORMATION_MESSAGE);
        }
        catch (Exception e) {
            showMessageDialog(
                    null,
                    MessageFormat.format(
                            rb.getString("IMAGEFAILED"), fileName),
                    rb.getString("FAILED"),
                    ERROR_MESSAGE);
        }
    }
}
