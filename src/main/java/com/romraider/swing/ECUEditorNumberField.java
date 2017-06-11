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

import com.romraider.util.NumberUtil;

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
							    
			    //Get separator for the defined locale
			    char seperator = NumberUtil.getSeperator();
			    
			    //Replace , with . or vice versa
			    if ((c == ',' || c == '.') && seperator != c)
			      e.setKeyChar(seperator);			    
			     			     
			    ECUEditorNumberField field = ECUEditorNumberField.this;
			     
			    String textValue = field.getText();
			    int dotCount = textValue.length() - textValue.replace(seperator + "", "").length();
			     
			    //Only allow one dot
			    if(e.getKeyChar() == seperator && (dotCount == 0 || field.getSelectionStart() == 0)) return;
			     
			    //Only one dash allowed at the start
			    else if(e.getKeyChar()== '-' && (field.getCaretPosition() == 0 || field.getSelectionStart() == 0)) return;
			     
			     //Only allow numbers
			     else if(c >= '0' && c <= '9') return;
			     
			     //Don't allow if input doesn't meet these rules
			     else e.consume();			    
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
