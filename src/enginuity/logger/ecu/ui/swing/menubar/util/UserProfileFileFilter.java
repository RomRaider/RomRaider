package enginuity.logger.ecu.ui.swing.menubar.util;

import enginuity.swing.GenericFileFilter;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public final class UserProfileFileFilter extends FileFilter {
    private final FileFilter filter = new GenericFileFilter("ECU Logger User Profiles", "xml");

    public boolean accept(File file) {
        return filter.accept(file);
    }

    public String getDescription() {
        return filter.getDescription();
    }

}
