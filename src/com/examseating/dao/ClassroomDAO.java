package com.examseating.dao;

import com.examseating.db.DatabaseConnection;
import com.examseating.model.Classroom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClassroomDAO {

    public List<Classroom> getAllClassrooms() {
        List<Classroom> list = new ArrayList<>();
        String sql = "SELECT room_id, room_number, benches, seats_per_bench, capacity FROM classrooms ORDER BY room_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Classroom(
                        rs.getInt("room_id"),
                        rs.getString("room_number"),
                        rs.getInt("benches"),
                        rs.getInt("seats_per_bench")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Classroom getClassroomById(int id) {
        String sql = "SELECT room_id, room_number, benches, seats_per_bench, capacity FROM classrooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Classroom(
                            rs.getInt("room_id"),
                            rs.getString("room_number"),
                            rs.getInt("benches"),
                            rs.getInt("seats_per_bench")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addClassroom(Classroom classroom) {
        String sql = "INSERT INTO classrooms (room_number, benches, seats_per_bench, capacity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classroom.getRoomNumber().trim());
            ps.setInt(2, classroom.getBenches());
            ps.setInt(3, classroom.getSeatsPerBench());
            ps.setInt(4, classroom.getCapacity());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateClassroom(Classroom classroom) {
        String sql = "UPDATE classrooms SET room_number = ?, benches = ?, seats_per_bench = ?, capacity = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classroom.getRoomNumber().trim());
            ps.setInt(2, classroom.getBenches());
            ps.setInt(3, classroom.getSeatsPerBench());
            ps.setInt(4, classroom.getCapacity());
            ps.setInt(5, classroom.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteClassroom(int id) {
        String sql = "DELETE FROM classrooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getTotalCapacity() {
        String sql = "SELECT SUM(capacity) FROM classrooms";
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
}
