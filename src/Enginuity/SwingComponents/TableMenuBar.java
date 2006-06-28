package Enginuity.SwingComponents;

import Enginuity.Maps.Table;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class TableMenuBar extends JMenuBar implements ActionListener {
    
    private Table     table;    
    private JMenu     fileMenu  = new JMenu("Table");
    private JMenuItem compare   = new JMenuItem("Compare");
    private JMenuItem copy      = new JMenuItem("Copy");
    private JMenuItem graph     = new JMenuItem("View Graph");
    private JMenuItem overlay   = new JMenuItem("Overlay Log");
    private JMenuItem undoSel   = new JMenuItem("Undo Selected Changes");
    private JMenuItem undoAll   = new JMenuItem("Undo All Changes");
    private JMenuItem revert    = new JMenuItem("Set Revert Point");
    private JMenuItem close     = new JMenuItem("Close Table");
    
    public TableMenuBar(Table table) {
        super();
        this.table = table;
        this.add(fileMenu);
        close.setText("Close " + table.getName());
        fileMenu.add(compare);
        fileMenu.add(copy);
        fileMenu.add(graph);
        fileMenu.add(overlay);
        fileMenu.add(new JSeparator());
        fileMenu.add(undoSel);
        fileMenu.add(undoAll);
        fileMenu.add(revert);
        fileMenu.add(new JSeparator());
        fileMenu.add(close);
        
        compare.addActionListener(this);
        graph.addActionListener(this);
        overlay.addActionListener(this);
        copy.addActionListener(this);
        undoSel.addActionListener(this);
        undoAll.addActionListener(this);
        revert.addActionListener(this);
        close.addActionListener(this);
        
        fileMenu.setMnemonic('F');
        fileMenu.setMnemonic('T');
        compare.setMnemonic('P');
        graph.setMnemonic('G');
        overlay.setMnemonic('L');
        copy.setMnemonic('O');
        undoSel.setMnemonic('U');
        undoAll.setMnemonic('A');
        revert.setMnemonic('R');
        close.setMnemonic('C');
        
        compare.setEnabled(false);
        graph.setEnabled(false);
        overlay.setEnabled(false);
        copy.setEnabled(false);
    }    

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == undoAll) {
            table.undoAll();
            System.out.println("undo");
        } else if (e.getSource() == revert) {
            table.setRevertPoint();
        } else if (e.getSource() == undoSel) {
            table.undoSelected();
        } else if (e.getSource() == close) {
            table.getFrame().setVisible(false);
        }
    }
}