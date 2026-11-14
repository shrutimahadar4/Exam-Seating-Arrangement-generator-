package com.examseating.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initializes database tables, applies migrations, and default sample records on startup
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String dbType = DatabaseConnection.getActiveDatabaseType();
            System.out.println("[DatabaseInitializer] Initializing schema for: " + DatabaseConnection.getConnectionInfo());

            if ("sqlite".equalsIgnoreCase(dbType)) {
                initSqlite(stmt);
            } else {
                initMySql(stmt);
            }

            // Apply migrations for upgrading schema (Year-wise, Exam timetable, Hold feature)
            applyMigrations(stmt);

            // Seed initial data if tables are empty
            seedInitialDataIfEmpty(conn);

            System.out.println("[DatabaseInitializer] Database schema & default data verified successfully.");
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void applyMigrations(Statement stmt) {
        // Upgrade students table
        try { stmt.execute("ALTER TABLE students ADD COLUMN academic_year TEXT DEFAULT 'First Year'"); } catch (Exception ignored) {}

        // Upgrade exams table
        try { stmt.execute("ALTER TABLE exams ADD COLUMN exam_name TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE exams ADD COLUMN academic_year TEXT DEFAULT 'All Years'"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE exams ADD COLUMN start_time TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE exams ADD COLUMN end_time TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE exams ADD COLUMN is_on_hold INTEGER DEFAULT 0"); } catch (Exception ignored) {}

        // Upgrade seating_allocations table
        try { stmt.execute("ALTER TABLE seating_allocations ADD COLUMN academic_year TEXT DEFAULT 'First Year'"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE seating_allocations ADD COLUMN exam_name TEXT"); } catch (Exception ignored) {}

        // Ensure default academic_year is populated if null
        try { stmt.executeUpdate("UPDATE students SET academic_year = 'Third Year' WHERE academic_year IS NULL OR academic_year = ''"); } catch (Exception ignored) {}
        try { stmt.executeUpdate("UPDATE exams SET academic_year = 'Third Year' WHERE academic_year IS NULL OR academic_year = ''"); } catch (Exception ignored) {}
        try { stmt.executeUpdate("UPDATE exams SET start_time = '10:00 AM' WHERE start_time IS NULL OR start_time = ''"); } catch (Exception ignored) {}
        try { stmt.executeUpdate("UPDATE exams SET end_time = '01:00 PM' WHERE end_time IS NULL OR end_time = ''"); } catch (Exception ignored) {}
        try { stmt.executeUpdate("UPDATE exams SET exam_name = subject WHERE exam_name IS NULL OR exam_name = ''"); } catch (Exception ignored) {}
    }

    private static void initSqlite(Statement stmt) throws Exception {
        stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                "admin_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS divisions (" +
                "division_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "division_name TEXT UNIQUE NOT NULL, " +
                "description TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "roll_no TEXT UNIQUE NOT NULL, " +
                "name TEXT NOT NULL, " +
                "division_name TEXT NOT NULL, " +
                "academic_year TEXT DEFAULT 'First Year', " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS classrooms (" +
                "room_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "room_number TEXT UNIQUE NOT NULL, " +
                "benches INTEGER NOT NULL, " +
                "seats_per_bench INTEGER NOT NULL, " +
                "capacity INTEGER NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS exams (" +
                "exam_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "exam_name TEXT, " +
                "subject TEXT NOT NULL, " +
                "academic_year TEXT DEFAULT 'All Years', " +
                "exam_date TEXT NOT NULL, " +
                "start_time TEXT, " +
                "end_time TEXT, " +
                "exam_time TEXT NOT NULL, " +
                "is_on_hold INTEGER DEFAULT 0, " +
                "status TEXT DEFAULT 'SCHEDULED', " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS seating_allocations (" +
                "allocation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "exam_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "room_id INTEGER NOT NULL, " +
                "room_number TEXT NOT NULL, " +
                "bench_no INTEGER NOT NULL, " +
                "seat_no INTEGER NOT NULL, " +
                "student_name TEXT NOT NULL, " +
                "roll_no TEXT NOT NULL, " +
                "division_name TEXT NOT NULL, " +
                "academic_year TEXT DEFAULT 'First Year', " +
                "exam_name TEXT, " +
                "conflict_flag INTEGER DEFAULT 0, " +
                "conflict_reason TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(exam_id, student_id), " +
                "UNIQUE(exam_id, room_id, bench_no, seat_no)" +
                ")");
    }

    private static void initMySql(Statement stmt) throws Exception {
        stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                "admin_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARCHAR(100) NOT NULL, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS divisions (" +
                "division_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "division_name VARCHAR(20) NOT NULL UNIQUE, " +
                "description VARCHAR(100), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                "student_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "roll_no VARCHAR(50) NOT NULL UNIQUE, " +
                "name VARCHAR(100) NOT NULL, " +
                "division_name VARCHAR(20) NOT NULL, " +
                "academic_year VARCHAR(30) DEFAULT 'First Year', " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARCHAR(100) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS classrooms (" +
                "room_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "room_number VARCHAR(50) NOT NULL UNIQUE, " +
                "benches INT NOT NULL, " +
                "seats_per_bench INT NOT NULL, " +
                "capacity INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS exams (" +
                "exam_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "exam_name VARCHAR(100), " +
                "subject VARCHAR(100) NOT NULL, " +
                "academic_year VARCHAR(30) DEFAULT 'All Years', " +
                "exam_date VARCHAR(50) NOT NULL, " +
                "start_time VARCHAR(30), " +
                "end_time VARCHAR(30), " +
                "exam_time VARCHAR(50) NOT NULL, " +
                "is_on_hold INT DEFAULT 0, " +
                "status VARCHAR(20) DEFAULT 'SCHEDULED', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS seating_allocations (" +
                "allocation_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "exam_id INT NOT NULL, " +
                "student_id INT NOT NULL, " +
                "room_id INT NOT NULL, " +
                "room_number VARCHAR(50) NOT NULL, " +
                "bench_no INT NOT NULL, " +
                "seat_no INT NOT NULL, " +
                "student_name VARCHAR(100) NOT NULL, " +
                "roll_no VARCHAR(50) NOT NULL, " +
                "division_name VARCHAR(20) NOT NULL, " +
                "academic_year VARCHAR(30) DEFAULT 'First Year', " +
                "exam_name VARCHAR(100), " +
                "conflict_flag INT DEFAULT 0, " +
                "conflict_reason VARCHAR(255) DEFAULT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT uk_exam_student UNIQUE (exam_id, student_id), " +
                "CONSTRAINT uk_exam_seat UNIQUE (exam_id, room_id, bench_no, seat_no)" +
                ")");
    }

    private static void seedInitialDataIfEmpty(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Check admins
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admins");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO admins (username, password, full_name) VALUES " +
                        "('admin', 'admin123', 'Head Exam Administrator')");
            }

            // Check divisions
            rs = stmt.executeQuery("SELECT COUNT(*) FROM divisions");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO divisions (division_name, description) VALUES " +
                        "('A', 'Computer Engineering - Div A'), " +
                        "('B', 'Information Technology - Div B'), " +
                        "('C', 'Electronics & Telecom - Div C')");
            }

            // Check classrooms
            rs = stmt.executeQuery("SELECT COUNT(*) FROM classrooms");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO classrooms (room_number, benches, seats_per_bench, capacity) VALUES " +
                        "('Room 101', 5, 2, 10), " +
                        "('Room 102', 5, 2, 10), " +
                        "('Room 204', 6, 2, 12)");
            }

            // Check exams timetable
            rs = stmt.executeQuery("SELECT COUNT(*) FROM exams");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO exams (exam_name, subject, academic_year, exam_date, start_time, end_time, exam_time, is_on_hold, status) VALUES " +
                        "('Winter End-Semester Exam', 'Advanced Java & Database Management', 'Third Year', '2026-10-15', '10:00 AM', '01:00 PM', '10:00 AM - 01:00 PM', 0, 'SCHEDULED'), " +
                        "('Mid-Term Assessment', 'Data Structures & Algorithms', 'Second Year', '2026-10-16', '10:00 AM', '01:00 PM', '10:00 AM - 01:00 PM', 0, 'SCHEDULED'), " +
                        "('University Foundation Test', 'Engineering Mathematics I', 'First Year', '2026-10-17', '02:00 PM', '05:00 PM', '02:00 PM - 05:00 PM', 0, 'SCHEDULED'), " +
                        "('Final Year Project Defense', 'Cloud Computing & DevOps', 'Fourth Year', '2026-10-18', '10:00 AM', '01:00 PM', '10:00 AM - 01:00 PM', 0, 'SCHEDULED')");
            }

            // Check students
            rs = stmt.executeQuery("SELECT COUNT(*) FROM students");
            int existingCount = (rs.next()) ? rs.getInt(1) : 0;
            if (existingCount == 0) {
                // Seed students for all 4 years
                // 1. Third Year Students
                stmt.executeUpdate("INSERT INTO students (roll_no, name, division_name, academic_year, username, password) VALUES " +
                        "('A-01', 'Aarav Sharma', 'A', 'Third Year', 'aarav', 'pass123'), " +
                        "('A-02', 'Aditi Verma', 'A', 'Third Year', 'aditi', 'pass123'), " +
                        "('A-03', 'Ananya Patel', 'A', 'Third Year', 'ananya', 'pass123'), " +
                        "('A-04', 'Ayush Kulkarni', 'A', 'Third Year', 'ayush', 'pass123'), " +
                        "('A-05', 'Akash Deshmukh', 'A', 'Third Year', 'akash', 'pass123'), " +
                        "('A-06', 'Avani Joshi', 'A', 'Third Year', 'avani', 'pass123'), " +
                        "('B-01', 'Bhavik Mehta', 'B', 'Third Year', 'bhavik', 'pass123'), " +
                        "('B-02', 'Bhumika Rao', 'B', 'Third Year', 'bhumika', 'pass123'), " +
                        "('B-03', 'Bilal Khan', 'B', 'Third Year', 'bilal', 'pass123'), " +
                        "('B-04', 'Barkha Singh', 'B', 'Third Year', 'barkha', 'pass123'), " +
                        "('B-05', 'Bipin Patil', 'B', 'Third Year', 'bipin', 'pass123'), " +
                        "('B-06', 'Brinda Nair', 'B', 'Third Year', 'brinda', 'pass123'), " +
                        "('C-01', 'Chetan Shinde', 'C', 'Third Year', 'chetan', 'pass123'), " +
                        "('C-02', 'Charvi Gupta', 'C', 'Third Year', 'charvi', 'pass123'), " +
                        "('C-03', 'Chirag Jadhav', 'C', 'Third Year', 'chirag', 'pass123'), " +
                        "('C-04', 'Chhaya More', 'C', 'Third Year', 'chhaya', 'pass123'), " +
                        "('C-05', 'Chinmayee Rane', 'C', 'Third Year', 'chinmayee', 'pass123'), " +
                        "('C-06', 'Chandan Tiwari', 'C', 'Third Year', 'chandan', 'pass123')");

                // 2. First Year Students
                stmt.executeUpdate("INSERT INTO students (roll_no, name, division_name, academic_year, username, password) VALUES " +
                        "('FE-01', 'Rohan Gupta', 'A', 'First Year', 'fe01', 'pass123'), " +
                        "('FE-02', 'Saanvi Malhotra', 'A', 'First Year', 'fe02', 'pass123'), " +
                        "('FE-03', 'Tanmay Sen', 'A', 'First Year', 'fe03', 'pass123'), " +
                        "('FE-04', 'Ishita Dave', 'B', 'First Year', 'fe04', 'pass123'), " +
                        "('FE-05', 'Pranav Kadam', 'B', 'First Year', 'fe05', 'pass123'), " +
                        "('FE-06', 'Diya Chawla', 'B', 'First Year', 'fe06', 'pass123')");

                // 3. Second Year Students
                stmt.executeUpdate("INSERT INTO students (roll_no, name, division_name, academic_year, username, password) VALUES " +
                        "('SE-01', 'Manish Iyer', 'A', 'Second Year', 'se01', 'pass123'), " +
                        "('SE-02', 'Kavya Pillai', 'A', 'Second Year', 'se02', 'pass123'), " +
                        "('SE-03', 'Harsh Wardhan', 'A', 'Second Year', 'se03', 'pass123'), " +
                        "('SE-04', 'Ritu Solanki', 'B', 'Second Year', 'se04', 'pass123'), " +
                        "('SE-05', 'Varun Bhatia', 'B', 'Second Year', 'se05', 'pass123'), " +
                        "('SE-06', 'Simran Kaur', 'B', 'Second Year', 'se06', 'pass123')");

                // 4. Fourth Year Students
                stmt.executeUpdate("INSERT INTO students (roll_no, name, division_name, academic_year, username, password) VALUES " +
                        "('BE-01', 'Devansh Oberoi', 'A', 'Fourth Year', 'be01', 'pass123'), " +
                        "('BE-02', 'Nisha Bajaj', 'A', 'Fourth Year', 'be02', 'pass123'), " +
                        "('BE-03', 'Kunal Sengupta', 'A', 'Fourth Year', 'be03', 'pass123'), " +
                        "('BE-04', 'Pooja Agarwal', 'B', 'Fourth Year', 'be04', 'pass123'), " +
                        "('BE-05', 'Rahul Chhabra', 'B', 'Fourth Year', 'be05', 'pass123'), " +
                        "('BE-06', 'Zoya Sheikh', 'B', 'Fourth Year', 'be06', 'pass123')");
            } else {
                // If old database had 18 students without year, set them to Third Year and add other years if missing
                ResultSet yearRs = stmt.executeQuery("SELECT COUNT(*) FROM students WHERE academic_year = 'First Year'");
                if (yearRs.next() && yearRs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO students (roll_no, name, division_name, academic_year, username, password) VALUES " +
                            "('FE-01', 'Rohan Gupta', 'A', 'First Year', 'fe01', 'pass123'), " +
                            "('FE-02', 'Saanvi Malhotra', 'A', 'First Year', 'fe02', 'pass123'), " +
                            "('FE-03', 'Tanmay Sen', 'A', 'First Year', 'fe03', 'pass123'), " +
                            "('FE-04', 'Ishita Dave', 'B', 'First Year', 'fe04', 'pass123'), " +
                            "('FE-05', 'Pranav Kadam', 'B', 'First Year', 'fe05', 'pass123'), " +
                            "('FE-06', 'Diya Chawla', 'B', 'First Year', 'fe06', 'pass123')");
                }
            }
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Notice during seeding: " + e.getMessage());
        }
    }
}
