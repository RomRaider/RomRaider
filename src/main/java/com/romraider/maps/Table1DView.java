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

package com.romraider.maps;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.romraider.Settings;
import com.romraider.util.NumberUtil;

public class Table1D extends Table {
    private static final long serialVersionUID = -8747180767803835631L;
    private Table axisParent = null;
    private final TableType type;

    public Table1D(TableType type) {
        this.type = type;
    }

    @Override
    public TableType getType() {
        return type;
    }

    public void setAxisParent(Table axisParent) {
        this.axisParent = axisParent;
    }

    public Table getAxisParent() {
        return axisParent;
    }

    public void addStaticDataCell(DataCellView input) {
        for(int i = 0; i < data.length; i++) {
            if(data[i] == null) {
                data[i] = input;
                data[i].setY(i);
                break;
            }
        }
    }
    
	@Override
	public byte[] saveFile(byte[] binData) {
		return binData;
	}
	
    @Override
    public void populateTable(byte[] input, int romRamOffset) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException  {
        centerLayout.setRows(1);
        centerLayout.setColumns(this.getDataSize());

        super.populateTable(input, romRamOffset);

        // add to table
        for (int i = 0; i < this.getDataSize(); i++) {
            centerPanel.add(this.getDataCell(i));
        }

        if(null == name || name.isEmpty()) {
            ;// Do not add label.
        } else if(null == getCurrentScale () || "0x" == getCurrentScale().getUnit()) {
            // static or no scale exists.
            tableLabel = new JLabel(getName(), JLabel.CENTER);
            add(tableLabel, BorderLayout.NORTH);
        } else {
            tableLabel = new JLabel(getName() + " (" + getCurrentScale().getUnit() + ")", JLabel.CENTER);
            add(tableLabel, BorderLayout.NORTH);
        }
        
        if(tableLabel != null)
        	tableLabel.setBorder(new EmptyBorder(2, 4, 2, 4));
        
        if(presetPanel != null) presetPanel.populatePanel();
    }

    @Override
    public String toString() {
        return super.toString() + " (1D)";
    }

    @Override
    public void cursorUp() {
        if (type == Table.TableType.Y_AXIS) {
            if (highlightY > 0 && data[highlightY].isSelected()) {
                selectCellAt(highlightY - 1);
            }
        } else if (type == Table.TableType.X_AXIS) {
            // Y axis is on top.. nothing happens
        } else if (type == Table.TableType.TABLE_1D) {
            // no where to move up to
        }
    }

    @Override
    public void cursorDown() {
        if (type == Table.TableType.Y_AXIS) {
            if (getAxisParent().getType() == Table.TableType.TABLE_3D) {
                if (highlightY < getDataSize() - 1 && data[highlightY].isSelected()) {
                    selectCellAt(highlightY + 1);
                }
            } else if (getAxisParent().getType() == Table.TableType.TABLE_2D) {
                if (data[highlightY].isSelected()) {
                    getAxisParent().selectCellAt(highlightY);
                }
            }
        } else if (type == Table.TableType.X_AXIS && data[highlightY].isSelected()) {
            ((Table3D) getAxisParent()).selectCellAt(highlightY, this);
        } else if (type == Table.TableType.TABLE_1D) {
            // no where to move down to
        }
    }

    @Override
    public void cursorLeft() {
        if (type == Table.TableType.Y_AXIS) {
            // X axis is on left.. nothing happens
            if (getAxisParent().getType() == Table.TableType.TABLE_2D) {
                if (data[highlightY].isSelected()) {
                    selectCellAt(highlightY - 1);
                }
            }
        } else if (type == Table.TableType.X_AXIS && data[highlightY].isSelected()) {
            if (highlightY > 0) {
                selectCellAt(highlightY - 1);
            }
        } else if (type == Table.TableType.TABLE_1D && data[highlightY].isSelected()) {
            if (highlightY > 0) {
                selectCellAt(highlightY - 1);
            }
        }
    }

