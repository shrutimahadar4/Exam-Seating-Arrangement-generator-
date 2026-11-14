package com.examseating.model;

/**
 * Seat model class representing an individual seat on a bench
 */
public class Seat {
    private int seatNumber; // 1, 2, ...
    private int benchNumber;
    private Student assignedStudent; // null if unassigned

    public Seat() {
    }

    public Seat(int seatNumber, int benchNumber) {
        this.seatNumber = seatNumber;
        this.benchNumber = benchNumber;
        this.assignedStudent = null;
    }

    public Seat(int seatNumber, int benchNumber, Student assignedStudent) {
        this.seatNumber = seatNumber;
        this.benchNumber = benchNumber;
        this.assignedStudent = assignedStudent;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getBenchNumber() {
        return benchNumber;
    }

    public void setBenchNumber(int benchNumber) {
        this.benchNumber = benchNumber;
    }

    public Student getAssignedStudent() {
        return assignedStudent;
    }

    public void setAssignedStudent(Student assignedStudent) {
        this.assignedStudent = assignedStudent;
    }

    public boolean isOccupied() {
        return this.assignedStudent != null;
    }

    @Override
    public String toString() {
        return "Seat [bench=" + benchNumber + ", seat=" + seatNumber + ", student=" + (assignedStudent != null ? assignedStudent.getRollNo() : "EMPTY") + "]";
    }
}
