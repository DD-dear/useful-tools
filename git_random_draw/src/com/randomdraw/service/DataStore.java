package com.randomdraw.service;

import com.randomdraw.model.AttendanceRecord;
import com.randomdraw.model.PerformanceRecord;
import com.randomdraw.model.Student;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataStore {
    private static final String INFO_HEADER = "学号,姓名,班级";
    private static final String ATTENDANCE_HEADER = "学号,姓名,班级,考勤情况,时间";
    private static final String PERFORMANCE_HEADER = "学号,姓名,班级,表现情况,时间";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path infoFile;
    private final Path attendanceFile;
    private final Path performanceFile;

    public DataStore(Path root) {
        Path dataDir = root.resolve("data");
        this.infoFile = dataDir.resolve("student_information.txt");
        this.attendanceFile = dataDir.resolve("student_attendance.txt");
        this.performanceFile = dataDir.resolve("student_performance.txt");
        initFiles(dataDir);
    }

    private void initFiles(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            ensureFile(infoFile, INFO_HEADER);
            ensureFile(attendanceFile, ATTENDANCE_HEADER);
            ensureFile(performanceFile, PERFORMANCE_HEADER);
        } catch (IOException e) {
            throw new IllegalStateException("初始化数据文件失败", e);
        }
    }

    private void ensureFile(Path file, String header) throws IOException {
        if (!Files.exists(file)) {
            Files.writeString(file, header + System.lineSeparator(), StandardCharsets.UTF_8);
            return;
        }

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            Files.writeString(file, header + System.lineSeparator(), StandardCharsets.UTF_8);
            return;
        }

        List<String> normalized = new ArrayList<>();
        normalized.add(header);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isBlank()) {
                continue;
            }
            String normalizedLine = normalizeLineForFile(file, line);
            if (normalizedLine != null && !normalizedLine.isBlank()) {
                normalized.add(normalizedLine);
            }
        }
        Files.write(file, normalized, StandardCharsets.UTF_8);
    }

    public List<Student> loadStudents() {
        try {
            List<String> lines = Files.readAllLines(infoFile, StandardCharsets.UTF_8);
            List<Student> students = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                Student student = parseStudent(lines.get(i));
                if (student != null) {
                    students.add(student);
                }
            }
            students.sort(Comparator.comparing(Student::className).thenComparing(Student::studentId));
            return students;
        } catch (IOException e) {
            throw new IllegalStateException("读取学生信息失败", e);
        }
    }

    public void saveStudents(List<Student> students) {
        List<String> lines = new ArrayList<>();
        lines.add(INFO_HEADER);
        students.stream()
                .sorted(Comparator.comparing(Student::className).thenComparing(Student::studentId))
                .map(this::formatStudent)
                .forEach(lines::add);
        writeLines(infoFile, lines);
    }

    public List<AttendanceRecord> loadAttendanceRecords() {
        try {
            List<String> lines = Files.readAllLines(attendanceFile, StandardCharsets.UTF_8);
            List<AttendanceRecord> records = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                AttendanceRecord record = parseAttendance(lines.get(i));
                if (record != null) {
                    records.add(record);
                }
            }
            return records;
        } catch (IOException e) {
            throw new IllegalStateException("读取考勤记录失败", e);
        }
    }

    public List<PerformanceRecord> loadPerformanceRecords() {
        try {
            List<String> lines = Files.readAllLines(performanceFile, StandardCharsets.UTF_8);
            List<PerformanceRecord> records = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                PerformanceRecord record = parsePerformance(lines.get(i));
                if (record != null) {
                    records.add(record);
                }
            }
            return records;
        } catch (IOException e) {
            throw new IllegalStateException("读取表现记录失败", e);
        }
    }

    public void appendAttendanceRecords(Collection<AttendanceRecord> records) {
        appendLines(attendanceFile, records.stream().map(this::formatAttendance).toList());
    }

    public void appendPerformanceRecords(Collection<PerformanceRecord> records) {
        appendLines(performanceFile, records.stream().map(this::formatPerformance).toList());
    }

    public void saveAttendanceRecords(List<AttendanceRecord> records) {
        List<String> lines = new ArrayList<>();
        lines.add(ATTENDANCE_HEADER);
        records.stream().map(this::formatAttendance).forEach(lines::add);
        writeLines(attendanceFile, lines);
    }

    public void savePerformanceRecords(List<PerformanceRecord> records) {
        List<String> lines = new ArrayList<>();
        lines.add(PERFORMANCE_HEADER);
        records.stream().map(this::formatPerformance).forEach(lines::add);
        writeLines(performanceFile, lines);
    }

    public String currentTimestamp() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    public Set<String> loadClasses() {
        return loadStudents().stream()
                .map(Student::className)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<String, Student> loadStudentMap() {
        return loadStudents().stream()
                .collect(Collectors.toMap(Student::studentId, student -> student, (left, right) -> right, LinkedHashMap::new));
    }

    private void appendLines(Path file, List<String> newLines) {
        if (newLines.isEmpty()) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            lines.addAll(newLines);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("写入数据失败", e);
        }
    }

    private void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("保存数据失败", e);
        }
    }

    private String normalizeLineForFile(Path file, String line) {
        if (file.equals(infoFile)) {
            Student student = parseStudent(line);
            return student == null ? null : formatStudent(student);
        }
        if (file.equals(attendanceFile)) {
            AttendanceRecord record = parseAttendance(line);
            return record == null ? null : formatAttendance(record);
        }
        PerformanceRecord record = parsePerformance(line);
        return record == null ? null : formatPerformance(record);
    }

    private Student parseStudent(String line) {
        String[] parts = splitColumns(line, 3, true);
        if (parts == null || parts[0].isBlank() || parts[2].isBlank()) {
            return null;
        }
        return new Student(parts[0], parts[1], parts[2]);
    }

    private AttendanceRecord parseAttendance(String line) {
        String[] parts = splitColumns(line, 5, true);
        if (parts == null || parts[0].isBlank() || parts[2].isBlank() || parts[3].isBlank()) {
            return null;
        }
        String time = parts[4].isBlank() ? currentTimestamp() : parts[4];
        return new AttendanceRecord(parts[0], parts[1], parts[2], parts[3], time);
    }

    private PerformanceRecord parsePerformance(String line) {
        String[] parts = splitColumns(line, 5, true);
        if (parts == null || parts[0].isBlank() || parts[2].isBlank() || parts[3].isBlank()) {
            return null;
        }
        String time = parts[4].isBlank() ? currentTimestamp() : parts[4];
        return new PerformanceRecord(parts[0], parts[1], parts[2], parts[3], time);
    }

    private String[] splitColumns(String line, int expectedCount, boolean allowMissingName) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String[] parts;
        if (line.contains(",")) {
            parts = line.split(",", -1);
        } else if (line.contains("\t")) {
            parts = line.split("\t", -1);
        } else {
            parts = line.trim().split("\\s+", -1);
        }

        parts = trimParts(parts);

        if (expectedCount == 5 && parts.length == 4) {
            String[] repaired = new String[expectedCount];
            repaired[0] = parts[0];
            repaired[1] = normalizeName(parts[1]);
            repaired[2] = safe(parts[2]);
            repaired[3] = safe(parts[3]);
            repaired[4] = "";
            return normalizeLength(repaired, expectedCount);
        }

        if (allowMissingName && parts.length == expectedCount - 1) {
            String[] repaired = new String[expectedCount];
            repaired[0] = parts[0];
            repaired[1] = " ";
            for (int i = 1; i < parts.length; i++) {
                repaired[i + 1] = parts[i];
            }
            return normalizeLength(repaired, expectedCount);
        }

        return normalizeLength(parts, expectedCount);
    }

    private String[] trimParts(String[] parts) {
        String[] result = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = parts[i] == null ? "" : parts[i].trim();
        }
        return result;
    }

    private String[] normalizeLength(String[] parts, int expectedCount) {
        String[] result = new String[expectedCount];
        for (int i = 0; i < expectedCount; i++) {
            String value = i < parts.length ? parts[i] : "";
            result[i] = i == 1 ? normalizeName(value) : safe(value);
        }
        return result;
    }

    private String formatStudent(Student student) {
        return String.join(",", safe(student.studentId()), normalizeName(student.name()), safe(student.className()));
    }

    private String formatAttendance(AttendanceRecord record) {
        return String.join(",", safe(record.studentId()), normalizeName(record.name()),
                safe(record.className()), safe(record.status()), safe(record.time()));
    }

    private String formatPerformance(PerformanceRecord record) {
        return String.join(",", safe(record.studentId()), normalizeName(record.name()),
                safe(record.className()), safe(record.rating()), safe(record.time()));
    }

    private String normalizeName(String value) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isEmpty() ? " " : normalized;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
