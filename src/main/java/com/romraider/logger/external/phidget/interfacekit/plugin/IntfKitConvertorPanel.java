package com.romraider.logger.external.phidget.interfacekit.plugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.ui.swing.tools.tablemodels.renderers.CentreRenderer;

/**
 * Display dialog to allow the user to custom define each Phidget InterfaceKit
 * sensor found. 
 */
public class IntfKitConvertorPanel extends JDialog {
    private static final long serialVersionUID = -4785866140260703021L;
    private final int IK_WIDTH = 600;
    private final int IK_HEIGHT = 300;
    private JTable table;

    /**
     * Phidget InterfaceKit display panel.
     * @param logger - the Logger frame
     * @param ikData - rows of InterfaceKit sensor parameters
     * @see #getTable()
     */
    public IntfKitConvertorPanel(
            EcuLogger logger, List<List<String>> ikData) {

        super(logger, true);
        setTitle("Phidget InterfaceKit User Defined Convertors");
        setBounds(
                (logger.getWidth() > IK_WIDTH) ?
                    logger.getX() + (logger.getWidth() - IK_WIDTH) / 2 : 0,
                (logger.getHeight() > IK_HEIGHT) ?
                    logger.getY() + ((logger.getHeight() - IK_HEIGHT) / 2) : 0,
                    IK_WIDTH,
                    IK_HEIGHT);
        getContentPane().setLayout(new BorderLayout());
        final JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        final IntfKitTableModel model = new IntfKitTableModel();
        model.setIkRowData(ikData);
        table = new JTable(model) {
            private static final long serialVersionUID = -6967080466164619695L;
            public final String getToolTipText(MouseEvent e) {
                String tip = null;
                final int colIndex = columnAtPoint(e.getPoint());
                final int column = convertColumnIndexToModel(colIndex);
                switch (column) {
                    case 0:
                        tip = "Sensor module serial number and input number";
                        break;
                    case 1:
                        tip = "Equation used to convert raw data to real value";
                        break;
                    case 2:
                        tip = "Format of real value for display and logging";
                        break;
                    case 3:
                        tip = "Units of measure of the real value";
                        break;
                    case 4:
                        tip = "Minimum real value expected";
                        break;
                    case 5:
                        tip = "Maximum real value expected";
                        break;
                    case 6:
                        tip = "Tick mark step for Gauge Tab display, " +
                              "typically (max-min)/10";
                        break;
                    default:
                        tip = super.getToolTipText(e);
                }
                return tip;
            }
        };

        setTableBehaviour(table);
        scrollPane.setViewportView(table);

        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        final JButton saveButton = new JButton("Save");
        saveButton.setToolTipText("Save convertor settings");
        saveButton.setMnemonic(KeyEvent.VK_S);
        saveButton.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent actionEvent) {
                closeDialog();
            }
        });
        buttonPane.add(saveButton);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel without saving changes");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                table = null;
                closeDialog();
            }
        });
        buttonPane.add(cancelButton);
        getRootPane().setDefaultButton(cancelButton);
    }

    /**
     * Display the Phidget InterfaceKit dialog.
     */
    public final void displayPanel() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                table = null;
                closeDialog();
            }
        });
        setVisible(true);
    }

    /**
     * After the Phidget InterfaceKit dialog is closed this method can be
     * called to retrieve the table of updated values.
     * @return a JTable of InterfaceKit sensor parameters.<br>
     * - if <b>Save</b> was pressed to close the dialog the table will be valid.<br>
     * - if <b>Cancel</b> was pressed to close the dialog the table will be <b>null</b>.
     */
    public final JTable getTable() {
        return table;
    }

    private final void setTableBehaviour(JTable table) {
        table.setBorder(null);
        table.setFillsViewportHeight(false);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);

        final JTableHeader th = table.getTableHeader();
        th.setReorderingAllowed(false);
        th.setDefaultRenderer(new CentreRenderer(table));

        TableColumn column = null;
        for (int i = 0; i < table.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(150);
            }
            else if (i == 1) {
                column.setPreferredWidth(150);
            }

            else {
                column.setPreferredWidth(50);
            }
        }
    }

    private final void closeDialog() {
        setVisible(false);
        dispose();
    }
}
