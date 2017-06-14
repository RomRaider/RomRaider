/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2017 RomRaider.com
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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import javax.swing.JFormattedTextField;

public class ECUEditorNumberField extends JFormattedTextField {
	private static final long serialVersionUID = 4756399956045598977L;
	
	static DecimalFormat format = new DecimalFormat("#.####");
	
	public ECUEditorNumberField(){
		super(format);
		
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(4);
		
		this.addKeyListener(new KeyListener(){

			  public void keyTyped(KeyEvent e) {
			    char c = e.getKeyChar();
				
			    //Replace , with .
			     if (c== ',')
			       e.setKeyChar('.');
			     
			     String textValue = ECUEditorNumberField.this.getText();
			     int dotCount = textValue.length() - textValue.replace(".", "").length();
			     
			     //Only allow one dot
			     if(e.getKeyChar()== '.' && dotCount == 1){
			    	e.consume();		    
			     }
			     
			     //dash can only be the first char
			     if(e.getKeyChar()== '-' && ECUEditorNumberField.this.getCaretPosition() != 0){
			    	e.consume();		    
			     }
			     
			     //Only allow numbers
			     else if(!(c >= '0' && c <= '9') && e.getKeyChar()!= '.' && e.getKeyChar()!= '-') e.consume();

			  }

			@Override
			public void keyPressed(KeyEvent arg0) {	
			}
			@Override
			public void keyReleased(KeyEvent arg0) {	
			}
		});

	}
	

}
