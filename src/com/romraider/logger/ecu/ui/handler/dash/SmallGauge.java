package com.romraider.logger.ecu.ui.handler.dash;

import com.romraider.logger.ecu.definition.LoggerData;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import java.awt.Dimension;
import java.awt.FlowLayout;
import static java.awt.Font.PLAIN;

public final class SmallGauge extends PlainGauge {
    public SmallGauge(LoggerData loggerData) {
        super(loggerData);
    }

    protected void initGaugeLayout() {
        refreshTitle();
        resetValue();
        setPreferredSize(new Dimension(150, 78));
        setBackground(LIGHT_GREY);
        setLayout(new BorderLayout(1, 0));

        // title
        title.setFont(getFont().deriveFont(PLAIN, 10F));
        title.setForeground(WHITE);
        add(title, NORTH);

        // data panel
        JPanel data = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 1));
        data.setBackground(BLACK);
        liveValueLabel.setFont(getFont().deriveFont(PLAIN, 20F));
        liveValueLabel.setForeground(WHITE);
        liveValuePanel.setBackground(LIGHT_GREY);
        liveValuePanel.setPreferredSize(new Dimension(85, 60));
        liveValuePanel.add(liveValueLabel, CENTER);
        data.add(liveValuePanel);

        // max/min panel
        JPanel maxMinPanel = new JPanel(new BorderLayout(1, 1));
        maxMinPanel.setBackground(BLACK);
        JPanel maxPanel = buildMaxMinPanel("max", maxLabel);
        JPanel minPanel = buildMaxMinPanel("min", minLabel);
        maxMinPanel.add(maxPanel, NORTH);
        maxMinPanel.add(minPanel, SOUTH);
        data.add(maxMinPanel);

        // progress bar
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(false);
        progressBar.setPreferredSize(new Dimension(10, 62));
        progressBar.setBackground(WHITE);
        progressBar.setForeground(GREEN);
        data.add(progressBar);

        // add panels
        add(data, CENTER);
    }

    private JPanel buildMaxMinPanel(String title, JLabel label) {
        label.setFont(getFont().deriveFont(PLAIN, 10F));
        label.setForeground(WHITE);
        JPanel panel = new JPanel(new BorderLayout(1, 1));
        panel.setPreferredSize(new Dimension(45, 28));
        panel.setBackground(LIGHT_GREY);
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(getFont().deriveFont(PLAIN, 10F));
        titleLabel.setForeground(WHITE);
        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.setBackground(DARK_GREY);
        dataPanel.add(label, CENTER);
        panel.add(titleLabel, NORTH);
        panel.add(dataPanel, CENTER);
        return panel;
    }
}
