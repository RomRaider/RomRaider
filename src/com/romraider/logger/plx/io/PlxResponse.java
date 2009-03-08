package com.romraider.logger.plx.io;

public final class PlxResponse {
    public PlxSensorType sensor;
    public int value;

    public PlxResponse(PlxSensorType sensor, int value) {
        this.sensor = sensor;
        this.value = value;
    }
}