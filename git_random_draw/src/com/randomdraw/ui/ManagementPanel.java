package com.randomdraw.ui;

import com.randomdraw.model.Student;
import com.randomdraw.service.DataStore;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ManagementPanel extends JPanel {
    private final DataStore dataStore;
    private final Runnable onDataChanged;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<String> classFilter;
    private final JTextField studentIdSearchField;
    private List<Student> allStudents = new ArrayList<>();

    public ManagementPanel(DataStore dataStore, Runnable onDataChanged) {
        this.dataStore = dataStore;
        this.onDataChanged = onDataChanged;
        setOpaque(false);
        setLayout(new BorderLayout(18, 18));

        this.tableModel = new DefaultTableModel(new Object[]{"学号", "姓名", "班级"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(tableModel);
        this.classFilter = new JComboBox<>();
        this.studentIdSearchField = new JTextField();
        initUi();
    }

    private void initUi() {
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 18, 0));
        wrapper.setOpaque(false);

        RoundPanel summary = new RoundPanel();
        summary.setLayout(new BorderLayout());
        summary.setBorder(AppTheme.paddedBorder(18, 18, 18, 18));
        JPanel summaryText = new JPanel();
        summaryText.setOpaque(false);
        summaryText.setLayout(new BoxLayout(summaryText, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("学生信息管理");
        title.setFont(AppTheme.H2_FONT);
        title.setForeground(AppTheme.TEXT);
        JLabel desc = new JLabel("支持读取、筛选、按学号搜索、添加、编辑以及单条或批量删除学生信息");
        desc.setForeground(AppTheme.MUTED);
        summaryText.add(title);
        summaryText.add(Box.createVerticalStrut(8));
        summaryText.add(desc);

        JPanel addButtonArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        addButtonArea.setOpaque(false);
        JButton addButton = AppTheme.createPrimaryButton("添加学生");
        addButton.setPreferredSize(new Dimension(150, 42));
        addButton.addActionListener(e -> openStudentDialog(null));
        addButtonArea.add(addButton);

        summary.add(summaryText, BorderLayout.CENTER);
        summary.add(addButtonArea, BorderLayout.EAST);

        RoundPanel action = new RoundPanel();
        action.setLayout(new BorderLayout(0, 14));
        action.setBorder(AppTheme.paddedBorder(14, 16, 14, 16));

        JPanel primaryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        primaryRow.setOpaque(false);

        classFilter.setPreferredSize(new Dimension(150, 36));
        classFilter.addActionListener(e -> reloadTable());

        studentIdSearchField.setPreferredSize(new Dimension(180, 36));
        AppTheme.styleTextField(studentIdSearchField);
        studentIdSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reloadTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reloadTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reloadTable();
            }
        });

        JButton clearSearchButton = AppTheme.createGhostButton("清空搜索");
        clearSearchButton.addActionListener(e -> {
            studentIdSearchField.setText("");
            reloadTable();
        });

        JButton editButton = AppTheme.createGhostButton("编辑所选");
        editButton.addActionListener(e -> editSelectedStudent());

        primaryRow.add(new JLabel("班级筛选"));
        primaryRow.add(classFilter);
        primaryRow.add(new JLabel("学号搜索"));
        primaryRow.add(studentIdSearchField);
        primaryRow.add(clearSearchButton);
        primaryRow.add(editButton);

        JPanel dangerRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        dangerRow.setOpaque(false);
        JButton deleteButton = AppTheme.createDangerButton("删除所选学生");
        deleteButton.setPreferredSize(new Dimension(160, 40));
        deleteButton.addActionListener(e -> deleteSelectedStudents());
        dangerRow.add(deleteButton);

        JLabel tip = new JLabel("姓名可留空，保存时会自动写入一个空格占位");
        tip.setForeground(AppTheme.DANGER);
        tip.setFont(AppTheme.SMALL_FONT);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(tip, BorderLayout.WEST);
        bottom.add(dangerRow, BorderLayout.EAST);

        action.add(primaryRow, BorderLayout.NORTH);
        action.add(bottom, BorderLayout.SOUTH);

        wrapper.add(summary);
        wrapper.add(action);
        return wrapper;
    }

    private JPanel buildTableCard() {
        RoundPanel panel = new RoundPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(AppTheme.paddedBorder(18, 18, 18, 18));

        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowVerticalLines(false);
        table.setGridColor(AppTheme.BORDER);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setVerticalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        allStudents = new ArrayList<>(dataStore.loadStudents());
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("全部班级");
        dataStore.loadClasses().forEach(model::addElement);
        Object selected = classFilter.getSelectedItem();
        classFilter.setModel(model);
        if (selected != null) {
            classFilter.setSelectedItem(selected);
            if (!Objects.equals(classFilter.getSelectedItem(), selected)) {
                classFilter.setSelectedIndex(0);
            }
        }
        reloadTable();
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        String selectedClass = (String) classFilter.getSelectedItem();
        String searchKeyword = studentIdSearchField.getText().trim();

        List<Student> students = allStudents.stream()
                .filter(student -> selectedClass == null
                        || "全部班级".equals(selectedClass)
                        || selectedClass.equals(student.className()))
                .filter(student -> searchKeyword.isBlank() || student.studentId().contains(searchKeyword))
                .collect(Collectors.toList());

        for (Student student : students) {
            tableModel.addRow(new Object[]{student.studentId(), student.name(), student.className()});
        }
    }

    private void editSelectedStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || table.getSelectedRowCount() != 1) {
            JOptionPane.showMessageDialog(this, "请先选择一条需要编辑的学生信息。");
            return;
        }
        String studentId = String.valueOf(tableModel.getValueAt(selectedRow, 0));
        Student student = allStudents.stream().filter(item -> item.studentId().equals(studentId)).findFirst().orElse(null);
        if (student != null) {
            openStudentDialog(student);
        }
    }

    private void deleteSelectedStudents() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "请至少选择一条学生信息。");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定删除选中的 " + rows.length + " 条学生信息吗？",
                "删除确认",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        List<String> ids = new ArrayList<>();
        for (int row : rows) {
            ids.add(String.valueOf(tableModel.getValueAt(row, 0)));
        }
        List<Student> remaining = allStudents.stream()
                .filter(student -> !ids.contains(student.studentId()))
                .collect(Collectors.toList());
        dataStore.saveStudents(remaining);
        onDataChanged.run();
    }

    private void openStudentDialog(Student student) {
        boolean editing = student != null;
        JDialog dialog = new JDialog();
        dialog.setTitle(editing ? "编辑学生信息" : "添加学生");
        dialog.setModal(true);
        dialog.setSize(420, 260);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 12, 12));
        form.setBorder(AppTheme.paddedBorder(18, 18, 18, 18));
        form.setBackground(AppTheme.PANEL);

        JTextField studentIdField = new JTextField(editing ? student.studentId() : "");
        JTextField nameField = new JTextField(editing ? student.name() : "");
        JTextField classField = new JTextField(editing ? student.className() : "");
        AppTheme.styleTextField(studentIdField);
        AppTheme.styleTextField(nameField);
        AppTheme.styleTextField(classField);

        form.add(new JLabel("学号 *"));
        form.add(studentIdField);
        form.add(new JLabel("姓名（可留空）"));
        form.add(nameField);
        form.add(new JLabel("班级 *"));
        form.add(classField);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setBackground(AppTheme.PANEL);
        footer.setBorder(AppTheme.paddedBorder(0, 18, 18, 18));

        JButton cancel = AppTheme.createGhostButton("取消");
        cancel.addActionListener(e -> dialog.dispose());
        JButton save = AppTheme.createPrimaryButton("保存");
        save.addActionListener(e -> {
            String studentId = studentIdField.getText().trim();
            String name = nameField.getText().trim();
            String className = classField.getText().trim();
            if (studentId.isBlank() || className.isBlank()) {
                JOptionPane.showMessageDialog(dialog, "学号和班级为必填项。");
                return;
            }

            String storedName = name.isBlank() ? " " : name;

            List<Student> updated = new ArrayList<>(allStudents);
            if (editing) {
                updated = updated.stream()
                        .map(item -> item.studentId().equals(student.studentId()) ? new Student(studentId, storedName, className) : item)
                        .collect(Collectors.toList());
                long duplicates = updated.stream().filter(item -> item.studentId().equals(studentId)).count();
                if (duplicates > 1) {
                    JOptionPane.showMessageDialog(dialog, "修改后的学号与其他学生重复。");
                    return;
                }
            } else {
                boolean exists = updated.stream().anyMatch(item -> item.studentId().equals(studentId));
                if (exists) {
                    JOptionPane.showMessageDialog(dialog, "学号已存在，请使用唯一学号。");
                    return;
                }
                updated.add(new Student(studentId, storedName, className));
            }

            dataStore.saveStudents(updated);
            dialog.dispose();
            onDataChanged.run();
        });

        footer.add(cancel);
        footer.add(save);

        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
