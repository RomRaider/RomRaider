package enginuity.NewGUI;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableMetaData;
import enginuity.NewGUI.desktop.EDesktopPane;
import enginuity.NewGUI.etable.EInternalFrame;
import enginuity.NewGUI.interfaces.TuningEntity;
import enginuity.NewGUI.interfaces.TuningEntityListener;
import enginuity.NewGUI.tree.ETree;
import enginuity.NewGUI.tree.ETreeNode;
import enginuity.logger.utec.impl.UtecTuningEntityImpl;
import enginuity.swing.LookAndFeelManager;
import org.apache.log4j.Logger;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.Vector;

public class NewGUI extends JFrame implements WindowListener, ActionListener,
        TreeSelectionListener, TuningEntityListener {
    private static final Logger LOGGER = Logger.getLogger(NewGUI.class);
    private final String engninuityVersionTitle = "RomRaider v0.5.0 alpha 1";

    private JPanel mainJPanel = new JPanel();

    private JMenuBar jMenuBar = new JMenuBar();
    private JMenu tuningEntitiesJMenu = new JMenu("Tuning Entities");

    private JSplitPane splitPane = new JSplitPane();
    private EDesktopPane rightDesktopPane = new EDesktopPane();

    private ETreeNode rootNode = new ETreeNode("RomRaider", new TableMetaData(
            TableMetaData.RESERVED_ROOT, 0.0, 0.0, new Object[0], null, null,
            false, "", "", "", "", "", null));
    private ETree leftJTree = new ETree(rootNode);

    private boolean newTree = true;

    private NewGUI() {
        // Define which tuning entities are available
        initData();

        // Initialize the GUI elements
        initGui();
    }

    public static NewGUI getInstance() {
        if (ApplicationStateManager.getEnginuityInstance() == null) {
            ApplicationStateManager.setEnginuityInstance(new NewGUI());
        }

        return ApplicationStateManager.getEnginuityInstance();
    }

    private void initData() {
        // Add supported tuning entities
        // As new tuning entities are developed, add them here
        UtecTuningEntityImpl utei = new UtecTuningEntityImpl();

        ApplicationStateManager.addTuningEntity(utei);
    }

    private void initGui() {
        LOGGER.info("Initializing GUI.");

        // Set the frame icon
        Image img = Toolkit.getDefaultToolkit().getImage(
                "graphics/enginuity-ico.gif");
        setIconImage(img);

        // Set frame title
        this.setTitle(this.engninuityVersionTitle);

        // Set main JFrame size
        this.setSize(800, 600);

        // Setup the look and feel
        LookAndFeelManager.initLookAndFeel();

        // This class implements its own windows closing methods. Duh!!! ;-)
        this.addWindowListener(this);

        // Setup JMenu
        Iterator tuningEntities = ApplicationStateManager.getTuningEntities()
                .iterator();
        while (tuningEntities.hasNext()) {
            TuningEntity theTuningEntity = (TuningEntity) tuningEntities.next();
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

        if (e.getActionCommand().equalsIgnoreCase("UTEC Tuning Entity")) {
            String theCommand = e.getActionCommand();

            ApplicationStateManager.setCurrentTuningEntity(theCommand, this);
        }
    }

    public void rebuildJMenuBar(Vector<JMenu> items) {
        Iterator iterator = items.iterator();

        this.jMenuBar.removeAll();

        while (iterator.hasNext()) {
            JMenu tempMenu = (JMenu) iterator.next();
            jMenuBar.add(tempMenu);
        }

        jMenuBar.add(this.tuningEntitiesJMenu);

        this.jMenuBar.revalidate();
    }

    public void valueChanged(TreeSelectionEvent arg0) {

        LOGGER.debug("Tree Node selected.");

    }

    public void addTuningGroupNameToTitle(String titleAppend) {
        this.setTitle(this.engninuityVersionTitle + ": " + titleAppend);
    }

    /**
     * Tuning group is a collection of maps and parameters, ala a ROM or a UTEC
     * Map file
     */
    public void addNewTuningGroup(ETreeNode newTreeModel) {
        LOGGER.debug("test: " + this.newTree);

        int childCount = this.rootNode.getChildCount();
        String newTuningGroup = newTreeModel.getTableMetaData().getTableGroup();

        LOGGER.debug("Children:" + childCount + "  :" + newTuningGroup);
        for (int i = 0; i < childCount; i++) {
            ETreeNode tempNode = (ETreeNode) this.rootNode.getChildAt(i);
            if (tempNode.getTableMetaData().getTableGroup().equals(
                    newTuningGroup)) {
                LOGGER.error("Can't open same ROM / Map file 2x");
                return;
            }
        }

        if (this.newTree == true) {
            this.newTree = false;
            this.rootNode.removeAllChildren();
        }

        this.rootNode.add(newTreeModel);
        this.leftJTree.updateUI();
        this.splitPane.repaint();
    }

    /**
     * Removes a tuning group from the GUI
     */
    public void removeTuningGroup(String tableGroup) {
        int childCount = this.rootNode.getChildCount();

        for (int i = 0; i < childCount; i++) {
            ETreeNode tempNode = (ETreeNode) this.rootNode.getChildAt(i);
            if (tempNode.getTableMetaData().getTableGroup().equals(tableGroup)) {
                ApplicationStateManager
                        .setSelectedTuningGroup("No Tuning Group Selected.");
                this.addTuningGroupNameToTitle("");
                this.rootNode.remove(i);
                this.leftJTree.updateUI();
                this.splitPane.repaint();

                // Clean up
                this.rightDesktopPane.removeInternalFrames(tableGroup);

                // Clean up on tuning entity sides
                Iterator tuningEntites = ApplicationStateManager
                        .getTuningEntities().iterator();

                while (tuningEntites.hasNext()) {
                    TuningEntity theTuningEntity = (TuningEntity) tuningEntites
                            .next();
                    theTuningEntity.removeTuningGroup(tableGroup);
                }

                return;
            }
        }
    }

    public void displayInternalFrameTable(Object[][] data, TableMetaData tableMetaData) {
        this.rightDesktopPane.add(data, tableMetaData);
    }

    public void removeInternalFrame(EInternalFrame frame) {
        this.rightDesktopPane.remove(frame);
    }

    public void setNewToolBar(JToolBar theToolBar) {
        // Ensure proper color
        theToolBar.setBackground(new Color(236, 233, 216));
        this.add(theToolBar, BorderLayout.NORTH);
    }

    /*
      * Helper method that returns the number of maps that have had their data changed.
      * (non-Javadoc)
      * @see enginuity.NewGUI.interfaces.TuningEntityListener#getMapChangeCount(enginuity.NewGUI.interfaces.TuningEntity, java.lang.String)
      */
    public int getMapChangeCount(TuningEntity tuningEntity, String tableGroup) {
        JInternalFrame[] allFrames = this.rightDesktopPane.getAllFrames();
        int number = 0;
        for (int i = 0; i < allFrames.length; i++) {
            EInternalFrame eInternalFrame = (EInternalFrame) allFrames[i];

            if (eInternalFrame.getTableMetaData().getTableGroup().equals(
                    tableGroup)) {
                if (eInternalFrame.dataChanged()) {
                    number++;
                }
            }
        }

        return number;

    }

    /*
      * Method walks through all opened JInternalFrames in right pane. If an InternFrame claims its
      * data has been changed, the tuning entity parent is notified.
      * (non-Javadoc)
      * @see enginuity.NewGUI.interfaces.TuningEntityListener#saveMaps()
      */
    public void saveMaps() {
        JInternalFrame[] allFrames = this.rightDesktopPane.getAllFrames();
        String tableGroup = ApplicationStateManager.getSelectedTuningGroup();

        for (int i = 0; i < allFrames.length; i++) {
            EInternalFrame eInternalFrame = (EInternalFrame) allFrames[i];

            if (eInternalFrame.getTableMetaData().getTableGroup().equals(
                    tableGroup)) {
                if (eInternalFrame.dataChanged()) {
                    eInternalFrame.saveDataToParentTuningEntity();
                }
            }
        }
    }

    /**
     * Getter that returns the title of Enginuity
     *
     * @return
     */
    public String getEngninuityTitle() {
        return engninuityVersionTitle;
    }

    /*
      * Tuning entity in scope will call this when it is ready to exit. Some entities might need to cleanup connections or save files.
      * (non-Javadoc)
      * @see enginuity.NewGUI.interfaces.TuningEntityListener#readyForExit()
      */
    public void readyForExit() {
        LOGGER.info("RomRaider is now exiting as per tuning entity notification: " + ApplicationStateManager.getCurrentTuningEntity().getName());
        System.exit(0);
    }

    // **************************************************************
    // Methods pertaining to the WindowListener this class implements
    // **************************************************************

    public void windowActivated(WindowEvent e) {
        LOGGER.info("Window Activated.");
    }

    public void windowClosed(WindowEvent e) {
        LOGGER.info("Window Closed.");
    }

    public void windowClosing(WindowEvent e) {
        LOGGER.info("Preparing RomRaider for exit.");

        TuningEntity currentTuningEntity = ApplicationStateManager
                .getCurrentTuningEntity();

        if (currentTuningEntity == null) {
            LOGGER.debug("No Tuning Entity ever selected.");
            LOGGER.info("RomRaider exiting immediately.");
            System.exit(0);
        } else {
            LOGGER.debug("Notify current tuning entity of pending exit.");
            currentTuningEntity.notifySystemExit();
        }
    }

    public void windowDeactivated(WindowEvent e) {
        // TODO Auto-generated method stub
        LOGGER.info("Window Deactivated.");
    }

    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub
        LOGGER.info("Window Deiconified.");
    }

    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub
        LOGGER.info("Window Iconified.");
    }

    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub
        LOGGER.info("Window Opened.");
    }

}
