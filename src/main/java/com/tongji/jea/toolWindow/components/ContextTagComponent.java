package com.tongji.jea.toolWindow.components;

import javax.swing.*;
import java.awt.*;

public class ContextTagComponent extends JPanel {
    private final String title;
    private final JButton closeButton;

    public ContextTagComponent(String title, Runnable onRemove) {
        this.title = title;
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 2));
        this.setBackground(new Color(60, 63, 65));
        this.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true));

        JLabel label = new JLabel(title);
        label.setForeground(Color.WHITE);

        closeButton = new JButton("✕");
        closeButton.setForeground(Color.LIGHT_GRAY);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> onRemove.run());

        this.add(label);
        this.add(closeButton);
    }

    public String getTitle() {
        return title;
    }
}
