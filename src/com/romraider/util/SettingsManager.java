package com.romraider.util;

import com.romraider.Settings;
import com.romraider.swing.JProgressPane;

public interface SettingsManager {
    Settings load();

    void save(Settings settings);

    void save(Settings settings, JProgressPane progress);
}
