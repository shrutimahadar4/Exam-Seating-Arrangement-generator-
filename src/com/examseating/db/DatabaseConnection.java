package com.examseating.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * JDBC Database Connection Manager
 * Supports MySQL with graceful SQLite fallback for instant zero-config execution
 */
public class DatabaseConnection {
    private static Properties properties = new Properties();
    private static String activeDbType = "unknown";
    private static String connectionInfo = "";
    private static boolean initialized = false;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try {
            File propFile = new File("config/db.properties");
            if (propFile.exists()) {
                try (InputStream is = new FileInputStream(propFile)) {
                    properties.load(is);
                }
            } else {
                // Default fallback configuration
                properties.setProperty("db.type", "mysql");
                properties.setProperty("db.auto_fallback", "true");
                properties.setProperty("db.mysql.host", "localhost");
                properties.setProperty("db.mysql.port", "3306");
                properties.setProperty("db.mysql.database", "exam_seating_db");
                properties.setProperty("db.mysql.username", "root");
                properties.setProperty("db.mysql.password", "");
                properties.setProperty("db.sqlite.file", "exam_seating.db");
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load config/db.properties, using defaults. Error: " + e.getMessage());
        }
    }

    /**
     * Obtains a live JDBC Connection
     */
    public static synchronized Connection getConnection() throws SQLException {
        String configuredType = properties.getProperty("db.type", "mysql").trim().toLowerCase();
        boolean autoFallback = "true".equalsIgnoreCase(properties.getProperty("db.auto_fallback", "true"));

        // If explicitly SQLite or already fell back to SQLite
        if ("sqlite".equals(configuredType) || "sqlite".equals(activeDbType)) {
            return getSqliteConnection();
        }

        // Try MySQL first
        try {
            Connection conn = getMySqlConnection();
            activeDbType = "mysql";
            connectionInfo = "MySQL (" + properties.getProperty("db.mysql.host", "localhost") + ":"
                    + properties.getProperty("db.mysql.port", "3306") + "/"
                    + properties.getProperty("db.mysql.database", "exam_seating_db") + ")";
            return conn;
        } catch (SQLException e) {
            if (autoFallback) {
                System.out.println("(!) MySQL server not reachable on localhost:3306. " +
                        "Switching smoothly to embedded SQLite database to ensure the project runs without errors.");
                activeDbType = "sqlite";
                return getSqliteConnection();
            } else {
                throw e;
            }
        }
    }

    private static Connection getMySqlConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found in classpath.", e);
        }

        String host = properties.getProperty("db.mysql.host", "localhost");
        String port = properties.getProperty("db.mysql.port", "3306");
        String dbName = properties.getProperty("db.mysql.database", "exam_seating_db");
        String user = properties.getProperty("db.mysql.username", "root");
        String pass = properties.getProperty("db.mysql.password", "");

        // Auto create database if it doesn't exist yet on MySQL
        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return DriverManager.getConnection(url, user, pass);
    }

    private static Connection getSqliteConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver (org.sqlite.JDBC) not found in classpath.", e);
        }

        String dbFile = properties.getProperty("db.sqlite.file", "exam_seating.db");
        String url = "jdbc:sqlite:" + dbFile;
        activeDbType = "sqlite";
        connectionInfo = "SQLite Local DB (" + dbFile + ")";
        return DriverManager.getConnection(url);
    }

    public static String getActiveDatabaseType() {
        return activeDbType;
    }

    public static String getConnectionInfo() {
        return connectionInfo;
    }

    public static Properties getProperties() {
        return properties;
    }
}
