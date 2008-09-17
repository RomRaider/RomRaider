/*
 *
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

package com.romraider.logger.ecu.definition;

import static com.romraider.logger.ecu.definition.EcuDataType.EXTERNAL;
import static com.romraider.logger.ecu.definition.xml.ConverterMaxMinDefaults.getMaxMin;
import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.ecu.external.ExternalDataSource;
import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import static com.romraider.util.ParamChecker.checkNotNull;
import java.text.DecimalFormat;

public final class ExternalDataImpl implements ExternalData {
    private final EcuDataConvertor[] convertors = new EcuDataConvertor[1];
    private final ExternalDataItem dataItem;
    private final ExternalDataSource dataSource;
    private final String id;
    private boolean selected;
    private EcuParameterWarning warning;

    public ExternalDataImpl(final ExternalDataItem dataItem, ExternalDataSource dataSource) {
        checkNotNull(dataItem, dataSource);
        this.dataItem = dataItem;
        this.dataSource = dataSource;
        id = createId(dataItem);
        this.warning = new EcuParameterWarning();
        convertors[0] = new EcuDataConvertor() {
            private static final String FORMAT = "0.##";
            private DecimalFormat format = new DecimalFormat(FORMAT);

            public double convert(byte[] bytes) {
                return dataItem.getData();
            }

            public String format(double value) {
                return format.format(value);
            }

            public String getUnits() {
                return dataItem.getUnits();
            }

            public GaugeMinMax getGaugeMinMax() {
                return getMaxMin(getUnits());
            }

            public String getFormat() {
                return FORMAT;
            }
        };
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return dataItem.getName();
    }

    public String getDescription() {
        return dataItem.getDescription();
    }

    public EcuDataConvertor getSelectedConvertor() {
        return convertors[0];
    }

    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }

    public void selectConvertor(EcuDataConvertor convertor) {
    }

    public EcuDataType getDataType() {
        return EXTERNAL;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateConnection(selected);
    }

    private String createId(ExternalDataItem dataItem) {
        return "X_" + dataItem.getName().replaceAll(" ", "_");
    }

    private void updateConnection(boolean connect) {
        if (connect) {
            dataSource.connect();
        } else {
            dataSource.disconnect();
        }
    }

    public EcuParameterWarning getWarning() {
    	return warning;
    }
    
    public void setWarning(EcuParameterWarning warning) {
    	this.warning = warning;
    }

}
