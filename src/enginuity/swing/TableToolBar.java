package enginuity.swing;

import enginuity.maps.Scale;
import enginuity.maps.Table;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;

public class TableToolBar extends JToolBar implements MouseListener, ItemListener {
    
    private JButton   incrementFine   = new JButton(new ImageIcon("./graphics/icon-incfine.png"));
    private JButton   decrementFine   = new JButton(new ImageIcon("./graphics/icon-decfine.png"));
    private JButton   incrementCoarse = new JButton(new ImageIcon("./graphics/icon-inccoarse.png"));
    private JButton   decrementCoarse = new JButton(new ImageIcon("./graphics/icon-deccoarse.png"));   
    private JButton   setValue        = new JButton("Set");
    
    private JFormattedTextField incrementByFine   = new JFormattedTextField(new DecimalFormat("#.####"));
    private JFormattedTextField incrementByCoarse = new JFormattedTextField(new DecimalFormat("#.####"));
    private JFormattedTextField setValueText      = new JFormattedTextField(new DecimalFormat("#.####"));    
    
    private JComboBox scaleSelection = new JComboBox();
    
    private Table table;
    private TableFrame frame;
    
    public TableToolBar(Table table, TableFrame frame) {
        this.table = table;
        this.setFrame(frame);
        this.setFloatable(false);
        this.add(incrementFine);
        this.add(decrementFine);
        this.add(incrementByFine);
        this.add(new JLabel("    "));
        this.add(incrementCoarse);
        this.add(decrementCoarse);
        this.add(new JLabel(" "));
        this.add(incrementByCoarse);
        this.add(new JLabel("    "));
        this.add(setValueText);
        this.add(new JLabel(" "));
        this.add(setValue);
        this.add(new JLabel(" "));
        this.add(scaleSelection);
                
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
        scaleSelection.setMaximumSize(new Dimension(80,23));
        scaleSelection.setFont(new Font("Tahoma", Font.PLAIN, 11));
        
        incrementByFine.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        incrementByFine.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
        incrementByFine.setMaximumSize(new Dimension(45, 23));
        incrementByCoarse.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        incrementByCoarse.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
        incrementByCoarse.setMaximumSize(new Dimension(45, 23));
        setValueText.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        setValueText.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
        setValueText.setMaximumSize(new Dimension(45, 23));
        
        incrementFine.setToolTipText("Increment Value (Fine)");
        decrementFine.setToolTipText("Decrement Value (Fine)");
        incrementCoarse.setToolTipText("Increment Value (Coarse)");
        decrementCoarse.setToolTipText("Decrement Value (Coarse)");
        setValue.setToolTipText("Set Absolute Value");
        setValueText.setToolTipText("Set Absolute Value");
        incrementByFine.setToolTipText("Fine Value Adjustment");
        incrementByCoarse.setToolTipText("Coarse Value Adjustment");
        
        incrementFine.addMouseListener(this);
        decrementFine.addMouseListener(this);        
        incrementCoarse.addMouseListener(this);
        decrementCoarse.addMouseListener(this);
        setValue.addMouseListener(this);
        scaleSelection.addItemListener(this);
        
        try {
            incrementByFine.setValue(Math.abs(table.getScale().getFineIncrement()));
            incrementByCoarse.setValue(Math.abs(table.getScale().getCoarseIncrement()));
        } catch (Exception ex) {
            // scaling units haven't been added yet -- no problem
        }
        
        // key binding actions
        Action enterAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getTable().requestFocus();
                setValue();
            }
        };  
        
        // set input mapping
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        
        im.put(enter, "enterAction");   
        getActionMap().put(im.get(enter), enterAction);
        
        incrementFine.getInputMap().put(enter, "enterAction");
        decrementFine.getInputMap().put(enter, "enterAction");
        incrementCoarse.getInputMap().put(enter, "enterAction");
        decrementCoarse.getInputMap().put(enter, "enterAction");
        incrementByFine.getInputMap().put(enter, "enterAction");
        incrementByCoarse.getInputMap().put(enter, "enterAction");
        setValueText.getInputMap().put(enter, "enterAction");
        setValue.getInputMap().put(enter, "enterAction");
        incrementFine.getInputMap().put(enter, "enterAction");
        
        setScales(table.getScales());
    }    
    
    public Table getTable() {
        return table;
    }
    
    public void setScales(Vector<Scale> scales) {
        for (int i = 0; i < scales.size(); i++) {
            scaleSelection.addItem(scales.get(i).getName());
        }
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
        table.increment(Double.parseDouble(incrementByFine.getValue()+""));   
    }
    
    public void decrementFine() {
        table.increment(0 - Double.parseDouble(incrementByFine.getValue()+"")); 
    }
    
    public void incrementCoarse() {
        table.increment(Double.parseDouble(incrementByCoarse.getValue()+""));        
    }
    
    public void decrementCoarse() {
        table.increment(0 - Double.parseDouble(incrementByCoarse.getValue()+""));        
    }
    
    public void setCoarseValue(double input) {
        incrementByCoarse.setText(input+"");
        try {
            incrementByCoarse.commitEdit();
        } catch (ParseException ex) { }
    }
    
    public void setFineValue(double input) {
        incrementByFine.setText(input+"");
        try {
            incrementByFine.commitEdit();
        } catch (ParseException ex) { }
    }
    
    public void focusSetValue(char input) {
        setValueText.requestFocus();
        setValueText.setText(input+"");
    }
    
    public void setInputMap(InputMap im) {
        incrementFine.setInputMap(WHEN_FOCUSED, im);
        decrementFine.setInputMap(WHEN_FOCUSED, im);
        incrementCoarse.setInputMap(WHEN_FOCUSED, im);
        decrementCoarse.setInputMap(WHEN_FOCUSED, im);
        setValue.setInputMap(WHEN_FOCUSED, im);        
    }
    
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

    public TableFrame getFrame() {
        return frame;
    }

    public void setFrame(TableFrame frame) {
        this.frame = frame;
    }
    
    public void itemStateChanged(ItemEvent e) {
        // scale changed
        if (e.getSource() == scaleSelection) {
            table.setScaleIndex(scaleSelection.getSelectedIndex());
        }
    }    
}