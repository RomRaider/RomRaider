package enginuity.swing;

import enginuity.maps.Table;

public class TablePropertyPanel extends javax.swing.JPanel {
    
    public TablePropertyPanel(Table table) {
        initComponents();
        setVisible(true);
        
        tableName.setText(table.getName() + " (" + table.getType() + "D)");
        category.setText(table.getCategory());
        unit.setText(table.getScale().getUnit());
        byteToReal.setText(table.getScale().getExpression());
        realToByte.setText(table.getScale().getByteExpression());
        storageSize.setText("uint" + (table.getStorageType() * 8));
        storageAddress.setText("0x" + Integer.toHexString(table.getStorageAddress()));
        if (table.getEndian() == Table.ENDIAN_BIG) endian.setText("big");
        else endian.setText("little");
        description.setText(table.getDescription());
        System.out.println(table.getDescription());
    }
    
    private TablePropertyPanel() { }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTable = new javax.swing.JLabel();
        tableName = new javax.swing.JLabel();
        lblCategory = new javax.swing.JLabel();
        category = new javax.swing.JLabel();
        lblStorageSize = new javax.swing.JLabel();
        lblStorageAddress = new javax.swing.JLabel();
        lblEndian = new javax.swing.JLabel();
        lblConversion = new javax.swing.JLabel();
        lblStorage = new javax.swing.JLabel();
        lblByteToReal = new javax.swing.JLabel();
        lblRealToByte = new javax.swing.JLabel();
        lblUnit = new javax.swing.JLabel();
        byteToReal = new javax.swing.JLabel();
        realToByte = new javax.swing.JLabel();
        unit = new javax.swing.JLabel();
        storageAddress = new javax.swing.JLabel();
        storageSize = new javax.swing.JLabel();
        endian = new javax.swing.JLabel();
        lblDescription = new javax.swing.JLabel();
        description = new javax.swing.JLabel();

        lblTable.setText("Table:");
        lblTable.setFocusable(false);

        tableName.setText("Tablename (3D)");
        tableName.setFocusable(false);

        lblCategory.setText("Category:");
        lblCategory.setFocusable(false);

        category.setText("Category");
        category.setFocusable(false);

        lblStorageSize.setText("Storage Size:");
        lblStorageSize.setFocusable(false);

        lblStorageAddress.setText("Storage Address:");
        lblStorageAddress.setFocusable(false);

        lblEndian.setText("Endian:");
        lblEndian.setFocusable(false);

        lblConversion.setText("----------- Conversion -----------");
        lblConversion.setFocusable(false);

        lblStorage.setText("---------- Storage ----------");
        lblStorage.setFocusable(false);

        lblByteToReal.setText("Byte to Real:");
        lblByteToReal.setFocusable(false);

        lblRealToByte.setText("Real to Byte:");
        lblRealToByte.setFocusable(false);

        lblUnit.setText("Unit:");
        lblUnit.setFocusable(false);

        byteToReal.setText("bytetoreal");
        byteToReal.setFocusable(false);

        realToByte.setText("realtobyte");
        realToByte.setFocusable(false);

        unit.setText("unit");
        unit.setFocusable(false);

        storageAddress.setText("0x00");
        storageAddress.setFocusable(false);

        storageSize.setText("uint16");
        storageSize.setFocusable(false);

        endian.setText("little");
        endian.setFocusable(false);

        lblDescription.setText("Description:");
        lblDescription.setFocusable(false);

        description.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        description.setText("Description");
        description.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        description.setFocusable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(description, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                    .add(lblDescription)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblConversion)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lblByteToReal)
                                    .add(lblRealToByte)
                                    .add(lblUnit))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(byteToReal)
                                    .add(realToByte)
                                    .add(unit)))
                            .add(layout.createSequentialGroup()
                                .add(lblTable)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(tableName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblStorage)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lblStorageAddress)
                                    .add(lblStorageSize)
                                    .add(lblEndian))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(endian)
                                    .add(storageSize)
                                    .add(storageAddress)))
                            .add(layout.createSequentialGroup()
                                .add(lblCategory)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(category)))
                        .add(20, 20, 20)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblTable)
                            .add(tableName))
                        .add(20, 20, 20)
                        .add(lblConversion)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(lblUnit)
                                .add(40, 40, 40))
                            .add(layout.createSequentialGroup()
                                .add(lblByteToReal)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRealToByte))
                            .add(layout.createSequentialGroup()
                                .add(unit)
                                .add(40, 40, 40))
                            .add(layout.createSequentialGroup()
                                .add(byteToReal)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(realToByte)))
                        .add(26, 26, 26)
                        .add(lblDescription))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblCategory)
                            .add(category))
                        .add(20, 20, 20)
                        .add(lblStorage)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblStorageSize)
                            .add(storageSize))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblStorageAddress)
                            .add(storageAddress))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblEndian)
                            .add(endian))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(description, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel byteToReal;
    private javax.swing.JLabel category;
    private javax.swing.JLabel description;
    private javax.swing.JLabel endian;
    private javax.swing.JLabel lblByteToReal;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblConversion;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblEndian;
    private javax.swing.JLabel lblRealToByte;
    private javax.swing.JLabel lblStorage;
    private javax.swing.JLabel lblStorageAddress;
    private javax.swing.JLabel lblStorageSize;
    private javax.swing.JLabel lblTable;
    private javax.swing.JLabel lblUnit;
    private javax.swing.JLabel realToByte;
    private javax.swing.JLabel storageAddress;
    private javax.swing.JLabel storageSize;
    private javax.swing.JLabel tableName;
    private javax.swing.JLabel unit;
    // End of variables declaration//GEN-END:variables
}