package com.romraider.io.j2534.api;

public final class Version {
    public final String firmware;
    public final String dll;
    public final String api;

    public Version(String firmware, String dll, String api) {
        this.firmware = firmware;
        this.dll = dll;
        this.api = api;
    }
}
