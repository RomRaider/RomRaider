/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2019 RomRaider.com
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

package com.romraider.logger.ecu.definition;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.definition.xml.EcuDefinitionHandler;
import com.romraider.logger.ecu.definition.xml.LoggerDefinitionHandler;
import com.romraider.logger.ecu.exception.ConfigurationException;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;
import com.romraider.Settings;

import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.SaxParserFactory.getSaxParser;
import org.xml.sax.SAXParseException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public final class EcuDataLoaderImpl implements EcuDataLoader {
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            EcuDataLoaderImpl.class.getName());
    private Map<String, EcuDefinition> ecuDefinitionMap = new HashMap<String, EcuDefinition>();
    private List<EcuParameter> ecuParameters = new ArrayList<EcuParameter>();
    private List<EcuSwitch> ecuSwitches = new ArrayList<EcuSwitch>();
    private List<EcuSwitch> dtcodes = new ArrayList<EcuSwitch>();
    private Map<String, Map<Transport, Collection<Module>>> protocolList = new HashMap<String, Map<Transport, Collection<Module>>>();
    private EcuSwitch fileLoggingControllerSwitch;
    private ConnectionProperties connectionProperties;
    private String defVersion;

    public void loadEcuDefsFromXml(File ecuDefsFile) {
        checkNotNull(ecuDefsFile, "ecuDefsFile");
        try {
            InputStream inputStream = new BufferedInputStream(
                    new FileInputStream(ecuDefsFile));
            try {
                EcuDefinitionHandler handler = new EcuDefinitionHandler(ecuDefsFile);
                getSaxParser().parse(inputStream, handler, ecuDefsFile.getAbsolutePath());
                ecuDefinitionMap = handler.getEcuDefinitionMap();
            } finally {
                inputStream.close();
            }
        } catch (SAXParseException spe) {
            // catch general parsing exception - enough people don't
            // unzip the defs that a better error message is in order
            throw new ConfigurationException(MessageFormat.format(
                    rb.getString("UNZIPECU"), ecuDefsFile));
        } catch (FileNotFoundException fnfe) {
            throw new ConfigurationException(MessageFormat.format(
                    rb.getString("ECUFNF"), ecuDefsFile));
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public void loadConfigFromXml(String loggerConfigFilePath,
                                  String protocol,
                                  String fileLoggingControllerSwitchId,
                                  EcuInit ecuInit) {
        checkNotNullOrEmpty(loggerConfigFilePath, "loggerConfigFilePath");
        checkNotNullOrEmpty(protocol, "protocol");
        checkNotNullOrEmpty(fileLoggingControllerSwitchId, "fileLoggingControllerSwitchId");
        

        Settings s = SettingsManager.getSettings();
        boolean valid = true;
        
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(new File(loggerConfigFilePath)));
            try {
                LoggerDefinitionHandler handler = new LoggerDefinitionHandler(
                        protocol, fileLoggingControllerSwitchId, ecuInit);
                getSaxParser().parse(inputStream, handler, loggerConfigFilePath);
                              
                ecuParameters = handler.getEcuParameters();
                ecuSwitches = handler.getEcuSwitches();
                fileLoggingControllerSwitch = handler.getFileLoggingControllerSwitch();
                connectionProperties = handler.getConnectionProperties();
                defVersion = handler.getVersion();
                dtcodes = handler.getEcuCodes();
                protocolList = handler.getProtocols();
                
                valid = isCurrentProtocolValid();

                if(!valid) {                	
                	s.setLoggerProtocol(protocolList.keySet().iterator().next());
                	s.setTransportProtocol(protocolList.values().iterator().next().
                			keySet().iterator().next().getId());               	               	
                }                              
            } finally {
                inputStream.close();
            }
        }/* catch (FileNotFoundException fnfe) {
            throw new ConfigurationException(MessageFormat.format(
                    rb.getString("LOGFNF"), loggerConfigFilePath));
        } catch (SAXParseException spe) {
            // catch general parsing exception - enough people don't
            // unzip the defs that a better error message is in order
            throw new ConfigurationException(MessageFormat.format(
                    rb.getString("UNZIPLOG"), loggerConfigFilePath));
        } */catch (Exception e) {
            throw new ConfigurationException(e);
        }
        
        //If the new definition did not contain the desired protocol, we have to 
        // re-call the function with the new (available) protocol
        // because only the loaded protocol gets parsed fully
        if(!valid && isCurrentProtocolValid()) {
        	loadConfigFromXml(loggerConfigFilePath,s.getLoggerProtocol(),
        			fileLoggingControllerSwitchId, ecuInit);
        }
    }
    
    private boolean isCurrentProtocolValid() {    	
        Map<Transport, Collection<Module>> transportMap;
        Settings s = SettingsManager.getSettings();
        
        if(!protocolList.containsKey(s.getLoggerProtocol())){  
        	return false;
        }
        else {
            transportMap = protocolList.get(s.getLoggerProtocol());               
            
            if(transportMap != null) {
	            for (Transport transport : transportMap.keySet()) {
	                if (transport.getId().equalsIgnoreCase(s.getTransportProtocol())) {
	                	return true;
	                }
	            }
            }
            
            return false;                 
        }
    }
    
    public Map<String, EcuDefinition> getEcuDefinitionMap() {
        return ecuDefinitionMap;
    }

    public List<EcuParameter> getEcuParameters() {
        return ecuParameters;
    }

    public List<EcuSwitch> getEcuSwitches() {
        return ecuSwitches;
    }

    public EcuSwitch getFileLoggingControllerSwitch() {
        return fileLoggingControllerSwitch;
    }

    public ConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    public String getDefVersion() {
        return defVersion;
    }

    public List<EcuSwitch> getEcuCodes() {
        return dtcodes;
    }

    public Map<String, Map<Transport, Collection<Module>>> getProtocols() {
        return protocolList;
    }
}
