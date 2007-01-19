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

package enginuity.logger.definition;

import enginuity.logger.comms.query.EcuInit;
import enginuity.logger.definition.xml.LoggerDefinitionHandler;
import enginuity.logger.exception.ConfigurationException;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;
import static enginuity.util.SaxParserFactory.getSaxParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class EcuDataLoaderImpl implements EcuDataLoader {
    private List<EcuParameter> ecuParameters = new ArrayList<EcuParameter>();
    private List<EcuSwitch> ecuSwitches = new ArrayList<EcuSwitch>();
    private EcuSwitch fileLoggingControllerSwitch;

    public void loadFromXml(String loggerConfigFilePath, String protocol, String fileLoggingControllerSwitchId,
                            EcuInit ecuInit) {
        checkNotNullOrEmpty(loggerConfigFilePath, "loggerConfigFilePath");
        checkNotNullOrEmpty(protocol, "protocol");
        checkNotNullOrEmpty(fileLoggingControllerSwitchId, "fileLoggingControllerSwitchId");
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(new File(loggerConfigFilePath)));
            try {
                LoggerDefinitionHandler handler = new LoggerDefinitionHandler(protocol, fileLoggingControllerSwitchId, ecuInit);
                getSaxParser().parse(inputStream, handler);
                ecuParameters = handler.getEcuParameters();
                ecuSwitches = handler.getEcuSwitches();
                fileLoggingControllerSwitch = handler.getFileLoggingControllerSwitch();
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
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
}
