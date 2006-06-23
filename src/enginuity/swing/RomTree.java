package enginuity.swing;

import enginuity.ECUEditor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class RomTree extends JTree implements MouseListener {
    
    private ECUEditor container;
    
    public RomTree (DefaultMutableTreeNode input) {
       super(input);
       this.setRootVisible(false);
       this.addMouseListener(this);
    }

    public ECUEditor getContainer() {
        return container;
    }

    public void setContainer(ECUEditor container) {
        this.container = container;
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            try {
                TableTreeNode node = (TableTreeNode)this.getLastSelectedPathComponent(); 
                container.displayTable(node.getFrame());
            } catch (ClassCastException ex) {
            } catch (NullPointerException ex) { }
        } if (e.getClickCount() == 1) {
            
            
            if (getLastSelectedPathComponent() instanceof TableTreeNode) {                
                TableTreeNode node = (TableTreeNode)this.getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getTable().getRom());
            } else if (getLastSelectedPathComponent() instanceof CategoryTreeNode) {
                CategoryTreeNode node = (CategoryTreeNode)this.getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getRom());
            } else if (getLastSelectedPathComponent() instanceof RomTreeNode) {
                RomTreeNode node = (RomTreeNode)this.getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getRom());
            }
        }
    }
  
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

}