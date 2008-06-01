package com.romraider.logger.ecu.ui.handler.dash;

import com.romraider.logger.ecu.definition.LoggerData;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import java.awt.Dimension;
import java.awt.FlowLayout;
import static java.awt.Font.PLAIN;

public final class NoFrillsGaugeStyle extends PlainGaugeStyle {
    public NoFrillsGaugeStyle(LoggerData loggerData) {
        super(loggerData);
    }

    protected void doApply(JPanel panel) {
        refreshTitle();
        resetValue();
        panel.setPreferredSize(new Dimension(150, 78));
        panel.setBackground(LIGHT_GREY);
        panel.setLayout(new BorderLayout(1, 0));

        // title
        title.setFont(panel.getFont().deriveFont(PLAIN, 10F));
        title.setForeground(WHITE);
        panel.add(title, NORTH);

        // data panel
        JPanel data = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        data.setBackground(BLACK);
        liveValueLabel.setFont(panel.getFont().deriveFont(PLAIN, 35F));
        liveValueLabel.setForeground(WHITE);
        liveValuePanel.setBackground(LIGHT_GREY);
        liveValuePanel.setPreferredSize(new Dimension(144, 60));
        liveValuePanel.add(liveValueLabel, CENTER);
        data.add(liveValuePanel);

        // add panels
        panel.add(data, CENTER);
    }
}