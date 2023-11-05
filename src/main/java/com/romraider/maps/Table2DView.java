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

package com.romraider.maps;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.romraider.Settings;
import com.romraider.maps.Table1DView.Table1DType;

public class Table2DView extends TableView {
    private static final long serialVersionUID = -7684570967109324784L;
    private JLabel axisLabel;
    private Table1DView axis;

    public Table2DView(Table2D table) {
        super(table);
        axis = new Table1DView(table.getAxis(), Table1DType.X_AXIS);
        axis.setAxisParent(this);
        verticalOverhead += 18;
    }

    public Table1DView getAxis() {
        return axis;
    }

    public JLabel getAxisLabel() {
        return axisLabel;
    }

    public void setAxisLabel(JLabel label) {
        axisLabel = label;
    }

    @Override
    public String toString() {
        return super.toString() + " (2D)";// + axis;
    }

    @Override
    public Dimension getFrameSize() {
        int height = verticalOverhead + cellHeight * 2;
        int width = horizontalOverhead + data.length * cellWidth;
        if (height < minHeight) {
            height = minHeight;
        }
        int minWidth = table.isLiveDataSupported() ? minWidthOverlay : minWidthNoOverlay;
        if (width < minWidth) {
            width = minWidth;
        }
        return new Dimension(width, height);
    }

    @Override
    public void populateTableVisual(){
        super.populateTableVisual();

        centerLayout.setRows(2);
        centerLayout.setColumns(table.getDataSize());
        
        axis.populateTableVisual();
        // add to table
        for (int i = 0; i < table.getDataSize(); i++) {
            centerPanel.add(axis.getDataCell(i));
        }

        if (table.flip) {
            for (int i = table.getDataSize() - 1; i >= 0; i--) {
                centerPanel.add(this.getDataCell(i));
            }
        } else {
            for (int i = 0; i < table.getDataSize(); i++) {
                centerPanel.add(this.getDataCell(i));
            }
        }

        if (null == axis.getName() || axis.getName().isEmpty() || Settings.BLANK == axis.getName()) {
            // Do not add label.
        } else if (null == axis.getTable().getCurrentScale() || "0x" == axis.getTable().getCurrentScale().getUnit()) {
            // static or no scale exists.
            axisLabel = new JLabel(axis.getName(), JLabel.CENTER);
            add(axisLabel, BorderLayout.NORTH);
        } else {
            axisLabel = new JLabel(axis.getName() + " (" + axis.getTable().getCurrentScale().getUnit() + ")", JLabel.CENTER);
            add(axisLabel, BorderLayout.NORTH);
        }

        tableLabel = new JLabel(table.getCurrentScale().getUnit(), JLabel.CENTER);
        add(tableLabel, BorderLayout.SOUTH);

        if (axisLabel != null)
            axisLabel.setBorder(new EmptyBorder(2, 4, 2, 4));

        if (presetPanel != null) presetPanel.populatePanel();
        repaint();
    }

    @Override
    public void updateTableLabel() {
        if (null == axis.getName() || axis.getName().length() < 1 || Settings.BLANK == axis.getName()) {
            // Do not update label.
        } else if (null == axis.getTable().getCurrentScale() || "0x" == axis.getTable().getCurrentScale().getUnit()) {
            // static or no scale exists.
            axisLabel.setText(axis.getName());
        } else {
            axisLabel.setText(axis.getName() + " (" + axis.getTable().getCurrentScale().getUnit() + ")");
        }

        tableLabel.setText(table.getCurrentScale().getUnit());
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        super.addKeyListener(listener);
        axis.addKeyListener(listener);
    }

    @Override
    public void cursorUp() {
        if (data[highlightBeginY].isSelected()) {
            axis.getTable().selectCellAt(highlightBeginY);
        }
    }

    @Override
    public void drawTable() {
        super.drawTable();

        if (axis != null)
            axis.drawTable();
    }

