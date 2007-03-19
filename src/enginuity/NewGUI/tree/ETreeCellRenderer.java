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

public class ETreeCellRenderer implements TreeCellRenderer{

    JLabel nodeNameJLabel;
    
    DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
    
    public ETreeCellRenderer(){
    	nodeNameJLabel = new JLabel(" ");
    	nodeNameJLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
    	nodeNameJLabel.setHorizontalAlignment(JLabel.CENTER);
    }
    
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
    	
    	
    	System.out.println("HI :"+value.getClass().toString());
    	
    	Component returnValue = null;
    	
    	if(value != null && value instanceof ETreeNode){
    		System.out.println("HI 2");
    		
    		ETreeNode eTreeNode = (ETreeNode)value;
    		
    		JPanel renderer = new JPanel(new GridLayout(1, 1));
            renderer.setBorder(createLineBorder(Color.WHITE));
            JLabel tableName = new JLabel("");
            renderer.setBackground(Color.WHITE);
    		
            // Define appropriate ICON to use for node
    		if(eTreeNode.getNodeType() == ETreeNode.DATA1D){
    			tableName = new JLabel(eTreeNode.getNodeName() + " ", new ImageIcon("./graphics/1d.gif"), JLabel.LEFT);
    		}else if(eTreeNode.getNodeType() == ETreeNode.DATA2D){
    			tableName = new JLabel(eTreeNode.getNodeName() + " ", new ImageIcon("./graphics/2d.gif"), JLabel.LEFT);
    		}else if(eTreeNode.getNodeType() == ETreeNode.DATA3D){
    			tableName = new JLabel(eTreeNode.getNodeName() + " ", new ImageIcon("./graphics/3d.gif"), JLabel.LEFT);
    		}else if(eTreeNode.getNodeType() == ETreeNode.CATEGORY){
    			tableName = new JLabel(eTreeNode.getNodeName() + " ", new ImageIcon("./graphics/1d.gif"), JLabel.LEFT);
    		}
    		

    		if(eTreeNode.getNodeType() == ETreeNode.CATEGORY){
    			
    			System.out.println("Name: "+eTreeNode.getNodeName());
                nodeNameJLabel.setText(eTreeNode.getNodeName());
                
                renderer = new JPanel(new GridLayout(2, 1));
                renderer.add(nodeNameJLabel);

                if (selected) {
                    renderer.setBackground(new Color(220, 220, 255));
                    renderer.setBorder(createLineBorder(new Color(0, 0, 225)));

                } else {
                    renderer.setBorder(createLineBorder(new Color(220, 0, 0)));
                    renderer.setBackground(new Color(255, 210, 210));
                }

                renderer.setPreferredSize(new Dimension(tree.getParent().getWidth(), 30));
                renderer.setMaximumSize(new Dimension(tree.getParent().getWidth(), 30));
                renderer.setEnabled(tree.isEnabled());
                returnValue = renderer;
    			
    		}else if(eTreeNode.getNodeType() == ETreeNode.DATA3D){
    			System.out.println("HI 3");

                // set color
                renderer.add(tableName);
                tableName.setFont(new Font("Tahoma", Font.PLAIN, 11));

                if (selected) {
                    renderer.setBackground(new Color(220, 220, 255));
                    renderer.setBorder(createLineBorder(new Color(0, 0, 225)));
                }

                // TODO Imlement user level
                
                if (eTreeNode.getUserLevel() == 5) {
                    tableName.setForeground(new Color(255, 150, 150));
                    tableName.setFont(new Font("Tahoma", Font.ITALIC, 11));

                } 
                
                /*else if (eTreeNode.getUserLevel() > table.getRom().getContainer().getSettings().getUserLevel()) {
                    //tableName.setForeground(new Color(185, 185, 185));
                    tableName.setFont(new Font("Tahoma", Font.ITALIC, 11));

                }
                
                */

                returnValue = renderer;
    		}
    		
    		
    	}
    	

        if (returnValue == null) {
            returnValue = defaultRenderer.getTreeCellRendererComponent(tree,value, selected, expanded, leaf, row, hasFocus);
            
            System.out.println("HI 4");
        }
    	
		return returnValue;
	}

}
