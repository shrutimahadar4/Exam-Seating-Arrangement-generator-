# Exam-Seating-Arrangement-generator-
An Exam Seating Arrangement Generator that automatically assigns students to exam halls and seats based on their details. It reduces manual work, saves time, avoids duplicate seat allocation, and generates organized seating plans for smooth and efficient examination management.

# 🎓 ExamSeat Pro — Professional Exam Seating Arrangement Generator

**ExamSeat Pro** is an upgraded, automated, rule-based examination seating management system developed with **Java**, **JDBC**, **MySQL / SQLite**, **HTML5**, and **CSS3**.

It completely replaces manual spreadsheet work with an auditable **deterministic rule-based algorithm** (strictly **zero AI / ML**), featuring **Year-Wise Student Management**, **Excel Import/Export**, **Multi-Slot Exam Timetables**, **Hold & Release Protection**, and a **Professional University Dashboard**.

---

## 🌟 Upgraded Features

### 1. 🎓 Year-Wise Student Management
- Separate tracking and filtering for **First Year (FE)**, **Second Year (SE)**, **Third Year (TE)**, and **Fourth Year (BE)**.
- Filter students by academic year in the admin roster.
- Year-wise seating allocation ensures exams can target specific student cohorts or combined years.

### 2. 📊 Excel Import & Export (`.xlsx` / `.csv`)
- **Download Sample Template:** Download a ready-to-use spreadsheet template (`ExamSeat_Student_Template.csv`) with 1 click.
- **Import from Excel:** Upload or paste student rosters containing Academic Year, Roll Number, Name, Division, and Credentials.
- **Export Seating Plan:** Export the complete final seating arrangement as an Excel-compatible spreadsheet with classroom, bench, seat, year, roll number, and student signature columns.

### 3. 📅 Exam Timetable Management
- Schedule multiple examinations with **Exam Title**, **Subject**, **Target Academic Year**, **Date**, **Start Time**, and **End Time**.
- Master timetable dashboard displays all scheduled slots and their current allocation statuses.

### 4. 🔒 Hold & Release Feature
- Put an exam arrangement **ON HOLD** (Locked / Frozen) to protect it from accidental regeneration or overrides.
- Visual **`[🔒 ON HOLD]`** status badges throughout the dashboard.
- Safe **RELEASE** toggle when the administrator explicitly wishes to re-run the algorithm.

### 5. 🏫 Professional Dashboard & Visual Seat Locator
- Real-time statistics: Total students, classroom capacity, active timetable slots, and year-by-year enrollment pills.
- Interactive **2D Classroom Floor Map** with color-coded division badges and academic year tags.
- Student portal with official **Examination Seating Pass / Hall Ticket** and a highlighted golden seat locator mini-map.

---

## 🚀 Quick Start (One-Click Launch)

### Prerequisites
- **Java JDK / JRE 8 or higher** (Fully tested and verified on Java 8 through Java 27)

### To Run the Application:
Simply double-click **`start.bat`** or run:
```cmd
run.bat
```
The application web server starts on port **`8080`** and opens your browser at:
👉 **`http://localhost:8080`**

*(To recompile anytime after source code edits, run `compile.bat` or `powershell -ExecutionPolicy Bypass -File build.ps1`).*

---

## 🔑 Default Login Credentials

| Role | Username / Identifier | Password | Access / Features |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | Master Admin Console: Year management, Timetable, Excel tools, Hold/Release, Seating Generator |
| **Third Year Student** | `A-01` | `pass123` | Student portal: View assigned Room 101, Bench 1, Seat 1, Print Hall Ticket |
| **First Year Student** | `FE-01` | `pass123` | First Year Student Portal |
| **Second Year Student** | `SE-01` | `pass123` | Second Year Student Portal |
| **Fourth Year Student** | `BE-01` | `pass123` | Fourth Year Student Portal |

---

## 🗄️ Database & Resilient JDBC Architecture

The system supports **MySQL** with **Smart Auto-Fallback to embedded SQLite**:
- If MySQL is running on `localhost:3306`, it connects via `mysql-connector-j`.
- If MySQL is not running or credentials aren't configured yet, it boots seamlessly into `exam_seating.db` via `sqlite-jdbc` so the project **always runs without setup errors or connection crashes**.

### Configuration: `config/db.properties`
```properties
db.type=mysql
db.auto_fallback=true

db.mysql.host=localhost
db.mysql.port=3306
db.mysql.database=exam_seating_db
db.mysql.username=root
db.mysql.password=

db.sqlite.file=exam_seating.db
server.port=8080
```

---

## 📁 Project Architecture & File Structure

