package enginuity.newmaps.definition.index;

import enginuity.util.Nameable;
import java.io.File;

public class IndexItem implements Nameable {
    
    private String name;
    private String base;
    private File file;
    private int idAddress;
    private String idString;
    
    public IndexItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(int idAddress) {
        this.idAddress = idAddress;
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }
    
}
