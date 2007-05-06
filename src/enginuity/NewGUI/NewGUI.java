package enginuity.NewGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableMetaData;
import enginuity.NewGUI.desktop.EDesktopPane;
import enginuity.NewGUI.etable.EInternalFrame;
import enginuity.NewGUI.etable.ETable;
import enginuity.NewGUI.etable.ETableToolBar;
import enginuity.NewGUI.interfaces.TuningEntity;
import enginuity.NewGUI.interfaces.TuningEntityListener;
import enginuity.NewGUI.tree.ETree;
import enginuity.NewGUI.tree.ETreeCellRenderer;
import enginuity.NewGUI.tree.ETreeNode;
import enginuity.logger.utec.impl.UtecTuningEntityImpl;
import enginuity.swing.LookAndFeelManager;

public class NewGUI extends JFrame implements ActionListener, TreeSelectionListener, TuningEntityListener{
	
	private final String engninuityVersionTitle = "Enginuity v0.5.0 alpha 1";
	
	private JPanel mainJPanel = new JPanel();
	
	private JMenuBar jMenuBar = new JMenuBar();
	private JMenu tuningEntitiesJMenu = new JMenu("Tuning Entities");
	
	private JSplitPane splitPane = new JSplitPane();
	private EDesktopPane rightDesktopPane = new EDesktopPane();
	
	private ETreeNode rootNode = new ETreeNode("Enginuity", new TableMetaData(TableMetaData.RESERVED_ROOT,0.0,0.0,new Object[0],null, null,false,"", "", "", "", "", null));
	private ETree leftJTree = new ETree(rootNode);
	
	private boolean newTree = true;
	
	private NewGUI(){
		// Define which tuning entities are available
		initData();
		
		// Initialize the GUI elements
		initGui();
	}
	
	public static NewGUI getInstance(){
		if(ApplicationStateManager.getEnginuityInstance() == null){
			ApplicationStateManager.setEnginuityInstance(new NewGUI());
		}
		
		return ApplicationStateManager.getEnginuityInstance();
	}
	
	private void initData(){
		// Add supported tuning entities
		UtecTuningEntityImpl utei = new UtecTuningEntityImpl();
		
		ApplicationStateManager.addTuningEntity(utei);
	}
	
	private void initGui(){
		System.out.println("Initializing GUI.");
		
		// Set the frame icon
		Image img = Toolkit.getDefaultToolkit().getImage("graphics/enginuity-ico.gif");
		setIconImage( img );
		
		
		// Set frame title
		this.setTitle(this.engninuityVersionTitle);
		
		
		// Set main JFrame size
		this.setSize(800,600);
		
		
		// Setup the look and feel
		LookAndFeelManager.initLookAndFeel();
		
		
		// Define window closed operation
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		// Setup JMenu
		Iterator tuningEntities = ApplicationStateManager.getTuningEntities().iterator();
		while(tuningEntities.hasNext()){
			TuningEntity theTuningEntity = (TuningEntity)tuningEntities.next();
			JMenuItem tempItem = new JMenuItem(theTuningEntity.getName());
			tempItem.addActionListener(this);
			tuningEntitiesJMenu.add(tempItem);
		}
		
		this.jMenuBar.add(this.tuningEntitiesJMenu);
		this.jMenuBar.setBackground(new Color(236, 233, 216));
		this.setLayout(new BorderLayout());
		this.setJMenuBar(this.jMenuBar);
	
		
		// Setup desktop pane
		rightDesktopPane.setBackground(Color.BLACK);
		
		
		// Setup split pane
		splitPane.setDividerLocation(200);
		splitPane.setLeftComponent(leftJTree);
		splitPane.setRightComponent(rightDesktopPane);
		splitPane.setDividerSize(5);
		
		
		// Setup main JPanel
		mainJPanel.setLayout(new BorderLayout());
		mainJPanel.add(splitPane, BorderLayout.CENTER);
		
		
		// Add everything to JFrame
		this.add(mainJPanel, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equalsIgnoreCase("UTEC Tuning Entity")){
			String theCommand = e.getActionCommand();
			
			
			ApplicationStateManager.setCurrentTuningEntity(theCommand, this);
		}
	}
	

