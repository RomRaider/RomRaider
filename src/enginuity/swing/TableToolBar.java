package enginuity.swing;

import com.ecm.graphics.Graph3dFrameManager;
import com.ecm.graphics.data.GraphData;
import com.ecm.graphics.data.GraphDataListener;
import enginuity.maps.DataCell;
import enginuity.maps.Scale;
import enginuity.maps.Table;
import enginuity.maps.Table1D;
import enginuity.maps.Table3D;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Vector;

public class TableToolBar extends JToolBar implements MouseListener, ItemListener, GraphDataListener {

    private JButton incrementFine = new JButton(new ImageIcon("./graphics/icon-incfine.png"));
    private JButton decrementFine = new JButton(new ImageIcon("./graphics/icon-decfine.png"));
    private JButton incrementCoarse = new JButton(new ImageIcon("./graphics/icon-inccoarse.png"));
    private JButton decrementCoarse = new JButton(new ImageIcon("./graphics/icon-deccoarse.png"));
    private JButton enable3d = new JButton(new ImageIcon("./graphics/3d_render.png"));

    private JButton setValue = new JButton("Set");
    private JButton multiply = new JButton("Mul");

    private JFormattedTextField incrementByFine = new JFormattedTextField(new DecimalFormat("#.####"));
    private JFormattedTextField incrementByCoarse = new JFormattedTextField(new DecimalFormat("#.####"));
    private JFormattedTextField setValueText = new JFormattedTextField(new DecimalFormat("#.####"));

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
        this.add(multiply);
        this.add(new JLabel("    "));

        //Only add the 3d button if table includes 3d data
        if (table.getType() == Table.TABLE_3D) {
            this.add(enable3d);
        }

        this.add(new JLabel(" "));
        //this.add(scaleSelection);

        incrementFine.setMaximumSize(new Dimension(33, 33));
        incrementFine.setBorder(new LineBorder(new Color(150, 150, 150), 1));
        decrementFine.setMaximumSize(new Dimension(33, 33));
        decrementFine.setBorder(new LineBorder(new Color(150, 150, 150), 1));
        incrementCoarse.setMaximumSize(new Dimension(33, 33));
        incrementCoarse.setBorder(new LineBorder(new Color(150, 150, 150), 1));
        decrementCoarse.setMaximumSize(new Dimension(33, 33));
        decrementCoarse.setBorder(new LineBorder(new Color(150, 150, 150), 1));
        enable3d.setMaximumSize(new Dimension(33, 33));
        enable3d.setBorder(new LineBorder(new Color(150, 150, 150), 1));
        setValue.setMaximumSize(new Dimension(33, 23));
        setValue.setBorder(new LineBorder(new Color(150, 150, 150), 1));
        multiply.setMaximumSize(new Dimension(33, 23));
        multiply.setBorder(new LineBorder(new Color(150, 150, 150), 1));
        scaleSelection.setMaximumSize(new Dimension(80, 23));
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
        enable3d.setToolTipText("Render data in 3d");
        setValue.setToolTipText("Set Absolute Value");
        setValueText.setToolTipText("Set Absolute Value");
        incrementByFine.setToolTipText("Fine Value Adjustment");
        incrementByCoarse.setToolTipText("Coarse Value Adjustment");
        multiply.setToolTipText("Multiply Value");

        incrementFine.addMouseListener(this);
        decrementFine.addMouseListener(this);
        incrementCoarse.addMouseListener(this);
        decrementCoarse.addMouseListener(this);
        enable3d.addMouseListener(this);
        setValue.addMouseListener(this);
        multiply.addMouseListener(this);
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

        // remove item listener to avoid null pointer exception when populating
        scaleSelection.removeItemListener(this);

        for (int i = 0; i < scales.size(); i++) {
            scaleSelection.addItem(scales.get(i).getName());
        }

