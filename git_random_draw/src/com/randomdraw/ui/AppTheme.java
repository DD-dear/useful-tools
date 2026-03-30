package com.randomdraw.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public final class AppTheme {
    public static final Color BACKGROUND = new Color(238, 241, 245);
    public static final Color PANEL = new Color(252, 252, 250);
    public static final Color PANEL_SOFT = new Color(247, 249, 252);
    public static final Color PRIMARY = new Color(22, 63, 101);
    public static final Color PRIMARY_LIGHT = new Color(88, 129, 168);
    public static final Color ACCENT = new Color(202, 145, 66);
    public static final Color TEXT = new Color(30, 36, 46);
    public static final Color MUTED = new Color(103, 114, 128);
    public static final Color BORDER = new Color(220, 225, 232);
    public static final Color SUCCESS = new Color(59, 122, 87);
    public static final Color DANGER = new Color(156, 73, 73);

    public static final Font TITLE_FONT = new Font("Microsoft YaHei UI", Font.BOLD, 28);
    public static final Font H2_FONT = new Font("Microsoft YaHei UI", Font.BOLD, 18);
    public static final Font BODY_FONT = new Font("Microsoft YaHei UI", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Microsoft YaHei UI", Font.PLAIN, 12);

    private AppTheme() {
    }

    public static void applyGlobalStyle() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("OptionPane.messageFont", BODY_FONT);
        UIManager.put("Label.font", BODY_FONT);
        UIManager.put("Table.font", BODY_FONT);
        UIManager.put("TableHeader.font", BODY_FONT.deriveFont(Font.BOLD));
        UIManager.put("TextField.font", BODY_FONT);
        UIManager.put("ComboBox.font", BODY_FONT);
        UIManager.put("Button.font", BODY_FONT);
        UIManager.put("List.font", BODY_FONT);
        UIManager.put("TabbedPane.font", BODY_FONT);
    }

    public static Border cardBorder() {
        return new CompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        );
    }

    public static Border paddedBorder(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static void styleTextField(JTextField field) {
        field.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT);
        field.setCaretColor(PRIMARY);
    }

    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        return button;
    }

    public static JButton createGhostButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(TEXT);
        button.setBackground(PANEL);
        button.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(10, 18, 10, 18)
        ));
        return button;
    }

    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(DANGER);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        return button;
    }

    public static void enableQuality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public static void paintCardSurface(Graphics2D g2, JComponent c) {
        enableQuality(g2);
        int w = c.getWidth();
        int h = c.getHeight();
        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255), w, h, new Color(242, 245, 249)));
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 28, 28));
        g2.setColor(new Color(255, 255, 255, 100));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 2.2f, h - 2.2f, 28, 28));
    }
}
