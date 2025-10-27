package com.tongji.jea.toolWindow.components;

import com.tongji.jea.model.ContextItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 美观的上下文标签组件（深色主题）
 */
public class ContextTagComponent extends JPanel {
    private final ContextItem item;
    private final Runnable onRemove;

    public ContextTagComponent(ContextItem item, Runnable onRemove) {
        this.item = item;
        this.onRemove = onRemove;
        initUI();
    }

    private void initUI() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        setOpaque(false);

        // 圆角背景（用 JLabel 的 paintComponent 实现）
        JLabel label = new JLabel(item.getLabel());
        label.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        label.setForeground(new Color(220, 220, 220));
        label.setOpaque(false);

        JLabel closeBtn = new JLabel("✕");
        closeBtn.setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        closeBtn.setForeground(new Color(180, 180, 180));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onRemove != null) onRemove.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtn.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeBtn.setForeground(new Color(180, 180, 180));
            }
        });

        JPanel bubble = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(45, 45, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(80, 80, 90));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.add(label);
        bubble.add(closeBtn);
        bubble.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        add(bubble);
    }

    public ContextItem getContextItem() {
        return item;
    }
}
