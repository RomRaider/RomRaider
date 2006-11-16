package enginuity.newmaps;

import enginuity.maps.Scale;
import enginuity.newmaps.ecudata.ECUData;
import enginuity.util.Nameable;

public class Rom implements Nameable {

    protected String name;
    protected int idAddress;
    protected String idString;    
    protected String description;    
    protected String memmodel;
    protected String flashmethod;
    protected String caseid;
    protected boolean obsolete;
    protected boolean isAbstract;
    
    protected Scale[] scales;
    protected ECUData[] tables;
    
    private Rom() { }
    
    public Rom(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getMemmodel() {
        return memmodel;
    }

    public void setMemmodel(String memmodel) {
        this.memmodel = memmodel;
    }

    public String getFlashmethod() {
        return flashmethod;
    }

    public void setFlashmethod(String flashmethod) {
        this.flashmethod = flashmethod;
    }

    public String getCaseid() {
        return caseid;
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }      

    public Scale[] getScales() {
        return scales;
    }

    public void setScales(Scale[] scales) {
        this.scales = scales;
    }

    public ECUData[] getTables() {
        return tables;
    }

    public void setTables(ECUData[] tables) {
        this.tables = tables;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
    
}