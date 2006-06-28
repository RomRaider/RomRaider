package Enginuity.TestDrivers;

// Imports
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.TreeNode;

class TreeExample
		extends 	JFrame
 {
	// Instance attributes used in this example
	private	JPanel		topPanel;
	private	JTree		tree;
	private	JScrollPane scrollPane;

	// Constructor of main frame
	public TreeExample()
	{
		// Set the frame characteristics
		setTitle( "Simple Tree Application" );
		setSize( 300, 300 );
		setBackground( Color.gray );

		// Create a panel to hold all other components
		topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout() );
		getContentPane().add( topPanel );

		// Create a new tree control
		tree = new JTree();
                
                //tree.add(new JTreeNode();

		// Add the listbox to a scrolling pane
		scrollPane = new JScrollPane();
		scrollPane.getViewport().add( tree );
		topPanel.add( scrollPane, BorderLayout.CENTER );
	}

	// Main entry point for this example
	public static void main( String args[] )
	{
		// Create an instance of the test application
		TreeExample mainFrame	= new TreeExample();
		mainFrame.setVisible( true );
	}
}