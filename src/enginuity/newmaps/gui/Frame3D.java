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

import enginuity.newmaps.ecumetadata.TableMetadata;
import enginuity.newmaps.ecumetadata.Table3D;
import javax.swing.JScrollPane;

 
public class Frame3D extends EnginuityFrame {
    
    EnginuityJTable xAxis;
    EnginuityJTable yAxis;
    EnginuityJTable table;
    
    public Frame3D(byte[] data, Table3D metadata) {
        
        super(data, (TableMetadata)metadata);
        
        table = new EnginuityJTable(1, 1);
        
        for (int i = 0; i < 100; i++)
        	for (int j = 0; j < 20; j++)
        	{
        		table.setValueAt(new Integer(i+j), i, j);
        	}
        JScrollPane scrollPane = new JScrollPane( table );
        getContentPane().add( scrollPane );
        
        this.pack();
        
    }
 
    public static void main(String[] args) {
        
        /*Frame3D frame = new Frame3D();
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.pack();
        frame.setLocationRelativeTo( null );
        frame.setVisible(true);*/
        
    }
    
}
