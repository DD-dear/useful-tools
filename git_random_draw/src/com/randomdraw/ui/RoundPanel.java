package com.randomdraw.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class RoundPanel extends JPanel {
    public RoundPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.paintCardSurface(g2, this);
        g2.dispose();
        super.paintComponent(g);
    }
}
