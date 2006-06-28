package enginuity.net;

import enginuity.net.BrowserControl;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;

public class URL extends JLabel implements MouseListener {
    
    String url = "";
    
    public URL(String url) {        
        super(url);
        this.url = url;
        this.setFont(new Font("Arial", Font.PLAIN, 12));
        this.addMouseListener(this);
    }    
    
    public void paint (Graphics g) {
        super.paint(g);
        Font f = getFont();
        FontMetrics fm = getFontMetrics(f);
        int x1 = 0;
        int y1 = fm.getHeight();
        int x2 = fm.stringWidth(getText());
        if (getText().length() > 0)
        g.drawLine(x1, y1, x2, y1);
    }

    public void mouseClicked(MouseEvent e) {
        BrowserControl.displayURL(url);
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
}