package enginuity.NewGUI.etable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class ETableMenuBar extends JMenuBar implements ActionListener{

	
	private JMenu tableMenu = new JMenu("Table");
	private JMenuItem saveItem = new JMenuItem("Save");
	private JMenuItem undoItem = new JMenuItem("Undo");
	private JMenuItem closeItem = new JMenuItem("Close");
	
	
	private JMenu editMenu = new JMenu("Edit");
	
	
	
	private EInternalFrame parentFrame;
	
	public ETableMenuBar(EInternalFrame parentFrame){
		this.parentFrame = parentFrame;
		
		// Setup the GUI below
		
		// Table Menu
		this.saveItem.addActionListener(this);
		this.undoItem.addActionListener(this);
		this.closeItem.addActionListener(this);
		this.tableMenu.add(saveItem);
		this.tableMenu.add(undoItem);
		this.tableMenu.add(closeItem);
		this.add(this.tableMenu);
		
		
		// Edit Menu
		this.add(this.editMenu);
		
	}
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.saveItem){
			this.parentFrame.saveDataState();
		}
		
		else if(e.getSource() == this.undoItem){
			this.parentFrame.revertDataState();
		}
		
		else if(e.getSource() == this.closeItem){
			try {
				this.parentFrame.setClosed(true);
			} catch (PropertyVetoException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
