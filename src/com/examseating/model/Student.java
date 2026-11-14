package com.examseating.model;

/**
 * Student model class extending User with Year-wise support
 */
public class Student extends User {
    private String rollNo;
    private String divisionName;
    private String academicYear; // "First Year", "Second Year", "Third Year", "Fourth Year"

    public Student() {
        super();
        setRole("STUDENT");
        this.academicYear = "First Year";
    }

    public Student(int id, String rollNo, String name, String divisionName, String username, String password) {
        super(id, username, password, name, "STUDENT");
        this.rollNo = rollNo;
        this.divisionName = divisionName;
        this.academicYear = "First Year";
    }

    public Student(int id, String rollNo, String name, String divisionName, String academicYear, String username, String password) {
        super(id, username, password, name, "STUDENT");
        this.rollNo = rollNo;
        this.divisionName = divisionName;
        this.academicYear = (academicYear != null && !academicYear.trim().isEmpty()) ? academicYear : "First Year";
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    @Override
    public String toString() {
        return "Student [rollNo=" + rollNo + ", name=" + getName() + ", year=" + academicYear + ", division=" + divisionName + "]";
    }
}
