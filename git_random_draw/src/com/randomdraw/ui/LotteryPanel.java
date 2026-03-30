package com.randomdraw.ui;

import com.randomdraw.model.AttendanceRecord;
import com.randomdraw.model.PerformanceRecord;
import com.randomdraw.model.Student;
import com.randomdraw.service.DataStore;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;

public class LotteryPanel extends JPanel {
    private final DataStore dataStore;
    private final Runnable onDataChanged;
    private final JRadioButton attendanceMode = new JRadioButton("考勤抽奖");
    private final JRadioButton performanceMode = new JRadioButton("回答问题抽奖");
    private final JPanel classSelectorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
    private final JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JPanel resultListPanel = new JPanel();
    private final List<JCheckBox> classBoxes = new ArrayList<>();
    private final Map<Student, JComboBox<String>> resultMarkers = new LinkedHashMap<>();
    private final JLabel rollingTitle = new JLabel("准备开始抽取");
    private final JLabel rollingStudentId = new JLabel("--");
    private final JLabel rollingStudentMeta = new JLabel("选择班级和人数后开始抽奖");
    private final JButton drawButton = AppTheme.createPrimaryButton("开始抽取");
    private final JButton saveButton = AppTheme.createPrimaryButton("保存标记名单");
    private final ButtonGroup group = new ButtonGroup();
    private Timer rollingTimer;
    private List<Student> students = new ArrayList<>();
    private List<Student> currentWinners = new ArrayList<>();
    private boolean drawing;

    public LotteryPanel(DataStore dataStore, Runnable onDataChanged) {
        this.dataStore = dataStore;
        this.onDataChanged = onDataChanged;
        setOpaque(false);
        setLayout(new BorderLayout());
        initUi();
    }

    private void initUi() {
        JPanel canvas = new JPanel();
        canvas.setOpaque(false);
        canvas.setLayout(new BoxLayout(canvas, BoxLayout.Y_AXIS));
        canvas.setBorder(AppTheme.paddedBorder(0, 0, 10, 0));
        canvas.add(buildTopArea());
        canvas.add(Box.createVerticalStrut(22));
        canvas.add(buildCenterArea());

        JScrollPane pageScroll = new JScrollPane(canvas);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.setOpaque(false);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.getVerticalScrollBar().setUnitIncrement(18);
        pageScroll.getHorizontalScrollBar().setUnitIncrement(18);

        add(pageScroll, BorderLayout.CENTER);
    }

