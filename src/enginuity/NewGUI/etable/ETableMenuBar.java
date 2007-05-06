package enginuity.NewGUI.etable;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import enginuity.NewGUI.etable.dataJPanel.DataJPanelInterface;

public class ETableMenuBar extends JMenuBar implements ActionListener{

	
	private JMenu tableMenu = new JMenu("File");
	private JMenuItem closeItem = new JMenuItem("Close");
	
	
	private JMenu editMenu = new JMenu("Edit");
	private JMenuItem saveItem = new JMenuItem("Save");
	private JMenuItem undoItem = new JMenuItem("Undo");
	private JMenuItem copyTable = new JMenuItem("Copy Table");
	private JMenuItem copySelection = new JMenuItem("Copy Selection");
	private JMenuItem paste = new JMenuItem("Paste Table Data");
	
	private DataJPanelInterface parentEInternalFrame;
	
	public ETableMenuBar(DataJPanelInterface parentFrame){
		this.parentEInternalFrame = parentFrame;
		
		// Setup the GUI below
		
		// Table Menu
		this.saveItem.addActionListener(this);
		this.undoItem.addActionListener(this);
		this.closeItem.addActionListener(this);
		this.tableMenu.add(closeItem);
		this.add(this.tableMenu);
		
		
		// Edit Menu
		this.copySelection.addActionListener(this);
		this.copyTable.addActionListener(this);
		this.paste.addActionListener(this);
		this.editMenu.add(saveItem);
		this.editMenu.add(undoItem);	
		this.editMenu.addSeparator();
		this.editMenu.add(this.copySelection);
		this.editMenu.add(this.copyTable);
		this.editMenu.addSeparator();
		this.editMenu.add(this.paste);
		this.add(this.editMenu);
		
	}
	
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.saveItem){
			this.parentEInternalFrame.saveDataState();
		}
		
		else if(e.getSource() == this.undoItem){
			this.parentEInternalFrame.revertDataState();
		}
		
		else if(e.getSource() == this.closeItem){
			this.parentEInternalFrame.setClosed(true);
		}
		

		else if(e.getSource() == this.copySelection){
			this.parentEInternalFrame.copySelectedTableData();
		}
		

		else if(e.getSource() == this.copyTable){
			this.parentEInternalFrame.copyEntireTable();
		}
		

		else if(e.getSource() == this.paste){
			this.parentEInternalFrame.pasteTableData();
		}
	}

}
