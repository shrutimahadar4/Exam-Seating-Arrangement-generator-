package com.examseating.service;

import com.examseating.algorithm.RuleBasedSeatingGenerator;
import com.examseating.algorithm.RuleBasedSeatingGenerator.SeatingResult;
import com.examseating.dao.*;
import com.examseating.db.DatabaseConnection;
import com.examseating.model.*;

import java.util.*;

public class SeatingService {
    private final StudentDAO studentDAO = new StudentDAO();
    private final ClassroomDAO classroomDAO = new ClassroomDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final SeatingDAO seatingDAO = new SeatingDAO();
    private final DivisionDAO divisionDAO = new DivisionDAO();
    private final RuleBasedSeatingGenerator generator = new RuleBasedSeatingGenerator();

    public SeatingResult generateAndSaveSeating(int examId) {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) {
            exam = examDAO.getActiveExam();
        }

        if (exam == null) {
            SeatingResult err = new SeatingResult();
            err.setSuccess(false);
            err.setMessage("Cannot generate seating: No exam found. Please schedule an exam in the Timetable first.");
            return err;
        }

        // Check if on hold
        if (exam.isOnHold()) {
            SeatingResult err = new SeatingResult();
            err.setSuccess(false);
            err.setMessage("Cannot generate seating: The examination arrangement for '" + exam.getExamName()
                    + "' is currently ON HOLD (Locked). Please release the hold before regenerating.");
            return err;
        }

        // Year-wise student selection
        List<Student> students;
        String year = exam.getAcademicYear();
        if (year != null && !year.trim().isEmpty() && !"All Years".equalsIgnoreCase(year)) {
            students = studentDAO.getStudentsByYear(year);
            if (students.isEmpty()) {
                // If no specific students for this year, inform admin
                SeatingResult err = new SeatingResult();
                err.setSuccess(false);
                err.setMessage("No registered students found for " + year + ". Please add students for this academic year or select 'All Years'.");
                return err;
            }
        } else {
            students = studentDAO.getAllStudents();
        }

        List<Classroom> classrooms = classroomDAO.getAllClassrooms();

        SeatingResult result = generator.generateSeating(exam, students, classrooms);

        if (result.isSuccess() && !result.getAllocations().isEmpty()) {
            boolean saved = seatingDAO.saveAllocations(result.getAllocations(), exam.getId());
            if (!saved) {
                result.setSuccess(false);
                result.setMessage("Seating was calculated but failed to persist to the database.");
            }
        }

        return result;
    }

    public List<SeatingArrangement> getSeatingForExam(int examId) {
        return seatingDAO.getAllocationsByExamId(examId);
    }

    public SeatingArrangement getStudentAllocation(int studentId, int examId) {
        if (examId > 0) {
            return seatingDAO.getAllocationForStudent(studentId, examId);
        }
        return seatingDAO.getLatestAllocationForStudent(studentId);
    }

    public boolean toggleHoldStatus(int examId, boolean onHold) {
        return examDAO.setHoldStatus(examId, onHold);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        int studentCount = studentDAO.getTotalStudentCount();
        int capacity = classroomDAO.getTotalCapacity();
        List<Division> divisions = divisionDAO.getAllDivisions();
        List<Classroom> classrooms = classroomDAO.getAllClassrooms();
        List<Exam> allExams = examDAO.getAllExams();
        Exam activeExam = examDAO.getActiveExam();

        stats.put("totalStudents", studentCount);
        stats.put("totalCapacity", capacity);
        stats.put("totalClassrooms", classrooms.size());
        stats.put("totalDivisions", divisions.size());
        stats.put("totalExams", allExams.size());
        stats.put("yearDistribution", studentDAO.getYearDistribution());
        stats.put("dbType", DatabaseConnection.getActiveDatabaseType());
        stats.put("dbInfo", DatabaseConnection.getConnectionInfo());

        if (activeExam != null) {
            stats.put("activeExamId", activeExam.getId());
            stats.put("activeExamName", activeExam.getExamName());
            stats.put("activeExamSubject", activeExam.getSubject());
            stats.put("activeExamYear", activeExam.getAcademicYear());
            stats.put("activeExamDate", activeExam.getExamDate());
            stats.put("activeExamStartTime", activeExam.getStartTime());
            stats.put("activeExamEndTime", activeExam.getEndTime());
            stats.put("activeExamTime", activeExam.getExamTime());
            stats.put("activeExamOnHold", activeExam.isOnHold());
            stats.put("activeExamStatus", activeExam.getStatus());

            Map<String, Object> summary = seatingDAO.getSummary(activeExam.getId());
            int allocated = (int) summary.getOrDefault("totalAllocated", 0);
            stats.put("totalAllocated", allocated);
            stats.put("conflictCount", summary.getOrDefault("conflictCount", 0));
            stats.put("seatingGenerated", allocated > 0);
        } else {
            stats.put("activeExamId", 0);
            stats.put("activeExamName", "No Scheduled Exam");
            stats.put("activeExamSubject", "No Active Exam");
            stats.put("activeExamYear", "-");
            stats.put("activeExamDate", "-");
            stats.put("activeExamStartTime", "-");
            stats.put("activeExamEndTime", "-");
            stats.put("activeExamTime", "-");
            stats.put("activeExamOnHold", false);
            stats.put("activeExamStatus", "NONE");
            stats.put("totalAllocated", 0);
            stats.put("conflictCount", 0);
            stats.put("seatingGenerated", false);
        }

        return stats;
    }
}
