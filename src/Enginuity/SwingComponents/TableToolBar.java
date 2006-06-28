package Enginuity.SwingComponents;

import Enginuity.Maps.Table;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;

public class TableToolBar extends JToolBar implements MouseListener {
    
    private JButton   incrementFine   = new JButton(new ImageIcon("./graphics/icon-incfine.png"));
    private JButton   decrementFine   = new JButton(new ImageIcon("./graphics/icon-decfine.png"));
    private JButton   incrementCoarse = new JButton(new ImageIcon("./graphics/icon-inccoarse.png"));
    private JButton   decrementCoarse = new JButton(new ImageIcon("./graphics/icon-deccoarse.png"));   
    private JButton   setValue        = new JButton("Set");
    
    private JFormattedTextField incrementBy = new JFormattedTextField(new DecimalFormat("#"));
    private JFormattedTextField setValueText = new JFormattedTextField(new DecimalFormat("#.####"));    
    
    private Table table;
    private TableFrame frame;
    
    public TableToolBar(Table table, TableFrame frame) {
        this.table = table;
        this.frame = frame;
        this.setFloatable(false);
        this.add(incrementFine);
        this.add(decrementFine);
        this.add(new JLabel("    "));
        this.add(incrementCoarse);
        this.add(decrementCoarse);
        this.add(new JLabel(" "));
        this.add(incrementBy);
        this.add(new JLabel("    "));
        this.add(setValueText);
        this.add(new JLabel(" "));
        this.add(setValue);
                
        incrementFine.setMaximumSize(new Dimension(33,33));
        incrementFine.setBorder(new LineBorder(new Color(150,150,150), 1));
        decrementFine.setMaximumSize(new Dimension(33,33));
        decrementFine.setBorder(new LineBorder(new Color(150,150,150), 1));
        incrementCoarse.setMaximumSize(new Dimension(33,33));
        incrementCoarse.setBorder(new LineBorder(new Color(150,150,150), 1));
        decrementCoarse.setMaximumSize(new Dimension(33,33));
        decrementCoarse.setBorder(new LineBorder(new Color(150,150,150), 1));
        setValue.setMaximumSize(new Dimension(33,23));
        setValue.setBorder(new LineBorder(new Color(150,150,150), 1));
        
        incrementBy.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        incrementBy.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
        incrementBy.setMaximumSize(new Dimension(45, 23));
        setValueText.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        setValueText.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
        setValueText.setMaximumSize(new Dimension(45, 23));
        
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
        
        // key binding actions
        Action enterAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getTable().requestFocus();
                setValue();
            }
        };  
        
        // set input mapping
        InputMap im = getInputMap(this.WHEN_IN_FOCUSED_WINDOW);
        
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        
        im.put(enter, "enterAction");   
        getActionMap().put(im.get(enter), enterAction);
        
        incrementFine.getInputMap().put(enter, "enterAction");
        decrementFine.getInputMap().put(enter, "enterAction");
        incrementCoarse.getInputMap().put(enter, "enterAction");
        decrementCoarse.getInputMap().put(enter, "enterAction");
        incrementBy.getInputMap().put(enter, "enterAction");
        setValueText.getInputMap().put(enter, "enterAction");
        setValue.getInputMap().put(enter, "enterAction");
        incrementFine.getInputMap().put(enter, "enterAction");
    }    
    
    public Table getTable() {
        return table;
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == incrementCoarse) incrementCoarse();
        else if (e.getSource() == decrementCoarse) decrementCoarse();
        else if (e.getSource() == incrementFine) incrementFine();
        else if (e.getSource() == decrementFine) decrementFine();
        else if (e.getSource() == setValue) setValue();
        
        table.colorize();
    }
    
    public void setValue() {
        table.setRealValue(setValueText.getText()+"");
    }
        
    
    public void incrementFine() {
        if (table.getScale().getIncrement() > 0) table.increment(1);
        else table.increment(-1);        
    }
    
    public void decrementFine() {
        if (table.getScale().getIncrement() > 0) table.increment(-1);
        else table.increment(1);        
    }
    
    public void incrementCoarse() {
        table.increment(Integer.parseInt(incrementBy.getValue()+""));        
    }
    
    public void decrementCoarse() {
        table.increment(0 - Integer.parseInt(incrementBy.getValue()+""));        
    }
    
    public void setCoarseValue(int input) {
        incrementBy.setText(input+"");
        try {
            incrementBy.commitEdit();
        } catch (ParseException ex) { }
    }
    
    public void focusSetValue(char input) {
        setValueText.requestFocus();
        setValueText.setText(input+"");
    }
    
    public void setInputMap(InputMap im) {
        incrementFine.setInputMap(this.WHEN_FOCUSED, im);
        decrementFine.setInputMap(this.WHEN_FOCUSED, im);
        incrementCoarse.setInputMap(this.WHEN_FOCUSED, im);
        decrementCoarse.setInputMap(this.WHEN_FOCUSED, im);
        setValue.setInputMap(this.WHEN_FOCUSED, im);        
    }
    
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
}