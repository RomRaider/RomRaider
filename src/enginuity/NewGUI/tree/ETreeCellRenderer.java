package enginuity.NewGUI.tree;

import static javax.swing.BorderFactory.createLineBorder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableMetaData;

public class ETreeCellRenderer implements TreeCellRenderer{
	
    DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
    	
    	Component returnValue = null;
    	
    	if(value != null && value instanceof ETreeNode){
    		
    		ETreeNode eTreeNode = (ETreeNode)value;
    		
    		JPanel namedJPanel = new JPanel(new GridLayout(1, 1));
            namedJPanel.setBorder(createLineBorder(Color.WHITE));
            JLabel nodeName = new JLabel("");
            namedJPanel.setBackground(Color.WHITE);
    		
            // Define appropriate ICON to use for node
    		if(eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_1D){
    			nodeName = new JLabel(eTreeNode.getNodeName() + " ", new ImageIcon("./graphics/1d.gif"), JLabel.LEFT);
    		}else if(eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_2D){
    			nodeName = new JLabel(eTreeNode.getNodeName() + " ", new ImageIcon("./graphics/2d.gif"), JLabel.LEFT);
    		}else if(eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_3D){
    			nodeName = new JLabel(eTreeNode.getNodeName() + " ", new ImageIcon("./graphics/3d.gif"), JLabel.LEFT);
    		}
    		

    		if(eTreeNode.getTableMetaData().getNodeType() == TableMetaData.MAP_SET_ROOT){
    			
                nodeName = new JLabel(eTreeNode.getNodeName(), JLabel.LEFT);
                nodeName.setFont(new Font("Tahoma", Font.PLAIN, 11));
                namedJPanel.add(nodeName);

                if (selected) {
                    namedJPanel.setBackground(new Color(220, 220, 255));
                    namedJPanel.setBorder(createLineBorder(new Color(0, 0, 225)));

                } else {
                    namedJPanel.setBorder(createLineBorder(new Color(220, 0, 0)));
                    namedJPanel.setBackground(new Color(255, 210, 210));
                }

                namedJPanel.setPreferredSize(new Dimension(tree.getParent().getWidth(), 30));
                namedJPanel.setMaximumSize(new Dimension(tree.getParent().getWidth(), 30));
                namedJPanel.setEnabled(tree.isEnabled());
                returnValue = namedJPanel;
    			
    		}else if(eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_3D || eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_2D || eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_1D){
    			
                namedJPanel.add(nodeName);
                nodeName.setFont(new Font("Tahoma", Font.PLAIN, 11));

                if (selected) {
                    namedJPanel.setBackground(new Color(220, 220, 255));
                    namedJPanel.setBorder(createLineBorder(new Color(0, 0, 225)));
                }

                if (eTreeNode.getUserLevel() == 5) {
                    nodeName.setForeground(new Color(255, 150, 150));
                    nodeName.setFont(new Font("Tahoma", Font.ITALIC, 11));

                } 
                else if (eTreeNode.getUserLevel() > ApplicationStateManager.getCurrentUserLevel()) {
                    //tableName.setForeground(new Color(185, 185, 185));
                	nodeName.setFont(new Font("Tahoma", Font.ITALIC, 11));

                }

                returnValue = namedJPanel;
    		}
    	}
    	

        if (returnValue == null) {
            returnValue = defaultRenderer.getTreeCellRendererComponent(tree,value, selected, expanded, leaf, row, hasFocus);
            
        }
    	
		return returnValue;
	}
}
