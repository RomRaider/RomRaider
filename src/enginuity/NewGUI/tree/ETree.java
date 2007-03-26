package enginuity.NewGUI.tree;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableMetaData;
import enginuity.swing.RomCellRenderer;

public class ETree extends JTree implements MouseListener {
	
	public ETree(ETreeNode treeRootNode){
		super(treeRootNode);
		setCellRenderer(new ETreeCellRenderer());
		setRootVisible(true);
        setRowHeight(0);
        addMouseListener(this);
        setFont(new Font("Tahoma", Font.PLAIN, 11));
	}

	public void mouseClicked(MouseEvent e) {
		if(e == null){
			return;
		}
		if(getPathForLocation(e.getX(), e.getY()) == null){
			return;
		}
		
		Object selectedObject = getPathForLocation(e.getX(), e.getY()).getLastPathComponent();
		
		// Null selection occurs when no tree row is selected
		if(selectedObject == null){
			return;
		}
		
		if(selectedObject instanceof ETreeNode){
			ETreeNode theNode = (ETreeNode)selectedObject;
			
			String tableGroup = theNode.getTableMetaData().getTableGroup();
			if(tableGroup != null && tableGroup != ""){
				ApplicationStateManager.setSelectedTuningGroup(tableGroup);
			}
			
			// If this is a table that contains data, then open it in the right pane in an internal frame
			if(theNode.getTableMetaData().getNodeType() == TableMetaData.DATA1D || theNode.getTableMetaData().getNodeType() == TableMetaData.DATA2D || theNode.getTableMetaData().getNodeType() == TableMetaData.DATA3D){
				System.out.println("Table data");
				Double[][] tableData = ApplicationStateManager.getCurrentTuningEntity().getTableData(theNode.getNodeName());
				ApplicationStateManager.getEnginuityInstance().displayInternalFrameTable(tableData, theNode.getTableMetaData());
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
