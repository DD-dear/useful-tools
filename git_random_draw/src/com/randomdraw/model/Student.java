package com.randomdraw.model;

public record Student(String studentId, String name, String className) {
    public String displayName() {
        return name == null || name.isBlank() ? "(未填写姓名)" : name.trim();
    }
}
