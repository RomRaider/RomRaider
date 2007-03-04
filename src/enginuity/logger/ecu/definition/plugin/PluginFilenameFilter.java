package enginuity.logger.ecu.definition.plugin;

import java.io.File;
import java.io.FilenameFilter;

public final class PluginFilenameFilter implements FilenameFilter {
    
    public boolean accept(File dir, String filename) {
        return filename.endsWith(".plugin");
    }
}