    @Override
    public void cursorRight() {
        if (type == Table.TableType.Y_AXIS && data[highlightY].isSelected()) {
            if (getAxisParent().getType() == Table.TableType.TABLE_3D) {
                ((Table3D) getAxisParent()).selectCellAt(highlightY, this);
            } else if (getAxisParent().getType() == Table.TableType.TABLE_2D) {
                selectCellAt(highlightY + 1);
            }
        } else if (type == Table.TableType.X_AXIS && data[highlightY].isSelected()) {
            if (highlightY < getDataSize() - 1) {
                selectCellAt(highlightY + 1);
            }
        } else if (type == Table.TableType.TABLE_1D && data[highlightY].isSelected()) {
            if (highlightY < getDataSize() - 1) {
                selectCellAt(highlightY + 1);
            }
        }
    }

	@Override
	public void shiftCursorUp() {
        if (type == Table.TableType.Y_AXIS) {
            if (highlightY > 0 && data[highlightY].isSelected()) {
            	selectCellAtWithoutClear(highlightY - 1);
            }
        } else if (type == Table.TableType.X_AXIS) {
            // Y axis is on top.. nothing happens
        } else if (type == Table.TableType.TABLE_1D) {
            // no where to move up to
        }
	}

	@Override
	public void shiftCursorDown() {
        if (type == Table.TableType.Y_AXIS) {
            if (getAxisParent().getType() == Table.TableType.TABLE_3D) {
                if (highlightY < getDataSize() - 1 && data[highlightY].isSelected()) {
                	selectCellAtWithoutClear(highlightY + 1);
                }
            } else if (getAxisParent().getType() == Table.TableType.TABLE_2D) {
                if (data[highlightY].isSelected()) {
                    getAxisParent().selectCellAtWithoutClear(highlightY);
                }
            }
        } else if (type == Table.TableType.X_AXIS && data[highlightY].isSelected()) {
            ((Table3D) getAxisParent()).selectCellAt(highlightY, this);
        } else if (type == Table.TableType.TABLE_1D) {
            // no where to move down to
        }
	}

	@Override
	public void shiftCursorLeft() {
        if (type == Table.TableType.Y_AXIS) {
            // X axis is on left.. nothing happens
            if (getAxisParent().getType() == Table.TableType.TABLE_2D) {
                if (data[highlightY].isSelected()) {
                	selectCellAtWithoutClear(highlightY - 1);
                }
            }
        } else if (type == Table.TableType.X_AXIS && data[highlightY].isSelected()) {
            if (highlightY > 0) {
            	selectCellAtWithoutClear(highlightY - 1);
            }
        } else if (type == Table.TableType.TABLE_1D && data[highlightY].isSelected()) {
            if (highlightY > 0) {
            	selectCellAtWithoutClear(highlightY - 1);
            }
        }
	}

	@Override
	public void shiftCursorRight() {
        if (type == Table.TableType.Y_AXIS && data[highlightY].isSelected()) {
            if (getAxisParent().getType() == Table.TableType.TABLE_3D) {
                ((Table3D) getAxisParent()).selectCellAt(highlightY, this);
            } else if (getAxisParent().getType() == Table.TableType.TABLE_2D) {
            	selectCellAtWithoutClear(highlightY + 1);
            }
        } else if (type == Table.TableType.X_AXIS && data[highlightY].isSelected()) {
            if (highlightY < getDataSize() - 1) {
            	selectCellAtWithoutClear(highlightY + 1);
            }
        } else if (type == Table.TableType.TABLE_1D && data[highlightY].isSelected()) {
            if (highlightY < getDataSize() - 1) {
            	selectCellAtWithoutClear(highlightY + 1);
            }
        }
	}

    @Override
    public void clearSelection() {
        // Call to the axis parent.  The axis parent should then call to clear this data.
    	Table p = getAxisParent();
    	
    	if(p != null)
    		p.clearSelection();
    }

