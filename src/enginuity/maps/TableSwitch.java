package enginuity.maps;

import enginuity.xml.RomAttributeParser;
import java.awt.Color;
import java.util.StringTokenizer;
import javax.swing.JCheckBox;

public class TableSwitch extends Table {
    
    private byte[]    on       = new byte[0];
    private byte[]    off      = new byte[0];
    private JCheckBox checkbox = new JCheckBox("Enabled", true); // checkbox selected by default
    
    public TableSwitch() {
        storageType = 1;
        add(checkbox);
    }
    
    public void setDataSize(int size) {
        on = new byte[size];
        off = new byte[size];
    }
    
    public void populateTable(byte[] input) {
        System.out.println(on.length);
        for (int i = 0; i < on.length; i++) {
            
            System.out.println(on[i] + " " + input[storageAddress - ramOffset + 1]);
            
            // check each byte -- if it doesn't match "on", it's off
            if (on[i] != input[storageAddress - ramOffset + i]) {
                checkbox.setSelected(false);
                break;
            }
        }
    }
    
    public byte[] saveFile(byte[] input) {
        if (checkbox.isSelected()) { // switch is on
            for (int i = 0; i < on.length; i++) {
                input[storageAddress - ramOffset + i] = on[i];
            }
            
        } else { // switch is off
            for (int i = 0; i < on.length; i++) {
                input[storageAddress - ramOffset + i] = off[i];
            }            
        }
        return input;
    }
    
    public void setOnValues(String input) {
        StringTokenizer tokens = new StringTokenizer(input);
        for (int i = 0; i < off.length; i++) {
            on[i] = (byte)RomAttributeParser.parseHexString(tokens.nextToken());
        }
    }

    public void setOffValues(String input) {
        StringTokenizer tokens = new StringTokenizer(input);
        for (int i = 0; i < off.length; i++) {
            off[i] = (byte)RomAttributeParser.parseHexString(tokens.nextToken());
        }
    }
    
    public void colorize() { }
    public void cursorUp() { }
    public void cursorDown() { }
    public void cursorLeft() { }
    public void cursorRight() { }
    public void setAxisColor(Color color) { }    
}