package enginuity.swing;

import enginuity.maps.Table;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;

public class TableFrame extends JInternalFrame implements InternalFrameListener {

    private Table table;
    private TableToolBar toolBar;

    public TableFrame(Table table) {
        super(table.getRom().getFileName() + " - " + table.getName(), true, true);
        setTable(table);
        add(table);
        setFrameIcon(null);
        setBorder(BorderFactory.createBevelBorder(0));
        setVisible(false);
        toolBar = new TableToolBar(table, this);
        add(toolBar, BorderLayout.NORTH);
        setJMenuBar(new TableMenuBar(table));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        table.setFrame(this);
        addInternalFrameListener(this);
    }

    public TableToolBar getToolBar() {
        return toolBar;
    }

    public void internalFrameActivated(InternalFrameEvent e) {
        getTable().getRom().getContainer().setLastSelectedRom(getTable().getRom());
    }


    public void internalFrameOpened(InternalFrameEvent e) {
    }

    public void internalFrameClosing(InternalFrameEvent e) {
        getTable().getRom().getContainer().removeDisplayTable(this);
    }

    public void internalFrameClosed(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}