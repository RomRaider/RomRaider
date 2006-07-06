package enginuity.definitions;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import enginuity.ECUEditor;

public class DefinitionEditor extends JFrame {
    
    public DefinitionEditor(ECUEditor editor) {
        
        setSize(new Dimension(800, 600));
        setLocation((int)editor.getLocation().getX() + 50, (int)editor.getLocation().getY() + 50);
        setIconImage(new ImageIcon("./graphics/enginuity-ico.gif").getImage());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("ECU Definition Editor");
        setResizable(false);
        setVisible(true);
        
        this.add(new RomIDPanel());
            
    }
    
}