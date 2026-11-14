package com.examseating.server;

import com.examseating.algorithm.RuleBasedSeatingGenerator.SeatingResult;
import com.examseating.dao.*;
import com.examseating.db.DatabaseConnection;
import com.examseating.model.*;
import com.examseating.service.AuthService;
import com.examseating.service.SeatingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class HttpServerApp {
    private final int port;
    private HttpServer server;
    private final AuthService authService = new AuthService();
    private final SeatingService seatingService = new SeatingService();
    private final StudentDAO studentDAO = new StudentDAO();
    private final ClassroomDAO classroomDAO = new ClassroomDAO();
    private final DivisionDAO divisionDAO = new DivisionDAO();
    private final ExamDAO examDAO = new ExamDAO();

    public HttpServerApp(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Handler
        server.createContext("/api", new ApiHandler());

        // Static Files Handler
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("===============================================================");
        System.out.println("  ExamSeat Pro - Web Server Running Live!");
        System.out.println("  Port: " + port);
        System.out.println("  Open your web browser at: http://localhost:" + port);
        System.out.println("  Database: " + DatabaseConnection.getConnectionInfo());
        System.out.println("===============================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Enable CORS for flexibility
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PUT");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Auth-Token");

            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParams(query);

            try {
                if (path.equals("/api/auth/login") && "POST".equals(method)) {
                    handleLogin(exchange);
                } else if (path.equals("/api/auth/logout") && "POST".equals(method)) {
                    handleLogout(exchange);
                } else if (path.equals("/api/auth/me") && "GET".equals(method)) {
                    handleGetMe(exchange);
                } else if (path.equals("/api/dashboard/stats") && "GET".equals(method)) {
                    sendJsonResponse(exchange, 200, seatingService.getDashboardStats());
                } else if (path.equals("/api/students") && "GET".equals(method)) {
                    handleGetStudents(exchange, queryParams);
                } else if (path.equals("/api/students") && "POST".equals(method)) {
                    handleAddStudent(exchange);
                } else if (path.equals("/api/students/bulk") && "POST".equals(method)) {
                    handleBulkAddStudents(exchange);
                } else if (path.equals("/api/students/delete") && "POST".equals(method)) {
                    handleDeleteStudent(exchange);
                } else if (path.equals("/api/students/delete-all") && "POST".equals(method)) {
                    handleDeleteAllStudents(exchange);
                } else if (path.equals("/api/divisions") && "GET".equals(method)) {
                    sendJsonResponse(exchange, 200, divisionDAO.getAllDivisions());
                } else if (path.equals("/api/divisions") && "POST".equals(method)) {
                    handleAddDivision(exchange);
                } else if (path.equals("/api/divisions/delete") && "POST".equals(method)) {
                    handleDeleteDivision(exchange);
                } else if (path.equals("/api/classrooms") && "GET".equals(method)) {
                    sendJsonResponse(exchange, 200, classroomDAO.getAllClassrooms());
                } else if (path.equals("/api/classrooms") && "POST".equals(method)) {
                    handleAddClassroom(exchange);
                } else if (path.equals("/api/classrooms/delete") && "POST".equals(method)) {
                    handleDeleteClassroom(exchange);
                } else if (path.equals("/api/exams") && "GET".equals(method)) {
                    handleGetExams(exchange, queryParams);
                } else if (path.equals("/api/exams") && "POST".equals(method)) {
                    handleAddExam(exchange);
                } else if (path.equals("/api/exams/delete") && "POST".equals(method)) {
                    handleDeleteExam(exchange);
                } else if (path.equals("/api/exams/hold") && "POST".equals(method)) {
                    handleToggleHold(exchange);
                } else if (path.equals("/api/seating/generate") && "POST".equals(method)) {
                    handleGenerateSeating(exchange);
                } else if (path.equals("/api/seating/exam") && "GET".equals(method)) {
                    handleGetSeatingForExam(exchange, queryParams);
                } else if (path.equals("/api/student/seating") && "GET".equals(method)) {
                    handleGetStudentSeating(exchange, queryParams);
                } else if (path.equals("/api/excel/template") && "GET".equals(method)) {
                    handleExcelTemplate(exchange);
                } else if (path.equals("/api/excel/export/seating") && "GET".equals(method)) {
                    handleExcelExportSeating(exchange, queryParams);
                } else if (path.equals("/api/excel/import") && "POST".equals(method)) {
                    handleExcelImport(exchange);
                } else if (path.equals("/api/database/status") && "GET".equals(method)) {
                    Map<String, Object> status = new HashMap<>();
                    status.put("type", DatabaseConnection.getActiveDatabaseType());
                    status.put("info", DatabaseConnection.getConnectionInfo());
                    sendJsonResponse(exchange, 200, status);
                } else {
                    sendErrorResponse(exchange, 404, "Endpoint not found: " + path);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }

        private void handleLogin(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            String username = data.get("username");
            String password = data.get("password");
            String role = data.get("role");

            Login result = authService.authenticate(username, password, role);
            if (result.isAuthenticated()) {
                String token = authService.createSession(result.getUser());
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("message", result.getMessage());
                resp.put("token", token);
                resp.put("role", result.getUser().getRole());
                resp.put("user", result.getUser());
                sendJsonResponse(exchange, 200, resp);
            } else {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("message", result.getMessage());
                sendJsonResponse(exchange, 401, resp);
            }
        }

        private void handleLogout(HttpExchange exchange) throws IOException {
            String token = getTokenFromHeader(exchange);
            if (token != null) {
                authService.removeSession(token);
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Logged out successfully.");
            sendJsonResponse(exchange, 200, resp);
        }

        private void handleGetMe(HttpExchange exchange) throws IOException {
            String token = getTokenFromHeader(exchange);
            User user = authService.getUserByToken(token);
            if (user != null) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("authenticated", true);
                resp.put("user", user);
                sendJsonResponse(exchange, 200, resp);
            } else {
                Map<String, Object> resp = new HashMap<>();
                resp.put("authenticated", false);
                sendJsonResponse(exchange, 401, resp);
            }
        }

        private void handleGetStudents(HttpExchange exchange, Map<String, String> queryParams) throws IOException {
            String year = queryParams.get("year");
            if (year != null && !year.trim().isEmpty() && !"All Years".equalsIgnoreCase(year)) {
                sendJsonResponse(exchange, 200, studentDAO.getStudentsByYear(year));
            } else {
                sendJsonResponse(exchange, 200, studentDAO.getAllStudents());
            }
        }

        private void handleAddStudent(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);

            Student student = new Student();
            student.setRollNo(data.get("rollNo"));
            student.setName(data.get("name"));
            student.setDivisionName(data.get("divisionName"));
            student.setAcademicYear(data.get("academicYear") != null && !data.get("academicYear").isEmpty() ?
                    data.get("academicYear") : "First Year");
            student.setUsername(data.get("username") != null && !data.get("username").isEmpty() ?
                    data.get("username") : data.get("rollNo"));
            student.setPassword(data.get("password") != null && !data.get("password").isEmpty() ?
                    data.get("password") : "pass123");

            boolean success = studentDAO.addStudent(student);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", success);
            resp.put("message", success ? "Student registered successfully." : "Failed to register student. Roll number or username might already exist.");
            sendJsonResponse(exchange, success ? 200 : 400, resp);
        }

        private void handleBulkAddStudents(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            String bulkText = data.get("bulkData");
            String defaultYear = data.get("defaultYear") != null ? data.get("defaultYear") : "First Year";

            int addedCount = 0;
            int errorCount = 0;
            if (bulkText != null) {
                String[] lines = bulkText.split("\\r?\\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split("[,;\\t]");
                    if (parts.length >= 3) {
                        String year = defaultYear;
                        String roll = "";
                        String name = "";
                        String div = "";
                        String username = "";
                        String password = "pass123";

                        // Check if line begins with Year: e.g. "Third Year, A-01, Name, Div"
                        if (isYearName(parts[0].trim())) {
                            year = parts[0].trim();
                            roll = parts[1].trim();
                            name = parts[2].trim();
                            div = (parts.length >= 4) ? parts[3].trim().toUpperCase() : "A";
                            username = (parts.length >= 5 && !parts[4].trim().isEmpty()) ? parts[4].trim() : roll;
                            password = (parts.length >= 6 && !parts[5].trim().isEmpty()) ? parts[5].trim() : "pass123";
                        } else {
                            roll = parts[0].trim();
                            name = parts[1].trim();
                            div = parts[2].trim().toUpperCase();
                            if (parts.length >= 4 && isYearName(parts[3].trim())) {
                                year = parts[3].trim();
                                username = (parts.length >= 5 && !parts[4].trim().isEmpty()) ? parts[4].trim() : roll;
                                password = (parts.length >= 6 && !parts[5].trim().isEmpty()) ? parts[5].trim() : "pass123";
                            } else {
                                username = (parts.length >= 4 && !parts[3].trim().isEmpty()) ? parts[3].trim() : roll;
                                password = (parts.length >= 5 && !parts[4].trim().isEmpty()) ? parts[4].trim() : "pass123";
                            }
                        }

                        Student s = new Student(0, roll, name, div, year, username, password);
                        if (studentDAO.addStudent(s)) {
                            addedCount++;
                        } else {
                            errorCount++;
                        }
                    }
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", addedCount > 0);
            resp.put("addedCount", addedCount);
            resp.put("errorCount", errorCount);
            resp.put("message", "Bulk import completed: " + addedCount + " students added (" + errorCount + " duplicates/errors).");
            sendJsonResponse(exchange, 200, resp);
        }

        private boolean isYearName(String val) {
            String v = val.toLowerCase();
            return v.contains("year") || v.equals("fe") || v.equals("se") || v.equals("te") || v.equals("be");
        }

        private void handleDeleteStudent(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            int id = Integer.parseInt(data.getOrDefault("id", "0"));
            boolean ok = studentDAO.deleteStudent(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            resp.put("message", ok ? "Student removed." : "Could not delete student.");
            sendJsonResponse(exchange, ok ? 200 : 400, resp);
        }

        private void handleDeleteAllStudents(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            String year = data.get("year"); // optional: specific year or empty/null for all

            boolean ok;
            int countBefore = studentDAO.getTotalStudentCount();
            boolean isYearSpecific = year != null && !year.trim().isEmpty() && !"All Years".equalsIgnoreCase(year.trim());

            if (isYearSpecific) {
                ok = studentDAO.deleteStudentsByYear(year.trim());
            } else {
                ok = studentDAO.deleteAllStudents();
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            if (ok) {
                resp.put("message", isYearSpecific
                    ? "All " + year + " students deleted successfully."
                    : "All " + countBefore + " students deleted successfully.");
            } else {
                resp.put("message", "Failed to delete students. Please try again.");
            }
            sendJsonResponse(exchange, ok ? 200 : 400, resp);
        }

        private void handleAddDivision(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            String name = data.get("divisionName");
            String desc = data.get("description");

            Division d = new Division(0, name, desc);
            boolean ok = divisionDAO.addDivision(d);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            resp.put("message", ok ? "Division added successfully." : "Division already exists.");
            sendJsonResponse(exchange, ok ? 200 : 400, resp);
        }

        private void handleDeleteDivision(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            String name = data.get("divisionName");
            boolean ok = divisionDAO.deleteDivision(name);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            resp.put("message", ok ? "Division removed." : "Could not delete division.");
            sendJsonResponse(exchange, ok ? 200 : 400, resp);
        }

        private void handleAddClassroom(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            String roomNum = data.get("roomNumber");
            int benches = Integer.parseInt(data.getOrDefault("benches", "5"));
            int seatsPerBench = Integer.parseInt(data.getOrDefault("seatsPerBench", "2"));

            Classroom c = new Classroom(0, roomNum, benches, seatsPerBench);
            boolean ok = classroomDAO.addClassroom(c);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            resp.put("message", ok ? "Classroom added successfully (Capacity: " + c.getCapacity() + ")." : "Failed to add classroom.");
            sendJsonResponse(exchange, ok ? 200 : 400, resp);
        }

        private void handleDeleteClassroom(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            int id = Integer.parseInt(data.getOrDefault("id", "0"));
            boolean ok = classroomDAO.deleteClassroom(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            resp.put("message", ok ? "Classroom removed." : "Could not delete classroom.");
            sendJsonResponse(exchange, ok ? 200 : 400, resp);
        }

        private void handleGetExams(HttpExchange exchange, Map<String, String> queryParams) throws IOException {
            String year = queryParams.get("year");
            if (year != null && !year.trim().isEmpty() && !"All Years".equalsIgnoreCase(year)) {
                sendJsonResponse(exchange, 200, examDAO.getExamsByYear(year));
            } else {
                sendJsonResponse(exchange, 200, examDAO.getAllExams());
            }
        }

        private void handleAddExam(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            String examName = data.get("examName");
            String subject = data.get("subject");
            String academicYear = data.get("academicYear");
            String date = data.get("examDate");
            String startTime = data.get("startTime");
            String endTime = data.get("endTime");

            if (startTime == null || startTime.isEmpty()) startTime = "10:00 AM";
            if (endTime == null || endTime.isEmpty()) endTime = "01:00 PM";
            if (examName == null || examName.isEmpty()) examName = subject + " Examination";
            if (academicYear == null || academicYear.isEmpty()) academicYear = "All Years";

            Exam exam = new Exam(0, examName, subject, academicYear, date, startTime, endTime, false, "SCHEDULED");
            boolean ok = examDAO.addExam(exam);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            resp.put("message", ok ? "Exam timetable entry added successfully." : "Failed to add exam.");
            sendJsonResponse(exchange, ok ? 200 : 400, resp);
        }

        private void handleDeleteExam(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            int id = Integer.parseInt(data.getOrDefault("id", "0"));
            boolean ok = examDAO.deleteExam(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            resp.put("message", ok ? "Exam deleted." : "Could not delete exam.");
            sendJsonResponse(exchange, ok ? 200 : 400, resp);
        }

        private void handleToggleHold(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            int examId = Integer.parseInt(data.getOrDefault("examId", "0"));
            boolean hold = "true".equalsIgnoreCase(data.get("hold"));

            boolean ok = seatingService.toggleHoldStatus(examId, hold);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            resp.put("onHold", hold);
            resp.put("message", ok ? (hold ? "Exam arrangement placed ON HOLD (Locked)." : "Exam arrangement RELEASED (Unlocked).") : "Failed to update hold status.");
            sendJsonResponse(exchange, ok ? 200 : 400, resp);
        }

        private void handleGenerateSeating(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            int examId = Integer.parseInt(data.getOrDefault("examId", "0"));

            SeatingResult result = seatingService.generateAndSaveSeating(examId);
            sendJsonResponse(exchange, result.isSuccess() ? 200 : 400, result);
        }

        private void handleGetSeatingForExam(HttpExchange exchange, Map<String, String> queryParams) throws IOException {
            int examId = Integer.parseInt(queryParams.getOrDefault("examId", "0"));
            if (examId == 0) {
                Exam active = examDAO.getActiveExam();
                if (active != null) examId = active.getId();
            }

            List<SeatingArrangement> list = seatingService.getSeatingForExam(examId);
            sendJsonResponse(exchange, 200, list);
        }

        private void handleGetStudentSeating(HttpExchange exchange, Map<String, String> queryParams) throws IOException {
            String token = getTokenFromHeader(exchange);
            User user = authService.getUserByToken(token);

            int studentId = 0;
            if (user instanceof Student) {
                studentId = user.getId();
            } else if (queryParams.containsKey("studentId")) {
                studentId = Integer.parseInt(queryParams.get("studentId"));
            } else if (queryParams.containsKey("rollNo")) {
                Student s = studentDAO.getStudentByRollNo(queryParams.get("rollNo"));
                if (s != null) studentId = s.getId();
            }

            int examId = Integer.parseInt(queryParams.getOrDefault("examId", "0"));
            SeatingArrangement sa = seatingService.getStudentAllocation(studentId, examId);

            if (sa != null) {
                sendJsonResponse(exchange, 200, sa);
            } else {
                Map<String, Object> resp = new HashMap<>();
                resp.put("found", false);
                resp.put("message", "No seating arrangement found for this student. The exam seating may not have been generated yet.");
                sendJsonResponse(exchange, 404, resp);
            }
        }

        // ==========================================
        // EXCEL IMPORT & EXPORT HANDLERS (Feature 2)
        // ==========================================

        private void handleExcelTemplate(HttpExchange exchange) throws IOException {
            String csvContent = "\uFEFF" +
                    "Academic Year,Roll No,Student Name,Division,Username,Password\r\n" +
                    "First Year,FE-10,Aakash Verma,A,fe10,pass123\r\n" +
                    "First Year,FE-11,Neha Sharma,B,fe11,pass123\r\n" +
                    "Second Year,SE-10,Rhea Pillai,A,se10,pass123\r\n" +
                    "Second Year,SE-11,Karan Johar,B,se11,pass123\r\n" +
                    "Third Year,TE-10,Siddharth Rao,A,te10,pass123\r\n" +
                    "Third Year,TE-11,Pooja Hegde,B,te11,pass123\r\n" +
                    "Fourth Year,BE-10,Tanvi Joshi,A,be10,pass123\r\n" +
                    "Fourth Year,BE-11,Rohit Mehra,B,be11,pass123\r\n";

            byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"ExamSeat_Student_Template.csv\"");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private void handleExcelImport(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> data = JsonUtil.parseSimpleMap(body);
            String csvData = data.get("excelData");

            int addedCount = 0;
            int errorCount = 0;
            if (csvData != null) {
                String[] lines = csvData.split("\\r?\\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    // Skip header row if present
                    if (i == 0 && (line.toLowerCase().contains("academic") || line.toLowerCase().contains("roll"))) continue;

                    String[] parts = line.split("[,;\\t]");
                    if (parts.length >= 3) {
                        String year = "First Year";
                        String roll = "";
                        String name = "";
                        String div = "A";
                        String username = "";
                        String password = "pass123";

                        if (isYearName(parts[0].trim())) {
                            year = parts[0].trim();
                            roll = parts[1].trim();
                            name = parts[2].trim();
                            div = (parts.length >= 4) ? parts[3].trim().toUpperCase() : "A";
                            username = (parts.length >= 5 && !parts[4].trim().isEmpty()) ? parts[4].trim() : roll;
                            password = (parts.length >= 6 && !parts[5].trim().isEmpty()) ? parts[5].trim() : "pass123";
                        } else {
                            roll = parts[0].trim();
                            name = parts[1].trim();
                            div = parts[2].trim().toUpperCase();
                            if (parts.length >= 4 && isYearName(parts[3].trim())) {
                                year = parts[3].trim();
                                username = (parts.length >= 5 && !parts[4].trim().isEmpty()) ? parts[4].trim() : roll;
                                password = (parts.length >= 6 && !parts[5].trim().isEmpty()) ? parts[5].trim() : "pass123";
                            } else {
                                username = (parts.length >= 4 && !parts[3].trim().isEmpty()) ? parts[3].trim() : roll;
                                password = (parts.length >= 5 && !parts[4].trim().isEmpty()) ? parts[4].trim() : "pass123";
                            }
                        }

                        Student s = new Student(0, roll, name, div, year, username, password);
                        if (studentDAO.addStudent(s)) {
                            addedCount++;
                        } else {
                            errorCount++;
                        }
                    }
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", addedCount > 0);
            resp.put("addedCount", addedCount);
            resp.put("errorCount", errorCount);
            resp.put("message", "Excel Import Completed: " + addedCount + " students successfully added (" + errorCount + " duplicates/errors).");
            sendJsonResponse(exchange, 200, resp);
        }

        private void handleExcelExportSeating(HttpExchange exchange, Map<String, String> queryParams) throws IOException {
            int examId = Integer.parseInt(queryParams.getOrDefault("examId", "0"));
            if (examId == 0) {
                Exam active = examDAO.getActiveExam();
                if (active != null) examId = active.getId();
            }

            List<SeatingArrangement> list = seatingService.getSeatingForExam(examId);
            Exam exam = examDAO.getExamById(examId);
            String examTitle = (exam != null) ? exam.getExamName() : "Examination_" + examId;

            StringBuilder sb = new StringBuilder();
            // Add UTF-8 Byte Order Mark (BOM) so Excel opens UTF-8 natively with no encoding issues
            sb.append("\uFEFF");
            sb.append("EXAM SEAT PRO - OFFICIAL EXAMINATION SEATING ARRANGEMENT\r\n");
            sb.append("Exam Name:,").append(escapeCsv(examTitle)).append("\r\n");
            sb.append("Subject:,").append(escapeCsv(exam != null ? exam.getSubject() : "")).append("\r\n");
            sb.append("Academic Year:,").append(escapeCsv(exam != null ? exam.getAcademicYear() : "")).append("\r\n");
            sb.append("Date & Time:,").append(escapeCsv(exam != null ? (exam.getExamDate() + " (" + exam.getExamTime() + ")") : "")).append("\r\n");
            sb.append("Total Allocated:,").append(list.size()).append("\r\n\r\n");

            // CSV Columns Table Header
            sb.append("Sr No,Classroom,Bench No,Seat No,Academic Year,Roll No,Student Name,Division,Constraint Status,Student Signature\r\n");

            for (int i = 0; i < list.size(); i++) {
                SeatingArrangement sa = list.get(i);
                sb.append(i + 1).append(",");
                sb.append(escapeCsv(sa.getRoomNumber())).append(",");
                sb.append("Bench ").append(sa.getBenchNo()).append(",");
                sb.append("Seat ").append(sa.getSeatNo()).append(",");
                sb.append(escapeCsv(sa.getAcademicYear())).append(",");
                sb.append(escapeCsv(sa.getRollNo())).append(",");
                sb.append(escapeCsv(sa.getStudentName())).append(",");
                sb.append("Division ").append(sa.getDivisionName()).append(",");
                sb.append(sa.getConflictFlag() == 1 ? "Adjacent Warning" : "Separated").append(",");
                sb.append("__________________\r\n");
            }

            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            String safeFileName = examTitle.replaceAll("[^a-zA-Z0-9_.-]", "_") + "_Seating.csv";

            exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + safeFileName + "\"");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String escapeCsv(String val) {
            if (val == null) return "\"\"";
            String s = val.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
    }

    private class StaticFileHandler implements HttpHandler {
        private final File webRoot = new File("web");

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File targetFile = new File(webRoot, path.substring(1));
            // Security check against directory traversal
            if (!targetFile.getCanonicalPath().startsWith(webRoot.getCanonicalPath())) {
                sendErrorResponse(exchange, 403, "Forbidden");
                return;
            }

            if (!targetFile.exists() || targetFile.isDirectory()) {
                sendErrorResponse(exchange, 404, "File not found: " + path);
                return;
            }

            String mimeType = getMimeType(targetFile.getName());
            exchange.getResponseHeaders().set("Content-Type", mimeType);

            byte[] bytes = Files.readAllBytes(targetFile.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static String getMimeType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (fileName.endsWith(".css")) return "text/css; charset=UTF-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (fileName.endsWith(".json")) return "application/json; charset=UTF-8";
        if (fileName.endsWith(".csv")) return "text/csv; charset=UTF-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        if (fileName.endsWith(".ico")) return "image/x-icon";
        return "text/plain; charset=UTF-8";
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            return bos.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = JsonUtil.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> err = new HashMap<>();
        err.put("error", true);
        err.put("status", statusCode);
        err.put("message", message);
        sendJsonResponse(exchange, statusCode, err);
    }

    private static String getTokenFromHeader(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        String xAuth = exchange.getRequestHeaders().getFirst("X-Auth-Token");
        if (xAuth != null && !xAuth.trim().isEmpty()) {
            return xAuth.trim();
        }
        return null;
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            try {
                String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name());
                String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name()) : "";
                map.put(k, v);
            } catch (Exception ignored) {
            }
        }
        return map;
    }
}
