/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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

import static com.romraider.maps.RomChecksum.validateRomChecksum;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import com.romraider.util.ResourceUtil;

public class TableSwitch extends Table1D {
    private static final long serialVersionUID = -4887718305447362308L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            TableSwitch.class.getName());
    
    public TableSwitch() {
    	super();
        storageType = 1;
    }
   
    @Override
    public TableType getType() {
        return Table.TableType.SWITCH;
    }
    
    //TODO: Clean this up!
    @Override
    public void populateTable(Rom rom) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {
        super.populateTable(rom);
        
	    // Validate the ROM image checksums.
	    // if the result is >0: position of failed checksum
	    // if the result is  0: all the checksums matched
	    // if the result is -1: all the checksums have been previously disabled
	    if (super.getName().contains("Checksum Fix")) {
			int result = validateRomChecksum(getDataCell(0).getBinary(), this);
	        
	        String message = MessageFormat.format(
	                rb.getString("CHKSUMINVALID"), result, super.getName());
	        if (result > 0) {
	            showMessageDialog(null,
	                    message,
	                    rb.getString("CHKSUMSFAILED"),
	                    WARNING_MESSAGE);
	           // setButtonsUnselected(buttonGroup);
	        }
	        else if (result == -1){
	            message = rb.getString("ALLDISABLED");
	            showMessageDialog(null,
	                    message,
	                    rb.getString("CHKSUMSTATUS"),
	                    INFORMATION_MESSAGE);
	            //getButtonByText(buttonGroup, "on").setSelected(true);
	        }
	        else {
	            //getButtonByText(buttonGroup, "off").setSelected(true);
	            locked = false;
	        }
	        return;
	    }
    }
}
