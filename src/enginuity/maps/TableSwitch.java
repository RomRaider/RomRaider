package enginuity.maps;

import java.awt.Color;
import javax.swing.JCheckBox;

public class TableSwitch extends Table {
    
    private byte[]    on       = new byte[0];
    private byte[]    off      = new byte[0];
    private JCheckBox checkbox = new JCheckBox();
    
    public TableSwitch() {
    }
    
    public void setDataSize(int size) {
        on = new byte[size];
        off = new byte[size];
    }
    
    public void setOnValues(byte[] input) {
        for (int i = 0; i < on.length; i++) {
            on[i] = input[i];
        }
    }

    public void setOffValues(byte[] input) {
        for (int i = 0; i < off.length; i++) {
            off[i] = input[i];
        }
    }
        
    public void colorize() { }
    public void cursorUp() { }
    public void cursorDown() { }
    public void cursorLeft() { }
    public void cursorRight() { }
    public void setAxisColor(Color color) { }    
}