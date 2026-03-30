package com.randomdraw.ui;

import com.randomdraw.model.AttendanceRecord;
import com.randomdraw.model.PerformanceRecord;
import com.randomdraw.service.DataStore;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class StatisticsPanel extends JPanel {
    private final DataStore dataStore;
    private final JComboBox<String> classCombo = new JComboBox<>();
    private final JTextField studentIdSearchField = new JTextField();
    private final DefaultTableModel summaryTableModel;
    private final JTable summaryTable = new JTable();
    private final AttendanceChartPanel chartPanel = new AttendanceChartPanel();
    private final JScrollPane chartScrollPane = new JScrollPane(chartPanel);
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private List<PerformanceRecord> performanceRecords = new ArrayList<>();
    private List<StudentStatsRow> visibleRows = new ArrayList<>();

    public StatisticsPanel(DataStore dataStore) {
        this.dataStore = dataStore;
        setOpaque(false);
        setLayout(new BorderLayout(18, 18));
        this.summaryTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        initUi();
    }

    private void initUi() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        rebuildSummaryColumns();
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 18, 0));
        wrapper.setOpaque(false);

        RoundPanel intro = new RoundPanel();
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.setBorder(AppTheme.paddedBorder(18, 18, 18, 18));
        JLabel title = new JLabel("综合统计分析");
        title.setFont(AppTheme.H2_FONT);
        title.setForeground(AppTheme.TEXT);
        JLabel desc = new JLabel("柱状图仅展示出勤与缺勤，默认按缺勤次数从高到低排序");
        desc.setForeground(AppTheme.MUTED);
        intro.add(title);
        intro.add(Box.createVerticalStrut(8));
        intro.add(desc);

        RoundPanel filters = new RoundPanel();
        filters.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 18));
        filters.setBorder(AppTheme.paddedBorder(0, 12, 0, 12));
        classCombo.setPreferredSize(new Dimension(150, 36));
        classCombo.addActionListener(e -> refreshView());
        studentIdSearchField.setPreferredSize(new Dimension(180, 36));
        AppTheme.styleTextField(studentIdSearchField);
        studentIdSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshView();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshView();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshView();
            }
        });
        filters.add(new JLabel("班级筛选"));
        filters.add(classCombo);
        filters.add(new JLabel("学号搜索"));
        filters.add(studentIdSearchField);

        wrapper.add(intro);
        wrapper.add(filters);
        return wrapper;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridLayout(1, 2, 18, 0));
        center.setOpaque(false);

        RoundPanel chartCard = new RoundPanel();
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(AppTheme.paddedBorder(18, 18, 18, 18));

        JLabel chartTitle = new JLabel("考勤统计柱状图");
        chartTitle.setFont(AppTheme.H2_FONT);
        chartTitle.setForeground(AppTheme.TEXT);
        JLabel chartDesc = new JLabel("图中只显示学号后三位，可左右滚动查看更多学生");
        chartDesc.setForeground(AppTheme.MUTED);

        JPanel chartHeader = new JPanel();
        chartHeader.setOpaque(false);
        chartHeader.setLayout(new BoxLayout(chartHeader, BoxLayout.Y_AXIS));
        chartHeader.add(chartTitle);
        chartHeader.add(Box.createVerticalStrut(6));
        chartHeader.add(chartDesc);

        chartScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chartScrollPane.setOpaque(false);
        chartScrollPane.getViewport().setOpaque(false);
        chartScrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        chartCard.add(chartHeader, BorderLayout.NORTH);
        chartCard.add(chartScrollPane, BorderLayout.CENTER);

        RoundPanel tableCard = new RoundPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(AppTheme.paddedBorder(18, 18, 18, 18));

        JLabel tableTitle = new JLabel("学生汇总记录");
        tableTitle.setFont(AppTheme.H2_FONT);
        tableTitle.setForeground(AppTheme.TEXT);
        JLabel tableDesc = new JLabel("显示每个学生的出勤/缺勤/优/良/差次数，并可查看详情");
        tableDesc.setForeground(AppTheme.MUTED);

        JPanel tableHeader = new JPanel();
        tableHeader.setOpaque(false);
        tableHeader.setLayout(new BoxLayout(tableHeader, BoxLayout.Y_AXIS));
        tableHeader.add(tableTitle);
        tableHeader.add(Box.createVerticalStrut(6));
        tableHeader.add(tableDesc);

        summaryTable.setModel(summaryTableModel);
        summaryTable.setRowHeight(34);
        summaryTable.setShowVerticalLines(false);
        summaryTable.getTableHeader().setReorderingAllowed(false);
        summaryTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyCenterRenderer(summaryTable);
        summaryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = summaryTable.rowAtPoint(e.getPoint());
                int col = summaryTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 8) {
                    openDetailDialog(visibleRows.get(row));
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(summaryTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());

        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(tableScrollPane, BorderLayout.CENTER);

        center.add(chartCard);
        center.add(tableCard);
        return center;
    }

    public void refreshData() {
        attendanceRecords = new ArrayList<>(dataStore.loadAttendanceRecords());
        performanceRecords = new ArrayList<>(dataStore.loadPerformanceRecords());

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("全部班级");
        dataStore.loadClasses().forEach(model::addElement);
        Object selected = classCombo.getSelectedItem();
        classCombo.setModel(model);
        if (selected != null) {
            classCombo.setSelectedItem(selected);
            if (!Objects.equals(classCombo.getSelectedItem(), selected)) {
                classCombo.setSelectedIndex(0);
            }
        }
        refreshView();
    }

    private void rebuildSummaryColumns() {
        summaryTableModel.setDataVector(new Object[0][0],
                new Object[]{"学号", "姓名", "班级", "出勤", "缺勤", "优", "良", "差", "查看详情"});
        applyCenterRenderer(summaryTable);
    }

    private void refreshView() {
        rebuildSummaryColumns();
        visibleRows = buildVisibleRows();
        for (StudentStatsRow row : visibleRows) {
            summaryTableModel.addRow(new Object[]{
                    shortStudentId(row.studentId()),
                    row.name(),
                    row.className(),
                    row.presentCount(),
                    row.absentCount(),
                    row.excellentCount(),
                    row.goodCount(),
                    row.poorCount(),
                    "查看详情"
            });
        }
        chartPanel.setRows(visibleRows);
        chartScrollPane.revalidate();
        chartScrollPane.repaint();
    }

    private List<StudentStatsRow> buildVisibleRows() {
        String selectedClass = Objects.toString(classCombo.getSelectedItem(), "全部班级");
        String searchKeyword = studentIdSearchField.getText().trim();
        Map<String, StudentStatsRowBuilder> builders = new LinkedHashMap<>();

        for (AttendanceRecord record : attendanceRecords) {
            if (!matchClass(selectedClass, record.className())) {
                continue;
            }
            if (!matchStudentId(searchKeyword, record.studentId())) {
                continue;
            }
            StudentStatsRowBuilder builder = builders.computeIfAbsent(record.studentId(),
                    key -> new StudentStatsRowBuilder(record.studentId(), displayName(record.name()), record.className()));
            if ("出勤".equals(record.status())) {
                builder.presentCount++;
            } else if ("缺勤".equals(record.status())) {
                builder.absentCount++;
            }
        }

        for (PerformanceRecord record : performanceRecords) {
            if (!matchClass(selectedClass, record.className())) {
                continue;
            }
            if (!matchStudentId(searchKeyword, record.studentId())) {
                continue;
            }
            StudentStatsRowBuilder builder = builders.computeIfAbsent(record.studentId(),
                    key -> new StudentStatsRowBuilder(record.studentId(), displayName(record.name()), record.className()));
            if ("优秀".equals(record.rating())) {
                builder.excellentCount++;
            } else if ("良好".equals(record.rating())) {
                builder.goodCount++;
            } else if ("差".equals(record.rating())) {
                builder.poorCount++;
            }
        }

        return builders.values().stream()
                .map(StudentStatsRowBuilder::build)
                .sorted(Comparator.comparingInt(StudentStatsRow::absentCount).reversed()
                        .thenComparing(StudentStatsRow::studentId))
                .toList();
    }

    private boolean matchClass(String selectedClass, String className) {
        return "全部班级".equals(selectedClass) || selectedClass.equals(className);
    }

    private boolean matchStudentId(String searchKeyword, String studentId) {
        return searchKeyword.isBlank() || studentId.contains(searchKeyword);
    }

    private String displayName(String name) {
        return name == null || name.isBlank() ? "(未填写姓名)" : name.trim();
    }

    private String shortStudentId(String studentId) {
        return studentId.length() <= 3 ? studentId : studentId.substring(studentId.length() - 3);
    }

    private void applyCenterRenderer(JTable targetTable) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setVerticalAlignment(SwingConstants.CENTER);
        targetTable.setDefaultRenderer(Object.class, centerRenderer);
        ((DefaultTableCellRenderer) targetTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void openDetailDialog(StudentStatsRow row) {
        JDialog dialog = new JDialog();
        dialog.setTitle("学生统计详情");
        dialog.setModal(true);
        dialog.setSize(920, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(16, 16));

        dialog.add(buildDetailTop(row), BorderLayout.NORTH);
        dialog.add(buildDetailCenter(dialog, row), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JPanel buildDetailTop(StudentStatsRow row) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setOpaque(false);

        RoundPanel infoCard = new RoundPanel();
        infoCard.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 16));
        infoCard.setBorder(AppTheme.paddedBorder(0, 18, 0, 18));
        JLabel nameLabel = new JLabel("学生姓名: " + row.name());
        nameLabel.setFont(AppTheme.H2_FONT);
        nameLabel.setForeground(AppTheme.TEXT);
        JLabel idLabel = new JLabel("学号: " + row.studentId());
        idLabel.setFont(AppTheme.H2_FONT);
        idLabel.setForeground(AppTheme.PRIMARY);
        JLabel classLabel = new JLabel("班级: " + row.className());
        classLabel.setFont(AppTheme.BODY_FONT);
        classLabel.setForeground(AppTheme.MUTED);
        infoCard.add(nameLabel);
        infoCard.add(idLabel);
        infoCard.add(classLabel);

        RoundPanel metrics = new RoundPanel();
        metrics.setLayout(new GridLayout(1, 5, 12, 0));
        metrics.setBorder(AppTheme.paddedBorder(18, 18, 18, 18));
        metrics.add(createMetricCard("出勤", String.valueOf(row.presentCount()), AppTheme.SUCCESS));
        metrics.add(createMetricCard("缺勤", String.valueOf(row.absentCount()), AppTheme.DANGER));
        metrics.add(createMetricCard("优", String.valueOf(row.excellentCount()), AppTheme.SUCCESS));
        metrics.add(createMetricCard("良", String.valueOf(row.goodCount()), AppTheme.ACCENT));
        metrics.add(createMetricCard("差", String.valueOf(row.poorCount()), AppTheme.DANGER));

        wrapper.add(infoCard, BorderLayout.NORTH);
        wrapper.add(metrics, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createMetricCard(String title, String value, Color color) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(AppTheme.MUTED);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 26));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(valueLabel);
        return panel;
    }

    private JPanel buildDetailCenter(JDialog dialog, StudentStatsRow row) {
        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setOpaque(false);
        center.add(buildAttendanceDetailCard(dialog, row));
        center.add(buildPerformanceDetailCard(dialog, row));
        return center;
    }

    private JPanel buildAttendanceDetailCard(JDialog dialog, StudentStatsRow row) {
        RoundPanel card = new RoundPanel();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(AppTheme.paddedBorder(18, 18, 18, 18));

        JLabel title = new JLabel("考勤详情");
        title.setFont(AppTheme.H2_FONT);
        title.setForeground(AppTheme.TEXT);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"状态", "时间"}, 0) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(false);
        applyCenterRenderer(table);

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < attendanceRecords.size(); i++) {
            AttendanceRecord record = attendanceRecords.get(i);
            if (row.studentId().equals(record.studentId())) {
                model.addRow(new Object[]{record.status(), record.time()});
                indices.add(i);
            }
        }

        JButton editButton = AppTheme.createGhostButton("修改");
        editButton.addActionListener(e -> editAttendanceRecord(dialog, row, table, indices));
        JButton deleteButton = AppTheme.createDangerButton("删除");
        deleteButton.addActionListener(e -> deleteAttendanceRecord(dialog, row, table, indices));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.add(editButton);
        footer.add(deleteButton);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        card.add(title, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildPerformanceDetailCard(JDialog dialog, StudentStatsRow row) {
        RoundPanel card = new RoundPanel();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(AppTheme.paddedBorder(18, 18, 18, 18));

        JLabel title = new JLabel("表现详情");
        title.setFont(AppTheme.H2_FONT);
        title.setForeground(AppTheme.TEXT);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"表现", "时间"}, 0) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(false);
        applyCenterRenderer(table);

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < performanceRecords.size(); i++) {
            PerformanceRecord record = performanceRecords.get(i);
            if (row.studentId().equals(record.studentId())) {
                model.addRow(new Object[]{record.rating(), record.time()});
                indices.add(i);
            }
        }

        JButton editButton = AppTheme.createGhostButton("修改");
        editButton.addActionListener(e -> editPerformanceRecord(dialog, row, table, indices));
        JButton deleteButton = AppTheme.createDangerButton("删除");
        deleteButton.addActionListener(e -> deletePerformanceRecord(dialog, row, table, indices));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.add(editButton);
        footer.add(deleteButton);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        card.add(title, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void editAttendanceRecord(JDialog dialog, StudentStatsRow row, JTable table, List<Integer> indices) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(dialog, "请先选择一条考勤记录。");
            return;
        }
        int sourceIndex = indices.get(selected);
        AttendanceRecord original = attendanceRecords.get(sourceIndex);
        String status = (String) JOptionPane.showInputDialog(dialog, "修改考勤状态", "编辑考勤",
                JOptionPane.PLAIN_MESSAGE, null, new String[]{"出勤", "缺勤"}, original.status());
        if (status == null) {
            return;
        }
        String time = askForTime(dialog, original.time());
        if (time == null) {
            return;
        }
        attendanceRecords.set(sourceIndex,
                new AttendanceRecord(original.studentId(), original.name(), original.className(), status, time));
        dataStore.saveAttendanceRecords(attendanceRecords);
        refreshData();
        dialog.dispose();
        StudentStatsRow refreshed = findRowByStudentId(row.studentId());
        if (refreshed != null) {
            openDetailDialog(refreshed);
        }
    }

    private void deleteAttendanceRecord(JDialog dialog, StudentStatsRow row, JTable table, List<Integer> indices) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(dialog, "请先选择一条考勤记录。");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(dialog, "确定删除这条考勤记录吗？", "删除确认", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        attendanceRecords.remove((int) indices.get(selected));
        dataStore.saveAttendanceRecords(attendanceRecords);
        refreshData();
        dialog.dispose();
        StudentStatsRow refreshed = findRowByStudentId(row.studentId());
        if (refreshed != null) {
            openDetailDialog(refreshed);
        }
    }

    private void editPerformanceRecord(JDialog dialog, StudentStatsRow row, JTable table, List<Integer> indices) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(dialog, "请先选择一条表现记录。");
            return;
        }
        int sourceIndex = indices.get(selected);
        PerformanceRecord original = performanceRecords.get(sourceIndex);
        String rating = (String) JOptionPane.showInputDialog(dialog, "修改表现等级", "编辑表现",
                JOptionPane.PLAIN_MESSAGE, null, new String[]{"优秀", "良好", "差"}, original.rating());
        if (rating == null) {
            return;
        }
        String time = askForTime(dialog, original.time());
        if (time == null) {
            return;
        }
        performanceRecords.set(sourceIndex,
                new PerformanceRecord(original.studentId(), original.name(), original.className(), rating, time));
        dataStore.savePerformanceRecords(performanceRecords);
        refreshData();
        dialog.dispose();
        StudentStatsRow refreshed = findRowByStudentId(row.studentId());
        if (refreshed != null) {
            openDetailDialog(refreshed);
        }
    }

    private void deletePerformanceRecord(JDialog dialog, StudentStatsRow row, JTable table, List<Integer> indices) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(dialog, "请先选择一条表现记录。");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(dialog, "确定删除这条表现记录吗？", "删除确认", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        performanceRecords.remove((int) indices.get(selected));
        dataStore.savePerformanceRecords(performanceRecords);
        refreshData();
        dialog.dispose();
        StudentStatsRow refreshed = findRowByStudentId(row.studentId());
        if (refreshed != null) {
            openDetailDialog(refreshed);
        }
    }

    private String askForTime(JDialog dialog, String initialValue) {
        JTextField timeField = new JTextField(initialValue);
        AppTheme.styleTextField(timeField);
        int result = JOptionPane.showConfirmDialog(dialog, timeField, "修改时间（yyyy-MM-dd HH:mm:ss）",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        String time = timeField.getText().trim();
        if (time.isBlank()) {
            JOptionPane.showMessageDialog(dialog, "时间不能为空。");
            return null;
        }
        return time;
    }

    private StudentStatsRow findRowByStudentId(String studentId) {
        return visibleRows.stream().filter(item -> item.studentId().equals(studentId)).findFirst().orElse(null);
    }

    private record StudentStatsRow(
            String studentId,
            String name,
            String className,
            int presentCount,
            int absentCount,
            int excellentCount,
            int goodCount,
            int poorCount) {
    }

    private static class StudentStatsRowBuilder {
        private final String studentId;
        private final String name;
        private final String className;
        private int presentCount;
        private int absentCount;
        private int excellentCount;
        private int goodCount;
        private int poorCount;

        private StudentStatsRowBuilder(String studentId, String name, String className) {
            this.studentId = studentId;
            this.name = name;
            this.className = className;
        }

        private StudentStatsRow build() {
            return new StudentStatsRow(studentId, name, className, presentCount, absentCount, excellentCount, goodCount, poorCount);
        }
    }

    private static class AttendanceChartPanel extends JPanel {
        private List<StudentStatsRow> rows = new ArrayList<>();

        AttendanceChartPanel() {
            setOpaque(false);
        }

        void setRows(List<StudentStatsRow> rows) {
            this.rows = new ArrayList<>(rows);
            int chartWidth = Math.max(720, rows.size() * 96 + 120);
            setPreferredSize(new Dimension(chartWidth, 320));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            AppTheme.enableQuality(g2);

            int width = getWidth();
            int height = getHeight();
            int left = 60;
            int top = 24;
            int right = 20;
            int bottom = 64;

            if (rows.isEmpty()) {
                g2.setColor(AppTheme.MUTED);
                g2.setFont(AppTheme.BODY_FONT);
                g2.drawString("暂无统计数据", width / 2 - 36, height / 2);
                g2.dispose();
                return;
            }

            int plotWidth = width - left - right;
            int plotHeight = height - top - bottom;
            int max = 1;
            for (StudentStatsRow row : rows) {
                max = Math.max(max, row.presentCount());
                max = Math.max(max, row.absentCount());
            }

            g2.setColor(new Color(225, 231, 238));
            for (int i = 0; i <= max; i++) {
                int y = top + plotHeight - (plotHeight * i / max);
                g2.drawLine(left, y, left + plotWidth, y);
                g2.setColor(AppTheme.MUTED);
                g2.setFont(AppTheme.SMALL_FONT);
                g2.drawString(String.valueOf(i), 34, y + 4);
                g2.setColor(new Color(225, 231, 238));
            }

            int groupWidth = Math.max(72, plotWidth / Math.max(rows.size(), 1));
            for (int index = 0; index < rows.size(); index++) {
                StudentStatsRow row = rows.get(index);
                int baseX = left + index * groupWidth + 10;
                int presentHeight = plotHeight * row.presentCount() / max;
                int absentHeight = plotHeight * row.absentCount() / max;

                g2.setColor(AppTheme.SUCCESS);
                g2.fillRoundRect(baseX, top + plotHeight - presentHeight, 14, presentHeight, 8, 8);
                g2.setColor(AppTheme.DANGER);
                g2.fillRoundRect(baseX + 20, top + plotHeight - absentHeight, 14, absentHeight, 8, 8);

                g2.setColor(AppTheme.TEXT);
                g2.setFont(AppTheme.SMALL_FONT.deriveFont(Font.BOLD));
                g2.drawString(row.studentId().length() <= 3 ? row.studentId()
                        : row.studentId().substring(row.studentId().length() - 3), baseX - 2, top + plotHeight + 20);
            }

            int legendY = height - 22;
            g2.setColor(AppTheme.SUCCESS);
            g2.fillRoundRect(left, legendY - 10, 16, 16, 6, 6);
            g2.setColor(AppTheme.TEXT);
            g2.drawString("出勤", left + 24, legendY + 2);

            g2.setColor(AppTheme.DANGER);
            g2.fillRoundRect(left + 80, legendY - 10, 16, 16, 6, 6);
            g2.setColor(AppTheme.TEXT);
            g2.drawString("缺勤", left + 104, legendY + 2);
            g2.dispose();
        }
    }
}
