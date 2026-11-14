-- ========================================================
-- ExamSeat Pro - Database Schema (MySQL Compatible)
-- ========================================================

CREATE DATABASE IF NOT EXISTS exam_seating_db;
USE exam_seating_db;

-- 1. Admin Table
CREATE TABLE IF NOT EXISTS admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Division Table
CREATE TABLE IF NOT EXISTS divisions (
    division_id INT AUTO_INCREMENT PRIMARY KEY,
    division_name VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Student Table (With Academic Year)
CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    division_name VARCHAR(20) NOT NULL,
    academic_year VARCHAR(30) NOT NULL DEFAULT 'First Year',
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Classroom Table
CREATE TABLE IF NOT EXISTS classrooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(50) NOT NULL UNIQUE,
    benches INT NOT NULL,
    seats_per_bench INT NOT NULL,
    capacity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Exam Timetable Table (With Year, Start/End Time & Hold Flag)
CREATE TABLE IF NOT EXISTS exams (
    exam_id INT AUTO_INCREMENT PRIMARY KEY,
    exam_name VARCHAR(100) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    academic_year VARCHAR(30) NOT NULL DEFAULT 'All Years',
    exam_date VARCHAR(50) NOT NULL,
    start_time VARCHAR(30) NOT NULL,
    end_time VARCHAR(30) NOT NULL,
    exam_time VARCHAR(50) NOT NULL,
    is_on_hold INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Seating Allocation Table
CREATE TABLE IF NOT EXISTS seating_allocations (
    allocation_id INT AUTO_INCREMENT PRIMARY KEY,
    exam_id INT NOT NULL,
    student_id INT NOT NULL,
    room_id INT NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    bench_no INT NOT NULL,
    seat_no INT NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    roll_no VARCHAR(50) NOT NULL,
    division_name VARCHAR(20) NOT NULL,
    academic_year VARCHAR(30) NOT NULL DEFAULT 'First Year',
    exam_name VARCHAR(100),
    conflict_flag INT DEFAULT 0,
    conflict_reason VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_exam_student UNIQUE (exam_id, student_id),
    CONSTRAINT uk_exam_seat UNIQUE (exam_id, room_id, bench_no, seat_no)
);
