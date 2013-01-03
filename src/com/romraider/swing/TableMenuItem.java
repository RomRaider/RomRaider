package com.romraider.swing;

import javax.swing.JRadioButtonMenuItem;

import com.romraider.maps.Table;

public class TableMenuItem extends JRadioButtonMenuItem{

    private static final long serialVersionUID = -3618983591185294967L;
    private final Table table;

    public TableMenuItem(Table table) {
        super(table.getFrame().getTitle());
        this.table = table;
    }

    public Table getTable() {
        return this.table;
    }
}