    @Override
    public void cursorDown() {
        axis.cursorDown();
    }

    @Override
    public void cursorLeft() {
        if (highlightBeginY > 0 && data[highlightBeginY].isSelected()) {
            table.selectCellAt(highlightBeginY - 1);
        } else {
            axis.cursorLeft();
        }
    }

    @Override
    public void cursorRight() {
        if (highlightBeginY < data.length - 1 && data[highlightBeginY].isSelected()) {
            table.selectCellAt(highlightBeginY + 1);
        } else {
            axis.cursorRight();
        }
    }

    @Override
    public void shiftCursorUp() {
        if (data[highlightBeginY].isSelected()) {
            data[highlightBeginY].getDataCell().setSelected(false);
        }
        axis.getTable().selectCellAt(highlightBeginY);
    }

    @Override
    public void shiftCursorDown() {
        axis.cursorDown();
    }

    @Override
    public void shiftCursorLeft() {
        if (highlightBeginY > 0 && data[highlightBeginY].isSelected()) {
            table.selectCellAtWithoutClear(highlightBeginY - 1);
        } else {
            axis.shiftCursorLeft();
        }
    }

    @Override
    public void shiftCursorRight() {
        if (highlightBeginY < data.length - 1 && data[highlightBeginY].isSelected()) {
            table.selectCellAtWithoutClear(highlightBeginY + 1);
        } else {
            axis.shiftCursorRight();
        }
    }

    @Override
    public void startHighlight(int x, int y) {
        axis.getTable().clearSelection();
        super.startHighlight(x, y);
    }

    @Override
    public void copySelection() {
        super.copySelection();
        axis.copySelection();
    }

    @Override
    public void copyTable() {
        String tableHeader = TableView.getSettings().getTable2DHeader();
        StringBuffer output = new StringBuffer(tableHeader);
        output.append(table.getTableAsString());

        setClipboard(output.toString());
    }

    @Override
    public void paste(String s) throws UserLevelException {
        StringTokenizer st = new StringTokenizer(s, Table.ST_DELIMITER);
        String pasteType = st.nextToken();

        if (pasteType.equalsIgnoreCase("[Table2D]")) { // Paste table
            String currentToken = st.nextToken(Settings.NEW_LINE);
            if (currentToken.endsWith("\t")) {
                currentToken = st.nextToken(Settings.NEW_LINE);
            }

            String axisValues = "[Table1D]" + Settings.NEW_LINE + currentToken;
            String dataValues = "[Table1D]" + Settings.NEW_LINE + st.nextToken(Settings.NEW_LINE);

            axis.paste(axisValues);
            super.paste(dataValues);
        } else if (pasteType.equalsIgnoreCase("[Selection1D]")) { // paste selection
            if (data[highlightBeginY].isSelected()) {
                super.paste(s);
            } else {
                axis.paste(s);
            }
        }
    }

    @Override
    public void clearLiveDataTrace() {
        super.clearLiveDataTrace();
        axis.clearLiveDataTrace();
    }

    @Override
    public void updateLiveDataHighlight() {
        if (getOverlayLog()) {
            data[axis.getPreviousLiveDataIndex()].setPreviousLiveDataTrace(true);
            data[axis.getLiveDataIndex()].setPreviousLiveDataTrace(false);
            data[axis.getLiveDataIndex()].setLiveDataTrace(true);
        }
    }

    @Override
    public void setOverlayLog(boolean overlayLog) {
        super.setOverlayLog(overlayLog);
        axis.setOverlayLog(overlayLog);
    }

    @Override
    public void setCompareDisplay(Settings.CompareDisplay compareDisplay) {
        super.setCompareDisplay(compareDisplay);
        axis.setCompareDisplay(compareDisplay);
    }

    @Override
    public boolean equals(Object other) {
        return table.equals(other);
    }

    @Override
    public void repaint() {
        super.repaint();
        if (null != axis) {
            axis.repaint();
        }
    }
}