    private JPanel buildTopArea() {
        JPanel wrapper = new JPanel(new GridLayout(1, 1));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundPanel hero = new RoundPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(AppTheme.paddedBorder(20, 24, 20, 24));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel title = new JLabel("抽取配置");
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 28));
        title.setForeground(AppTheme.TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("居中设置抽奖类型、参与班级和抽取人数，支持滚动查看完整内容");
        desc.setForeground(AppTheme.MUTED);
        desc.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 16));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        group.add(attendanceMode);
        group.add(performanceMode);
        attendanceMode.setSelected(true);
        attendanceMode.setOpaque(false);
        performanceMode.setOpaque(false);
        attendanceMode.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        performanceMode.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        modePanel.setOpaque(false);
        modePanel.add(attendanceMode);
        modePanel.add(performanceMode);

        JPanel classWrap = new JPanel();
        classWrap.setOpaque(false);
        classWrap.setLayout(new BoxLayout(classWrap, BoxLayout.Y_AXIS));
        classWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel classLabel = new JLabel("参与班级");
        classLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        classLabel.setFont(AppTheme.H2_FONT);
        classSelectorPanel.setOpaque(false);
        classSelectorPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        classWrap.add(classLabel);
        classWrap.add(Box.createVerticalStrut(10));
        classWrap.add(classSelectorPanel);

        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        countPanel.setOpaque(false);
        JLabel countLabel = new JLabel("抽取人数");
        countLabel.setFont(AppTheme.H2_FONT);
        countSpinner.setPreferredSize(new Dimension(110, 40));
        countPanel.add(countLabel);
        countPanel.add(countSpinner);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        actionPanel.setOpaque(false);
        drawButton.setPreferredSize(new Dimension(180, 46));
        drawButton.addActionListener(e -> drawStudents());
        actionPanel.add(drawButton);

        hero.add(title);
        hero.add(Box.createVerticalStrut(8));
        hero.add(desc);
        hero.add(Box.createVerticalStrut(12));
        hero.add(modePanel);
        hero.add(Box.createVerticalStrut(12));
        hero.add(classWrap);
        hero.add(Box.createVerticalStrut(12));
        hero.add(countPanel);
        hero.add(Box.createVerticalStrut(14));
        hero.add(actionPanel);

        wrapper.add(hero);
        return wrapper;
    }

    private JPanel buildCenterArea() {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 22, 0));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 620));
        wrapper.setPreferredSize(new Dimension(1100, 620));

        RoundPanel stage = new RoundPanel();
        stage.setLayout(new BoxLayout(stage, BoxLayout.Y_AXIS));
        stage.setBorder(AppTheme.paddedBorder(34, 28, 34, 28));
        stage.setPreferredSize(new Dimension(520, 620));
        stage.setMinimumSize(new Dimension(420, 540));

        JLabel badge = new JLabel("LIVE DRAW");
        badge.setOpaque(true);
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        badge.setBackground(new Color(229, 236, 244));
        badge.setForeground(AppTheme.PRIMARY);
        badge.setBorder(AppTheme.paddedBorder(8, 14, 8, 14));

        rollingTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 28));
        rollingTitle.setForeground(AppTheme.TEXT);
        rollingTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        rollingStudentId.setFont(new Font("Consolas", Font.BOLD, 56));
        rollingStudentId.setForeground(AppTheme.PRIMARY);
        rollingStudentId.setAlignmentX(Component.CENTER_ALIGNMENT);

        rollingStudentMeta.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 16));
        rollingStudentMeta.setForeground(AppTheme.MUTED);
        rollingStudentMeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        stage.add(Box.createVerticalStrut(28));
        stage.add(badge);
        stage.add(Box.createVerticalStrut(28));
        stage.add(rollingTitle);
        stage.add(Box.createVerticalStrut(28));
        stage.add(rollingStudentId);
        stage.add(Box.createVerticalStrut(16));
        stage.add(rollingStudentMeta);
        stage.add(Box.createVerticalGlue());

        RoundPanel results = new RoundPanel();
        results.setLayout(new BorderLayout());
        results.setBorder(AppTheme.paddedBorder(22, 20, 20, 20));
        results.setPreferredSize(new Dimension(520, 620));
        results.setMinimumSize(new Dimension(420, 540));

        JLabel resultTitle = new JLabel("最终名单");
        resultTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 26));
        resultTitle.setForeground(AppTheme.TEXT);

        JLabel resultDesc = new JLabel("抽中后可直接标记考勤或表现，并保存到 TXT 文件");
        resultDesc.setForeground(AppTheme.MUTED);

        saveButton.setPreferredSize(new Dimension(180, 42));
        saveButton.addActionListener(e -> saveMarkedResults());

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(resultTitle, BorderLayout.WEST);
        titleRow.add(saveButton, BorderLayout.EAST);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titleRow);
        header.add(Box.createVerticalStrut(6));
        header.add(resultDesc);
        header.add(Box.createVerticalStrut(12));

        resultListPanel.setOpaque(false);
        resultListPanel.setLayout(new BoxLayout(resultListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(resultListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        results.add(header, BorderLayout.NORTH);
        results.add(scrollPane, BorderLayout.CENTER);

        wrapper.add(stage);
        wrapper.add(results);
        return wrapper;
    }

    public void refreshData() {
        students = dataStore.loadStudents();
        rebuildClassSelector();
        renderEmptyState();
    }

    private void rebuildClassSelector() {
        Set<String> classes = dataStore.loadClasses();
        classSelectorPanel.removeAll();
        classBoxes.clear();
        for (String className : classes) {
            JCheckBox box = new JCheckBox(className);
            box.setOpaque(false);
            box.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
            classBoxes.add(box);
            classSelectorPanel.add(box);
        }
        classSelectorPanel.revalidate();
        classSelectorPanel.repaint();
    }

    private void drawStudents() {
        if (drawing) {
            return;
        }
        List<String> selectedClasses = classBoxes.stream()
                .filter(JCheckBox::isSelected)
                .map(JCheckBox::getText)
                .collect(Collectors.toList());
        if (selectedClasses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请至少选择一个参与抽奖的班级。");
            return;
        }

        List<Student> pool = students.stream()
                .filter(student -> selectedClasses.contains(student.className()))
                .collect(Collectors.toList());
        if (pool.isEmpty()) {
            JOptionPane.showMessageDialog(this, "当前条件下没有可抽取的学生。");
            return;
        }

        int requestedCount = (Integer) countSpinner.getValue();
        if (requestedCount > pool.size()) {
            requestedCount = pool.size();
            countSpinner.setValue(requestedCount);
            JOptionPane.showMessageDialog(this, "抽取人数已自动调整为候选学生总数。");
        }

        Collections.shuffle(pool);
        currentWinners = new ArrayList<>(pool.subList(0, requestedCount));
        startRollingAnimation(pool);
    }

    private void startRollingAnimation(List<Student> pool) {
        drawing = true;
        drawButton.setEnabled(false);
        saveButton.setEnabled(false);
        resultMarkers.clear();
        resultListPanel.removeAll();
        resultListPanel.revalidate();
        resultListPanel.repaint();

        rollingTitle.setText("正在高速抽取...");
        rollingStudentMeta.setText("学号正在动态变化，请稍候");

        if (rollingTimer != null && rollingTimer.isRunning()) {
            rollingTimer.stop();
        }

        final int[] tick = {0};
        final int maxTick = 28;
        rollingTimer = new Timer(85, e -> {
            Student preview = pool.get((int) (Math.random() * pool.size()));
            rollingStudentId.setText(preview.studentId());
            rollingStudentMeta.setText(preview.displayName() + "  |  " + preview.className());
            tick[0]++;
            if (tick[0] >= maxTick) {
                rollingTimer.stop();
                finishDrawAnimation();
            }
        });
        rollingTimer.start();
    }

    private void finishDrawAnimation() {
        drawing = false;
        drawButton.setEnabled(true);
        saveButton.setEnabled(true);

        if (currentWinners.isEmpty()) {
            renderEmptyState();
            return;
        }

        Student focus = currentWinners.get(0);
        rollingTitle.setText(currentWinners.size() == 1 ? "本轮抽中学生" : "本轮抽取完成");
        rollingStudentId.setText(focus.studentId());
        rollingStudentMeta.setText(focus.displayName() + "  |  " + focus.className()
                + (currentWinners.size() > 1 ? "  |  共 " + currentWinners.size() + " 位" : ""));
        renderResults(currentWinners);
    }

    private void renderResults(List<Student> winners) {
        resultMarkers.clear();
        resultListPanel.removeAll();

        JLabel countLabel = new JLabel("共抽中 " + winners.size() + " 位学生");
        countLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 28));
        countLabel.setForeground(AppTheme.PRIMARY);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultListPanel.add(countLabel);
        resultListPanel.add(Box.createVerticalStrut(16));

        for (Student student : winners) {
            RoundPanel item = new RoundPanel();
            item.setLayout(new BorderLayout(12, 0));
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
            item.setBorder(AppTheme.paddedBorder(18, 18, 18, 18));

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

            JLabel name = new JLabel(student.displayName());
            name.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 22));
            name.setForeground(AppTheme.TEXT);

            JLabel meta = new JLabel("学号: " + student.studentId() + "    班级: " + student.className());
            meta.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 17));
            meta.setForeground(AppTheme.MUTED);

            text.add(name);
            text.add(Box.createVerticalStrut(4));
            text.add(meta);

            String[] options = attendanceMode.isSelected()
                    ? new String[]{"出勤", "缺勤"}
                    : new String[]{"优秀", "良好", "差"};
            JComboBox<String> marker = new JComboBox<>(new DefaultComboBoxModel<>(options));
            marker.setPreferredSize(new Dimension(120, 34));

            JPanel action = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 12));
            action.setOpaque(false);
            action.add(marker);

            item.add(text, BorderLayout.CENTER);
            item.add(action, BorderLayout.EAST);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);

            resultListPanel.add(item);
            resultListPanel.add(Box.createVerticalStrut(12));
            resultMarkers.put(student, marker);
        }

        resultListPanel.revalidate();
        resultListPanel.repaint();
    }

    private void saveMarkedResults() {
        if (drawing) {
            JOptionPane.showMessageDialog(this, "请等待当前抽取动画结束。");
            return;
        }
        if (resultMarkers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先执行抽奖，再保存标记结果。");
            return;
        }

        if (attendanceMode.isSelected()) {
            String timestamp = dataStore.currentTimestamp();
            List<AttendanceRecord> records = resultMarkers.entrySet().stream()
                    .map(entry -> new AttendanceRecord(
                            entry.getKey().studentId(),
                            entry.getKey().name(),
                            entry.getKey().className(),
                            Objects.toString(entry.getValue().getSelectedItem(), ""),
                            timestamp
                    ))
                    .toList();
            dataStore.appendAttendanceRecords(records);
        } else {
            String timestamp = dataStore.currentTimestamp();
            List<PerformanceRecord> records = resultMarkers.entrySet().stream()
                    .map(entry -> new PerformanceRecord(
                            entry.getKey().studentId(),
                            entry.getKey().name(),
                            entry.getKey().className(),
                            Objects.toString(entry.getValue().getSelectedItem(), ""),
                            timestamp
                    ))
                    .toList();
            dataStore.appendPerformanceRecords(records);
        }
        JOptionPane.showMessageDialog(this, "标记结果已保存到对应 TXT 文件。");
        onDataChanged.run();
    }

    private void renderEmptyState() {
        currentWinners = new ArrayList<>();
        resultMarkers.clear();
        if (rollingTimer != null && rollingTimer.isRunning()) {
            rollingTimer.stop();
        }
        drawing = false;
        drawButton.setEnabled(true);
        saveButton.setEnabled(true);
        rollingTitle.setText("准备开始抽取");
        rollingStudentId.setText("--");
        rollingStudentMeta.setText("选择班级和人数后开始抽奖");

        resultListPanel.removeAll();
        JLabel label = new JLabel("抽取结果会显示在这里");
        label.setFont(AppTheme.H2_FONT);
        label.setForeground(AppTheme.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultListPanel.add(label);
        resultListPanel.revalidate();
        resultListPanel.repaint();
    }
}
