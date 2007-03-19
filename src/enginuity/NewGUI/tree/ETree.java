package enginuity.NewGUI.tree;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;

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

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
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
