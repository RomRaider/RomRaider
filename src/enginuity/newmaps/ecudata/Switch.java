package enginuity.newmaps.ecudata;

public class Switch extends ECUData {
    
    protected byte[] stateOn = new byte[1];
    protected byte[] stateOff = new byte[1];
    protected int size;
    
    public Switch(String name) {
        super(name);
    } 
    
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }  

    public byte[] getStateOn() {
        return stateOn;
    }

    public void setStateOn(byte[] stateOn) {
        this.stateOn = stateOn;
    }

    public byte[] getStateOff() {
        return stateOff;
    }

    public void setStateOff(byte[] stateOff) {
        this.stateOff = stateOff;
    }
    
}
