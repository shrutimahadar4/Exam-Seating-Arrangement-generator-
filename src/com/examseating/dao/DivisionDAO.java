package com.examseating.dao;

import com.examseating.db.DatabaseConnection;
import com.examseating.model.Division;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DivisionDAO {

    public List<Division> getAllDivisions() {
        List<Division> list = new ArrayList<>();
        String sql = "SELECT d.division_id, d.division_name, d.description, " +
                "(SELECT COUNT(*) FROM students s WHERE s.division_name = d.division_name) AS student_count " +
                "FROM divisions d ORDER BY d.division_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Division d = new Division(
                        rs.getInt("division_id"),
                        rs.getString("division_name"),
                        rs.getString("description")
                );
                d.setStudentCount(rs.getInt("student_count"));
                list.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addDivision(Division division) {
        String sql = "INSERT INTO divisions (division_name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, division.getDivisionName().trim().toUpperCase());
            ps.setString(2, division.getDescription());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteDivision(String divisionName) {
        String sql = "DELETE FROM divisions WHERE division_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, divisionName);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
