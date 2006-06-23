package enginuity.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.LineBorder;

public class JProgressPane extends JFrame {
    
    JLabel       label       = new JLabel();
    JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
    
    public JProgressPane(Component component, String title, String status) {
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        label.setHorizontalAlignment(label.CENTER);
        label.setText(status);
        panel.add(label);
        panel.add(progressBar);
        panel.setBorder(new LineBorder(getBackground(), 12));       
        
        setSize(400,60);        
        setLocationRelativeTo(component);
        setTitle(title);       
        setUndecorated(true);
        getContentPane().add(panel);
        
        setVisible(true);
        requestFocus();
    }   
    
    public void update(String status, int percent) {
        label.setText(status);
        progressBar.setValue(percent);
        update(getGraphics());
        requestFocus();
    }
}