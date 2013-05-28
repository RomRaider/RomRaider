package com.romraider.swing;

import javax.swing.JRadioButtonMenuItem;

public class TableMenuItem extends JRadioButtonMenuItem{

    private static final long serialVersionUID = -3618983591185294967L;
    private final TableFrame tableFrame;

    public TableMenuItem(TableFrame tableFrame) {
        super(tableFrame.getTitle());
        this.tableFrame = tableFrame;
    }

    public TableFrame getFrame() {
        return this.tableFrame;
    }
}
