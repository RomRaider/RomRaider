package enginuity.swing;

import enginuity.maps.Table;
import enginuity.maps.Table3D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

public class TableMenuBar extends JMenuBar implements ActionListener {
    
    private Table     table;    
    private JMenu     fileMenu  = new JMenu("Table");
    private JMenuItem compare   = new JMenuItem("Compare");
    private JMenuItem graph     = new JMenuItem("View Graph");
    private JMenuItem overlay   = new JMenuItem("Overlay Log");
    private JMenuItem close     = new JMenuItem("Close Table");
    private JMenu     editMenu  = new JMenu("Edit");
    private JMenuItem undoSel   = new JMenuItem("Undo Selected Changes");
    private JMenuItem undoAll   = new JMenuItem("Undo All Changes");
    private JMenuItem revert    = new JMenuItem("Set Revert Point");
    private JMenuItem copySel   = new JMenuItem("Copy Selection");
    private JMenuItem copyTable = new JMenuItem("Copy Table");
    private JMenuItem paste     = new JMenuItem("Paste");
    private JMenu     viewMenu  = new JMenu("View");
    private JMenuItem tableProperties = new JMenuItem("Table Properties");
    
    public TableMenuBar(Table table) {
        super();
        this.table = table;
        this.add(fileMenu);
        fileMenu.add(compare);
        fileMenu.add(graph);
        fileMenu.add(overlay);
        fileMenu.add(new JSeparator());
        fileMenu.add(close);
        close.setText("Close " + table.getName());
        
        this.add(editMenu);
        editMenu.add(undoSel);
        editMenu.add(undoAll);
        editMenu.add(revert);
        editMenu.add(new JSeparator());
        editMenu.add(copySel);
        editMenu.add(copyTable);
        editMenu.add(new JSeparator());
        editMenu.add(paste);
        editMenu.setMnemonic('E');
        copySel.setMnemonic('C');
        copyTable.setMnemonic('T');
        paste.setMnemonic('P');
        copySel.addActionListener(this);
        copyTable.addActionListener(this);
        paste.addActionListener(this);
        
        this.add(viewMenu);
        viewMenu.add(tableProperties);
        viewMenu.setMnemonic('V');
        tableProperties.setMnemonic('P');        
        tableProperties.addActionListener(this);
        
        compare.addActionListener(this);
        graph.addActionListener(this);
        overlay.addActionListener(this);
        undoSel.addActionListener(this);
        undoAll.addActionListener(this);
        revert.addActionListener(this);
        close.addActionListener(this);
        
        fileMenu.setMnemonic('F');
        fileMenu.setMnemonic('T');
        compare.setMnemonic('P');
        graph.setMnemonic('G');
        overlay.setMnemonic('L');
        undoSel.setMnemonic('U');
        undoAll.setMnemonic('A');
        revert.setMnemonic('R');
        close.setMnemonic('C');
        
        compare.setEnabled(false);
        graph.setEnabled(false);
        overlay.setEnabled(false);
    }   
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == undoAll) {
            table.undoAll();
        } else if (e.getSource() == revert) {
            table.setRevertPoint();
        } else if (e.getSource() == undoSel) {
            table.undoSelected();
        } else if (e.getSource() == close) {
            table.getFrame().dispose();
        } else if (e.getSource() == tableProperties) {
            new JOptionPane().showMessageDialog(table, (Object)(new TablePropertyPanel(table)),
                    table.getName() + " Table Properties", JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getSource() == copySel) {
            table.copySelection();
        } else if (e.getSource() == copyTable) {
            table.copyTable();
        } else if (e.getSource() == paste) {
            table.paste();
        }
    }
}