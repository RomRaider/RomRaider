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

package com.romraider.maps.checksum;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.romraider.maps.Rom;
import com.romraider.util.ResourceUtil;

/**
 * Instantiate a ChecksumManager class.
 */
public final class ChecksumFactory {
    private static final Logger LOGGER = Logger.getLogger(ChecksumFactory.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            ChecksumFactory.class.getName());
    private static final String PATH = "path";
    private static final String TYPE = "type";
    private static final String MISSING = rb.getString("MISSING");
    private static final String NO_CLASS = rb.getString("NOCLASS");
    private ChecksumFactory() {
    }

    /**
     * Instantiate the specific ChecksumManager class based on
     * the "type" requested.
     * @param attrs    - the configuration for the checksum manager, must
     *                   contain a "type" K,V pair
     * @return    a configured instance of the requested ChecksumManager
     * @throws     ClassNotFoundException if the class    based on "type"
     *             does not exist
     */
    public static ChecksumManager getManager(Rom rom,
            Map<String, String> attrs) {
           
        ChecksumManager cm = null;
        Class<?> cls;
        ClassLoader cl;
        
        final String type = attrs.get(TYPE);
        final String pathCustomChecksum = attrs.get(PATH);
        
        try {
        	String path;
        	
        	//Custom checksum which comes with the definition
        	//Path is relative to the current definition directory
        	if(pathCustomChecksum != null && rom.getDefinitionPath() != null) {
        		path = rom.getDefinitionPath().getParent() + pathCustomChecksum;
        	    cl = new URLClassLoader(new URL[]{new File(path).toURI().toURL()});
        	    cls = cl.loadClass(ChecksumFactory.class.getPackage().getName() + "." + type);
        	    
        		LOGGER.info("Loaded custom checksum type " + type + " from " + path);
        	}
        	//Checksum included in RR
        	else {
        		path = ChecksumFactory.class.getPackage().getName() + ".Checksum" + type.toUpperCase();
                cls = Class.forName(path);
        	}

            cm = (ChecksumManager) cls.newInstance();
            cm.configure(attrs);
        } catch (Exception e) {
            String message = null;
            if (type == null) {
                message = MISSING;
            }
            else {
                message = MessageFormat.format(NO_CLASS, type.toUpperCase());
            }
            
            e.printStackTrace();
            showMessageDialog(null,
                    message,
                    e.toString(), ERROR_MESSAGE);
        }
        return cm;
    }
}
