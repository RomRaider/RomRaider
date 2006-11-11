package enginuity.maps;

import enginuity.Settings;
import enginuity.xml.RomAttributeParser;

import javax.swing.*;
import java.awt.*;
import java.util.StringTokenizer;

public class TableSwitch extends Table {

    private byte[] on = new byte[0];
    private byte[] off = new byte[0];
    private JCheckBox checkbox = new JCheckBox("Enabled", true); // checkbox selected by default

    public TableSwitch(Settings settings) {
        super(settings);
        storageType = 1;
        removeAll();
        setLayout(new BorderLayout());
    }

    public void setDataSize(int size) {
        if (on.length == 0) {
            on = new byte[size];
            off = new byte[size];
        }
    }

    public void populateTable(byte[] input) {
        for (int i = 0; i < on.length; i++) {

            // check each byte -- if it doesn't match "on", it's off
            if (!beforeRam) {
                ramOffset = container.getRomID().getRamOffset();
            }

            if (on[i] != input[storageAddress - ramOffset + i]) {
                checkbox.setSelected(false);
                break;
            }
        }
    }

    public void setName(String name) {
        super.setName(name);
        checkbox.setText("Enable " + name);

        add(checkbox, BorderLayout.NORTH);
    }

    public void setDescription(String description) {
        super.setDescription(description);
        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setOpaque(false);
        descriptionArea.setEditable(false);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);

        add(descriptionArea, BorderLayout.CENTER);
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
            on[i] = (byte) RomAttributeParser.parseHexString(tokens.nextToken());
        }
    }

    public void setOffValues(String input) {
        StringTokenizer tokens = new StringTokenizer(input);
        for (int i = 0; i < off.length; i++) {
            off[i] = (byte) RomAttributeParser.parseHexString(tokens.nextToken());
        }
    }

    public Dimension getFrameSize() {
        int height = verticalOverhead + 75;
        int width = horizontalOverhead;
        if (height < minHeight) {
            height = minHeight;
        }
        if (width < minWidth) {
            width = minWidth;
        }

        return new Dimension(width, height);
    }

    public void colorize() {
    }

    public void cursorUp() {
    }

    public void cursorDown() {
    }

    public void cursorLeft() {
    }

    public void cursorRight() {
    }

    public void setAxisColor(Color color) {
    }
}