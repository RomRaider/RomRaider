package enginuity.NewGUI.desktop;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.beans.PropertyVetoException;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableMetaData;
import enginuity.NewGUI.etable.EInternalFrame;


public class EDesktopPane extends JDesktopPane{
    private static int FRAME_OFFSET = 20;
    private MDIDesktopManager manager;

    public EDesktopPane() {
        manager = new MDIDesktopManager(this);
        setDesktopManager(manager);
        setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
    }

    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        checkDesktopSize();
    }
    
    /**
     * Removed the internal frames associated with the passed tunign group.
     * 
     * @param tuningGroup
     */
    public void removeInternalFrames(String tuningGroup){
    	JInternalFrame[] allFrames = getAllFrames();
    	
    	EInternalFrame tempFrame = null;
        for(int i = 0; i < allFrames.length ; i++){
        	tempFrame = (EInternalFrame)allFrames[i];
        	if(tempFrame.getTableMetaData().getTableGroup().equals(tuningGroup)){
        		System.out.println("EDesktopPane: removing internal frame.");
        		this.remove(tempFrame);
        	}
        }
    }

    public Component add(Object[][] data, TableMetaData tableMetaData) {
        Point p;
        int w;
        int h;

        JInternalFrame[] allFrames = getAllFrames();
    	boolean addFrame = true;
    	EInternalFrame tempFrame = null;
        for(int i = 0; i < allFrames.length ; i++){
    		tempFrame = (EInternalFrame)allFrames[i];
    		
    		//Does the fram we're adding already exist?
    		if(tempFrame.getTableMetaData().getTableIdentifier() == tableMetaData.getTableIdentifier()){
    			addFrame = false;
    			break;
    		}
    	}
        
        // Frame does not exist already, now add
        if(addFrame){
        	EInternalFrame frame = new EInternalFrame(tableMetaData, data);
        	Component retval = super.add(frame);
            checkDesktopSize();
            if (allFrames.length > 0) {
                p = allFrames[0].getLocation();
                p.x = p.x + FRAME_OFFSET;
                p.y = p.y + FRAME_OFFSET;
            } else {
                p = new Point(0, 0);
            }
            frame.setLocation(p.x, p.y);
            if (frame.isResizable()) {
                w = getWidth() - (getWidth() / 3);
                h = getHeight() - (getHeight() / 3);
                if (w < frame.getMinimumSize().getWidth()) {
                    w = (int) frame.getMinimumSize().getWidth();
                }
                if (h < frame.getMinimumSize().getHeight()) {
                    h = (int) frame.getMinimumSize().getHeight();
                }
                //frame.setSize(w, h);
            }
            moveToFront(frame);
            frame.setVisible(true);
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException e) {
                frame.toBack();
            }
            return retval;
        }
        
        // Since frame already exists, toggle its visibility
        else{
        	boolean isVisible = tempFrame.isVisible();
        	// Toggle the visibility of the frame
        	if(isVisible){
        		tempFrame.setVisible(false);
        	}else{
        		tempFrame.setVisible(true);
        	}
        }
        
        return null;
    }

    public void remove(Component c) {
        super.remove(c);
        checkDesktopSize();
    }

    /**
     * Cascade all internal frames
     */
    public void cascadeFrames() {
        int x = 0;
        int y = 0;
        JInternalFrame allFrames[] = getAllFrames();

        manager.setNormalSize();
        int frameHeight = (getBounds().height - 5) - allFrames.length * FRAME_OFFSET;
        int frameWidth = (getBounds().width - 5) - allFrames.length * FRAME_OFFSET;
        for (int i = allFrames.length - 1; i >= 0; i--) {
            //allFrames[i].setSize(frameWidth, frameHeight);
            allFrames[i].setLocation(x, y);
            x = x + FRAME_OFFSET;
            y = y + FRAME_OFFSET;
        }
    }

    /**
     * Tile all internal frames
     */
    public void tileFrames() {
        java.awt.Component allFrames[] = getAllFrames();
        manager.setNormalSize();
        int frameHeight = getBounds().height / allFrames.length;
        int y = 0;
        for (int i = 0; i < allFrames.length; i++) {
            //allFrames[i].setSize(getBounds().width, frameHeight);
            allFrames[i].setLocation(0, y);
            y = y + frameHeight;
        }
    }

    /**
     * Sets all component size properties ( maximum, minimum, preferred)
     * to the given dimension.
     */
    public void setAllSize(Dimension d) {
        setMinimumSize(d);
        setMaximumSize(d);
        setPreferredSize(d);
    }

    /**
     * Sets all component size properties ( maximum, minimum, preferred)
     * to the given width and height.
     */
    public void setAllSize(int width, int height) {
        setAllSize(new Dimension(width, height));
    }

    private void checkDesktopSize() {
        if (getParent() != null && isVisible()) {
            manager.resizeDesktop();
        }
    }
}

