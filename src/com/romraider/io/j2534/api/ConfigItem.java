package com.romraider.io.j2534.api;

public final class ConfigItem {
    public final int parameter;
    public final int value;

    public ConfigItem(int parameter, int value) {
        this.parameter = parameter;
        this.value = value;
    }
}
