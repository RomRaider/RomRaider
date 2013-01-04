package com.romraider.logger.external.core;

import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;

public interface ExternalSensorConversions {

    String units();
    
    String expression();
    
    String format();
    
    GaugeMinMax gaugeMinMax();
    
}
