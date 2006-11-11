package enginuity.swing;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class ECUImageFilter extends FileFilter {

    private final FileFilter filter = new GenericFileFilter("ECU Image Files", "bin", "hex");

    public boolean accept(File file) {
        return filter.accept(file);
    }

    public String getDescription() {
        return filter.getDescription();
    }
}