package Enginuity.SwingComponents;

import Enginuity.Maps.Table;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

public class TableFrame extends JInternalFrame implements InternalFrameListener {
    
    private Table table;
    private TableToolBar toolBar;
    
    public TableFrame(Table table) {
        super(table.getContainer().getFileName() + " - " + table.getName(), true, true);
        this.setTable(table);
        this.add(table);
        this.setFrameIcon(null);
        this.setBorder(BorderFactory.createBevelBorder(0));
        this.setVisible(false);
        toolBar = new TableToolBar(table, this);
        this.add(toolBar, BorderLayout.NORTH);
        this.setJMenuBar(new TableMenuBar(table));
        this.setDefaultCloseOperation(this.HIDE_ON_CLOSE);
        table.setFrame(this);
        this.addInternalFrameListener(this);
        
    }
    
    public TableToolBar getToolBar() {
        return toolBar;
    }
    
    public void internalFrameActivated(InternalFrameEvent e) {
        getTable().getContainer().getContainer().setLastSelectedRom(getTable().getContainer());
    }    


    public void internalFrameOpened(InternalFrameEvent e) { }
    public void internalFrameClosing(InternalFrameEvent e) { 
        getTable().getContainer().getContainer().removeDisplayTable(this);
    }
    public void internalFrameClosed(InternalFrameEvent e) { }
    public void internalFrameIconified(InternalFrameEvent e) { }
    public void internalFrameDeiconified(InternalFrameEvent e) { }
    public void internalFrameDeactivated(InternalFrameEvent e) { }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}