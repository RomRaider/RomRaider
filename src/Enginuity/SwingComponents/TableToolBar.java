package Enginuity.SwingComponents;

import Enginuity.Maps.Table;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

public class TableToolBar extends JToolBar implements MouseListener {
    
    private JLabel    fineLabel       = new JLabel(" Fine ");
    private JButton   incrementFine   = new JButton("  +  ");
    private JButton   decrementFine   = new JButton("  -  ");
    private JLabel    coarseLabel     = new JLabel(" Coarse ");
    private JButton   incrementCoarse = new JButton("  +  ");
    private JButton   decrementCoarse = new JButton("  -  ");   
    private JButton   setValue        = new JButton("Set Value");
    
    private JFormattedTextField incrementBy = new JFormattedTextField(new DecimalFormat("#"));
    private JFormattedTextField setValueText = new JFormattedTextField(new DecimalFormat("#.#"));    
    
    private Table table;
    
    public TableToolBar(Table table) {
        this.table = table;
        this.setFloatable(false);
        this.add(fineLabel);
        this.add(incrementFine);
        this.add(decrementFine);
        this.add(new JLabel("    "));
        this.add(coarseLabel);
        this.add(incrementCoarse);
        this.add(decrementCoarse);
        this.add(new JLabel(" "));
        this.add(incrementBy);
        this.add(new JLabel("    "));
        this.add(setValueText);
        this.add(new JLabel(" "));
        this.add(setValue);
        
        incrementBy.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        incrementBy.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
        incrementBy.setMaximumSize(new Dimension(35, 23));
        setValueText.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        setValueText.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
        setValueText.setMaximumSize(new Dimension(35, 23));
        
        incrementFine.setToolTipText("Increment Value (Fine)");
        decrementFine.setToolTipText("Decrement Value (Fine)");
        incrementCoarse.setToolTipText("Increment Value (Coarse)");
        decrementCoarse.setToolTipText("Decrement Value (Coarse)");
        setValue.setToolTipText("Set Absolute Value");
        setValueText.setToolTipText("Set Absolute Value");
        incrementBy.setToolTipText("Coarse Value Adjustment");
        
        incrementFine.addMouseListener(this);
        decrementFine.addMouseListener(this);        
        incrementCoarse.addMouseListener(this);
        decrementCoarse.addMouseListener(this);
        setValue.addMouseListener(this);
        
        incrementBy.setValue(Math.abs(table.getScale().getIncrement()));
    }    

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == incrementCoarse) {
            table.increment(Integer.parseInt(incrementBy.getValue()+""));
        } else if (e.getSource() == decrementCoarse) { 
            table.increment(0 - Integer.parseInt(incrementBy.getValue()+""));
        } else if (e.getSource() == incrementFine) {
            if (table.getScale().getIncrement() > 0) table.increment(1);
            else table.increment(-1);
        } else if (e.getSource() == decrementFine) {
            table.increment(1);       
        } else if (e.getSource() == setValue) {
            table.setRealValue(setValueText.getValue()+"");
        }
        table.colorize();
    }
    
    public void setCoarseValue(int input) {
        incrementBy.setText(input+"");
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
}