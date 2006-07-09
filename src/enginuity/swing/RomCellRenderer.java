package enginuity.swing;

import enginuity.maps.Rom;
import enginuity.maps.Table;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class RomCellRenderer implements TreeCellRenderer {
    
    JLabel fileName;
    JLabel carInfo;
    DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
    Color background = new Color(255,210,210);

    public RomCellRenderer() {
        fileName = new JLabel(" ");
        fileName.setFont(new Font("Tahoma", Font.BOLD, 11));
        fileName.setHorizontalAlignment(JLabel.CENTER);
        
        carInfo = new JLabel(" ");
        carInfo.setFont(new Font("Tahoma", Font.PLAIN, 10));
        carInfo.setHorizontalAlignment(JLabel.CENTER);
        
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
        
        Component returnValue = null;

        if (value != null && value instanceof RomTreeNode) {
            
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            
            if (userObject instanceof Rom) {
                Rom rom = (Rom) userObject;
                
                if (expanded) fileName.setText("- " + rom.getFileName());
                else fileName.setText("+ " + rom.getFileName());
                
                
                carInfo.setText(rom.getRomIDString() + ", " +
                                rom.getRomID().getCaseId() + "; " +                     
                                rom.getRomID().getYear() + " " +
                                rom.getRomID().getMake() + " " + 
                                rom.getRomID().getModel() + " " +
                                rom.getRomID().getSubModel() + ", " + 
                                rom.getRomID().getTransmission()
                        );
                
                JPanel renderer = new JPanel(new GridLayout(2,1));
                renderer.add(fileName);
                renderer.add(carInfo);        
                renderer.setBorder(BorderFactory.createLineBorder(new Color(220,0,0)));
                
                renderer.setPreferredSize(new Dimension(tree.getParent().getWidth(), 30));
                renderer.setMaximumSize(new Dimension(tree.getParent().getWidth(), 30));
                renderer.setBackground(background);
                renderer.setEnabled(tree.isEnabled());
                returnValue = renderer;           
            }        
            
        } else if (value != null && value instanceof TableTreeNode) {
            
            Table table = (Table)((DefaultMutableTreeNode)value).getUserObject();
            JPanel renderer = new JPanel(new GridLayout(1,1));
            renderer.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            JLabel tableName = new JLabel("");
            renderer.setBackground(Color.WHITE);
            
            // display icon
            if (table.getType() == Table.TABLE_1D) {
                tableName = new JLabel(table.getName()+" ", new ImageIcon("./graphics/1d.gif"), JLabel.LEFT);
            } else if (table.getType() == Table.TABLE_2D) {
                tableName = new JLabel(table.getName()+" ", new ImageIcon("./graphics/2d.gif"), JLabel.LEFT);
            } else if (table.getType() == Table.TABLE_3D) {
                tableName = new JLabel(table.getName()+" ", new ImageIcon("./graphics/3d.gif"), JLabel.LEFT);
            } else if (table.getType() == Table.TABLE_SWITCH) {
                tableName = new JLabel(table.getName()+" ", new ImageIcon("./graphics/switch.gif"), JLabel.LEFT);        
            }           
            
            // set color
            renderer.add(tableName);
            tableName.setFont(new Font("Tahoma", Font.PLAIN, 11)); 
            
            if (selected) {
                renderer.setBackground(new Color(220,220,255));
                renderer.setBorder(BorderFactory.createLineBorder(new Color(0,0,225)));
            }
            
            if (table.getUserLevel() == 5) {
                tableName.setForeground(new Color(255,150,150));
                tableName.setFont(new Font("Tahoma", Font.ITALIC, 11)); 
                
            } else if (table.getUserLevel() > table.getRom().getContainer().getSettings().getUserLevel()) {
                tableName.setForeground(new Color(185,185,185));
                tableName.setFont(new Font("Tahoma", Font.ITALIC, 11)); 
                
            }    
            
            returnValue = renderer;
        }

        if (returnValue == null) {
            returnValue = defaultRenderer.getTreeCellRendererComponent(tree,
                value, selected, expanded, leaf, row, hasFocus);  
        }

        return returnValue;
        
    }
}