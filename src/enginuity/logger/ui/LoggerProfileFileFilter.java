package enginuity.logger.ui;

import enginuity.swing.GenericFileFilter;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public final class LoggerProfileFileFilter extends FileFilter {

    private final FileFilter filter = new GenericFileFilter("Logger Profiles", "xml");

    public boolean accept(File file) {
        return filter.accept(file);
    }

    public String getDescription() {
        return filter.getDescription();
    }

}
