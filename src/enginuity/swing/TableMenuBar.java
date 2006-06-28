package enginuity.swing;

import enginuity.maps.Table;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

public class TableMenuBar extends JMenuBar implements ActionListener {
    
    private Table     table;    
    private JMenu     fileMenu        = new JMenu("Table");
    private JMenuItem graph           = new JMenuItem("View Graph");
    private JMenuItem overlay         = new JMenuItem("Overlay Log");

    private JMenu     compareMenu                = new JMenu("Compare");
    private JRadioButtonMenuItem compareOriginal = new JRadioButtonMenuItem("Show Changes");
    private JRadioButtonMenuItem compareMap      = new JRadioButtonMenuItem("Compare to Another Map");
    private JRadioButtonMenuItem compareOff      = new JRadioButtonMenuItem("Off");   
    private JMenu     compareDisplay             = new JMenu("Display");
    private JRadioButtonMenuItem comparePercent  = new JRadioButtonMenuItem("Percent Difference");
    private JRadioButtonMenuItem compareAbsolute = new JRadioButtonMenuItem("Absolute Difference");
    
    private JMenuItem close           = new JMenuItem("Close Table");
    private JMenu     editMenu        = new JMenu("Edit");
    private JMenuItem undoSel         = new JMenuItem("Undo Selected Changes");
    private JMenuItem undoAll         = new JMenuItem("Undo All Changes");
    private JMenuItem revert          = new JMenuItem("Set Revert Point");
    private JMenuItem copySel         = new JMenuItem("Copy Selection");
    private JMenuItem copyTable       = new JMenuItem("Copy Table");
    private JMenuItem paste           = new JMenuItem("Paste");
    private JMenu     viewMenu        = new JMenu("View");
    private JMenuItem tableProperties = new JMenuItem("Table Properties");
    
    private ButtonGroup compareGroup  = new ButtonGroup();
    private ButtonGroup compareDisplayGroup = new ButtonGroup();
        
    public TableMenuBar(Table table) {
        super();
        this.table = table;
        this.add(fileMenu);
        fileMenu.add(graph);
        fileMenu.add(overlay);
        fileMenu.add(compareMenu);
        compareMenu.add(compareOriginal);
        compareMenu.add(compareMap);
        compareMenu.add(compareOff);
        compareMenu.add(new JSeparator());
        compareMenu.add(compareDisplay);
        compareDisplay.add(comparePercent);
        compareDisplay.add(compareAbsolute);
        fileMenu.add(new JSeparator());
        fileMenu.add(close);
        close.setText("Close " + table.getName());
        
        compareMenu.setMnemonic('C');
        compareOriginal.setMnemonic('C');
        compareMap.setMnemonic('M');
        compareOff.setMnemonic('O');
        compareOff.setSelected(true);
        compareDisplay.setMnemonic('D');
        comparePercent.setMnemonic('P');
        compareAbsolute.setMnemonic('A');
        compareAbsolute.setSelected(true);
        
        compareGroup.add(compareOriginal);
        compareGroup.add(compareMap);
        compareGroup.add(compareOff);
        
        compareDisplayGroup.add(comparePercent);
        compareDisplayGroup.add(compareAbsolute);
        
        compareOriginal.addActionListener(this);
        compareMap.addActionListener(this);
        compareOff.addActionListener(this);
        comparePercent.addActionListener(this);
        compareAbsolute.addActionListener(this);
        
        // FOR 0.2.7.4b ONLY!! DELETE FOR 0.2.8b!
        compareMap.setEnabled(false);
        
        
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
        
        graph.addActionListener(this);
        overlay.addActionListener(this);
        undoSel.addActionListener(this);
        undoAll.addActionListener(this);
        revert.addActionListener(this);
        close.addActionListener(this);
        
        fileMenu.setMnemonic('F');
        fileMenu.setMnemonic('T');
        graph.setMnemonic('G');
        overlay.setMnemonic('L');
        undoSel.setMnemonic('U');
        undoAll.setMnemonic('A');
        revert.setMnemonic('R');
        close.setMnemonic('X');
        
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
            JOptionPane.showMessageDialog(table, (Object)(new TablePropertyPanel(table)),
                    table.getName() + " Table Properties", JOptionPane.INFORMATION_MESSAGE);
            
        } else if (e.getSource() == copySel) {
            table.copySelection();
            
        } else if (e.getSource() == copyTable) {
            table.copyTable();
            
        } else if (e.getSource() == paste) {
            table.paste();
            
        } else if (e.getSource() == compareOff) {
            table.compare(Table.COMPARE_OFF);
            
        } else if (e.getSource() == compareOriginal) {
            table.compare(Table.COMPARE_ORIGINAL);
            
        } else if (e.getSource() == compareMap) {
            JTableChooser chooser = new JTableChooser();
            if (chooser.showChooser(table.getRom().getContainer().getImages(), table.getRom().getContainer(), table) == true) {
                table.pasteCompare();
                table.compare(Table.COMPARE_TABLE);
            }            
            
        } else if (e.getSource() == compareAbsolute) {
            table.setCompareDisplay(Table.COMPARE_ABSOLUTE);
            
        } else if (e.getSource() == comparePercent) {
            table.setCompareDisplay(Table.COMPARE_PERCENT);
            
        }
    }
}