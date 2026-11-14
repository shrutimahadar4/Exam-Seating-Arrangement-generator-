package com.examseating.model;

/**
 * SeatingArrangement model class representing a generated seat allocation
 */
public class SeatingArrangement {
    private int allocationId;
    private int examId;
    private int studentId;
    private int roomId;
    private String roomNumber;
    private int benchNo;
    private int seatNo;
    private String studentName;
    private String rollNo;
    private String divisionName;
    private String academicYear; // "First Year", "Second Year", "Third Year", "Fourth Year"
    private int conflictFlag;    // 0 = Clean, 1 = Conflict/Constraint Warning
    private String conflictReason;

    // Optional exam details for student view / hall ticket
    private String examName;
    private String examSubject;
    private String examDate;
    private String examTime;
    private String startTime;
    private String endTime;

    public SeatingArrangement() {
        this.academicYear = "First Year";
        this.conflictFlag = 0;
        this.conflictReason = "";
    }

    public SeatingArrangement(int examId, int studentId, int roomId, String roomNumber,
                              int benchNo, int seatNo, String studentName, String rollNo,
                              String divisionName) {
        this.examId = examId;
        this.studentId = studentId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.benchNo = benchNo;
        this.seatNo = seatNo;
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.divisionName = divisionName;
        this.academicYear = "First Year";
        this.conflictFlag = 0;
        this.conflictReason = "";
    }

    public SeatingArrangement(int examId, int studentId, int roomId, String roomNumber,
                              int benchNo, int seatNo, String studentName, String rollNo,
                              String divisionName, String academicYear) {
        this.examId = examId;
        this.studentId = studentId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.benchNo = benchNo;
        this.seatNo = seatNo;
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.divisionName = divisionName;
        this.academicYear = (academicYear != null && !academicYear.isEmpty()) ? academicYear : "First Year";
        this.conflictFlag = 0;
        this.conflictReason = "";
    }

    public int getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(int allocationId) {
        this.allocationId = allocationId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getBenchNo() {
        return benchNo;
    }

    public void setBenchNo(int benchNo) {
        this.benchNo = benchNo;
    }

    public int getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(int seatNo) {
        this.seatNo = seatNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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
        return academicYear != null ? academicYear : "First Year";
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public int getConflictFlag() {
        return conflictFlag;
    }

    public void setConflictFlag(int conflictFlag) {
        this.conflictFlag = conflictFlag;
    }

    public String getConflictReason() {
        return conflictReason;
    }

    public void setConflictReason(String conflictReason) {
        this.conflictReason = conflictReason;
    }

    public String getExamName() {
        return examName != null ? examName : examSubject;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamSubject() {
        return examSubject;
    }

    public void setExamSubject(String examSubject) {
        this.examSubject = examSubject;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getExamTime() {
        return examTime;
    }

    public void setExamTime(String examTime) {
        this.examTime = examTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "SeatingArrangement [room=" + roomNumber + ", bench=" + benchNo + ", seat=" + seatNo
                + ", year=" + academicYear + ", rollNo=" + rollNo + ", name=" + studentName + ", div=" + divisionName + "]";
    }
}
