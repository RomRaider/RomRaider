/*
 * Copyright (c) 2009. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.romraider.logger.innovate.lm2.plugin;

import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.innovate.generic.plugin.DataConvertor;
import com.romraider.logger.innovate.generic.plugin.DataListener;

public final class Lm2DataItem implements ExternalDataItem, DataListener {
    private final DataConvertor convertor = new Lm2DataConvertor();
    private byte[] bytes;

    public String getName() {
        return "Innovate LM-2";
    }

    public String getDescription() {
        return "Innovate LM-2 AFR data";
    }

    public String getUnits() {
        return "AFR";
    }

    public double getData() {
        if (bytes == null) return 0.0;
        return convertor.convert(bytes);
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}