package com.examseating.model;

/**
 * Division model class
 */
public class Division {
    private int id;
    private String divisionName;
    private String description;
    private int studentCount;

    public Division() {
    }

    public Division(int id, String divisionName, String description) {
        this.id = id;
        this.divisionName = divisionName;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    @Override
    public String toString() {
        return "Division [divisionName=" + divisionName + ", description=" + description + ", studentCount=" + studentCount + "]";
    }
}
