package enginuity.swing;

import ZoeloeSoft.projects.JFontChooser.JFontChooser;
import enginuity.ECUEditor;
import enginuity.Settings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class SettingsForm extends JFrame implements MouseListener {
    
    Settings settings;
    ECUEditor parent;
    
    public SettingsForm(ECUEditor parent) {
        this.parent = parent;        
        settings = parent.getSettings();   
        initComponents(); 
        initSettings();

        maxColor.addMouseListener(this);
        minColor.addMouseListener(this);
        highlightColor.addMouseListener(this);
        axisColor.addMouseListener(this);
        increaseColor.addMouseListener(this);
        decreaseColor.addMouseListener(this);
        
        btnOk.addMouseListener(this);
        btnApply.addMouseListener(this);
        btnCancel.addMouseListener(this);
        btnEcuDefinitionBrowse.addMouseListener(this);
        btnChooseFont.addMouseListener(this);
        reset.addMouseListener(this);
    }
    
    private void initSettings() {
        
        ecuDefinitionFile.setText(this.settings.getEcuDefinitionFile().getAbsolutePath());
        
        obsoleteWarning.setSelected(settings.isObsoleteWarning());
        calcConflictWarning.setSelected(settings.isCalcConflictWarning());
        singleTableView.setSelected(settings.isSingleTableView());
        debug.setSelected(settings.isDebug());
        
        maxColor.setBackground(settings.getMaxColor());
        minColor.setBackground(settings.getMinColor());
        highlightColor.setBackground(settings.getHighlightColor());
        axisColor.setBackground(settings.getAxisColor());
        increaseColor.setBackground(settings.getIncreaseBorder());
        decreaseColor.setBackground(settings.getDecreaseBorder());
        
        cellWidth.setText(((int)settings.getCellSize().getWidth())+"");
        cellHeight.setText(((int)settings.getCellSize().getHeight())+"");
        
        btnChooseFont.setFont(settings.getTableFont());
        btnChooseFont.setText(settings.getTableFont().getFontName());
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblEcuDef = new javax.swing.JLabel();
        ecuDefinitionFile = new javax.swing.JTextField();
        btnEcuDefinitionBrowse = new javax.swing.JButton();
        lblMax = new javax.swing.JLabel();
        lblMin = new javax.swing.JLabel();
        lblHighlight = new javax.swing.JLabel();
        lblAxis = new javax.swing.JLabel();
        maxColor = new javax.swing.JLabel();
        minColor = new javax.swing.JLabel();
        highlightColor = new javax.swing.JLabel();
        axisColor = new javax.swing.JLabel();
        lblIncrease = new javax.swing.JLabel();
        lblDecrease = new javax.swing.JLabel();
        lblBackgrounds = new javax.swing.JLabel();
        increaseColor = new javax.swing.JLabel();
        obsoleteWarning = new javax.swing.JCheckBox();
        calcConflictWarning = new javax.swing.JCheckBox();
        debug = new javax.swing.JCheckBox();
        btnCancel = new javax.swing.JButton();
        btnOk = new javax.swing.JButton();
        btnApply = new javax.swing.JButton();
        decreaseColor = new javax.swing.JLabel();
        lblBorders = new javax.swing.JLabel();
        lblCellSize = new javax.swing.JLabel();
        lblCellHeight = new javax.swing.JLabel();
        lblCellWidth = new javax.swing.JLabel();
        cellWidth = new javax.swing.JTextField();
        cellHeight = new javax.swing.JTextField();
        singleTableView = new javax.swing.JCheckBox();
        lblFont = new javax.swing.JLabel();
        btnChooseFont = new javax.swing.JButton();
        reset = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Enginuity Settings");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lblEcuDef.setText("ECU Definition File:");

        btnEcuDefinitionBrowse.setText("Browse");

        lblMax.setText("Maximum Value:");

        lblMin.setText("Minimum Value:");

        lblHighlight.setText("Highlighted Cell:");

        lblAxis.setText("Axis Cell:");

        maxColor.setBackground(new java.awt.Color(255, 0, 0));
        maxColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        maxColor.setOpaque(true);

        minColor.setBackground(new java.awt.Color(255, 0, 0));
        minColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        minColor.setOpaque(true);

        highlightColor.setBackground(new java.awt.Color(255, 0, 0));
        highlightColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        highlightColor.setOpaque(true);

        axisColor.setBackground(new java.awt.Color(255, 0, 0));
        axisColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        axisColor.setOpaque(true);

        lblIncrease.setText("Increased Value:");

        lblDecrease.setText("Decreased Value:");

        lblBackgrounds.setText("Backgrounds");

        increaseColor.setBackground(new java.awt.Color(255, 0, 0));
        increaseColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        increaseColor.setOpaque(true);

        obsoleteWarning.setText("Warn me when opening out of date ECU image revision");
        obsoleteWarning.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        obsoleteWarning.setMargin(new java.awt.Insets(0, 0, 0, 0));

        calcConflictWarning.setText("Warn me when real and byte value calculations conflict");
        calcConflictWarning.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        calcConflictWarning.setMargin(new java.awt.Insets(0, 0, 0, 0));

        debug.setText("Debug mode");
        debug.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        debug.setEnabled(false);
        debug.setMargin(new java.awt.Insets(0, 0, 0, 0));

        btnCancel.setMnemonic('C');
        btnCancel.setText("Cancel");

        btnOk.setMnemonic('O');
        btnOk.setText("OK");

        btnApply.setMnemonic('A');
        btnApply.setText("Apply");

        decreaseColor.setBackground(new java.awt.Color(255, 0, 0));
        decreaseColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        decreaseColor.setOpaque(true);

        lblBorders.setText("Borders");

        lblCellSize.setText("Cell Size");

        lblCellHeight.setText("Height:");

        lblCellWidth.setText("Width:");

        singleTableView.setText("Open tables in place of existing tables");
        singleTableView.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        singleTableView.setEnabled(false);
        singleTableView.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lblFont.setText("Font");

        btnChooseFont.setText("Choose");

        reset.setText("Restore Defaults");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblEcuDef)
                            .add(layout.createSequentialGroup()
                                .add(ecuDefinitionFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 257, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnEcuDefinitionBrowse))))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(calcConflictWarning)
                            .add(obsoleteWarning)
                            .add(singleTableView)
                            .add(debug)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                                .add(lblAxis)
                                                .add(lblHighlight)
                                                .add(lblMin))
                                            .add(lblMax))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, maxColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, minColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, highlightColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, axisColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                    .add(layout.createSequentialGroup()
                                        .add(53, 53, 53)
                                        .add(lblCellSize)))
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(57, 57, 57)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(layout.createSequentialGroup()
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(lblDecrease)
                                                    .add(layout.createSequentialGroup()
                                                        .add(3, 3, 3)
                                                        .add(lblIncrease)))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(increaseColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(decreaseColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                            .add(layout.createSequentialGroup()
                                                .add(47, 47, 47)
                                                .add(lblBorders))
                                            .add(btnChooseFont, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(lblFont)
                                        .add(65, 65, 65))))
                            .add(layout.createSequentialGroup()
                                .add(47, 47, 47)
                                .add(lblBackgrounds))))
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(lblCellWidth)
                            .add(lblCellHeight))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(cellWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(cellHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(reset)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 34, Short.MAX_VALUE)
                        .add(btnApply)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnOk)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblEcuDef)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnEcuDefinitionBrowse)
                    .add(ecuDefinitionFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(27, 27, 27)
                .add(obsoleteWarning)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(calcConflictWarning)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(singleTableView)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(debug)
                .add(31, 31, 31)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lblBorders)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(increaseColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(lblIncrease))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblDecrease)
                            .add(decreaseColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(lblBackgrounds)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblMax)
                            .add(maxColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblMin)
                            .add(minColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblHighlight)
                            .add(highlightColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblAxis)
                            .add(axisColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(29, 29, 29)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblCellSize)
                    .add(lblFont))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblCellWidth)
                    .add(cellWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnChooseFont, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblCellHeight)
                    .add(cellHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnCancel)
                    .add(btnOk)
                    .add(btnApply)
                    .add(reset))
                .addContainerGap())
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == maxColor) {
            JColorChooser chooser = new JColorChooser();                    
            Color color = chooser.showDialog(this.getContentPane(), "Background Color", settings.getMaxColor());
            
            if (color != null) {
                maxColor.setBackground(color);
            }
            
        } else if (e.getSource() == minColor) {
            JColorChooser chooser = new JColorChooser();                    
            Color color = chooser.showDialog(this.getContentPane(), "Background Color", settings.getMinColor());
            
            if (color != null) {
                minColor.setBackground(color);
            }
            
        } else if (e.getSource() == highlightColor) {
            JColorChooser chooser = new JColorChooser();                    
            Color color = chooser.showDialog(this.getContentPane(), "Background Color", settings.getHighlightColor());
            
            if (color != null) {
                highlightColor.setBackground(color);
            }
            
        } else if (e.getSource() == axisColor) {
            JColorChooser chooser = new JColorChooser();                    
            Color color = chooser.showDialog(this.getContentPane(), "Background Color", settings.getAxisColor());
            
            if (color != null) {
                axisColor.setBackground(color);
            }
            
        } else if (e.getSource() == increaseColor) {
            JColorChooser chooser = new JColorChooser();                    
            Color color = chooser.showDialog(this.getContentPane(), "Background Color", settings.getIncreaseBorder());
            
            if (color != null) {
                increaseColor.setBackground(color);
            }
            
        } else if (e.getSource() == decreaseColor) {
            JColorChooser chooser = new JColorChooser();                    
            Color color = chooser.showDialog(this.getContentPane(), "Background Color", settings.getDecreaseBorder());
            
            if (color != null) {
                decreaseColor.setBackground(color);
            }
            
       } else if (e.getSource() == btnApply) {
            applySettings();
            
        } else if (e.getSource() == btnOk) {
            applySettings();
            this.dispose();
            
        } else if (e.getSource() == btnCancel) {
            this.dispose();            
            
        } else if (e.getSource() == btnEcuDefinitionBrowse) {
            JFileChooser fc = new JFileChooser(new File(ecuDefinitionFile.getText()));  
            fc.setFileFilter(new XMLFilter());
            
            if (fc.showOpenDialog(this) == fc.APPROVE_OPTION) {
                ecuDefinitionFile.setText(fc.getSelectedFile().getAbsolutePath());
            }            

        } else if (e.getSource() == btnChooseFont) {
            JFontChooser fc = new JFontChooser(this);
            fc.setLocationRelativeTo(this);
            if (fc.showDialog(settings.getTableFont()) == fc.OK_OPTION) {
                btnChooseFont.setFont(fc.getFont());
                btnChooseFont.setText(fc.getFont().getFontName());
            }            
            
        } else if (e.getSource() == reset) {
            settings = new Settings();
            initSettings();
            
        }
    }
    
    public void applySettings() {
        try {
            Integer.parseInt(cellHeight.getText());
        } catch (NumberFormatException ex) {
            //number formatted imporperly, reset
            cellHeight.setText((int)(settings.getCellSize().getHeight())+"");
        }
        try {
            Integer.parseInt(cellWidth.getText());
        } catch (NumberFormatException ex) {
            //number formatted imporperly, reset
            cellWidth.setText((int)(settings.getCellSize().getWidth())+"");
        }        
        
        settings.addEcuDefinitionFile(new File(ecuDefinitionFile.getText()));
        
        settings.setObsoleteWarning(obsoleteWarning.isSelected());
        settings.setCalcConflictWarning(calcConflictWarning.isSelected());
        settings.setSingleTableView(singleTableView.isSelected());
        settings.setDebug(debug.isSelected());
        
        settings.setMaxColor(maxColor.getBackground());
        settings.setMinColor(minColor.getBackground());
        settings.setHighlightColor(highlightColor.getBackground());
        settings.setAxisColor(axisColor.getBackground());
        settings.setIncreaseBorder(increaseColor.getBackground());
        settings.setDecreaseBorder(decreaseColor.getBackground());
        
        settings.setCellSize(new Dimension(Integer.parseInt(cellWidth.getText()),
                Integer.parseInt(cellHeight.getText())));
        
        settings.setTableFont(btnChooseFont.getFont());
        
        parent.setSettings(settings);
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel axisColor;
    private javax.swing.JButton btnApply;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnChooseFont;
    private javax.swing.JButton btnEcuDefinitionBrowse;
    private javax.swing.JButton btnOk;
    private javax.swing.JCheckBox calcConflictWarning;
    private javax.swing.JTextField cellHeight;
    private javax.swing.JTextField cellWidth;
    private javax.swing.JCheckBox debug;
    private javax.swing.JLabel decreaseColor;
    private javax.swing.JTextField ecuDefinitionFile;
    private javax.swing.JLabel highlightColor;
    private javax.swing.JLabel increaseColor;
    private javax.swing.JLabel lblAxis;
    private javax.swing.JLabel lblBackgrounds;
    private javax.swing.JLabel lblBorders;
    private javax.swing.JLabel lblCellHeight;
    private javax.swing.JLabel lblCellSize;
    private javax.swing.JLabel lblCellWidth;
    private javax.swing.JLabel lblDecrease;
    private javax.swing.JLabel lblEcuDef;
    private javax.swing.JLabel lblFont;
    private javax.swing.JLabel lblHighlight;
    private javax.swing.JLabel lblIncrease;
    private javax.swing.JLabel lblMax;
    private javax.swing.JLabel lblMin;
    private javax.swing.JLabel maxColor;
    private javax.swing.JLabel minColor;
    private javax.swing.JCheckBox obsoleteWarning;
    private javax.swing.JButton reset;
    private javax.swing.JCheckBox singleTableView;
    // End of variables declaration//GEN-END:variables
    
}