	public void rebuildJMenuBar(Vector<JMenu> items) {
		Iterator iterator = items.iterator();
		
		this.jMenuBar.removeAll();
		
		while(iterator.hasNext()){
			JMenu tempMenu = (JMenu)iterator.next();
			jMenuBar.add(tempMenu);
		}
		
		jMenuBar.add(this.tuningEntitiesJMenu);
		
		this.jMenuBar.revalidate();
	}

	public void valueChanged(TreeSelectionEvent arg0) {
		
		System.out.println("Tree Node selected.");
		
	}
	
	public void addTuningGroupNameToTitle(String titleAppend){
		this.setTitle(this.engninuityVersionTitle+": "+titleAppend);
	}

	/**
	 * Tuning group is a collection of maps and parameters, ala a ROM or a UTEC Map file
	 * 
	 */
	public void addNewTuningGroup(ETreeNode newTreeModel) {
		System.out.println("test: "+this.newTree);
		
		int childCount = this.rootNode.getChildCount();
		String newTuningGroup = newTreeModel.getTableMetaData().getTableGroup();
		
		System.out.println("Children:"+childCount +"  :"+newTuningGroup);
		for(int i = 0; i < childCount; i++){
			ETreeNode tempNode = (ETreeNode)this.rootNode.getChildAt(i);
			if(tempNode.getTableMetaData().getTableGroup().equals(newTuningGroup)){
				System.out.println("Can't open same ROM / Map file 2x");
				
				return;
			}
		}
		
		
		if(this.newTree == true){
			this.newTree = false;
			this.rootNode.removeAllChildren();
		}
		
		this.rootNode.add(newTreeModel);
		this.leftJTree.updateUI();
		this.splitPane.repaint();
	}
	
	/**
	 * Removes a tuning group from the GUI
	 * 
	 */
	public void removeTuningGroup(String tableGroup){
		int childCount = this.rootNode.getChildCount();
		
		for(int i = 0; i < childCount; i++){
			ETreeNode tempNode = (ETreeNode)this.rootNode.getChildAt(i);
			if(tempNode.getTableMetaData().getTableGroup().equals(tableGroup)){
				ApplicationStateManager.setSelectedTuningGroup("No Tuning Group Selected.");
				this.addTuningGroupNameToTitle("");
				this.rootNode.remove(i);
				this.leftJTree.updateUI();
				this.splitPane.repaint();
				
				// Clean up
				this.rightDesktopPane.removeInternalFrames(tableGroup);
				
				// Clean up on tuning entity sides
				Iterator tuningEntites = ApplicationStateManager.getTuningEntities().iterator();
				
				while(tuningEntites.hasNext()){
					TuningEntity theTuningEntity = (TuningEntity)tuningEntites.next();
					theTuningEntity.removeTuningGroup(tableGroup);
				}
				
				
				return;
			}
		}
	}

	public void displayInternalFrameTable(Object[][] data, TableMetaData tableMetaData){
		this.rightDesktopPane.add(data, tableMetaData);
	}
	
	public void removeInternalFrame(EInternalFrame frame){
		this.rightDesktopPane.remove(frame);
	}

	public void setNewToolBar(JToolBar theToolBar) {
		// Ensure proper color
		theToolBar.setBackground(new Color(236, 233, 216));
		this.add(theToolBar, BorderLayout.NORTH);
	}
	
	
	public int getMapChangeCount(TuningEntity tuningEntity, String tableGroup){
		JInternalFrame[] allFrames = this.rightDesktopPane.getAllFrames();
		int number = 0;
		for(int i = 0 ; i < allFrames.length; i++){
			EInternalFrame eInternalFrame = (EInternalFrame)allFrames[i];
			
			if(eInternalFrame.getTableMetaData().getTableGroup().equals(tableGroup)){
				if(eInternalFrame.dataChanged()){
					number++;
				}
			}
		}
		
		return number;
		
	}
	
	public void saveMaps(){
		JInternalFrame[] allFrames = this.rightDesktopPane.getAllFrames();
		String tableGroup = ApplicationStateManager.getSelectedTuningGroup();
		
		for(int i = 0 ; i < allFrames.length; i++){
			EInternalFrame eInternalFrame = (EInternalFrame)allFrames[i];
			
			if(eInternalFrame.getTableMetaData().getTableGroup().equals(tableGroup)){
				if(eInternalFrame.dataChanged()){
					eInternalFrame.saveDataToParentTuningEntity();
				}
			}
		}
	}

	public String getEngninuityTitle() {
		return engninuityVersionTitle;
	}

}
