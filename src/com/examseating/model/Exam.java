package com.examseating.model;

/**
 * Exam model class supporting Exam Timetable and Hold/Release feature
 */
public class Exam {
    private int id;
    private String examName;      // e.g. "Final Semester Examination", "Unit Test 1"
    private String subject;       // e.g. "Advanced Java & Database Management"
    private String academicYear;  // e.g. "Third Year", "First Year", "All Years"
    private String examDate;      // e.g. "2026-10-15"
    private String startTime;     // e.g. "10:00 AM"
    private String endTime;       // e.g. "01:00 PM"
    private String examTime;      // e.g. "10:00 AM - 01:00 PM"
    private boolean onHold;       // true if arrangement is locked/frozen
    private String status;        // "SCHEDULED", "ON_HOLD", "COMPLETED"

    public Exam() {
        this.status = "SCHEDULED";
        this.academicYear = "All Years";
        this.onHold = false;
    }

    public Exam(int id, String subject, String examDate, String examTime, String status) {
        this.id = id;
        this.examName = subject + " Exam";
        this.subject = subject;
        this.academicYear = "All Years";
        this.examDate = examDate;
        this.examTime = examTime;
        this.startTime = parseStartTime(examTime);
        this.endTime = parseEndTime(examTime);
        this.onHold = "ON_HOLD".equalsIgnoreCase(status);
        this.status = status != null ? status : "SCHEDULED";
    }

    public Exam(int id, String examName, String subject, String academicYear,
                String examDate, String startTime, String endTime, boolean onHold, String status) {
        this.id = id;
        this.examName = examName;
        this.subject = subject;
        this.academicYear = (academicYear != null && !academicYear.isEmpty()) ? academicYear : "All Years";
        this.examDate = examDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.examTime = (startTime != null && endTime != null) ? (startTime + " - " + endTime) : startTime;
        this.onHold = onHold;
        this.status = status != null ? status : (onHold ? "ON_HOLD" : "SCHEDULED");
    }

    private String parseStartTime(String time) {
        if (time != null && time.contains("-")) {
            return time.split("-")[0].trim();
        }
        return time != null ? time : "10:00 AM";
    }

    private String parseEndTime(String time) {
        if (time != null && time.contains("-")) {
            String[] p = time.split("-");
            return p.length > 1 ? p[1].trim() : "01:00 PM";
        }
        return "01:00 PM";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExamName() {
        return examName != null && !examName.isEmpty() ? examName : subject + " Examination";
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAcademicYear() {
        return academicYear != null ? academicYear : "All Years";
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
        updateExamTimeString();
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
        updateExamTimeString();
    }

    public String getExamTime() {
        if (examTime != null && !examTime.isEmpty()) return examTime;
        if (startTime != null && endTime != null) return startTime + " - " + endTime;
        return startTime;
    }

    public void setExamTime(String examTime) {
        this.examTime = examTime;
    }

    private void updateExamTimeString() {
        if (startTime != null && endTime != null) {
            this.examTime = startTime + " - " + endTime;
        }
    }

    public boolean isOnHold() {
        return onHold;
    }

    public void setOnHold(boolean onHold) {
        this.onHold = onHold;
        if (onHold) {
            this.status = "ON_HOLD";
        } else if ("ON_HOLD".equals(this.status)) {
            this.status = "SCHEDULED";
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.onHold = "ON_HOLD".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Exam [name=" + examName + ", subject=" + subject + ", year=" + academicYear + ", date=" + examDate + ", time=" + getExamTime() + ", onHold=" + onHold + "]";
    }
}
