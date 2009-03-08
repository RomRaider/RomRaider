package com.romraider.logger.plx.io;

public enum PlxSensorType {
    WIDEBAND_AFR(0),
    EXHAUST_GAS_TEMPERATURE(1),
    FLUID_TEMPERATURE(2),
    VACUUM(3),
    BOOST(4),
    AIR_INTAKE_TEMPERATURE(5),
    RPM(6),
    VEHICLE_SPEED(7),
    THROTTLE_POSITION(8),
    ENGINE_LOAD(9),
    FLUID_PRESSURE(10),
    TIMING(11),
    MANIFOLD_ABSOLUTE_PRESSURE(12),
    MASS_AIR_FLOW(13),
    SHORT_TERM_FUEL_TRIM(14),
    LONG_TERM_FUEL_TRIM(15),
    NARROWBAND_AFR(16),
    FUEL_LEVEL(17),
    VOLTAGE(18),
    KNOCK(19),
    DUTY_CYCLE(20),
    UNKNOWN(4032);

    private final int value;

    private PlxSensorType(int value) {
        this.value = value;
    }

    public int v() {
        return value;
    }

    public static PlxSensorType valueOf(int value) {
        PlxSensorType[] types = values();
        for (PlxSensorType type : types) {
            if (type.v() == value) return type;
        }
        throw new IllegalArgumentException("Unknown PLX Sensor Type: " + value);
    }
}
