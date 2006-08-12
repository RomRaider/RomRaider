package enginuity.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class JProgressPane extends JPanel {
    
    JLabel       label       = new JLabel();
    JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
    
    public JProgressPane() {
        
        this.setPreferredSize(new Dimension(500, 18));
        this.setLayout(new BorderLayout(1, 2));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setText(" Ready...");
        label.setFont(new Font("Tahoma", Font.PLAIN, 11));
        label.setHorizontalAlignment(JLabel.LEFT);
        progressBar.setMinimumSize(new Dimension(200, 50));
        
        this.add(progressBar, BorderLayout.WEST);       
        this.add(label, BorderLayout.CENTER); 
        
    }   
    
    public void update(String status, int percent) {
        label.setText(" " + status);
        progressBar.setValue(percent);
        repaint();
        this.update(this.getGraphics());
    }
}