package com.romraider.io.j2534.api;

public final class J2534Exception extends RuntimeException {
    public J2534Exception(String msg) {
        super(msg);
    }

    public J2534Exception(String msg, Throwable t) {
        super(msg, t);
    }
}
