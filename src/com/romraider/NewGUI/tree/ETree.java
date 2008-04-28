package com.romraider.NewGUI.tree;

import com.romraider.NewGUI.data.ApplicationStateManager;
import com.romraider.NewGUI.data.TableMetaData;
import org.apache.log4j.Logger;
import javax.swing.JTree;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ETree extends JTree implements MouseListener {
    private static final Logger LOGGER = Logger.getLogger(ETree.class);

    public ETree(ETreeNode treeRootNode) {
        super(treeRootNode);
        setCellRenderer(new ETreeCellRenderer());
        setRootVisible(true);
        setRowHeight(0);
        addMouseListener(this);
        setFont(new Font("Tahoma", Font.PLAIN, 11));
    }

    public void mouseClicked(MouseEvent e) {
        if (e == null) {
            return;
        }
        if (getPathForLocation(e.getX(), e.getY()) == null) {
            return;
        }

        Object selectedObject = getPathForLocation(e.getX(), e.getY()).getLastPathComponent();

        // Null selection occurs when no tree row is selected
        if (selectedObject == null) {
            return;
        }

        if (selectedObject instanceof ETreeNode) {
            ETreeNode theNode = (ETreeNode) selectedObject;
            ApplicationStateManager.getRomRaiderInstance().addTuningGroupNameToTitle(theNode.getTableMetaData().getTableGroup());
            String tableGroup = theNode.getTableMetaData().getTableGroup();
            if (tableGroup != null && tableGroup != "") {
                ApplicationStateManager.setSelectedTuningGroup(tableGroup);
            }

            // If this is a table that contains data, then open it in the right pane in an internal frame
            if (theNode.getTableMetaData().getNodeType() == TableMetaData.DATA_1D || theNode.getTableMetaData().getNodeType() == TableMetaData.DATA_2D || theNode.getTableMetaData().getNodeType() == TableMetaData.DATA_3D) {
                LOGGER.debug("ETree Table data:" + theNode.getTableMetaData().getTableIdentifier());
                Object[][] tableData = ApplicationStateManager.getCurrentTuningEntity().getTableData(theNode.getTableMetaData().getTableIdentifier());
                LOGGER.debug("ETree size:" + tableData.length);
                ApplicationStateManager.getRomRaiderInstance().displayInternalFrameTable(tableData, theNode.getTableMetaData());
            }

        }
    }

    public void mousePressed(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

}