    @Override
    public void startHighlight(int x, int y) {
        Table axisParent = getAxisParent();
        
        if(axisParent != null)
        	axisParent.clearSelectedData();

        if(axisParent instanceof Table3D) {
            Table3D table3D = (Table3D) axisParent;
            if(getType() == Table.TableType.X_AXIS) {
                table3D.getYAxis().clearSelectedData();
            } else if (getType() == Table.TableType.Y_AXIS) {
                table3D.getXAxis().clearSelectedData();
            }
        } else if (axisParent instanceof Table2D) {
            ((Table2D) axisParent).getAxis().clearSelectedData();
        }


        super.startHighlight(x, y);
    }

    @Override
    public String getCellAsString(int index) {
        return data[index].getText();
    }

    @Override
    public void highlightLiveData(String liveVal) {
        if (getOverlayLog()) {
            double liveValue = 0.0;
            try {
                liveValue = NumberUtil.doubleValue(liveVal);
            } catch (Exception ex) {
            	LOGGER.error("Table1D - live data highlight parsing error for value: " + liveVal);
                return;
            }

            int startIdx = data.length;
            for (int i = 0; i < data.length; i++) {
                double currentValue = 0.0;
                if(isStaticDataTable() && null != data[i].getStaticText()) {
                    try {
                        currentValue = Double.parseDouble(data[i].getStaticText());
                    } catch(NumberFormatException nex) {
                        return;
                    }
                } else {
                    currentValue = data[i].getDataCell().getRealValue();
                }

                if (liveValue == currentValue) {
                    startIdx = i;
                    break;
                } else if (liveValue < currentValue){
                    startIdx = i-1;
                    break;
                }
            }

            setLiveDataIndex(startIdx);
            DataCellView cellp = data[getPreviousLiveDataIndex()];
            cellp.setPreviousLiveDataTrace(true);
            DataCellView cell = data[getLiveDataIndex()];
            cell.setPreviousLiveDataTrace(false);
            cell.setLiveDataTrace(true);
            cell.getDataCell().setLiveDataTraceValue(liveVal);
            getToolbar().setLiveDataValue(liveVal);
        }
        getAxisParent().updateLiveDataHighlight();
    }

    @Override
    public boolean isLiveDataSupported() {
        return false;
    }

    @Override
    public boolean isButtonSelected() {
        return true;
    }

    public boolean isAxis() {
        return getType() == Table.TableType.X_AXIS ||
                getType() == Table.TableType.Y_AXIS || isStaticDataTable();
    }

    @Override
    public boolean equals(Object other) {
        try {
            if(null == other) {
                return false;
            }

            if(other == this) {
                return true;
            }

            if(!(other instanceof Table1D)) {
                return false;
            }

            Table1D otherTable = (Table1D)other;

            if(this.isAxis() != otherTable.isAxis()) {
                return false;
            }

            if(this.data.length != otherTable.data.length)
            {
                return false;
            }

            if(this.data.equals(otherTable.data))
            {
                return true;
            }

            // Compare Bin Values
            for(int i=0 ; i < this.data.length ; i++) {
                if(! this.data[i].equals(otherTable.data[i])) {
                    return false;
                }
            }

            return true;
        } catch(Exception ex) {
            // TODO: Log Exception.
            return false;
        }
    }

    @Override
    public void updateTableLabel() {
        this.getAxisParent().updateTableLabel();
    }

    @Override
    public StringBuffer getTableAsString() {
        if(isStaticDataTable()) {
            StringBuffer output = new StringBuffer(Settings.BLANK);
            for (int i = 0; i < data.length; i++) {
                output.append(data[i].getStaticText());
                if (i < data.length - 1) {
                    output.append(Settings.TAB);
                }
            }
            return output;
        } else {
            return super.getTableAsString();
        }
    }
}