package enginuity.definitionbuilder;

import enginuity.definitionbuilder.DefinitionBuilder;
import enginuity.definitionbuilder.SubaruMapDefinitionParser;
import enginuity.maps.Rom;
import enginuity.xml.RomAttributeParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;

public class DefinitionBuilder extends javax.swing.JFrame {
    
    File imageDir = new File(".");
    
    public DefinitionBuilder(Rom rom, File imageDir) {
        initComponents();
        
        try {
            setLocationRelativeTo(rom.getContainer());
            inputFile.setText(rom.getFullFileName().getAbsolutePath());
        } catch (NullPointerException ex) { }
        
        this.imageDir = imageDir;
        setVisible(true);
    }
    
    public DefinitionBuilder() {
        initComponents();
        setVisible(true);
    }
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JLabel filesizeLabel;

        filesizeGroup = new javax.swing.ButtonGroup();
        inputFile = new javax.swing.JTextField();
        browseBtn = new javax.swing.JButton();
        inputLabel = new javax.swing.JLabel();
        tableLabel = new javax.swing.JLabel();
        inputName = new javax.swing.JTextField();
        addressLabel = new javax.swing.JLabel();
        inputAddress = new javax.swing.JTextField();
        generateBtn = new javax.swing.JButton();
        outputScrollPane = new javax.swing.JScrollPane();
        output = new javax.swing.JTextArea();
        clearBtn = new javax.swing.JButton();
        outputStorage = new javax.swing.JCheckBox();
        outputSize = new javax.swing.JCheckBox();
        outputLabel = new javax.swing.JLabel();
        filesizeLabel = new javax.swing.JLabel();
        radio160 = new javax.swing.JRadioButton();
        radio192 = new javax.swing.JRadioButton();
        dimension = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Table Definition Generator");
        setResizable(false);
        getAccessibleContext().setAccessibleParent(outputScrollPane);
        inputFile.setToolTipText("ECU image to check");

        browseBtn.setText("Browse");
        browseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseBtnActionPerformed(evt);
            }
        });

        inputLabel.setText("Input File:");

        tableLabel.setText("Table Name:");

        inputName.setToolTipText("Name of table to generate");

        addressLabel.setText("Address:");

        inputAddress.setToolTipText("Memory address");

        generateBtn.setText("Generate");
        generateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateBtnActionPerformed(evt);
            }
        });

        output.setColumns(20);
        output.setRows(5);
        outputScrollPane.setViewportView(output);

        clearBtn.setText("Clear");
        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });

        outputStorage.setText("Storage Type");
        outputStorage.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        outputStorage.setMargin(new java.awt.Insets(0, 0, 0, 0));

        outputSize.setSelected(true);
        outputSize.setText("Size");
        outputSize.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        outputSize.setMargin(new java.awt.Insets(0, 0, 0, 0));

        outputLabel.setText("Output:");

        filesizeLabel.setText("ExpectedFilesize:");

        filesizeGroup.add(radio160);
        radio160.setText("160kb");
        radio160.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radio160.setMargin(new java.awt.Insets(0, 0, 0, 0));

        filesizeGroup.add(radio192);
        radio192.setSelected(true);
        radio192.setText("192kb");
        radio192.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radio192.setMargin(new java.awt.Insets(0, 0, 0, 0));

        dimension.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "3D", "2D" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, outputScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(tableLabel)
                            .add(inputLabel)
                            .add(outputLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(inputName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 262, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 19, Short.MAX_VALUE)
                                .add(addressLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(inputAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, inputFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(outputStorage)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(outputSize)
                                .add(30, 30, 30)
                                .add(filesizeLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(radio160)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(radio192)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 39, Short.MAX_VALUE)
                                .add(dimension, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(clearBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, browseBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, generateBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inputFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseBtn)
                    .add(inputLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tableLabel)
                    .add(generateBtn)
                    .add(inputAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addressLabel)
                    .add(inputName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(clearBtn)
                    .add(outputLabel)
                    .add(outputStorage)
                    .add(dimension, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(outputSize)
                    .add(filesizeLabel)
                    .add(radio160)
                    .add(radio192))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 10, Short.MAX_VALUE)
                .add(outputScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 307, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void generateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateBtnActionPerformed
        // generate code
        SubaruMapDefinitionParser parser = new SubaruMapDefinitionParser();
        byte[] data = new byte[1];
        int address = RomAttributeParser.parseHexString(inputAddress.getText());
        String name = inputName.getText();
        int dimensions = 0;
        if (dimension.getSelectedIndex() == 0) dimensions = 3;        
        else dimensions = 2;
        
        try {            
            FileInputStream fis = new FileInputStream(new File(inputFile.getText()));
            data = new byte[fis.available()];
            fis = new FileInputStream(new File(inputFile.getText()));
            fis.read(data);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        if (radio192.isSelected()) {
            output.setText(parser.parseTable(data, address, name, 192, dimensions, outputSize.isSelected(), outputStorage.isSelected()));
        } else { // 160kb
            output.setText(parser.parseTable(data, address, name, 160, dimensions, outputSize.isSelected(), outputStorage.isSelected()));
        }
    }//GEN-LAST:event_generateBtnActionPerformed

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        output.setText("");
    }//GEN-LAST:event_clearBtnActionPerformed

    private void browseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseBtnActionPerformed
        JFileChooser fc = new JFileChooser(imageDir);  
        if (fc.showOpenDialog(this) == fc.APPROVE_OPTION) {
            inputFile.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseBtnActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addressLabel;
    private javax.swing.JButton browseBtn;
    private javax.swing.JButton clearBtn;
    private javax.swing.JComboBox dimension;
    private javax.swing.ButtonGroup filesizeGroup;
    private javax.swing.JButton generateBtn;
    private javax.swing.JTextField inputAddress;
    private javax.swing.JTextField inputFile;
    private javax.swing.JLabel inputLabel;
    private javax.swing.JTextField inputName;
    private javax.swing.JTextArea output;
    private javax.swing.JLabel outputLabel;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JCheckBox outputSize;
    private javax.swing.JCheckBox outputStorage;
    private javax.swing.JRadioButton radio160;
    private javax.swing.JRadioButton radio192;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables
    
}
