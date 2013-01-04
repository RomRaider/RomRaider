package com.romraider.logger.ecu.ui.swing.menubar.action;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

public class LogFileNameFieldAction extends MouseAdapter {
    private final JPopupMenu menu;
    
    public LogFileNameFieldAction(JPopupMenu menu){
        this.menu = menu;
    }
    
    public void mousePressed(MouseEvent e) {
        ShowFieldPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        ShowFieldPopup(e);
    }

    private void ShowFieldPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            menu.show(e.getComponent(),
                       e.getX(), e.getY());
        }
    }
}