```
mini project/
├── lib/                             # JDBC Drivers & Dependencies
│   ├── mysql-connector-j.jar        # MySQL JDBC Driver (v8.4.0)
│   ├── sqlite-jdbc.jar              # SQLite JDBC Driver (v3.45.1)
│   ├── slf4j-api.jar                # Logging API
│   └── slf4j-simple.jar             # Logging Implementation
├── config/
│   └── db.properties                # Database & Server configuration
├── sql/
│   ├── schema.sql                   # MySQL database DDL schema (with Years & Timetable)
│   └── sample_data.sql              # Initial 4-year student records & exam timetable
├── web/                             # Frontend User Interface
│   ├── css/
│   │   └── style.css                # Modern responsive UI & printable styles
│   ├── js/
│   │   ├── app.js                   # Shared utilities, auth tokens, Excel triggers
│   │   ├── admin.js                 # Admin dashboard interactive controller
│   │   └── student.js               # Student hall ticket & seat locator controller
│   ├── index.html                   # Unified login & entry portal
│   ├── admin.html                   # Admin Dashboard (All 5 modules)
│   └── student.html                 # Student Examination Seating Portal
├── src/                             # Java Backend Codebase
│   └── com/examseating/
│       ├── Main.java                # Application bootstrap & entry point
│       ├── model/                   # OOP Data Models
│       │   ├── User.java            # Base User entity
│       │   ├── Admin.java           # Admin model (extends User)
│       │   ├── Student.java         # Student model (with academicYear)
│       │   ├── Division.java        # Division model
│       │   ├── Classroom.java       # Classroom model
│       │   ├── Bench.java           # Bench model
│       │   ├── Seat.java            # Seat model
│       │   ├── Exam.java            # Exam model (with Timetable & Hold fields)
│       │   ├── SeatingArrangement.java # Allocation records model
│       │   └── Login.java           # Login request & session model
│       ├── db/
│       │   ├── DatabaseConnection.java  # JDBC connection manager
│       │   └── DatabaseInitializer.java # Schema creation & auto-seeding
│       ├── dao/                     # Data Access Objects (CRUD via JDBC)
│       │   ├── AdminDAO.java
│       │   ├── StudentDAO.java      # Year-wise queries
│       │   ├── DivisionDAO.java
│       │   ├── ClassroomDAO.java
│       │   ├── ExamDAO.java         # Timetable & Hold management
│       │   └── SeatingDAO.java      # Allocation persistence & export
│       ├── algorithm/
│       │   └── RuleBasedSeatingGenerator.java # Rule-Based Algorithm with Hold checks
│       ├── server/
│       │   ├── HttpServerApp.java   # Embedded HTTP Server & REST API + Excel endpoints
│       │   └── JsonUtil.java        # Zero-dependency JSON parser & serializer
│       └── service/
│           ├── AuthService.java     # Authentication service
│           └── SeatingService.java  # Business logic orchestrator
├── build.ps1                        # PowerShell build script
├── compile.bat                      # Windows compile batch script
├── run.bat                          # Windows execution batch script
├── start.bat                        # One-click compile & run launcher
├── test_api.ps1                     # Automated end-to-end API test suite
├── README.md                        # Project documentation
└── PROJECT_REPORT_SYNOPSIS.md       # Formal college project report
```

---

## 🧠 Rule-Based Seating Algorithm Flow

```
[Start Allocation]
   │
   ├─► 1. Check Hold Status:
   │      If exam.isOnHold() == true:
   │         ABORT with message: "Arrangement is ON HOLD. Release hold to regenerate."
   │
   ├─► 2. Filter Cohort:
   │      Select students matching target Academic Year (FE, SE, TE, BE, or Combined).
   │
   ├─► 3. Validate Capacity:
   │      Total Capacity = ∑(Classrooms × Benches × SeatsPerBench).
   │      If Cohort Count > Total Capacity:
   │         ABORT with "Insufficient Capacity" message and required seat count.
   │
   ├─► 4. Partition & Interleave:
   │      Group students into division queues (Div A, Div B, Div C).
   │      Rotate divisions across each bench.
   │      Check horizontal neighbour (left seat) and vertical neighbour (front seat).
   │
   └─► 5. Persist: Commit seating matrix to MySQL/SQLite via JDBC batch transaction.
```

---

## 👨‍🏫 Viva Questions & Answers

1. **Q: How does the Hold & Release feature protect examination arrangements?**
   - **A:** Once an arrangement is finalized and published, the admin clicks "Put on Hold". The backend sets `is_on_hold = 1` in the database. Any regeneration attempts are blocked by the rule engine until the administrator explicitly clicks "Release Hold".

2. **Q: How does Excel Import & Export operate without heavy external dependencies?**
   - **A:** The system generates standard RFC 4180 CSV streams encoded with a UTF-8 Byte Order Mark (`\uFEFF`). When opened, Microsoft Excel immediately formats it with clean tabular columns and no encoding errors. Import supports both CSV and tab-delimited spreadsheets.

3. **Q: How does year-wise student management benefit examination planning?**
   - **A:** In universities, different academic years take different exams on different dates and times (e.g., Third Year takes Java at 10 AM, First Year takes Mathematics at 2 PM). Year-wise management allows independent timetable scheduling and prevents year-level student collisions.
