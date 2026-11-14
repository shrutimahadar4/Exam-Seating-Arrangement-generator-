package com.examseating.dao;

import com.examseating.db.DatabaseConnection;
import com.examseating.model.Exam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    public List<Exam> getAllExams() {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT exam_id, exam_name, subject, academic_year, exam_date, start_time, end_time, exam_time, is_on_hold, status " +
                     "FROM exams ORDER BY exam_date ASC, exam_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapExam(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Exam> getExamsByYear(String academicYear) {
        if (academicYear == null || academicYear.isEmpty() || "All Years".equalsIgnoreCase(academicYear)) {
            return getAllExams();
        }
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT exam_id, exam_name, subject, academic_year, exam_date, start_time, end_time, exam_time, is_on_hold, status " +
                     "FROM exams WHERE academic_year = ? OR academic_year = 'All Years' ORDER BY exam_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapExam(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Exam getActiveExam() {
        // Return first scheduled or unheld exam, or latest
        String sql = "SELECT exam_id, exam_name, subject, academic_year, exam_date, start_time, end_time, exam_time, is_on_hold, status " +
                     "FROM exams ORDER BY exam_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            Exam first = null;
            while (rs.next()) {
                Exam ex = mapExam(rs);
                if (first == null) first = ex;
                if (!ex.isOnHold() && !"COMPLETED".equalsIgnoreCase(ex.getStatus())) {
                    return ex;
                }
            }
            return first;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Exam getExamById(int id) {
        String sql = "SELECT exam_id, exam_name, subject, academic_year, exam_date, start_time, end_time, exam_time, is_on_hold, status " +
                     "FROM exams WHERE exam_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapExam(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addExam(Exam exam) {
        String sql = "INSERT INTO exams (exam_name, subject, academic_year, exam_date, start_time, end_time, exam_time, is_on_hold, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, exam.getExamName());
            ps.setString(2, exam.getSubject().trim());
            ps.setString(3, exam.getAcademicYear() != null ? exam.getAcademicYear() : "All Years");
            ps.setString(4, exam.getExamDate().trim());
            ps.setString(5, exam.getStartTime() != null ? exam.getStartTime().trim() : "10:00 AM");
            ps.setString(6, exam.getEndTime() != null ? exam.getEndTime().trim() : "01:00 PM");
            ps.setString(7, exam.getExamTime() != null ? exam.getExamTime().trim() : "10:00 AM - 01:00 PM");
            ps.setInt(8, exam.isOnHold() ? 1 : 0);
            ps.setString(9, exam.getStatus() != null ? exam.getStatus() : "SCHEDULED");
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateExam(Exam exam) {
        String sql = "UPDATE exams SET exam_name = ?, subject = ?, academic_year = ?, exam_date = ?, " +
                     "start_time = ?, end_time = ?, exam_time = ?, is_on_hold = ?, status = ? WHERE exam_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, exam.getExamName());
            ps.setString(2, exam.getSubject().trim());
            ps.setString(3, exam.getAcademicYear() != null ? exam.getAcademicYear() : "All Years");
            ps.setString(4, exam.getExamDate().trim());
            ps.setString(5, exam.getStartTime().trim());
            ps.setString(6, exam.getEndTime().trim());
            ps.setString(7, exam.getExamTime().trim());
            ps.setInt(8, exam.isOnHold() ? 1 : 0);
            ps.setString(9, exam.getStatus());
            ps.setInt(10, exam.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setHoldStatus(int examId, boolean onHold) {
        String status = onHold ? "ON_HOLD" : "SCHEDULED";
        String sql = "UPDATE exams SET is_on_hold = ?, status = ? WHERE exam_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, onHold ? 1 : 0);
            ps.setString(2, status);
            ps.setInt(3, examId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteExam(int id) {
        String sql = "DELETE FROM exams WHERE exam_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Exam mapExam(ResultSet rs) throws Exception {
        int id = rs.getInt("exam_id");
        String name = rs.getString("exam_name");
        String subject = rs.getString("subject");
        String year = rs.getString("academic_year");
        String date = rs.getString("exam_date");
        String start = rs.getString("start_time");
        String end = rs.getString("end_time");
        String time = rs.getString("exam_time");
        boolean onHold = rs.getInt("is_on_hold") == 1;
        String status = rs.getString("status");

        if (name == null || name.isEmpty()) name = subject + " Exam";
        if (year == null || year.isEmpty()) year = "All Years";
        if (start == null || start.isEmpty()) start = "10:00 AM";
        if (end == null || end.isEmpty()) end = "01:00 PM";
        if (time == null || time.isEmpty()) time = start + " - " + end;

        return new Exam(id, name, subject, year, date, start, end, onHold, status);
    }
}
