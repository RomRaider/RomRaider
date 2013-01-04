package com.romraider.logger.external.core;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.ExternalDataConvertorImpl;

public class ExternalDataConvertorLoader {

    public static EcuDataConvertor[] loadConvertors(
            ExternalDataItem dataItem,
            EcuDataConvertor[] convertors,
            ExternalSensorConversions... convertorList)
        {
        int i = 0;
        for (ExternalSensorConversions convertor : convertorList) {
            convertors[i] = new ExternalDataConvertorImpl(
                    dataItem,
                    convertor.units(),
                    convertor.expression(),
                    convertor.format(),
                    convertor.gaugeMinMax()
            );
            i++;
        }
    return convertors;
    }
}
