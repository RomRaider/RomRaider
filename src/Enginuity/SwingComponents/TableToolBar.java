package Enginuity.SwingComponents;

import Enginuity.Maps.Table;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JToolBar;

public class TableToolBar extends JToolBar implements MouseListener {
    
    private JButton increment   = new JButton("  +  ");
    private JButton decrement   = new JButton("  -  ");
    private Table table;
    
    public TableToolBar(Table table) {
        this.table = table;
        this.setFloatable(false);
        this.add(increment);
        this.add(decrement);
        increment.addMouseListener(this);
        decrement.addMouseListener(this);
    }    

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == increment) {
            table.increment();
        } else if (e.getSource() == decrement) { 
            table.decrement();
        }
        table.colorize();
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
}