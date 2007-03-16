package enginuity.NewGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import enginuity.NewGUI.data.DataManager;
import enginuity.NewGUI.interfaces.TreeNode;
import enginuity.NewGUI.interfaces.TuningEntity;
import enginuity.logger.utec.impl.UtecTuningEntityImpl;

public class NewGUI extends JFrame implements ActionListener, TreeSelectionListener{
	private static NewGUI instance;
	private JPanel mainJPanel = new JPanel();
	private JSplitPane splitPane = new JSplitPane();
	private JTree leftJTree;
	private JDesktopPane rightDesktopPane = new JDesktopPane();
	private JMenuBar jMenuBar = new JMenuBar();
	private JMenu tuningEntitiesJMenu = new JMenu("Tuning Entities");
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Waiting...");
	
	private NewGUI(){
		initData();
		initGui();
	}
	
	public static NewGUI getInstance(){
		if(instance == null){
			instance = new NewGUI();
		}
		
		return instance;
	}
	
	private void initData(){
		// Add supported tuning entities
		DataManager.addTuningEntity(new UtecTuningEntityImpl());
	}
	
	private void initGui(){
		System.out.println("Initializing GUI.");
		
		
		// Set main JFrame size
		this.setSize(800,600);
		
		
		// Initialise tree
		this.treeModel = new DefaultTreeModel(rootNode);
		this.leftJTree = new JTree(treeModel);
		this.leftJTree.setEditable(true);
		this.leftJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.leftJTree.addTreeSelectionListener(this);
		this.leftJTree.setShowsRootHandles(true);
		
		// Setup JMenu
		Iterator tuningEntities = DataManager.getTuningEntities().iterator();
		while(tuningEntities.hasNext()){
			TuningEntity theTuningEntity = (TuningEntity)tuningEntities.next();
			JMenuItem tempItem = new JMenuItem(theTuningEntity.getName());
			tempItem.addActionListener(this);
			tuningEntitiesJMenu.add(tempItem);
		}
		this.jMenuBar.add(this.tuningEntitiesJMenu);
		this.setJMenuBar(this.jMenuBar);
		
		
		// Test internalFrames
		JInternalFrame internalTest = new JInternalFrame("Test Internal", true, true, true, true);
		internalTest.setSize(300,300);
		internalTest.setVisible(true);
		
		
		// Setup desktop pane
		rightDesktopPane.setBackground(Color.BLACK);
		rightDesktopPane.add(internalTest);
		
		
		// Setup split pane
		splitPane.setDividerLocation(200);
		splitPane.setLeftComponent(leftJTree);
		splitPane.setRightComponent(rightDesktopPane);
		
		
		// Setup main JPanel
		mainJPanel.setLayout(new BorderLayout());
		mainJPanel.add(splitPane, BorderLayout.CENTER);
		
		
		// Add everything to JFrame
		this.add(mainJPanel);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equalsIgnoreCase("UTEC Tuning Entity")){
			String theCommand = e.getActionCommand();
			if(DataManager.getCurrentTuningEntity() != null && DataManager.getCurrentTuningEntity().getName().equals("UTEC Tuning Entity")){
				return;
			}
			
			DataManager.setCurrentTuningEntity(theCommand);
			rebuildJMenuBar();
			rebuildJTree();
		}
	}
	
	private void rebuildJTree(){
		this.leftJTree.removeAll();
		DefaultMutableTreeNode treeMainNode = new DefaultMutableTreeNode(DataManager.getCurrentTuningEntity().getName());
		TreeNode parentNode = DataManager.getCurrentTuningEntity().getJTreeNodeStructure();
		DefaultMutableTreeNode topJTreeNode = getChildren(parentNode);
		System.out.println("Parent:"+parentNode.getName());
		this.treeModel.removeNodeFromParent(this.rootNode);
		//this.treeModel.insertNodeInto(rootNode);
		this.leftJTree.revalidate();
		
		
	}
	
	public DefaultMutableTreeNode getChildren(TreeNode theNode){
		DefaultMutableTreeNode returnNode = new DefaultMutableTreeNode(theNode.getName());
		LinkedList children = theNode.getChildren();
		
		Iterator childIterator = children.iterator();
		while(childIterator.hasNext()){
			TreeNode tempNode = (TreeNode)childIterator.next();
			DefaultMutableTreeNode jtreeNode = getChildren(tempNode);
			returnNode.add(jtreeNode);
		}
		
		
		return returnNode;
	}

	private void rebuildJMenuBar() {
		Vector<JMenu> items = DataManager.getCurrentTuningEntity().getMenuItems();
		Iterator iterator = items.iterator();
		
		this.jMenuBar.removeAll();
		jMenuBar.add(this.tuningEntitiesJMenu);
		while(iterator.hasNext()){
			JMenu tempMenu = (JMenu)iterator.next();
			jMenuBar.add(tempMenu);
		}
		this.jMenuBar.revalidate();
	}

	public void valueChanged(TreeSelectionEvent arg0) {
		
		System.out.println("Tree Node selected.");
		
	}
	
}
