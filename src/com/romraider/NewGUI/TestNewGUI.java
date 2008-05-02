package com.romraider.NewGUI;

import com.romraider.util.LogManager;

public class TestNewGUI {
    public static void main(String[] args) {
        LogManager.initDebugLogging();
        NewGUI.getInstance().setVisible(true);
    }
}