        // and put it back
        scaleSelection.addItemListener(this);
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == incrementCoarse) {
            incrementCoarse();
        } else if (e.getSource() == decrementCoarse) {
            decrementCoarse();
        } else if (e.getSource() == enable3d) {
            enable3d();
        } else if (e.getSource() == incrementFine) {
            incrementFine();
        } else if (e.getSource() == decrementFine) {
            decrementFine();
        } else if (e.getSource() == multiply) {
            multiply();
        } else if (e.getSource() == setValue) {
            setValue();
        }

        table.colorize();
    }

    public void setValue() {
        table.setRealValue(setValueText.getText());
    }

    public void multiply() {
        table.multiply(Double.parseDouble(setValueText.getText()));
    }

    public void incrementFine() {
        table.increment(Double.parseDouble(String.valueOf(incrementByFine.getValue())));
    }

    public void decrementFine() {
        table.increment(0 - Double.parseDouble(String.valueOf(incrementByFine.getValue())));
    }

    public void incrementCoarse() {
        table.increment(Double.parseDouble(String.valueOf(incrementByCoarse.getValue())));
    }

    public void decrementCoarse() {
        table.increment(0 - Double.parseDouble(String.valueOf(incrementByCoarse.getValue())));
    }

    /**
     * Method launches a 3d Frame.
     */
    public void enable3d() {
        int rowCount = 0;
        int valueCount = 0;

        //Pull data into format 3d graph understands
        Vector graphValues = new Vector();
        if (table.getType() == Table.TABLE_3D) {
            Table3D table3d = (Table3D) table;
            DataCell[][] tableData = table3d.get3dData();
            valueCount = tableData.length;
            DataCell[] dataRow = tableData[0];
            rowCount = dataRow.length;

            for (int j = (rowCount - 1); j >= 0; j--) {
                float[] rowValues = new float[valueCount];
                for (int i = 0; i < valueCount; i++) {
                    DataCell theCell = tableData[i][j];
                    rowValues[i] = (float) theCell.getValue();
                    //float theValue = (float)theCell.getValue();
                    //BigDecimal finalRoundedValue = new BigDecimal(theValue).setScale(2,BigDecimal.ROUND_HALF_UP);
                    //rowValues[i] = finalRoundedValue.floatValue();
                }
                graphValues.add(rowValues);
            }

            Table1D xAxisTable1D = ((Table3D) table).getXAxis();
            Table1D yAxisTable1D = ((Table3D) table).getYAxis();

            //Gather x axis values
            DataCell[] dataCells = xAxisTable1D.getData();
            int length = dataCells.length;
            double[] xValues = new double[length];

            for (int i = 0; i < length; i++) {
                xValues[i] = dataCells[i].getValue();
                //double theValue = dataCells[i].getValue();
                //BigDecimal finalRoundedValue = new BigDecimal(theValue).setScale(2,BigDecimal.ROUND_HALF_UP);
                //xValues[i] = finalRoundedValue.doubleValue();
            }

            //Gather y/z axis values
            dataCells = yAxisTable1D.getData();
            length = dataCells.length;
            double[] yValues = new double[length];

            for (int i = 0; i < length; i++) {
                double theValue = dataCells[i].getValue();
                BigDecimal finalRoundedValue = new BigDecimal(theValue).setScale(2, BigDecimal.ROUND_HALF_UP);
                yValues[i] = finalRoundedValue.doubleValue();
            }

            //Define Labels for graph
            String xLabel = ((Table3D) table).getXAxis().getName();
            String zLabel = ((Table3D) table).getYAxis().getName();
            String yLabel = ((Table3D) table).getCategory();

            //TODO Figure out mix between heavy weight and lightweight components
            //Below is initial work on making graph3d a JInternal Frame
            /*
               Graph3dJPanel graph3dJPanel = new Graph3dJPanel(graphValues, testX, testZ,xLabel, yLabel, zLabel);
               graph3dJPanel.addModifiedDataListener(this);
               JInternalFrame graphFrame = new JInternalFrame();
               graphFrame.add(graph3dJPanel);
               graphFrame.setSize(200, 200);

               graphFrame.setFrameIcon(null);
               graphFrame.setBorder(BorderFactory.createBevelBorder(0));
               graphFrame.setVisible(true);
               graphFrame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
               ECUEditorManager.getECUEditor().rightPanel.add(graphFrame);
            */
            
            
            
            
            double maxV = table.getScale().getMax();
            double minV = table.getScale().getMin();
            //TODO Remove this when above is working
            //***********
            minV = 0.0;
            maxV = 13.01;
            System.out.println("Scale: " + maxV + "," + minV);
            //***********
            
           
            //Render 3d
            Graph3dFrameManager.openGraph3dFrame(graphValues, minV, maxV, xValues, yValues, xLabel, yLabel, zLabel, table.getName());
            GraphData.addGraphDataListener(this);
        }
    }

    public void setCoarseValue(double input) {
        incrementByCoarse.setText(String.valueOf(input));
        try {
            incrementByCoarse.commitEdit();
        } catch (ParseException ex) {
        }
    }

    public void setFineValue(double input) {
        incrementByFine.setText(String.valueOf(input));
        try {
            incrementByFine.commitEdit();
        } catch (ParseException ex) {
        }
    }

    public void focusSetValue(char input) {
        setValueText.requestFocus();
        setValueText.setText(String.valueOf(input));
    }

    public void setInputMap(InputMap im) {
        incrementFine.setInputMap(WHEN_FOCUSED, im);
        decrementFine.setInputMap(WHEN_FOCUSED, im);
        incrementCoarse.setInputMap(WHEN_FOCUSED, im);
        decrementCoarse.setInputMap(WHEN_FOCUSED, im);
        setValue.setInputMap(WHEN_FOCUSED, im);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

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


    // ******************************************
    // Code for listening to graph3d data changes
    // ******************************************
    public void newGraphData(int x, int z, float value) {
        Table3D table3d = (Table3D) table;
        table3d.selectCellAt(x, table3d.getSizeY() - z - 1);

        //Set the value
        table.setRealValue(String.valueOf(value));
    }

    public void selectStateChange(int x, int z, boolean value) {
        if (value) {
            Table3D table3d = (Table3D) table;
            table3d.selectCellAtWithoutClear(x, table3d.getSizeY() - z - 1);
        } else {
            Table3D table3d = (Table3D) table;
            table3d.deSelectCellAt(x, table3d.getSizeY() - z - 1);
        }
    }
}