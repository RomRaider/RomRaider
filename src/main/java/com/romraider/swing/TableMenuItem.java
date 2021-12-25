package com.romraider.swing;

import javax.swing.JRadioButtonMenuItem;

import com.romraider.maps.Table;

public class TableMenuItem extends JRadioButtonMenuItem{

    private static final long serialVersionUID = -3618983591185294967L;
    Table table;
    
    public TableMenuItem(Table t) {
        super(t.getRom().getFileName());
    	this.table = t;
    }
    
    public Table getTable() {
    	return table;
    }
}
