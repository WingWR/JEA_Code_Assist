package com.tongji.jea.toolWindow.components;

import javax.swing.*;
import java.awt.*;

/**
 * FlowLayout 的增强版：支持自动换行。
 */
public class WrapLayout extends FlowLayout {

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int maxWidth = target.getParent() instanceof JComponent
                    ? target.getParent().getWidth() : Integer.MAX_VALUE;
            Insets insets = target.getInsets();
            int hgap = getHgap(), vgap = getVgap();
            int width = maxWidth - (insets.left + insets.right + hgap * 2);

            int x = 0, y = insets.top + vgap, rowHeight = 0;
            int reqdWidth = 0;

            for (Component c : target.getComponents()) {
                if (!c.isVisible()) continue;
                Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();

                if (x == 0 || (x + d.width <= width)) {
                    if (x > 0) x += hgap;
                    x += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                } else {
                    x = d.width;
                    y += vgap + rowHeight;
                    rowHeight = d.height;
                }
                reqdWidth = Math.max(reqdWidth, x);
            }

            y += rowHeight + vgap;
            return new Dimension(reqdWidth + insets.left + insets.right, y + insets.bottom);
        }
    }
}
