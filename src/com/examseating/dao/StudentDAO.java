package com.examseating.dao;

import com.examseating.db.DatabaseConnection;
import com.examseating.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDAO {

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT student_id, roll_no, name, division_name, academic_year, username, password " +
                     "FROM students ORDER BY academic_year ASC, division_name ASC, roll_no ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Student(
                        rs.getInt("student_id"),
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getString("division_name"),
                        rs.getString("academic_year"),
                        rs.getString("username"),
                        rs.getString("password")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Student> getStudentsByYear(String academicYear) {
        if (academicYear == null || academicYear.trim().isEmpty() || "All Years".equalsIgnoreCase(academicYear)) {
            return getAllStudents();
        }
        List<Student> list = new ArrayList<>();
        String sql = "SELECT student_id, roll_no, name, division_name, academic_year, username, password " +
                     "FROM students WHERE academic_year = ? ORDER BY division_name ASC, roll_no ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("division_name"),
                            rs.getString("academic_year"),
                            rs.getString("username"),
                            rs.getString("password")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Student> getStudentsByDivision(String divisionName) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT student_id, roll_no, name, division_name, academic_year, username, password " +
                     "FROM students WHERE division_name = ? ORDER BY roll_no ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, divisionName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("division_name"),
                            rs.getString("academic_year"),
                            rs.getString("username"),
                            rs.getString("password")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Student getStudentById(int id) {
        String sql = "SELECT student_id, roll_no, name, division_name, academic_year, username, password FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("division_name"),
                            rs.getString("academic_year"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Student getStudentByRollNo(String rollNo) {
        String sql = "SELECT student_id, roll_no, name, division_name, academic_year, username, password FROM students WHERE roll_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("division_name"),
                            rs.getString("academic_year"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Student authenticate(String rollOrUsername, String password) {
        String sql = "SELECT student_id, roll_no, name, division_name, academic_year, username, password " +
                "FROM students WHERE (roll_no = ? OR username = ?) AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollOrUsername);
            ps.setString(2, rollOrUsername);
            ps.setString(3, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("division_name"),
                            rs.getString("academic_year"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students (roll_no, name, division_name, academic_year, username, password) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getRollNo().trim());
            ps.setString(2, student.getName().trim());
            ps.setString(3, student.getDivisionName().trim().toUpperCase());
            ps.setString(4, student.getAcademicYear() != null ? student.getAcademicYear() : "First Year");
            ps.setString(5, student.getUsername().trim());
            ps.setString(6, student.getPassword().trim());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET roll_no = ?, name = ?, division_name = ?, academic_year = ?, username = ?, password = ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getRollNo().trim());
            ps.setString(2, student.getName().trim());
            ps.setString(3, student.getDivisionName().trim().toUpperCase());
            ps.setString(4, student.getAcademicYear() != null ? student.getAcademicYear() : "First Year");
            ps.setString(5, student.getUsername().trim());
            ps.setString(6, student.getPassword().trim());
            ps.setInt(7, student.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAllStudents() {
        String sqlAlloc = "DELETE FROM seating_allocations";
        String sqlStudents = "DELETE FROM students";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate(sqlAlloc);
            } catch (Exception ignored) {}
            return stmt.executeUpdate(sqlStudents) >= 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteStudentsByYear(String academicYear) {
        if (academicYear == null || academicYear.isEmpty() || "All Years".equalsIgnoreCase(academicYear)) {
            return deleteAllStudents();
        }
        String sqlAlloc = "DELETE FROM seating_allocations WHERE academic_year = ?";
        String sqlStudents = "DELETE FROM students WHERE academic_year = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlAlloc)) {
                ps.setString(1, academicYear);
                ps.executeUpdate();
            } catch (Exception ignored) {}
            try (PreparedStatement ps = conn.prepareStatement(sqlStudents)) {
                ps.setString(1, academicYear);
                return ps.executeUpdate() >= 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getTotalStudentCount() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Integer> getYearDistribution() {
        Map<String, Integer> map = new HashMap<>();
        map.put("First Year", 0);
        map.put("Second Year", 0);
        map.put("Third Year", 0);
        map.put("Fourth Year", 0);

        String sql = "SELECT academic_year, COUNT(*) AS cnt FROM students GROUP BY academic_year";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String yr = rs.getString("academic_year");
                int cnt = rs.getInt("cnt");
                if (yr != null) {
                    map.put(yr, cnt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}

