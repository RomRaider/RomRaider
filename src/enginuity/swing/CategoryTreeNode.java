package enginuity.swing;

import enginuity.maps.Rom;
import javax.swing.tree.DefaultMutableTreeNode;

public class CategoryTreeNode extends DefaultMutableTreeNode {
    
    private Rom rom;
    
    public CategoryTreeNode(String name, Rom rom) {
        super(name);
        this.setRom(rom);
    }   

    public Rom getRom() {
        return rom;
    }

    public void setRom(Rom rom) {
        this.rom = rom;
    }
}