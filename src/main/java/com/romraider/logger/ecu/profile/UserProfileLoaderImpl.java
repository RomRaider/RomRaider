/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

package com.romraider.logger.ecu.profile;

import com.romraider.logger.ecu.profile.xml.UserProfileHandler;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.SaxParserFactory.getSaxParser;
import org.apache.log4j.Logger;
import org.xml.sax.SAXParseException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public final class UserProfileLoaderImpl implements UserProfileLoader {
    private static final Logger LOGGER = Logger.getLogger(UserProfileLoaderImpl.class);

    public UserProfile loadProfile(String userProfileFilePath) {
        checkNotNullOrEmpty(userProfileFilePath, "userProfileFilePath");
        LOGGER.info("Loading profile: " + userProfileFilePath);
        try {
                File userProfileFile = new File(userProfileFilePath);
                UserProfileHandler handler = new UserProfileHandler();
                getSaxParser().parse(userProfileFile, handler);
                return handler.getUserProfile();
        } catch (FileNotFoundException fileException) {
        	if(fileException.getMessage().contains("profile.dtd")) {
        		LOGGER.info("Profile.dtd missing, applying patch: " + userProfileFilePath);
        		patchUserProfile(userProfileFilePath);
        		return loadProfile(userProfileFilePath);
        	}
        	else {
        		LOGGER.error("Error loading user profile file: " + userProfileFilePath, fileException);
        		return null;
        	}
        } catch (SAXParseException spe) {
            // catch general parsing exception - enough people don't unzip the defs that a better error message is in order
            LOGGER.error("Error loading user profile file: " + userProfileFilePath + ".  Please make sure the definition file is correct.  If it is in a ZIP archive, unzip the file and try again.");
            return null;
        } catch (Exception e) {
            LOGGER.error("Error loading user profile file: " + userProfileFilePath, e);
            return null;
        }
    }
    
    //The .dtd file was removed in a later version. This causes an error while loading the profile.xml, because profile.dtd is missing
    //This removes the line from the profile.xml
    private void patchUserProfile(String userProfileFilePath) {   	
    	try {
    		BufferedReader br = new BufferedReader(new FileReader(userProfileFilePath));
    		List<String> newLines = new LinkedList<String>();
		    String line;
		    while ((line = br.readLine()) != null) {
    			if (!line.contains("profile.dtd")) {
    				newLines.add(line);
    			} 
		    }      	     	
		    br.close();	   
		    
		    FileWriter writer = new FileWriter(userProfileFilePath); 
		    for(String str: newLines) {
		      writer.write(str + "\n");
		    }
		    writer.close();      	
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
