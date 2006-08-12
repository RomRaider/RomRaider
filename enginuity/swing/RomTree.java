package enginuity.swing;

import enginuity.ECUEditor;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

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
        
        try {
        
            Object selectedRow = getPathForLocation(e.getX(), e.getY()).getLastPathComponent();

            if (e.getClickCount() >= container.getSettings().getTableClickCount() &&
                             selectedRow instanceof TableTreeNode) {

                TableTreeNode node = (TableTreeNode)selectedRow;

                if (!(node.getTable().getUserLevel() > container.getSettings().getUserLevel())) {
                    container.displayTable(node.getFrame());
                }

            }

            if (selectedRow instanceof TableTreeNode) {                
                TableTreeNode node = (TableTreeNode)getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getTable().getRom());

            } else if (selectedRow instanceof CategoryTreeNode) {
                CategoryTreeNode node = (CategoryTreeNode)getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getRom());

            } else if (selectedRow instanceof RomTreeNode) {
                RomTreeNode node = (RomTreeNode)getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getRom());
            }
        } catch (NullPointerException ex) { }
    }
  
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

    public void removeDescendantToggledPaths(Enumeration<TreePath> toRemove) {
        super.removeDescendantToggledPaths(toRemove);
    }
}