-- ========================================================
-- ExamSeat Pro - Sample Data
-- ========================================================

USE exam_seating_db;

-- Insert Admin (Username: admin, Password: admin123)
INSERT INTO admins (username, password, full_name)
VALUES ('admin', 'admin123', 'Head Exam Administrator')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Insert Divisions
INSERT INTO divisions (division_name, description) VALUES
('A', 'Computer Engineering - Division A'),
('B', 'Information Technology - Division B'),
('C', 'Electronics & Telecom - Division C')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- Insert Classrooms
INSERT INTO classrooms (room_number, benches, seats_per_bench, capacity) VALUES
('Room 101', 5, 2, 10),
('Room 102', 5, 2, 10),
('Room 204', 6, 2, 12)
ON DUPLICATE KEY UPDATE benches=VALUES(benches), seats_per_bench=VALUES(seats_per_bench), capacity=VALUES(capacity);

-- Insert Exam Timetable
INSERT INTO exams (exam_name, subject, academic_year, exam_date, start_time, end_time, exam_time, is_on_hold, status) VALUES
('Winter End-Semester Exam', 'Advanced Java & Database Management', 'Third Year', '2026-10-15', '10:00 AM', '01:00 PM', '10:00 AM - 01:00 PM', 0, 'SCHEDULED'),
('Mid-Term Assessment', 'Data Structures & Algorithms', 'Second Year', '2026-10-16', '10:00 AM', '01:00 PM', '10:00 AM - 01:00 PM', 0, 'SCHEDULED'),
('University Foundation Test', 'Engineering Mathematics I', 'First Year', '2026-10-17', '02:00 PM', '05:00 PM', '02:00 PM - 05:00 PM', 0, 'SCHEDULED'),
('Final Year Project Defense', 'Cloud Computing & DevOps', 'Fourth Year', '2026-10-18', '10:00 AM', '01:00 PM', '10:00 AM - 01:00 PM', 0, 'SCHEDULED')
ON DUPLICATE KEY UPDATE exam_date=VALUES(exam_date);

-- Insert Third Year Students (Division A, B, C)
INSERT INTO students (roll_no, name, division_name, academic_year, username, password) VALUES
('A-01', 'Aarav Sharma', 'A', 'Third Year', 'aarav', 'pass123'),
('A-02', 'Aditi Verma', 'A', 'Third Year', 'aditi', 'pass123'),
('A-03', 'Ananya Patel', 'A', 'Third Year', 'ananya', 'pass123'),
('A-04', 'Ayush Kulkarni', 'A', 'Third Year', 'ayush', 'pass123'),
('A-05', 'Akash Deshmukh', 'A', 'Third Year', 'akash', 'pass123'),
('A-06', 'Avani Joshi', 'A', 'Third Year', 'avani', 'pass123'),
('B-01', 'Bhavik Mehta', 'B', 'Third Year', 'bhavik', 'pass123'),
('B-02', 'Bhumika Rao', 'B', 'Third Year', 'bhumika', 'pass123'),
('B-03', 'Bilal Khan', 'B', 'Third Year', 'bilal', 'pass123'),
('B-04', 'Barkha Singh', 'B', 'Third Year', 'barkha', 'pass123'),
('B-05', 'Bipin Patil', 'B', 'Third Year', 'bipin', 'pass123'),
('B-06', 'Brinda Nair', 'B', 'Third Year', 'brinda', 'pass123'),
('C-01', 'Chetan Shinde', 'C', 'Third Year', 'chetan', 'pass123'),
('C-02', 'Charvi Gupta', 'C', 'Third Year', 'charvi', 'pass123'),
('C-03', 'Chirag Jadhav', 'C', 'Third Year', 'chirag', 'pass123'),
('C-04', 'Chhaya More', 'C', 'Third Year', 'chhaya', 'pass123'),
('C-05', 'Chinmayee Rane', 'C', 'Third Year', 'chinmayee', 'pass123'),
('C-06', 'Chandan Tiwari', 'C', 'Third Year', 'chandan', 'pass123')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Insert First Year Students
INSERT INTO students (roll_no, name, division_name, academic_year, username, password) VALUES
('FE-01', 'Rohan Gupta', 'A', 'First Year', 'fe01', 'pass123'),
('FE-02', 'Saanvi Malhotra', 'A', 'First Year', 'fe02', 'pass123'),
('FE-03', 'Tanmay Sen', 'A', 'First Year', 'fe03', 'pass123'),
('FE-04', 'Ishita Dave', 'B', 'First Year', 'fe04', 'pass123'),
('FE-05', 'Pranav Kadam', 'B', 'First Year', 'fe05', 'pass123'),
('FE-06', 'Diya Chawla', 'B', 'First Year', 'fe06', 'pass123')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Insert Second Year Students
INSERT INTO students (roll_no, name, division_name, academic_year, username, password) VALUES
('SE-01', 'Manish Iyer', 'A', 'Second Year', 'se01', 'pass123'),
('SE-02', 'Kavya Pillai', 'A', 'Second Year', 'se02', 'pass123'),
('SE-03', 'Harsh Wardhan', 'A', 'Second Year', 'se03', 'pass123'),
('SE-04', 'Ritu Solanki', 'B', 'Second Year', 'se04', 'pass123'),
('SE-05', 'Varun Bhatia', 'B', 'Second Year', 'se05', 'pass123'),
('SE-06', 'Simran Kaur', 'B', 'Second Year', 'se06', 'pass123')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Insert Fourth Year Students
INSERT INTO students (roll_no, name, division_name, academic_year, username, password) VALUES
('BE-01', 'Devansh Oberoi', 'A', 'Fourth Year', 'be01', 'pass123'),
('BE-02', 'Nisha Bajaj', 'A', 'Fourth Year', 'be02', 'pass123'),
('BE-03', 'Kunal Sengupta', 'A', 'Fourth Year', 'be03', 'pass123'),
('BE-04', 'Pooja Agarwal', 'B', 'Fourth Year', 'be04', 'pass123'),
('BE-05', 'Rahul Chhabra', 'B', 'Fourth Year', 'be05', 'pass123'),
('BE-06', 'Zoya Sheikh', 'B', 'Fourth Year', 'be06', 'pass123')
ON DUPLICATE KEY UPDATE name=VALUES(name);
