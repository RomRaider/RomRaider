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

package com.romraider.swing;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import com.romraider.maps.Table;
import com.romraider.maps.Table3D;
import com.romraider.util.ResourceUtil;

public class TableMenuBar extends JMenuBar {

    private static final long serialVersionUID = -695692646459410510L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            TableMenuBar.class.getName());
    private JMenu fileMenu = new JMenu(rb.getString("TABLE"));
    private JMenuItem graph = new JMenuItem(rb.getString("VIEWGRAPH"));

    private JMenu compareMenu = new JMenu(rb.getString("COMPARE"));
    private JRadioButtonMenuItem compareOriginal = new JRadioButtonMenuItem(rb.getString("SHOWCHNG"));
    private JRadioButtonMenuItem compareMap = new JRadioButtonMenuItem(rb.getString("COMPAREMAP"));
    private JMenu similarOpenTables = new JMenu(rb.getString("COMPARETBL"));
    private JRadioButtonMenuItem compareOff = new JRadioButtonMenuItem(rb.getString("OFF"));
    private JMenu compareDisplay = new JMenu(rb.getString("DISPLAY"));
    private JRadioButtonMenuItem comparePercent = new JRadioButtonMenuItem(rb.getString("PERDIFF"));
    private JRadioButtonMenuItem compareAbsolute = new JRadioButtonMenuItem(rb.getString("ABSDIFF"));
    private JMenu compareToValue = new JMenu(rb.getString("COMAPRETO"));
    private JRadioButtonMenuItem compareToOriginal = new JRadioButtonMenuItem(rb.getString("COMPARETOOV"));
    private JRadioButtonMenuItem compareToBin = new JRadioButtonMenuItem(rb.getString("COMPARETOBV"));

    private JMenuItem close;
    private JMenu editMenu = new JMenu(rb.getString("EDIT"));
    private JMenuItem undoSel = new JMenuItem(rb.getString("UNDOCHNGS"));
    private JMenuItem undoAll = new JMenuItem(rb.getString("UNDOALL"));
    private JMenuItem revert = new JMenuItem(rb.getString("SETREVERT"));
    private JMenuItem copySel = new JMenuItem(rb.getString("COPYSEL"));
    private JMenuItem copyTable = new JMenuItem(rb.getString("COPYTBL"));
    private JMenuItem paste = new JMenuItem(rb.getString("PASTE"));
    private JMenuItem interp = new JMenuItem(rb.getString("INTERP"));
    private JMenuItem vertInterp = new JMenuItem(rb.getString("VINTERP"));
    private JMenuItem horizInterp = new JMenuItem(rb.getString("HINTERP"));
    private JMenu viewMenu = new JMenu(rb.getString("VIEW"));
    private JMenuItem tableProperties = new JMenuItem(rb.getString("TBLPROP"));

    private ButtonGroup compareGroup;
    private ButtonGroup compareDisplayGroup;
    private ButtonGroup compareToGroup;

    public TableMenuBar(TableFrame frame) {
        initFileMenu(frame);
        initEditMenu(frame);
        initViewMenu(frame);
        applyTableTypeRules(frame);
    }

    private void initFileMenu(TableFrame frame) {
        close = new JMenuItem(rb.getString("CLOSETBL"));

        initCompareMenu(frame);
        getClose().setText(MessageFormat.format(
                rb.getString("CLOSEFRM"), frame.getTable().getName()));

        graph.addActionListener(frame);
        getClose().addActionListener(frame);

        graph.setMnemonic('G');
        getClose().setMnemonic('X');
        graph.setEnabled(false);

        fileMenu.add(graph);
        fileMenu.add(compareMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(getClose());

        this.add(fileMenu);
    }

    private void initEditMenu(TableFrame frame) {
        editMenu.add(getUndoSel());
        editMenu.add(getUndoAll());
        editMenu.add(getRevert());
        editMenu.add(new JSeparator());
        editMenu.add(getCopySel());
        editMenu.add(getCopyTable());
        editMenu.add(new JSeparator());
        editMenu.add(getPaste());
        editMenu.add(new JSeparator());
        if (frame.getTable() instanceof Table3D) {
            editMenu.add(getVertInterp());
            editMenu.add(getHorizInterp());
        }
        editMenu.add(getInterp());

        editMenu.setMnemonic('E');
        getUndoSel().setMnemonic('U');
        getUndoAll().setMnemonic('A');
        getRevert().setMnemonic('R');
        getCopySel().setMnemonic('C');
        getCopyTable().setMnemonic('T');
        getPaste().setMnemonic('P');
        getInterp().setMnemonic('I');
        getVertInterp().setMnemonic('V');
        getHorizInterp().setMnemonic('H');

        getUndoSel().addActionListener(frame);
        getUndoAll().addActionListener(frame);
        getRevert().addActionListener(frame);
        getCopySel().addActionListener(frame);
        getCopyTable().addActionListener(frame);
        getPaste().addActionListener(frame);
        getInterp().addActionListener(frame);
        getVertInterp().addActionListener(frame);
        getHorizInterp().addActionListener(frame);
        this.add(editMenu);
    }

    private void initViewMenu(TableFrame frame) {
        viewMenu.setMnemonic('V');

        getTableProperties().setToolTipText(rb.getString("TBLPROPTT"));
        getTableProperties().setMnemonic('P');
        getTableProperties().addActionListener(frame);

        fileMenu.setMnemonic('F');
        fileMenu.setMnemonic('T');

        viewMenu.add(getTableProperties());

        this.add(viewMenu);
    }

    private void initCompareMenu(TableFrame frame) {
        getCompareOriginal().setToolTipText(rb.getString("COMPARETT"));
        getCompareMap().setToolTipText(rb.getString("COMPARETBLTT"));
        getSimilarOpenTables().setToolTipText(rb.getString("COMPARESIMTT"));
        getSimilarOpenTables().setEnabled(false);
        
        compareDisplayGroup = new ButtonGroup();
        compareDisplayGroup.add(getComparePercent());
        compareDisplayGroup.add(getCompareAbsolute());
        compareDisplay.add(getComparePercent());
        compareDisplay.add(getCompareAbsolute());

        getCompareToOriginal().setToolTipText(rb.getString("COMPARETHIS"));
        getCompareToBin().setToolTipText(rb.getString("COMPARECURRENT"));
        compareToGroup = new ButtonGroup();
        compareToGroup.add(getCompareToOriginal());
        compareToGroup.add(getCompareToBin());
        compareToValue.add(getCompareToOriginal());
        compareToValue.add(getCompareToBin());

        compareMenu.add(getCompareOriginal());
        compareMenu.add(getCompareMap());
        compareMenu.add(getSimilarOpenTables());
        compareMenu.add(getCompareOff());
        compareMenu.add(new JSeparator());
        compareMenu.add(compareDisplay);
        compareMenu.add(new JSeparator());
        compareMenu.add(compareToValue);

        compareMenu.setMnemonic('C');
        getCompareOriginal().setMnemonic('C');
        getCompareMap().setMnemonic('M');
        getCompareOff().setMnemonic('O');
        compareDisplay.setMnemonic('D');
        getComparePercent().setMnemonic('P');
        getCompareAbsolute().setMnemonic('A');
        getSimilarOpenTables().setMnemonic('S');
        compareToValue.setMnemonic('T');
        getCompareToOriginal().setMnemonic('R');
        getCompareToOriginal().setMnemonic('B');

        getCompareOff().setSelected(true);
        getCompareAbsolute().setSelected(true);
        getCompareToOriginal().setSelected(true);

        getCompareOriginal().addActionListener(frame);
        getCompareMap().addActionListener(frame);
        getCompareOff().addActionListener(frame);
        getComparePercent().addActionListener(frame);
        getCompareAbsolute().addActionListener(frame);
        getCompareToOriginal().addActionListener(frame);
        getCompareToBin().addActionListener(frame);
    }

    public void initCompareGroup(TableFrame frame) {
        compareGroup = new ButtonGroup();

        compareGroup.add(getCompareOriginal());
        compareGroup.add(getCompareMap());
        compareGroup.add(getCompareOff());
            
        for(int i = 0; i< getSimilarOpenTables().getItemCount(); i++) {
            compareGroup.add(getSimilarOpenTables().getItem(i));
        }
    }

    private void applyTableTypeRules(TableFrame frame) {
        // Hide items that don't work with a DTC tables.
        if(frame.getTable().getType() == Table.TableType.SWITCH) {
            editMenu.setEnabled(false);
            getCompareOriginal().setEnabled(false);
            getComparePercent().setEnabled(false);
            getCompareAbsolute().setEnabled(false);
            getCompareToOriginal().setEnabled(false);
            getCompareToBin().setEnabled(false);
        }
    }

    public JMenuItem getUndoAll() {
        return undoAll;
    }

    public JMenuItem getRevert() {
        return revert;
    }

    public JMenuItem getUndoSel() {
        return undoSel;
    }

    public JMenuItem getClose() {
        return close;
    }

    public JMenuItem getTableProperties() {
        return tableProperties;
    }

    public JMenuItem getCopySel() {
        return copySel;
    }

    public JMenuItem getCopyTable() {
        return copyTable;
    }

    public JMenuItem getPaste() {
        return paste;
    }

    public JRadioButtonMenuItem getCompareOff() {
        return compareOff;
    }

    public JRadioButtonMenuItem getCompareAbsolute() {
        return compareAbsolute;
    }

    public JRadioButtonMenuItem getComparePercent() {
        return comparePercent;
    }

    public JRadioButtonMenuItem getCompareOriginal() {
        return compareOriginal;
    }

    public JRadioButtonMenuItem getCompareToOriginal() {
        return compareToOriginal;
    }

    public JRadioButtonMenuItem getCompareMap() {
        return compareMap;
    }

    public JRadioButtonMenuItem getCompareToBin() {
        return compareToBin;
    }

    public JMenu getSimilarOpenTables() {
        return similarOpenTables;
    }

    public JMenuItem getInterp() {
        return interp;
    }

    public JMenuItem getHorizInterp() {
        return this.horizInterp;
    }

    public JMenuItem getVertInterp() {
        return this.vertInterp;
    }

}