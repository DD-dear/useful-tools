package com.randomdraw.ui;

import com.randomdraw.service.DataStore;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class MainFrame extends JFrame {
    private final java.awt.CardLayout cardLayout = new java.awt.CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final DataStore dataStore;
    private final ManagementPanel managementPanel;
    private final LotteryPanel lotteryPanel;
    private final StatisticsPanel statisticsPanel;
    private final ButtonGroup navGroup = new ButtonGroup();
    private final Map<String, JToggleButton> navButtons = new LinkedHashMap<>();

    public MainFrame(Path root) {
        super("学生随机抽奖系统");
        AppTheme.applyGlobalStyle();
        this.dataStore = new DataStore(root);
        this.managementPanel = new ManagementPanel(dataStore, this::refreshAllData);
        this.lotteryPanel = new LotteryPanel(dataStore, this::refreshAllData);
        this.statisticsPanel = new StatisticsPanel(dataStore);
        initFrame();
    }

    private void initFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1380, 900));
        setSize(1420, 940);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel shell = new GradientShell();
        shell.setLayout(new BorderLayout());
        shell.setBorder(AppTheme.paddedBorder(24, 26, 24, 26));
        shell.add(buildHeroSection(), BorderLayout.NORTH);
        shell.add(buildContent(), BorderLayout.CENTER);
        add(shell, BorderLayout.CENTER);

        refreshAllData();
        showPage("管理");
    }

    private JPanel buildHeroSection() {
        RoundPanel hero = new RoundPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(AppTheme.paddedBorder(28, 34, 24, 34));

        JLabel title = new JLabel("学生随机抽奖系统");
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 38));
        title.setForeground(AppTheme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel desc = new JLabel("管理、抽奖、统计三大模块集中呈现，TXT 数据即改即存");
        desc.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 16));
        desc.setForeground(AppTheme.MUTED);
        desc.setAlignmentX(CENTER_ALIGNMENT);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        nav.setOpaque(false);
        nav.add(createNavButton("管理"));
        nav.add(createNavButton("抽奖"));
        nav.add(createNavButton("统计"));

        hero.add(title);
        hero.add(Box.createVerticalStrut(10));
        hero.add(desc);
        hero.add(Box.createVerticalStrut(22));
        hero.add(nav);
        return hero;
    }

    private JToggleButton createNavButton(String pageName) {
        JToggleButton button = new JToggleButton(pageName);
        navGroup.add(button);
        navButtons.put(pageName, button);
        button.setFocusPainted(false);
        button.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 24));
        button.setForeground(AppTheme.TEXT);
        button.setBackground(Color.WHITE);
        button.setPreferredSize(new Dimension(180, 66));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                AppTheme.paddedBorder(10, 22, 10, 22)
        ));
        button.addActionListener(e -> {
            button.setSelected(true);
            updateNavStyles();
            showPage(pageName);
        });
        return button;
    }

    private JPanel buildContent() {
        JPanel wrapper = new JPanel(new GridLayout(1, 1));
        wrapper.setOpaque(false);
        wrapper.setBorder(AppTheme.paddedBorder(22, 0, 0, 0));

        contentPanel.setOpaque(false);
        contentPanel.add(managementPanel, "管理");
        contentPanel.add(lotteryPanel, "抽奖");
        contentPanel.add(statisticsPanel, "统计");

        wrapper.add(contentPanel);
        return wrapper;
    }

    private void showPage(String pageName) {
        JToggleButton button = navButtons.get(pageName);
        if (button != null) {
            button.setSelected(true);
        }
        cardLayout.show(contentPanel, pageName);
        updateNavStyles();
    }

    private void updateNavStyles() {
        for (var element = navGroup.getElements(); element.hasMoreElements(); ) {
            JToggleButton button = (JToggleButton) element.nextElement();
            if (button.isSelected()) {
                button.setBackground(AppTheme.PRIMARY);
                button.setForeground(Color.WHITE);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(79, 126, 170), 1, true),
                        AppTheme.paddedBorder(10, 22, 10, 22)
                ));
            } else {
                button.setBackground(Color.WHITE);
                button.setForeground(AppTheme.TEXT);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                        AppTheme.paddedBorder(10, 22, 10, 22)
                ));
            }
        }
    }

    private void refreshAllData() {
        managementPanel.refreshData();
        lotteryPanel.refreshData();
        statisticsPanel.refreshData();
    }

    private static class GradientShell extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(232, 238, 245), w, h, new Color(245, 240, 232)));
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(255, 255, 255, 120));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(w - 280, 40, 220, 220);
            g2.drawOval(30, h - 240, 180, 180);
            g2.dispose();
        }
    }
}
