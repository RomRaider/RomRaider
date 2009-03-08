package com.romraider.logger.plx.io;

public enum PlxSensorUnits {
        WIDEBAND_AFR_LAMBDA(0),
        WIDEBAND_AFR_GASOLINE147(1),
        WIDEBAND_AFR_DIESEL146(2),
        WIDEBAND_AFR_METHANOL64(3),
        WIDEBAND_AFR_ETHANOL90(4),
        WIDEBAND_AFR_LPG155(5),
        WIDEBAND_AFR_CNG172(6),
        EXHAUST_GAS_TEMPERATURE_CELSIUS(0),
        EXHAUST_GAS_TEMPERATURE_FAHRENHEIT(1),
        AIR_INTAKE_TEMPERATURE_CELSUIS(0),
        AIR_INTAKE_TEMPERATURE_FAHRENHEIT(1),
        KNOCK(0);

    private final int value;

    private PlxSensorUnits(int value) {
        this.value = value;
    }

    public int v() {
        return value;
    }

    public static PlxSensorUnits valueOf(int value) {
        PlxSensorUnits[] types = values();
        for (PlxSensorUnits type : types) {
            if (type.v() == value) return type;
        }
        throw new IllegalArgumentException("Unknown PLX Sensor Units: " + value);
    }
}
