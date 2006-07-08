package enginuity.swing;

import enginuity.ECUEditor;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class RomTree extends JTree implements MouseListener {
    
    private ECUEditor container;
    
    public RomTree (DefaultMutableTreeNode input) {
       super(input);
       setRootVisible(false);
       setRowHeight(0);
       addMouseListener(this);
       setCellRenderer(new RomCellRenderer());
       setFont(new Font("Tahoma", Font.PLAIN, 11));
    }

    public ECUEditor getContainer() {
        return container;
    }

    public void setContainer(ECUEditor container) {
        this.container = container;
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= container.getSettings().getTableClickCount() &&
                         getLastSelectedPathComponent() instanceof TableTreeNode) {
            
            TableTreeNode node = (TableTreeNode)getLastSelectedPathComponent(); 
            container.displayTable(node.getFrame());
            
        }
        
        if (e.getClickCount() == 1) {
                        
            if (getLastSelectedPathComponent() instanceof TableTreeNode) {                
                TableTreeNode node = (TableTreeNode)getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getTable().getRom());
                
            } else if (getLastSelectedPathComponent() instanceof CategoryTreeNode) {
                CategoryTreeNode node = (CategoryTreeNode)getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getRom());
                
            } else if (getLastSelectedPathComponent() instanceof RomTreeNode) {
                RomTreeNode node = (RomTreeNode)getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getRom());
                
            }
        }
    }
  
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

    public void cleanup() {
    	clearToggledPaths();
    }
}