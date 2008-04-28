package com.romraider.xml;

public final class TableNotFoundException extends Exception {
    public String getMessage() {
        return "Table not found.";
    }
}