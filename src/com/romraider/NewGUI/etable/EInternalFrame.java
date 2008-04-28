package com.romraider.NewGUI.etable;

import com.romraider.NewGUI.data.TableMetaData;
import com.romraider.NewGUI.etable.dataJPanel.DataJPanel1DString;
import com.romraider.NewGUI.etable.dataJPanel.DataJPanel3DDouble;
import com.romraider.NewGUI.etable.dataJPanel.DataJPanelInterface;
import org.apache.log4j.Logger;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

public class EInternalFrame extends JInternalFrame implements InternalFrameListener, ActionListener {
    private Stack<ETableSaveState> savedData = new Stack<ETableSaveState>();
    private TableMetaData tableMetaData;
    private DataJPanelInterface dataJPanel = null;
    private static final Logger LOGGER = Logger.getLogger(EInternalFrame.class);

    public EInternalFrame(TableMetaData tableMetaData, Object[][] data) {
        super(tableMetaData.getTableName() + "   " + tableMetaData.getTableGroup(), true, true, true, true);
        this.tableMetaData = tableMetaData;

        // Pull the appropriate jpanel based on tableMetaData
        if (tableMetaData.getNodeType() == TableMetaData.DATA_1D) {
            if (data[0][0] instanceof String) {
                dataJPanel = new DataJPanel1DString(tableMetaData, data);
            }
        } else if (tableMetaData.getNodeType() == TableMetaData.DATA_2D) {

        } else if (tableMetaData.getNodeType() == TableMetaData.DATA_3D) {

            if (data[0][0] instanceof Double) {
                dataJPanel = new DataJPanel3DDouble(tableMetaData, data);
            }

        }

        // Ensure we set the appropriate frame dimensions
        if (tableMetaData.getNodeType() == TableMetaData.DATA_1D) {
            this.setSize(tableMetaData.getData1DDimension());
        } else if (tableMetaData.getNodeType() == TableMetaData.DATA_2D) {
            this.setSize(tableMetaData.getData2DDimension());
        } else if (tableMetaData.getNodeType() == TableMetaData.DATA_3D) {
            this.setSize(tableMetaData.getData3DDimension());
        }

        // *****************************
        // Build up final internal frame
        // *****************************
        Image img = Toolkit.getDefaultToolkit().getImage("graphics/romraider-ico.gif");
        ImageIcon imgIcon = new ImageIcon(img);
        this.setFrameIcon(imgIcon);
        this.setVisible(true);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        this.addInternalFrameListener(this);
        this.add((JPanel) this.dataJPanel, BorderLayout.CENTER);
        this.setJMenuBar(this.dataJPanel.getMenuBar());
        this.add(this.dataJPanel.getToolBar(), BorderLayout.NORTH);
    }


    /**
     * Check to see if data relevant to this frame has changed
     *
     * @return
     */
    public boolean dataChanged() {
        return this.dataJPanel.dataChanged();
    }

    public void saveDataToParentTuningEntity() {
        if (this.dataJPanel.getData() == null) {
            LOGGER.error("EInternalFrame data in cells is null.");
        }
        this.tableMetaData.getParentTuningEntity().setTableData(this.tableMetaData.getTableIdentifier(), this.dataJPanel.getData());
    }

    public void saveDataState() {
        this.savedData.push(new ETableSaveState(this.dataJPanel.getData()));
    }

    public void revertDataState() {
        if (!this.savedData.isEmpty()) {
            if (this.savedData.size() > 1) {
                this.setTableData(this.savedData.pop().getData());
            } else if (savedData.size() == 1) {
                this.setTableData(this.savedData.peek().getData());
            }
        }
    }


    public void setTableData(Object[][] data) {
        this.dataJPanel.replaceData(data);
    }

    public void internalFrameOpened(InternalFrameEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void internalFrameClosing(InternalFrameEvent arg0) {
        this.setVisible(false);

    }

    public void internalFrameClosed(InternalFrameEvent arg0) {
    }

    public void internalFrameIconified(InternalFrameEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void internalFrameDeiconified(InternalFrameEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void internalFrameActivated(InternalFrameEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void internalFrameDeactivated(InternalFrameEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void actionPerformed(ActionEvent arg0) {
        // TODO Auto-generated method stub

    }


    public TableMetaData getTableMetaData() {
        return tableMetaData;
    }

    /*
     public ETable getETable() {
         return eTable;
     }

     public ClipBoardCopy getExcelCopy() {
         return excelCopy;
     }
     */
}
