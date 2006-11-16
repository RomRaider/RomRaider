package enginuity.newmaps.ecudata;

public class Table3D extends ECUData {
    
    protected Axis xaxis;
    protected Axis yaxis;
    
    
    public Table3D(String name) {
        super(name);
    }

    public Axis getXaxis() {
        return xaxis;
    }

    public void setXaxis(Axis xaxis) {
        this.xaxis = xaxis;
    }

    public Axis getYaxis() {
        return yaxis;
    }

    public void setYaxis(Axis yaxis) {
        this.yaxis = yaxis;
    }
    
}
