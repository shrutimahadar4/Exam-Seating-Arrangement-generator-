package com.examseating.service;

import com.examseating.dao.AdminDAO;
import com.examseating.dao.StudentDAO;
import com.examseating.model.Admin;
import com.examseating.model.Login;
import com.examseating.model.Student;
import com.examseating.model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final AdminDAO adminDAO = new AdminDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    // In-memory token-to-user session cache
    private static final Map<String, User> sessions = new ConcurrentHashMap<>();

    public Login authenticate(String usernameOrRoll, String password, String role) {
        Login response = new Login(usernameOrRoll, password, role);

        if ("ADMIN".equalsIgnoreCase(role)) {
            Admin admin = adminDAO.authenticate(usernameOrRoll, password);
            if (admin != null) {
                response.setAuthenticated(true);
                response.setUser(admin);
                response.setMessage("Admin authenticated successfully.");
            } else {
                response.setAuthenticated(false);
                response.setMessage("Invalid admin username or password.");
            }
        } else {
            Student student = studentDAO.authenticate(usernameOrRoll, password);
            if (student != null) {
                response.setAuthenticated(true);
                response.setUser(student);
                response.setMessage("Student authenticated successfully.");
            } else {
                response.setAuthenticated(false);
                response.setMessage("Invalid roll number/username or password.");
            }
        }

        return response;
    }

    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public User getUserByToken(String token) {
        if (token == null) return null;
        return sessions.get(token);
    }

    public void removeSession(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
