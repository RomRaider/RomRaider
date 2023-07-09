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

package com.romraider.swing;

import javax.swing.filechooser.FileFilter;

import com.romraider.util.ResourceUtil;
import com.romraider.xml.ConversionLayer.BMWCodingConversionLayer;
import com.romraider.xml.ConversionLayer.ConversionLayer;
import com.romraider.xml.ConversionLayer.VDFConversionLayer;
import com.romraider.xml.ConversionLayer.XDFConversionLayer;

import java.io.File;
import java.util.ResourceBundle;

public class DefinitionFilter extends FileFilter {

    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            DefinitionFilter.class.getName());
    
    
    private String[] regexFilters = {
    		ConversionLayer.xmlRegexFileNameFilter,
    		new BMWCodingConversionLayer().getRegexFileNameFilter(),
    		new XDFConversionLayer().getRegexFileNameFilter(),
    		new VDFConversionLayer().getRegexFileNameFilter()
    		};
    
    private String[] filterDescr = {".xml", ".Cxx (NCS Expert)", ".xdf (Tuner Pro)", ".vdf|.jdf (Jet Tuner)"};
    private String startDescription;

    public DefinitionFilter() {
        startDescription = rb.getString("DESC");
    }
    
    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }
            for (String s : regexFilters) {
            	if(f.getName().matches(s)) return true;
            }
        }
        return false;
    }
    
  public String getDescription() {
	  	String s = startDescription;
	  	s+=" (";
	  	
	  	for(int i=0; i < filterDescr.length; i++){
	  		s+=filterDescr[i];
	  		
	  		if(i < filterDescr.length - 1) s+=", ";
	  	}
	  	
	  	s+=")";	  	
        return s;
    }
}