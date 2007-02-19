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

package enginuity.newmaps.swing;

import enginuity.newmaps.ecudata.DataCell;
import enginuity.newmaps.ecudata.Table3DData;
import enginuity.newmaps.ecumetadata.AxisMetadata;
import enginuity.newmaps.ecumetadata.Scale;
import enginuity.newmaps.ecumetadata.TableMetadata;
import enginuity.newmaps.ecumetadata.Table3DMetadata;
import enginuity.newmaps.ecumetadata.Unit;
import enginuity.newmaps.exception.DataPopulationException;
import javax.swing.JScrollPane;


public class Frame3D extends EnginuityFrame {

    Table3DData table;
    EnginuityJTable xAxisTable;
    EnginuityJTable yAxisTable;
    EnginuityJTable jTable;

    public Frame3D(Table3DData table) {

    	this.table = table;

        //super(data, (TableMetadata)metadata);
    	Table3DMetadata metadata = (Table3DMetadata) table.getMetadata();
    	DataCell[][] dataCell = table.getValues();
        jTable = new EnginuityJTable(metadata.getSizeX(), metadata.getSizeY());
        setLocationRelativeTo( null );
        setVisible(true);

        for (int y = 0; y < metadata.getSizeY(); y++) {
        	for (int x = 0; x < metadata.getSizeX(); x++) {
        		
        		System.out.println("x=" + x + " y=" + y + " sizex=" 
        				+ metadata.getSizeX() + " sizey="
        				+ metadata.getSizeY());
        		
        		jTable.setValueAt(dataCell[x][y], x, y);
        	}
        }

        JScrollPane scrollPane = new JScrollPane( jTable );
        getContentPane().add( scrollPane );

        this.pack();

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
        Table3DMetadata tableMetadata = new Table3DMetadata("Test");
        tableMetadata.setDescription("This is the table");
        tableMetadata.setAddress(1);
        AxisMetadata xAxisMetadata = new AxisMetadata("X Axis");
        xAxisMetadata.setDescription("This is the X Axis");
        xAxisMetadata.setAddress(0);
        xAxisMetadata.setSize(5);
        tableMetadata.setXaxis(xAxisMetadata);
        AxisMetadata yAxisMetadata = new AxisMetadata("Y Axis");
        yAxisMetadata.setDescription("This is the Y Axis");
        yAxisMetadata.setAddress(25);
        yAxisMetadata.setSize(4);
        tableMetadata.setYaxis(yAxisMetadata);


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
        tableScale.setStorageType(Scale.STORAGE_TYPE_UINT8);
        tableMetadata.setScale(tableScale);

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
        xAxisMetadata.setScale(xScale);

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
        yAxisMetadata.setScale(yScale);

    	try {
			Table3DData table = new Table3DData(data, tableMetadata);

	        //
	        // Create frame
	        //
	        Frame3D frame = new Frame3D(table);
	        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );

		} catch (DataPopulationException e) {
			e.printStackTrace();
		}
    }

}
