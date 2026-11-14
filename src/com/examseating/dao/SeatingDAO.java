package com.examseating.dao;

import com.examseating.db.DatabaseConnection;
import com.examseating.model.SeatingArrangement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatingDAO {

    public List<SeatingArrangement> getAllocationsByExamId(int examId) {
        List<SeatingArrangement> list = new ArrayList<>();
        String sql = "SELECT s.allocation_id, s.exam_id, s.student_id, s.room_id, s.room_number, " +
                "s.bench_no, s.seat_no, s.student_name, s.roll_no, s.division_name, s.academic_year, s.conflict_flag, s.conflict_reason, " +
                "e.exam_name, e.subject, e.exam_date, e.start_time, e.end_time, e.exam_time " +
                "FROM seating_allocations s " +
                "JOIN exams e ON s.exam_id = e.exam_id " +
                "WHERE s.exam_id = ? " +
                "ORDER BY s.room_number ASC, s.bench_no ASC, s.seat_no ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAllocation(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public SeatingArrangement getAllocationForStudent(int studentId, int examId) {
        String sql = "SELECT s.allocation_id, s.exam_id, s.student_id, s.room_id, s.room_number, " +
                "s.bench_no, s.seat_no, s.student_name, s.roll_no, s.division_name, s.academic_year, s.conflict_flag, s.conflict_reason, " +
                "e.exam_name, e.subject, e.exam_date, e.start_time, e.end_time, e.exam_time " +
                "FROM seating_allocations s " +
                "JOIN exams e ON s.exam_id = e.exam_id " +
                "WHERE s.student_id = ? AND s.exam_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAllocation(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SeatingArrangement getLatestAllocationForStudent(int studentId) {
        String sql = "SELECT s.allocation_id, s.exam_id, s.student_id, s.room_id, s.room_number, " +
                "s.bench_no, s.seat_no, s.student_name, s.roll_no, s.division_name, s.academic_year, s.conflict_flag, s.conflict_reason, " +
                "e.exam_name, e.subject, e.exam_date, e.start_time, e.end_time, e.exam_time " +
                "FROM seating_allocations s " +
                "JOIN exams e ON s.exam_id = e.exam_id " +
                "WHERE s.student_id = ? " +
                "ORDER BY s.allocation_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAllocation(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean clearAllocationsForExam(int examId) {
        String sql = "DELETE FROM seating_allocations WHERE exam_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean saveAllocations(List<SeatingArrangement> allocations, int examId) {
        String insertSql = "INSERT INTO seating_allocations " +
                "(exam_id, student_id, room_id, room_number, bench_no, seat_no, student_name, roll_no, division_name, academic_year, exam_name, conflict_flag, conflict_reason) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Clear existing for this exam
                try (PreparedStatement delPs = conn.prepareStatement("DELETE FROM seating_allocations WHERE exam_id = ?")) {
                    delPs.setInt(1, examId);
                    delPs.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (SeatingArrangement sa : allocations) {
                        ps.setInt(1, sa.getExamId());
                        ps.setInt(2, sa.getStudentId());
                        ps.setInt(3, sa.getRoomId());
                        ps.setString(4, sa.getRoomNumber());
                        ps.setInt(5, sa.getBenchNo());
                        ps.setInt(6, sa.getSeatNo());
                        ps.setString(7, sa.getStudentName());
                        ps.setString(8, sa.getRollNo());
                        ps.setString(9, sa.getDivisionName());
                        ps.setString(10, sa.getAcademicYear() != null ? sa.getAcademicYear() : "First Year");
                        ps.setString(11, sa.getExamName() != null ? sa.getExamName() : sa.getExamSubject());
                        ps.setInt(12, sa.getConflictFlag());
                        ps.setString(13, sa.getConflictReason());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Map<String, Object> getSummary(int examId) {
        Map<String, Object> map = new HashMap<>();
        String sql = "SELECT COUNT(*) AS total_allocated, " +
                "COUNT(DISTINCT room_id) AS rooms_used, " +
                "SUM(CASE WHEN conflict_flag = 1 THEN 1 ELSE 0 END) AS conflict_count " +
                "FROM seating_allocations WHERE exam_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    map.put("totalAllocated", rs.getInt("total_allocated"));
                    map.put("roomsUsed", rs.getInt("rooms_used"));
                    map.put("conflictCount", rs.getInt("conflict_count"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private SeatingArrangement mapAllocation(ResultSet rs) throws Exception {
        SeatingArrangement sa = new SeatingArrangement();
        sa.setAllocationId(rs.getInt("allocation_id"));
        sa.setExamId(rs.getInt("exam_id"));
        sa.setStudentId(rs.getInt("student_id"));
        sa.setRoomId(rs.getInt("room_id"));
        sa.setRoomNumber(rs.getString("room_number"));
        sa.setBenchNo(rs.getInt("bench_no"));
        sa.setSeatNo(rs.getInt("seat_no"));
        sa.setStudentName(rs.getString("student_name"));
        sa.setRollNo(rs.getString("roll_no"));
        sa.setDivisionName(rs.getString("division_name"));
        sa.setAcademicYear(rs.getString("academic_year"));
        sa.setConflictFlag(rs.getInt("conflict_flag"));
        sa.setConflictReason(rs.getString("conflict_reason"));
        sa.setExamName(rs.getString("exam_name"));
        sa.setExamSubject(rs.getString("subject"));
        sa.setExamDate(rs.getString("exam_date"));
        sa.setStartTime(rs.getString("start_time"));
        sa.setEndTime(rs.getString("end_time"));
        sa.setExamTime(rs.getString("exam_time"));
        return sa;
    }
}
