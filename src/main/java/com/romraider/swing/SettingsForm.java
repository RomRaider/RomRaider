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

package com.romraider.swing;

import static com.romraider.Version.PRODUCT_NAME;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

import ZoeloeSoft.projects.JFontChooser.JFontChooser;

public class SettingsForm extends JFrame implements MouseListener {

    private static final long serialVersionUID = 3910602424260147767L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            SettingsForm.class.getName());

    public SettingsForm() {
        this.setIconImage(getEditor().getIconImage());
        initComponents();
        initSettings();

        maxColor.addMouseListener(this);
        minColor.addMouseListener(this);
        highlightColor.addMouseListener(this);
        selectColor.addMouseListener(this);
        axisColor.addMouseListener(this);
        increaseColor.addMouseListener(this);
        decreaseColor.addMouseListener(this);
        warningColor.addMouseListener(this);
        liveValueColor.addMouseListener(this);
        curLiveValueColor.addMouseListener(this);

        btnOk.addMouseListener(this);
        btnApply.addMouseListener(this);
        btnCancel.addMouseListener(this);
        btnChooseFont.addMouseListener(this);
        reset.addMouseListener(this);

        tableClickCount.setBackground(Color.WHITE);
        tableClickBehavior.setBackground(Color.WHITE);
    }

    private void initSettings() {
        Settings settings = getSettings();
        obsoleteWarning.setSelected(settings.isObsoleteWarning());
        oldLocale = settings.getLocale();
        localeFormatCheckBox.setSelected(oldLocale.equals("en_US"));
        calcConflictWarning.setSelected(settings.isCalcConflictWarning());
        displayHighTables.setSelected(settings.isDisplayHighTables());
        saveDebugTables.setSelected(settings.isSaveDebugTables());
        debug.setSelected(settings.isDebug());

        maxColor.setBackground(settings.getMaxColor());
        minColor.setBackground(settings.getMinColor());
        highlightColor.setBackground(settings.getHighlightColor());
        selectColor.setBackground(settings.getSelectColor());
        axisColor.setBackground(settings.getAxisColor());
        increaseColor.setBackground(settings.getIncreaseBorder());
        decreaseColor.setBackground(settings.getDecreaseBorder());
        liveValueColor.setBackground(settings.getliveValueColor());
        liveValueColor.setToolTipText(rb.getString("CLROVERLAYV"));
        curLiveValueColor.setBackground(settings.getCurLiveValueColor());
        curLiveValueColor.setToolTipText(rb.getString("CLROVERLAYLIVE"));

        cellWidth.setText(((int) settings.getCellSize().getWidth()) + "");
        cellHeight.setText(((int) settings.getCellSize().getHeight()) + "");

        btnChooseFont.setFont(settings.getTableFont());
        btnChooseFont.setText(settings.getTableFont().getFontName());

        if (settings.getTableClickCount() == 1) { // single click opens table
            tableClickCount.setSelectedIndex(0);
        } else { // double click opens table
            tableClickCount.setSelectedIndex(1);
        }

        if(1 == settings.getTableClickBehavior()) { // open/focus
            tableClickBehavior.setSelectedIndex(1);
        } else { // open/close
            tableClickBehavior.setSelectedIndex(0);
        }
        chckbxSortTree.setSelected(settings.isTableTreeSorted());

        valueLimitWarning.setSelected(settings.isValueLimitWarning());
        warningColor.setBackground(settings.getWarningColor());
        chckbxColorAxis.setSelected(settings.isColorAxis());

        defaultScale.setText(settings.getDefaultScale());

        if(settings.getTableClipboardFormat().equalsIgnoreCase(Settings.AIRBOYS_CLIPBOARD_FORMAT)) {
            this.rdbtnAirboys.setSelected(true);
        } else if(settings.getTableClipboardFormat().equalsIgnoreCase(Settings.CUSTOM_CLIPBOARD_FORMAT)) {
            this.rdbtnCustom.setSelected(true);
        } else {
            this.rdbtnDefault.setSelected(true);
        }

        this.textFieldEditorIconScale.setText(String.valueOf(settings.getEditorIconScale()));
        this.textFieldTableIconScale.setText(String.valueOf(settings.getTableIconScale()));

        chckbxShowTableToolbar.setSelected(settings.isShowTableToolbarBorder());
        chckbxOpenRomNode.setSelected(settings.isOpenExpanded());
        chckbxOpenTablesAt.setSelected(settings.isAlwaysOpenTableAtZero());

        if(settings.getDefaultScale().equalsIgnoreCase(Settings.DEFAULT_SCALE)) {
            comboBoxDefaultScale.setSelectedItem(Settings.DEFAULT_SCALE);
        } else if (settings.getDefaultScale().equalsIgnoreCase(Settings.METRIC_SCALE)) {
            comboBoxDefaultScale.setSelectedItem(Settings.METRIC_SCALE);
        } else if (settings.getDefaultScale().equalsIgnoreCase(Settings.STANDARD_SCALE)) {
            comboBoxDefaultScale.setSelectedItem(Settings.STANDARD_SCALE);
        } else {
            comboBoxDefaultScale.setSelectedIndex(0);
        }

        cbScaleHeaderAndData.setSelected(settings.isScaleHeadersAndData());
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        obsoleteWarning = new javax.swing.JCheckBox();
        calcConflictWarning = new javax.swing.JCheckBox();
        localeFormatCheckBox = new javax.swing.JCheckBox();
        debug = new javax.swing.JCheckBox();
        btnCancel = new javax.swing.JButton();
        btnOk = new javax.swing.JButton();
        btnApply = new javax.swing.JButton();
        reset = new javax.swing.JButton();
        settingsTabbedPane = new javax.swing.JTabbedPane();
        jPanelClipboard = new javax.swing.JPanel();
        jPanelDefault = new javax.swing.JPanel();
        jPanelTableDisplay = new javax.swing.JPanel();
        jPanelIcons = new javax.swing.JPanel();
        jPanelScale = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblAxis = new javax.swing.JLabel();
        lblHighlight = new javax.swing.JLabel();
        lblSelect = new javax.swing.JLabel();
        lblMin = new javax.swing.JLabel();
        lblMax = new javax.swing.JLabel();
        maxColor = new javax.swing.JLabel();
        minColor = new javax.swing.JLabel();
        highlightColor = new javax.swing.JLabel();
        selectColor = new javax.swing.JLabel();
        axisColor = new javax.swing.JLabel();
        warningColor = new javax.swing.JLabel();
        lblWarning = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblIncrease = new javax.swing.JLabel();
        increaseColor = new javax.swing.JLabel();
        decreaseColor = new javax.swing.JLabel();
        lblDecrease = new javax.swing.JLabel();
        liveValueColor = new javax.swing.JLabel();
        lblLiveValue = new javax.swing.JLabel();
        curLiveValueColor = new javax.swing.JLabel();
        lblCurLiveValue = new javax.swing.JLabel();
        lblCellHeight = new javax.swing.JLabel();
        cellHeight = new javax.swing.JTextField();
        cellWidth = new javax.swing.JTextField();
        lblCellWidth = new javax.swing.JLabel();
        lblFont = new javax.swing.JLabel();
        btnChooseFont = new javax.swing.JButton();
        saveDebugTables = new javax.swing.JCheckBox();
        displayHighTables = new javax.swing.JCheckBox();
        valueLimitWarning = new javax.swing.JCheckBox();
        chckbxColorAxis = new javax.swing.JCheckBox();
        editorIconsPanel = new javax.swing.JPanel();
        tableIconsPanel = new javax.swing.JPanel();
        tableClickBehavior = new javax.swing.JComboBox();
        labelTableClick = new javax.swing.JLabel();
        tableClickCount = new javax.swing.JComboBox();
        chckbxSortTree = new javax.swing.JCheckBox();
        defaultScale = new javax.swing.JTextField();
        comboBoxDefaultScale = new javax.swing.JComboBox();
        cbScaleHeaderAndData = new javax.swing.JCheckBox();

        clipboardButtonGroup = new ButtonGroup();
        rdbtnDefault = new JRadioButton(rb.getString("RRDEFAULT"));
        rdbtnAirboys = new JRadioButton(rb.getString("ABSS"));
        rdbtnCustom = new JRadioButton(rb.getString("CUSTOM"));
        clipboardButtonGroup.add(this.rdbtnDefault);
        clipboardButtonGroup.add(this.rdbtnAirboys);
        clipboardButtonGroup.add(this.rdbtnCustom);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(MessageFormat.format(
                rb.getString("RRSETTINGS"), PRODUCT_NAME));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFont(new java.awt.Font("Tahoma", 0, 12));

        obsoleteWarning.setText(rb.getString("WARNOOD"));
        obsoleteWarning.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        obsoleteWarning.setMargin(new java.awt.Insets(0, 0, 0, 0));

        localeFormatCheckBox.setText(rb.getString("USNUMBER"));
        localeFormatCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        localeFormatCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        calcConflictWarning.setText(rb.getString("WARNREAL"));
        calcConflictWarning.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        calcConflictWarning.setMargin(new java.awt.Insets(0, 0, 0, 0));

        debug.setText(rb.getString("DEBUG"));
        debug.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        debug.setEnabled(false);
        debug.setMargin(new java.awt.Insets(0, 0, 0, 0));

        btnCancel.setMnemonic('C');
        btnCancel.setText(rb.getString("CANCEL"));

        btnOk.setMnemonic('O');
        btnOk.setText(rb.getString("OK"));

        btnApply.setMnemonic('A');
        btnApply.setText(rb.getString("APPLY"));

        reset.setText(rb.getString("RESTOREDEF"));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(
                rb.getString("BAKGND")));
        lblAxis.setText(rb.getString("AXIS"));

        lblHighlight.setText(rb.getString("HLTCELL"));
        lblSelect.setText(rb.getString("SLTDCELL"));

        lblMin.setText(rb.getString("MINVALUE"));

        lblMax.setText(rb.getString("MAXVALUE"));

        maxColor.setBackground(new java.awt.Color(255, 0, 0));
        maxColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        maxColor.setOpaque(true);

        minColor.setBackground(new java.awt.Color(255, 0, 0));
        minColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        minColor.setOpaque(true);

        highlightColor.setBackground(new java.awt.Color(255, 0, 0));
        highlightColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        highlightColor.setOpaque(true);

        selectColor.setBackground(new java.awt.Color(255, 0, 0));
        selectColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        selectColor.setOpaque(true);

        axisColor.setBackground(new java.awt.Color(255, 0, 0));
        axisColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        axisColor.setOpaque(true);

        warningColor.setBackground(new java.awt.Color(255, 0, 0));
        warningColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        warningColor.setOpaque(true);

        lblWarning.setText(rb.getString("WARNING"));

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(Alignment.TRAILING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(Alignment.TRAILING)
                                .addComponent(lblWarning)
                                .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGap(4)
                                                .addComponent(lblMin))
                                                .addComponent(lblMax)))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout.createParallelGroup(Alignment.TRAILING)
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(maxColor, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(ComponentPlacement.RELATED, 139, Short.MAX_VALUE)
                                                                .addComponent(lblHighlight)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(highlightColor, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
                                                                .addGroup(jPanel2Layout.createSequentialGroup()
                                                                        .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                                                                                .addGroup(Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                                                                        .addComponent(minColor, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                                                                                        .addPreferredGap(ComponentPlacement.RELATED, 172, Short.MAX_VALUE)
                                                                                        .addComponent(lblAxis))
                                                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                                                .addComponent(warningColor, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(ComponentPlacement.RELATED, 151, Short.MAX_VALUE)
                                                                                                .addComponent(lblSelect)))
                                                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                                                .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                                                                                                        .addComponent(selectColor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                                        .addComponent(axisColor, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))))
                                                                                                        .addContainerGap())
                );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblMax)
                                .addComponent(maxColor, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                                .addComponent(highlightColor, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblHighlight))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblMin)
                                        .addComponent(minColor, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(axisColor, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblAxis))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                                                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(warningColor, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblWarning))
                                                        .addComponent(selectColor, GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                                                        .addComponent(lblSelect))
                                                        .addContainerGap())
                );
        jPanel2.setLayout(jPanel2Layout);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(rb.getString("CELLBDR")));
        lblIncrease.setText(rb.getString("INCRSD"));

        increaseColor.setBackground(new java.awt.Color(255, 0, 0));
        increaseColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        increaseColor.setOpaque(true);

        decreaseColor.setBackground(new java.awt.Color(255, 0, 0));
        decreaseColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        decreaseColor.setOpaque(true);

        lblDecrease.setText(rb.getString("DECRSD"));

        liveValueColor.setBackground(new java.awt.Color(255, 0, 0));
        liveValueColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        liveValueColor.setOpaque(true);

        lblLiveValue.setText(rb.getString("STLLIVE"));

        curLiveValueColor.setBackground(new java.awt.Color(255, 0, 0));
        curLiveValueColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        curLiveValueColor.setOpaque(true);

        lblCurLiveValue.setText(rb.getString("CNTLIVE"));

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(Alignment.TRAILING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addContainerGap()

                    .addGroup(jPanel3Layout.createParallelGroup(Alignment.TRAILING)
                        .addComponent(lblLiveValue)
                        .addComponent(lblIncrease))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(jPanel3Layout.createParallelGroup(Alignment.TRAILING)
                        .addComponent(increaseColor, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                        .addComponent(liveValueColor, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                            .addComponent(lblCurLiveValue)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(curLiveValueColor, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
                        .addGroup(Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                            .addComponent(lblDecrease)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(decreaseColor, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                            .addComponent(decreaseColor, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDecrease)
                            .addComponent(lblIncrease))
                        .addComponent(increaseColor, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblLiveValue)
                        .addComponent(liveValueColor, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblCurLiveValue)
                        .addComponent(curLiveValueColor, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)))
        );

        lblCellHeight.setText(rb.getString("CELLHT"));

        lblCellWidth.setText(rb.getString("CELLWD"));

        lblFont.setText(rb.getString("FONT"));

        btnChooseFont.setText(rb.getString("CHOOSE"));

        saveDebugTables.setText(rb.getString("SAVEDEBUG"));
        saveDebugTables.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        saveDebugTables.setMargin(new java.awt.Insets(0, 0, 0, 0));

        displayHighTables.setText(rb.getString("LISTTBLS"));
        displayHighTables.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        displayHighTables.setMargin(new java.awt.Insets(0, 0, 0, 0));

        valueLimitWarning.setText(rb.getString("WARNUSER"));
        valueLimitWarning.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        valueLimitWarning.setMargin(new java.awt.Insets(0, 0, 0, 0));

        chckbxColorAxis.setText(rb.getString("CLRAXIS"));
        chckbxColorAxis.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chckbxColorAxis.setMargin(new java.awt.Insets(0, 0, 0, 0));

        initTabs();

        settingsTabbedPane.addTab(rb.getString("TABGEN"), jPanelDefault);
        settingsTabbedPane.addTab(rb.getString("TABTABL"), jPanelTableDisplay);
        settingsTabbedPane.addTab(rb.getString("TABCLIP"), jPanelClipboard);
        settingsTabbedPane.addTab(rb.getString("TABICON"), jPanelIcons);
        settingsTabbedPane.addTab(rb.getString("TABSCAL"), jPanelScale);

        this.cbScaleHeaderAndData = new JCheckBox(rb.getString("SCLHEADER"));
        this.cbScaleHeaderAndData.setToolTipText(rb.getString("SCLHEADERTT"));

        JLabel lblDefaultScale = new JLabel(rb.getString("DEFSCALE"));

        comboBoxDefaultScale.setModel(new DefaultComboBoxModel(new String[] { Settings.DEFAULT_SCALE, Settings.METRIC_SCALE, Settings.STANDARD_SCALE}));

        GroupLayout gl_jPanelScale = new GroupLayout(jPanelScale);
        gl_jPanelScale.setHorizontalGroup(
                gl_jPanelScale.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_jPanelScale.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(gl_jPanelScale.createParallelGroup(Alignment.LEADING)
                                .addComponent(cbScaleHeaderAndData)
                                .addGroup(gl_jPanelScale.createSequentialGroup()
                                        .addComponent(lblDefaultScale)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(comboBoxDefaultScale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                        .addContainerGap(277, Short.MAX_VALUE))
                );
        gl_jPanelScale.setVerticalGroup(
                gl_jPanelScale.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_jPanelScale.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(gl_jPanelScale.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblDefaultScale)
                                .addComponent(comboBoxDefaultScale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(cbScaleHeaderAndData)
                                .addContainerGap(453, Short.MAX_VALUE))
                );
        jPanelScale.setLayout(gl_jPanelScale);

        editorIconsPanel = new JPanel();

        // Content Pane Layout
        GroupLayout layout = new GroupLayout(getContentPane());
        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(settingsTabbedPane, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 432, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(reset)
                                        .addPreferredGap(ComponentPlacement.RELATED, 136, Short.MAX_VALUE)
                                        .addComponent(btnApply)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnOk)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnCancel)))
                                        .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(settingsTabbedPane, GroupLayout.PREFERRED_SIZE, 542, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnCancel)
                                .addComponent(btnOk)
                                .addComponent(btnApply)
                                .addComponent(reset))
                                .addContainerGap())
                );
        getContentPane().setLayout(layout);
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initTabs() {

        JPanel panelUISettings = new JPanel();
        panelUISettings.setBorder(new TitledBorder(UIManager.getBorder(
                "TitledBorder.border"),
                rb.getString("UISETTINGS"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        // Init Default Tab Panel
        GroupLayout jPanelDefaultLayout = new GroupLayout(jPanelDefault);
        jPanelDefaultLayout.setHorizontalGroup(
                jPanelDefaultLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(jPanelDefaultLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelDefaultLayout.createParallelGroup(Alignment.LEADING)
                                .addComponent(panelUISettings, GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                                .addComponent(obsoleteWarning)
                                .addComponent(calcConflictWarning)
                                .addComponent(localeFormatCheckBox)
                                .addComponent(debug))
                                .addContainerGap())
                );
        jPanelDefaultLayout.setVerticalGroup(
                jPanelDefaultLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(jPanelDefaultLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(obsoleteWarning)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(calcConflictWarning)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(localeFormatCheckBox)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(debug)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(panelUISettings, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(267, Short.MAX_VALUE))
                );
        chckbxOpenRomNode = new JCheckBox(rb.getString("ORNE"));

        chckbxShowTableToolbar = new JCheckBox(rb.getString("STTB"));

        panelTreeSettings = new JPanel();
        panelTreeSettings.setBorder(new TitledBorder(null, rb.getString("RTS"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));

        chckbxOpenTablesAt = new JCheckBox(rb.getString("AOA00"));
        GroupLayout gl_panelUISettings = new GroupLayout(panelUISettings);
        gl_panelUISettings.setHorizontalGroup(
                gl_panelUISettings.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelUISettings.createSequentialGroup()
                        .addGroup(gl_panelUISettings.createParallelGroup(Alignment.LEADING)
                                .addComponent(chckbxOpenRomNode)
                                .addComponent(chckbxShowTableToolbar))
                                .addContainerGap(244, Short.MAX_VALUE))
                                .addComponent(panelTreeSettings, GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                                .addGroup(gl_panelUISettings.createSequentialGroup()
                                        .addComponent(chckbxOpenTablesAt)
                                        .addContainerGap())
                );
        gl_panelUISettings.setVerticalGroup(
                gl_panelUISettings.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelUISettings.createSequentialGroup()
                        .addComponent(chckbxOpenRomNode)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(chckbxShowTableToolbar)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(chckbxOpenTablesAt)
                        .addPreferredGap(ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                        .addComponent(panelTreeSettings, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE))
                );

        labelTableClick.setText(rb.getString("CTOT"));
        tableClickCount.setModel(new javax.swing.DefaultComboBoxModel(
                new String[]{rb.getString("SINGLE"), rb.getString("DOUBLE")}));
        tableClickBehavior.setModel(new DefaultComboBoxModel(
                new String[] {rb.getString("OPENCLOSE"), rb.getString("OPENFOCUS")}));
        chckbxSortTree.setText(rb.getString("STLABEL"));
        chckbxSortTree.setToolTipText(rb.getString("STLABELTT"));

        lblClickBehavior = new JLabel(rb.getString("TCB"));
        GroupLayout gl_panelTreeSettings = new GroupLayout(panelTreeSettings);
        gl_panelTreeSettings.setHorizontalGroup(
                gl_panelTreeSettings.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelTreeSettings.createSequentialGroup()
                        .addGroup(gl_panelTreeSettings.createParallelGroup(Alignment.LEADING)
                                .addComponent(tableClickCount, 0, 72, Short.MAX_VALUE)
                                .addComponent(tableClickBehavior, 0, 86, Short.MAX_VALUE)

                                )
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_panelTreeSettings.createParallelGroup(Alignment.LEADING)
                                        .addComponent(labelTableClick)
                                        .addComponent(lblClickBehavior)
                                        .addGap(200)
                                        )
                        ).addComponent(chckbxSortTree, 0, 100, Short.MAX_VALUE)
        );
        gl_panelTreeSettings.setVerticalGroup(
                gl_panelTreeSettings.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelTreeSettings.createSequentialGroup()
                        .addGroup(gl_panelTreeSettings.createParallelGroup(Alignment.BASELINE)
                                .addComponent(labelTableClick)
                                .addComponent(tableClickCount, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
                                )
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_panelTreeSettings.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblClickBehavior)
                                        .addComponent(tableClickBehavior, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        )
                                        .addContainerGap(23, Short.MAX_VALUE)
                                        .addComponent(chckbxSortTree, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
                          )
                );
        panelTreeSettings.setLayout(gl_panelTreeSettings);
        panelUISettings.setLayout(gl_panelUISettings);
        jPanelDefault.setLayout(jPanelDefaultLayout);

        // Init Table Display Tab
        GroupLayout jPanelTableDisplayLayout = new GroupLayout(jPanelTableDisplay);
        jPanelTableDisplayLayout.setHorizontalGroup(
                jPanelTableDisplayLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(jPanelTableDisplayLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelTableDisplayLayout.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblCellHeight)
                                .addComponent(lblFont))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(jPanelTableDisplayLayout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(btnChooseFont)
                                        .addGroup(jPanelTableDisplayLayout.createSequentialGroup()
                                                .addComponent(cellHeight, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED, 155, Short.MAX_VALUE)
                                                .addComponent(lblCellWidth)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(cellWidth, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)))
                                                .addGap(47))
                                                .addGroup(jPanelTableDisplayLayout.createSequentialGroup()
                                                        .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                                                        .addContainerGap())
                                                        .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(jPanelTableDisplayLayout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(saveDebugTables)
                                                                .addContainerGap(184, Short.MAX_VALUE))
                                                                .addGroup(jPanelTableDisplayLayout.createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .addComponent(displayHighTables)
                                                                        .addContainerGap(214, Short.MAX_VALUE))
                                                                        .addGroup(jPanelTableDisplayLayout.createSequentialGroup()
                                                                                .addContainerGap()
                                                                                .addComponent(valueLimitWarning)
                                                                                .addContainerGap(246, Short.MAX_VALUE))
                                                                                .addGroup(jPanelTableDisplayLayout.createSequentialGroup()
                                                                                        .addContainerGap()
                                                                                        .addComponent(chckbxColorAxis)
                                                                                        .addContainerGap(324, Short.MAX_VALUE))
                );
        jPanelTableDisplayLayout.setVerticalGroup(
                jPanelTableDisplayLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(jPanelTableDisplayLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(saveDebugTables)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(displayHighTables)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(valueLimitWarning)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(chckbxColorAxis)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(jPanelTableDisplayLayout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblCellWidth)
                                .addComponent(cellWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblCellHeight)
                                .addComponent(cellHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(jPanelTableDisplayLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblFont)
                                        .addComponent(btnChooseFont, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap())
                );
        jPanelTableDisplay.setLayout(jPanelTableDisplayLayout);

        // Init Clipboard Tab Panel
        GroupLayout jPanelClipboardLayout = new GroupLayout(jPanelClipboard);
        jPanelClipboardLayout.setHorizontalGroup(
                jPanelClipboardLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(jPanelClipboardLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelClipboardLayout.createParallelGroup(Alignment.LEADING)
                                .addGroup(jPanelClipboardLayout.createSequentialGroup()
                                        .addGap(17)
                                        .addGroup(jPanelClipboardLayout.createParallelGroup(Alignment.LEADING)
                                                .addComponent(rdbtnAirboys)
                                                .addComponent(rdbtnDefault)
                                                .addComponent(rdbtnCustom))))
                                                .addGap(157))
                );
        jPanelClipboardLayout.setVerticalGroup(
                jPanelClipboardLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(jPanelClipboardLayout.createSequentialGroup()
                        .addContainerGap()
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(rdbtnDefault)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(rdbtnAirboys)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(rdbtnCustom)
                        .addGap(435))
                );
        jPanelClipboard.setLayout(jPanelClipboardLayout);

        // Init Icons Tab panel
        editorIconsPanel.setBorder(new TitledBorder(null, rb.getString("ETI"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        tableIconsPanel.setBorder(new TitledBorder(null, rb.getString("TTI"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));

        GroupLayout jPanelIconsLayout = new GroupLayout(jPanelIcons);
        jPanelIconsLayout.setHorizontalGroup(
                jPanelIconsLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(jPanelIconsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelIconsLayout.createParallelGroup(Alignment.LEADING)
                                .addComponent(editorIconsPanel, GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                                .addComponent(tableIconsPanel, GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE))
                                .addContainerGap())
                );
        jPanelIconsLayout.setVerticalGroup(
                jPanelIconsLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(jPanelIconsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(editorIconsPanel, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(tableIconsPanel, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(367, Short.MAX_VALUE))
                );

        JLabel lblTableIconScale = new JLabel(rb.getString("SCALE"));

        textFieldTableIconScale = new JTextField();
        textFieldTableIconScale.setToolTipText(rb.getString("SCALEPER"));
        textFieldTableIconScale.setColumns(10);

        JLabel labelTableScalePercent = new JLabel("%");
        GroupLayout tableIconsPanelLayout = new GroupLayout(tableIconsPanel);
        tableIconsPanelLayout.setHorizontalGroup(
                tableIconsPanelLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(tableIconsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblTableIconScale)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(textFieldTableIconScale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(labelTableScalePercent)
                        .addContainerGap(216, Short.MAX_VALUE))
                );
        tableIconsPanelLayout.setVerticalGroup(
                tableIconsPanelLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(tableIconsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(tableIconsPanelLayout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblTableIconScale)
                                .addComponent(textFieldTableIconScale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelTableScalePercent))
                                .addContainerGap(116, Short.MAX_VALUE))
                );
        tableIconsPanel.setLayout(tableIconsPanelLayout);

        JLabel lblEditorIconScale = new JLabel(rb.getString("SCALE"));

        textFieldEditorIconScale = new JTextField();
        textFieldEditorIconScale.setToolTipText(rb.getString("SCALEPER"));
        textFieldEditorIconScale.setColumns(10);

        JLabel labelEditorScalePercent = new JLabel("%");
        GroupLayout editorIconsPanelLayout = new GroupLayout(editorIconsPanel);
        editorIconsPanelLayout.setHorizontalGroup(
                editorIconsPanelLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(editorIconsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblEditorIconScale)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(textFieldEditorIconScale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(labelEditorScalePercent)
                        .addContainerGap(216, Short.MAX_VALUE))
                );
        editorIconsPanelLayout.setVerticalGroup(
                editorIconsPanelLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(editorIconsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(editorIconsPanelLayout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblEditorIconScale)
                                .addComponent(textFieldEditorIconScale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelEditorScalePercent))
                                .addContainerGap(95, Short.MAX_VALUE))
                );
        editorIconsPanel.setLayout(editorIconsPanelLayout);

        jPanelIcons.setLayout(jPanelIconsLayout);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Settings settings = getSettings();
        if (e.getSource() == maxColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    rb.getString("BGCOLOR"), settings.getMaxColor());
            if (color != null) {
                maxColor.setBackground(color);
            }
        } else if (e.getSource() == minColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    rb.getString("BGCOLOR"), settings.getMinColor());
            if (color != null) {
                minColor.setBackground(color);
            }
        } else if (e.getSource() == highlightColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    rb.getString("BGCOLOR"), settings.getHighlightColor());
            if (color != null) {
                highlightColor.setBackground(color);
            }
        } else if (e.getSource() == selectColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    rb.getString("BGCOLOR"), settings.getSelectColor());
            if (color != null) {
                selectColor.setBackground(color);
            }
        } else if (e.getSource() == axisColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    rb.getString("BGCOLOR"), settings.getAxisColor());
            if (color != null) {
                axisColor.setBackground(color);
            }
        } else if (e.getSource() == increaseColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    rb.getString("BGCOLOR"), settings.getIncreaseBorder());
            if (color != null) {
                increaseColor.setBackground(color);
            }
        } else if (e.getSource() == decreaseColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    rb.getString("BGCOLOR"), settings.getDecreaseBorder());
            if (color != null) {
                decreaseColor.setBackground(color);
            }
        } else if (e.getSource() == liveValueColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    rb.getString("BGCOLOR"), settings.getliveValueColor());
            if (color != null) {
                liveValueColor.setBackground(color);
            }
        } else if (e.getSource() == curLiveValueColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    rb.getString("BGCOLOR"), settings.getCurLiveValueColor());
            if (color != null) {
                curLiveValueColor.setBackground(color);
            }
        } else if (e.getSource() == warningColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    rb.getString("WARNCOLOR"), settings.getWarningColor());
            if (color != null) {
                warningColor.setBackground(color);
            }
        } else if (e.getSource() == btnApply) {
            applySettings();
        } else if (e.getSource() == btnOk) {
            // Apply settings to Settings object.
            applySettings();
            // Write settings to file.
            saveSettings();
            this.dispose();
        } else if (e.getSource() == btnCancel) {
            this.dispose();
        } else if (e.getSource() == btnChooseFont) {
            JFontChooser fc = new JFontChooser(this);
            fc.setLocationRelativeTo(this);
            if (fc.showDialog(settings.getTableFont()) == JFontChooser.OK_OPTION) {
                btnChooseFont.setFont(fc.getFont());
                btnChooseFont.setText(fc.getFont().getFontName());
            }
        } else if (e.getSource() == reset) {
            Settings newSettings = new Settings();
            Settings curSettings = getSettings();

            newSettings.setEcuDefinitionFiles(curSettings.getEcuDefinitionFiles());
            newSettings.setLastImageDir(curSettings.getLastImageDir());
            newSettings.setLastRepositoryDir(curSettings.getLastRepositoryDir());
            newSettings.setUserLevel(curSettings.getUserLevel());
            newSettings.setLoggerDefinitionFilePath(curSettings.getLoggerDefinitionFilePath());
            newSettings.setLoggerDebuggingLevel(curSettings.getLoggerDebuggingLevel());
            newSettings.setLoggerProfileFilePath(curSettings.getLoggerProfileFilePath());
            newSettings.setLoggerOutputDirPath(curSettings.getLoggerOutputDirPath());

            SettingsManager.save(newSettings);

            initSettings();
        }
    }

    public void applySettings() {
        try {
            Integer.parseInt(cellHeight.getText());
        } catch (NumberFormatException ex) {
            // number formatted imporperly, reset
            cellHeight.setText((int) (getSettings().getCellSize().getHeight()) + "");
        }
        try {
            Integer.parseInt(cellWidth.getText());
        } catch (NumberFormatException ex) {
            // number formatted improperly, reset
            cellWidth.setText((int) (getSettings().getCellSize().getWidth()) + "");
        }

        getSettings().setObsoleteWarning(obsoleteWarning.isSelected());

        //Apply locale settings
        if (localeFormatCheckBox.isSelected())
            getSettings().setLocale("en_US");
        else
            getSettings().setLocale("system");

        //Show Info if locale changed
        if(!oldLocale.equals(getSettings().getLocale())){
        showMessageDialog(this, MessageFormat.format(
                rb.getString("LOCALEMSG"),
                getSettings().getLocale()),
                rb.getString("LOCALETITLE"), INFORMATION_MESSAGE);
        }

        getSettings().setCalcConflictWarning(calcConflictWarning.isSelected());
        getSettings().setDisplayHighTables(displayHighTables.isSelected());
        getSettings().setSaveDebugTables(saveDebugTables.isSelected());
        getSettings().setDebug(debug.isSelected());
        getSettings().setOpenExpanded(chckbxOpenRomNode.isSelected());
        getSettings().setAlwaysOpenTableAtZero(chckbxOpenTablesAt.isSelected());
        getSettings().setShowTableToolbarBorder(chckbxShowTableToolbar.isSelected());

        getSettings().setMaxColor(maxColor.getBackground());
        getSettings().setMinColor(minColor.getBackground());
        getSettings().setHighlightColor(highlightColor.getBackground());
        getSettings().setSelectColor(selectColor.getBackground());
        getSettings().setAxisColor(axisColor.getBackground());
        getSettings().setIncreaseBorder(increaseColor.getBackground());
        getSettings().setDecreaseBorder(decreaseColor.getBackground());
        getSettings().setLiveValueColor(liveValueColor.getBackground());
        getSettings().setCurLiveValueColor(curLiveValueColor.getBackground());

        getSettings().setScaleHeadersAndData(this.cbScaleHeaderAndData.isSelected());

        getSettings().setCellSize(new Dimension(Integer.parseInt(cellWidth.getText()),
                Integer.parseInt(cellHeight.getText())));

        getSettings().setTableFont(btnChooseFont.getFont());

        if (tableClickCount.getSelectedIndex() == 0) { // single click opens table
            getSettings().setTableClickCount(1);
        } else { // double click opens table
            getSettings().setTableClickCount(2);
        }

        getEditor().getImageList().setToggleClickCount(getSettings().getTableClickCount());

        if(1 == tableClickBehavior.getSelectedIndex()) { // open/close frame
            getSettings().setTableClickBehavior(1);
        } else { // open/focus frame
            getSettings().setTableClickBehavior(0);
        }
        getSettings().setTableTreeSorted(chckbxSortTree.isSelected());

        getSettings().setValueLimitWarning(valueLimitWarning.isSelected());
        getSettings().setWarningColor(warningColor.getBackground());
        getSettings().setColorAxis(chckbxColorAxis.isSelected());
        getSettings().setDefaultScale(defaultScale.getText());

        if(rdbtnAirboys.isSelected())
        {
            getSettings().setAirboysFormat();
        } else if(rdbtnCustom.isSelected()) {
            getSettings().setTableClipboardFormat(Settings.CUSTOM_CLIPBOARD_FORMAT);
            // Table Header settings need to be manually edited in the settings.xml file;
        } else {
            getSettings().setDefaultFormat();
        }

        try{
            getSettings().setEditorIconScale(Integer.parseInt(textFieldEditorIconScale.getText()));
            getEditor().getToolBar().updateIcons();
        } catch(NumberFormatException ex) {
            // Number formatted incorrectly reset.
            textFieldEditorIconScale.setText(String.valueOf(getSettings().getEditorIconScale()));
        }

        try{
            getSettings().setTableIconScale(Integer.parseInt(textFieldTableIconScale.getText()));
            getEditor().getTableToolBar().updateIcons();
        } catch(NumberFormatException ex) {
            // Number formatted incorrectly reset.
            textFieldTableIconScale.setText(String.valueOf(getSettings().getTableIconScale()));
        }

        getSettings().setDefaultScale(comboBoxDefaultScale.getSelectedItem().toString());
    }

    private Settings getSettings()
    {
        return SettingsManager.getSettings();
    }

    private ECUEditor getEditor()
    {
        return ECUEditorManager.getECUEditor();
    }

    public void saveSettings()
    {
        saveSettings(getSettings());
    }

    public void saveSettings(Settings newSettings) {
        SettingsManager.save(newSettings);
        drawVisibleTables();
        getEditor().refreshUI();
    }

    private void drawVisibleTables() {
        for(JInternalFrame frame : getEditor().getRightPanel().getAllFrames()) {
            if(frame instanceof TableFrame && frame.isVisible()) {
                TableFrame tableFrame = (TableFrame) frame;
                tableFrame.getTable().getTableView().drawTable();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel axisColor;
    private javax.swing.JButton btnApply;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnChooseFont;
    private javax.swing.JButton btnOk;
    private javax.swing.JCheckBox calcConflictWarning;
    private javax.swing.JTextField cellHeight;
    private javax.swing.JTextField cellWidth;
    private javax.swing.JLabel curLiveValueColor;
    private javax.swing.JCheckBox debug;
    private javax.swing.JLabel decreaseColor;
    private javax.swing.JCheckBox displayHighTables;
    private javax.swing.JLabel highlightColor;
    private javax.swing.JLabel selectColor;
    private javax.swing.JLabel increaseColor;
    private javax.swing.JLabel labelTableClick;
    private javax.swing.JLabel lblClickBehavior;
    private javax.swing.JTabbedPane settingsTabbedPane;
    private javax.swing.JPanel jPanelDefault;
    private javax.swing.JPanel jPanelClipboard;
    private javax.swing.JPanel jPanelTableDisplay;
    private javax.swing.JPanel jPanelIcons;
    private javax.swing.JPanel jPanelScale;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblAxis;
    private javax.swing.JLabel lblCellHeight;
    private javax.swing.JLabel lblCellWidth;
    private javax.swing.JLabel lblCurLiveValue;
    private javax.swing.JLabel lblDecrease;
    private javax.swing.JLabel lblFont;
    private javax.swing.JLabel lblHighlight;
    private javax.swing.JLabel lblSelect;
    private javax.swing.JLabel lblIncrease;
    private javax.swing.JLabel lblLiveValue;
    private javax.swing.JLabel lblMax;
    private javax.swing.JLabel lblMin;
    private javax.swing.JLabel lblWarning;
    private javax.swing.JLabel liveValueColor;
    private javax.swing.JLabel maxColor;
    private javax.swing.JLabel minColor;
    private javax.swing.JCheckBox obsoleteWarning;
    private javax.swing.JButton reset;
    private javax.swing.JCheckBox saveDebugTables;
    private javax.swing.JComboBox tableClickCount;
    private javax.swing.JCheckBox valueLimitWarning;
    private javax.swing.JCheckBox chckbxColorAxis;
    private javax.swing.JLabel warningColor;
    private ButtonGroup clipboardButtonGroup;
    private javax.swing.JRadioButton rdbtnDefault;
    private javax.swing.JRadioButton rdbtnAirboys;
    private javax.swing.JRadioButton rdbtnCustom;
    private JPanel editorIconsPanel;
    private JPanel tableIconsPanel;
    private JTextField textFieldTableIconScale;
    private JTextField textFieldEditorIconScale;
    private javax.swing.JCheckBox chckbxShowTableToolbar;
    private javax.swing.JCheckBox chckbxOpenRomNode;
    private JPanel panelTreeSettings;
    private javax.swing.JComboBox tableClickBehavior;
    private javax.swing.JCheckBox chckbxOpenTablesAt;
    private javax.swing.JTextField defaultScale;
    private javax.swing.JComboBox comboBoxDefaultScale;
    private javax.swing.JCheckBox cbScaleHeaderAndData;
    private javax.swing.JCheckBox localeFormatCheckBox;
    private String oldLocale;
    private javax.swing.JCheckBox chckbxSortTree;
}
