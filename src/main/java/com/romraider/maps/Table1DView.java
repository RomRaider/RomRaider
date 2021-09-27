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

import com.romraider.maps.Table.TableType;
import com.romraider.util.NumberUtil;

public class Table1DView extends TableView {

	private static final long serialVersionUID = -8747180767803835631L;
	private Table1D table;
	
    public Table1DView(Table1D table) {
		this(table, false);
	}
    
    public Table1DView(Table1D table, boolean hide) {
		super(table);
    	this.table = table;
    	this.hide = hide;
    	
    	populateTableVisual();
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
    public void populateTableVisual() {
        super.populateTableVisual();
        
        //Only populate the rest if we aren't an axis
    	if(table.getType() != TableType.X_AXIS && table.getType() != TableType.Y_AXIS && !isHidden()) {
	        centerLayout.setRows(1);
	        centerLayout.setColumns(table.getDataSize());

	        // add to table
	        for (int i = 0; i < table.getDataSize(); i++) {
	            centerPanel.add(this.getDataCell(i));
	        }
	
	        if(null == table.name || table.name.isEmpty()) {
	            ;// Do not add label.
	        } else if(null == table.getCurrentScale () || "0x" == table.getCurrentScale().getUnit()) {
	            // static or no scale exists.
	            tableLabel = new JLabel(getName(), JLabel.CENTER);
	            add(tableLabel, BorderLayout.NORTH);
	        } else {
	            tableLabel = new JLabel(getName() + " (" + table.getCurrentScale().getUnit() + ")", JLabel.CENTER);
	            add(tableLabel, BorderLayout.NORTH);
	        }
	        
	        if(tableLabel != null)
	        	tableLabel.setBorder(new EmptyBorder(2, 4, 2, 4));	       
    	}
    	
    	if(table.getType() != TableType.X_AXIS && table.getType() != TableType.Y_AXIS)
    		if(presetPanel != null) presetPanel.populatePanel();
    }

    @Override
    public String toString() {
        return table.toString() + " View";
    }

    @Override
    public void cursorUp() {
        if (table.getType() == Table.TableType.Y_AXIS) {
            if (highlightBeginY > 0 && data[highlightBeginY].isSelected()) {
                table.selectCellAt(highlightBeginY - 1);
            }
        } else if (table.getType() == Table.TableType.X_AXIS) {
            // Y axis is on top.. nothing happens
        } else if (table.getType() == Table.TableType.TABLE_1D) {
            // no where to move up to
        }
    }

    @Override
    public void cursorDown() {
        if (table.getType() == Table.TableType.Y_AXIS) {
            if (table.getAxisParent().getType() == Table.TableType.TABLE_3D) {
                if (highlightBeginY < table.getDataSize() - 1 && data[highlightBeginY].isSelected()) {
                    table.selectCellAt(highlightBeginY + 1);
                }
            } else if (table.getAxisParent().getType() == Table.TableType.TABLE_2D) {
                if (data[highlightBeginY].isSelected()) {
                	table.selectCellAt(highlightBeginY);
                }
            }
        } else if (table.getType() == Table.TableType.X_AXIS && data[highlightBeginY].isSelected()) {
            ((Table3D) table.getAxisParent()).selectCellAt(highlightBeginY);
        } else if (table.getType() == Table.TableType.TABLE_1D) {
            // no where to move down to
        }
    }

    @Override
    public void cursorLeft() {
        if (table.getType() == Table.TableType.Y_AXIS) {
            // X axis is on left.. nothing happens
            if (table.getAxisParent().getType() == Table.TableType.TABLE_2D) {
                if (data[highlightBeginY].isSelected()) {
                    table.selectCellAt(highlightBeginY - 1);
                }
            }
        } else if (table.getType() == Table.TableType.X_AXIS && data[highlightBeginY].isSelected()) {
            if (highlightBeginY > 0) {
                table.selectCellAt(highlightBeginY - 1);
            }
        } else if (table.getType() == Table.TableType.TABLE_1D && data[highlightBeginY].isSelected()) {
            if (highlightBeginY > 0) {
                table.selectCellAt(highlightBeginY - 1);
            }
        }
    }

    @Override
    public void cursorRight() {
        if (table.getType() == Table.TableType.Y_AXIS && data[highlightBeginY].isSelected()) {
            if (table.getAxisParent().getType() == Table.TableType.TABLE_3D) {
                ((Table3D) table.getAxisParent()).selectCellAt(highlightBeginY);
            } else if (table.getAxisParent().getType() == Table.TableType.TABLE_2D) {
                table.selectCellAt(highlightBeginY + 1);
            }
        } else if (table.getType() == Table.TableType.X_AXIS && data[highlightBeginY].isSelected()) {
            if (highlightBeginY < table.getDataSize() - 1) {
                table.selectCellAt(highlightBeginY + 1);
            }
        } else if (table.getType() == Table.TableType.TABLE_1D && data[highlightBeginY].isSelected()) {
            if (highlightBeginY < table.getDataSize() - 1) {
                table.selectCellAt(highlightBeginY + 1);
            }
        }
    }

	@Override
	public void shiftCursorUp() {
        if (table.getType() == Table.TableType.Y_AXIS) {
            if (highlightBeginY > 0 && data[highlightBeginY].isSelected()) {
            	table.selectCellAtWithoutClear(highlightBeginY - 1);
            }
        } else if (table.getType() == Table.TableType.X_AXIS) {
            // Y axis is on top.. nothing happens
        } else if (table.getType() == Table.TableType.TABLE_1D) {
            // no where to move up to
        }
	}

	@Override
	public void shiftCursorDown() {
        if (table.getType() == Table.TableType.Y_AXIS) {
            if (table.getAxisParent().getType() == Table.TableType.TABLE_3D) {
                if (highlightBeginY < table.getDataSize() - 1 && data[highlightBeginY].isSelected()) {
                	table.selectCellAtWithoutClear(highlightBeginY + 1);
                }
            } else if (table.getAxisParent().getType() == Table.TableType.TABLE_2D) {
                if (data[highlightBeginY].isSelected()) {
                    table.getAxisParent().getTableView().table.selectCellAtWithoutClear(highlightBeginY);
                }
            }
        } else if (table.getType() == Table.TableType.X_AXIS && data[highlightBeginY].isSelected()) {
            ((Table3D) table.getAxisParent()).selectCellAt(highlightBeginY);
        } else if (table.getType() == Table.TableType.TABLE_1D) {
            // no where to move down to
        }
	}

	@Override
	public void shiftCursorLeft() {
        if (table.getType() == Table.TableType.Y_AXIS) {
            // X axis is on left.. nothing happens
            if (table.getAxisParent().getType() == Table.TableType.TABLE_2D) {
                if (data[highlightBeginY].isSelected()) {
                	table.selectCellAtWithoutClear(highlightBeginY - 1);
                }
            }
        } else if (table.getType() == Table.TableType.X_AXIS && data[highlightBeginY].isSelected()) {
            if (highlightBeginY > 0) {
            	table.selectCellAtWithoutClear(highlightBeginY - 1);
            }
        } else if (table.getType() == Table.TableType.TABLE_1D && data[highlightBeginY].isSelected()) {
            if (highlightBeginY > 0) {
            	table.selectCellAtWithoutClear(highlightBeginY - 1);
            }
        }
	}

	@Override
	public void shiftCursorRight() {
        if (table.getType() == Table.TableType.Y_AXIS && data[highlightBeginY].isSelected()) {
            if (table.getAxisParent().getType() == Table.TableType.TABLE_3D) {
                ((Table3D) table.getAxisParent()).selectCellAt(highlightBeginY);
            } else if (table.getAxisParent().getType() == Table.TableType.TABLE_2D) {
            	table.selectCellAtWithoutClear(highlightBeginY + 1);
            }
        } else if (table.getType() == Table.TableType.X_AXIS && data[highlightBeginY].isSelected()) {
            if (highlightBeginY < table.getDataSize() - 1) {
            	table.selectCellAtWithoutClear(highlightBeginY + 1);
            }
        } else if (table.getType() == Table.TableType.TABLE_1D && data[highlightBeginY].isSelected()) {
            if (highlightBeginY < table.getDataSize() - 1) {
            	table.selectCellAtWithoutClear(highlightBeginY + 1);
            }
        }
	}

    public void clearSelectionInParent() {
        // Call to the axis parent. The axis parent should then call to clear this data.
    	Table p = table.getAxisParent();
    	
    	if(p != null)
    		p.clearSelection();
    }

    @Override
    public void startHighlight(int x, int y) {
        Table axisParent = table.getAxisParent();
        
        if(axisParent != null)
        	axisParent.clearSelection();

        if(axisParent instanceof Table3D) {
            Table3D table3D = (Table3D) axisParent;
            if(table.getType() == Table.TableType.X_AXIS) {
                table3D.getYAxis().clearSelection();
            } else if (table.getType() == Table.TableType.Y_AXIS) {
                table3D.getXAxis().clearSelection();
            }
        } else if (axisParent instanceof Table2D) {
            ((Table2D) axisParent).getAxis().clearSelection();
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
                if(table.isStaticDataTable() && null != data[i].getStaticText()) {
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
        
        table.getAxisParent().getTableView().updateLiveDataHighlight();
    }

    public boolean isAxis() {
        return table.getType() == Table.TableType.X_AXIS ||
                table.getType() == Table.TableType.Y_AXIS || table.isStaticDataTable();
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
        this.table.getAxisParent().getTableView().updateTableLabel();
    }
}