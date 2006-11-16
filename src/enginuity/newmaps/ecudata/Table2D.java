package enginuity.newmaps.ecudata;

public class Table2D extends Axis {
    
    protected Axis axis;
    
    public Table2D(String name) {
        super(name);
    }    

    public Axis getAxis() {
        return axis;
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }

}