/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.newmaps.gui;

import enginuity.newmaps.ecumetadata.AxisMetadata;
import enginuity.newmaps.ecumetadata.Scale;
import enginuity.newmaps.ecumetadata.TableMetadata;
import enginuity.newmaps.ecumetadata.Table3DMetadata;
import enginuity.newmaps.ecumetadata.Unit;
import javax.swing.JScrollPane;

 
public class Frame3D extends EnginuityFrame {
    
    EnginuityJTable xAxisTable;
    EnginuityJTable yAxisTable;
    EnginuityJTable table;
    
    public Frame3D(byte[] data, Table3DMetadata metadata) {
        
        super(data, (TableMetadata)metadata);
        
        table = new EnginuityJTable(5, 5);
        
        for (int i = 0; i < 5; i++)
        	for (int j = 0; j < 5; j++)
        	{
        		table.setValueAt(new Integer(i+j), i, j);
        	}
        JScrollPane scrollPane = new JScrollPane( table );
        getContentPane().add( scrollPane );
        
        this.pack();
        
    }
    
    
    private void populateTable(byte[] data) {
        //
        // Do table first..
        //
        
        
    }
 
    
    
    
    //
    // Test driver
    //
    public static void main(String[] args) {
        
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)i;
        }
        
        //
        // Create table and axis
        //
        Table3DMetadata table = new Table3DMetadata("Test");
        table.setDescription("This is the table");
        table.setAddress(75);
        AxisMetadata xAxis = new AxisMetadata("X Axis");
        xAxis.setDescription("This is the X Axis");
        xAxis.setAddress(0);
        xAxis.setSize(5);
        table.setXaxis(xAxis);
        AxisMetadata yAxis = new AxisMetadata("Y Axis");
        yAxis.setDescription("This is the Y Axis");
        yAxis.setAddress(25);
        yAxis.setSize(5);
        table.setYaxis(yAxis);
        
        //
        // Create scales        
        //
        Unit tableUnit = new Unit("Table Unit");
        tableUnit.setCoarseIncrement(2);
        tableUnit.setFineIncrement(1);
        tableUnit.setFormat("0.0");
        tableUnit.setTo_byte("x / 2");
        tableUnit.setTo_real("x * 2");        
        Scale tableScale = new Scale("Table Scale");
        Unit[] tableUnits = {tableUnit};
        tableScale.setUnits(tableUnits);
        tableScale.setDescription("This is the table scale");
        tableScale.setEndian(Scale.ENDIAN_BIG);
        tableScale.setStorageType(Scale.STORAGE_TYPE_UINT16);
        table.setScale(tableScale);
        
        Unit xUnit = new Unit("X Unit");
        xUnit.setCoarseIncrement(2);
        xUnit.setFineIncrement(1);
        xUnit.setFormat("0.0");
        xUnit.setTo_byte("x / 2");
        xUnit.setTo_real("x * 2");        
        Scale xScale = new Scale("X Scale");
        Unit[] xUnits = {xUnit};
        xScale.setUnits(xUnits);
        xScale.setDescription("This is the x scale");
        xScale.setEndian(Scale.ENDIAN_LITTLE);
        xScale.setStorageType(Scale.STORAGE_TYPE_INT8);
        xAxis.setScale(xScale);
        
        Unit yUnit = new Unit("Y Unit");
        yUnit.setCoarseIncrement(3);
        yUnit.setFineIncrement(2);
        yUnit.setFormat("0.00");
        yUnit.setTo_byte("x * 2");
        yUnit.setTo_real("x / 2");        
        Scale yScale = new Scale("Y Scale");
        Unit[] yUnits = {yUnit};
        yScale.setUnits(yUnits); 
        yScale.setDescription("This is the y scale");
        yScale.setEndian(Scale.ENDIAN_LITTLE);
        yScale.setStorageType(Scale.STORAGE_TYPE_FLOAT);
        yAxis.setScale(yScale);
        
        
        //
        // Create frame
        //
        Frame3D frame = new Frame3D(data, table);
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.pack();
        frame.setLocationRelativeTo( null );
        frame.setVisible(true);
        
    }
    
}
