package enginuity.swing;

import enginuity.maps.Rom;
import enginuity.maps.Table;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Vector;

public class JTableChooser extends JOptionPane {

    JPanel displayPanel = new JPanel();
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Open Images");
    JTree displayTree = new JTree(rootNode);

    public boolean showChooser(Vector<Rom> roms, Component parent, Table targetTable) {

        for (int i = 0; i < roms.size(); i++) {
            Rom rom = roms.get(i);
            DefaultMutableTreeNode romNode = new DefaultMutableTreeNode(rom.getFileName());
            rootNode.add(romNode);

            for (int j = 0; j < rom.getTables().size(); j++) {
                Table table = rom.getTables().get(j);
                TableChooserTreeNode tableNode = new TableChooserTreeNode(table.getName(), table);

                // categories
                boolean categoryExists = false;
                for (int k = 0; k < romNode.getChildCount(); k++) {
                    if (romNode.getChildAt(k).toString().equalsIgnoreCase(table.getCategory())) {
                        ((DefaultMutableTreeNode) romNode.getChildAt(k)).add(tableNode);
                        categoryExists = true;
                        break;
                    }
                }

                if (!categoryExists) {
                    DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(table.getCategory());
                    romNode.add(categoryNode);
                    categoryNode.add(tableNode);
                }
            }
        }

        displayPanel.setPreferredSize(new Dimension(350, 400));
        displayPanel.setMinimumSize(new Dimension(350, 400));
        displayTree.setPreferredSize(new Dimension(330, 400));
        displayTree.setMinimumSize(new Dimension(330, 400));

        displayTree.setRootVisible(true);
        displayTree.updateUI();
        displayPanel.add(new JScrollPane(displayTree));

        Object[] values = {"Compare", "Cancel"};

        if ((showOptionDialog(parent, displayPanel, "Select a Map", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, values, values[0]) == 0 &&
                (displayTree.getLastSelectedPathComponent() instanceof TableChooserTreeNode))) {
            ((TableChooserTreeNode) displayTree.getLastSelectedPathComponent()).getTable().copyTable();
            return true;
        } else {
            return false;
        }
    }
}