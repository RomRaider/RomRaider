package enginuity.util;

import enginuity.Settings;
import enginuity.swing.JProgressPane;

public interface SettingsManager {
    Settings load(String settingsNotFoundMessage);

    void save(Settings settings);

    void save(Settings settings, JProgressPane progress);
}