/**
 * Private class used to replace the standard DesktopManager for JDesktopPane.
 * Used to provide scrollbar functionality.
 */
class MDIDesktopManager extends DefaultDesktopManager {
    private EDesktopPane desktop;

    public MDIDesktopManager(EDesktopPane desktop) {
        this.desktop = desktop;
    }

    public void endResizingFrame(JComponent f) {
        super.endResizingFrame(f);
        resizeDesktop();
    }

    public void endDraggingFrame(JComponent f) {
        super.endDraggingFrame(f);
        resizeDesktop();
    }

    public void setNormalSize() {
        JScrollPane scrollPane = getScrollPane();
        int x = 0;
        int y = 0;
        Insets scrollInsets = getScrollPaneInsets();

        if (scrollPane != null) {
            Dimension d = scrollPane.getVisibleRect().getSize();
            if (scrollPane.getBorder() != null) {
                d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
                        d.getHeight() - scrollInsets.top - scrollInsets.bottom);
            }

            d.setSize(d.getWidth() - 20, d.getHeight() - 20);
            desktop.setAllSize(x, y);
            scrollPane.invalidate();
            scrollPane.validate();
        }
    }

    private Insets getScrollPaneInsets() {
        JScrollPane scrollPane = getScrollPane();
        if (scrollPane == null) {
            return new Insets(0, 0, 0, 0);
        } else {
            return getScrollPane().getBorder().getBorderInsets(scrollPane);
        }
    }

    private JScrollPane getScrollPane() {
        if (desktop.getParent() instanceof JViewport) {
            JViewport viewPort = (JViewport) desktop.getParent();
            if (viewPort.getParent() instanceof JScrollPane) {
                return (JScrollPane) viewPort.getParent();
            }
        }
        return null;
    }

    protected void resizeDesktop() {
        int x = 0;
        int y = 0;
        JScrollPane scrollPane = getScrollPane();
        Insets scrollInsets = getScrollPaneInsets();

        if (scrollPane != null) {
            JInternalFrame allFrames[] = desktop.getAllFrames();
            for (int i = 0; i < allFrames.length; i++) {
                if (allFrames[i].getX() + allFrames[i].getWidth() > x) {
                    x = allFrames[i].getX() + allFrames[i].getWidth();
                }
                if (allFrames[i].getY() + allFrames[i].getHeight() > y) {
                    y = allFrames[i].getY() + allFrames[i].getHeight();
                }
            }
            Dimension d = scrollPane.getVisibleRect().getSize();
            if (scrollPane.getBorder() != null) {
                d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
                        d.getHeight() - scrollInsets.top - scrollInsets.bottom);
            }

            if (x <= d.getWidth()) {
                x = ((int) d.getWidth()) - 20;
            }
            if (y <= d.getHeight()) {
                y = ((int) d.getHeight()) - 20;
            }
            desktop.setAllSize(x, y);
            scrollPane.invalidate();
            scrollPane.validate();
        }
    }
}
