package enginuity.swing;

import enginuity.net.URL;

import javax.swing.*;
import java.awt.*;

public class DebugPanel extends JPanel {

    public DebugPanel(Exception ex, String url) {
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(7, 1));
        top.add(new JLabel("Enginuity has encountered an exception. Please review the details below."));
        top.add(new JLabel("If you are unable to fix this problem please visit the following website"));
        top.add(new JLabel("and provide these details and the steps that lead to this error."));
        top.add(new JLabel());
        top.add(new URL(url));
        top.add(new JLabel());
        top.add(new JLabel("Details:"));
        add(top, BorderLayout.NORTH);

        JTextArea output = new JTextArea(ex.getMessage());
        add(output, BorderLayout.CENTER);
        output.setAutoscrolls(true);
        output.setRows(10);
        output.setColumns(40);
        ex.printStackTrace();
    }
}