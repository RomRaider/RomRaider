package Enginuity.SwingComponents;

import Enginuity.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class RomTree extends JTree implements MouseListener, TreeSelectionListener {
    
    private ECUEditor container;
    
    public RomTree (DefaultMutableTreeNode input) {
       super(input);
       this.setRootVisible(false);
       this.addMouseListener(this);
       this.addTreeSelectionListener(this);
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
                RomTreeNode node = (RomTreeNode)this.getLastSelectedPathComponent(); 
                node.getTable().getFrame().setVisible(true);
                node.getTable().getFrame().setSelected(true);
            } catch (PropertyVetoException ex) {
            } catch (ClassCastException ex) {
            } catch (NullPointerException ex) { }
        }
    }

    public void mousePressed(MouseEvent e) {  }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

    public void valueChanged(TreeSelectionEvent e) {        
        try {
            RomTreeNode node = (RomTreeNode)this.getLastSelectedPathComponent();
            if (node.getRom() != null) {
                container.setLastSelectedRom(node.getRom());
            } else if (node.getTable() != null) {
                container.setLastSelectedRom(node.getTable().getContainer());
            }
        } catch (NullPointerException ex) {
            // node wasn't table or rom (do nothing)
        } catch (ClassCastException ex) {
            // node wasn't table or rom (do nothing)
        }
    }
}