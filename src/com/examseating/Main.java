package com.examseating;

import com.examseating.db.DatabaseConnection;
import com.examseating.db.DatabaseInitializer;
import com.examseating.server.HttpServerApp;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;

/**
 * Main application entry point for Exam Sitting Arrangement Generator
 */
public class Main {
    public static final String APP_NAME = "Exam Sitting Arrangement Generator";
    public static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("       " + APP_NAME + " (v" + VERSION + ")");
        System.out.println("       Developed with Java, JDBC, HTML, CSS & Rule-Based Seating Logic");
        System.out.println("=======================================================================");

        try {
            // Step 1: Initialize Database Connection & Tables
            System.out.println("[Main] Initializing database and verifying schema...");
            DatabaseInitializer.initializeDatabase();

            // Step 2: Read Server Port from configuration
            int port = 8080;
            try {
                File propFile = new File("config/db.properties");
                if (propFile.exists()) {
                    Properties props = new Properties();
                    try (FileInputStream fis = new FileInputStream(propFile)) {
                        props.load(fis);
                        port = Integer.parseInt(props.getProperty("server.port", "8080").trim());
                    }
                }
            } catch (Exception e) {
                System.out.println("[Main] Using default port 8080.");
            }

            // Step 3: Launch Web Server
            HttpServerApp webServer = new HttpServerApp(port);
            webServer.start();

            String appUrl = "http://localhost:" + port;
            System.out.println();
            System.out.println(">>> SUCCESS: Application is running live!");
            System.out.println(">>> Web Portal URL: " + appUrl);
            System.out.println(">>> Default Admin Login:   username: 'admin'  | password: 'admin123'");
            System.out.println(">>> Default Student Login: username: 'A-01'   | password: 'pass123'");
            System.out.println();

            // Try to open default browser if supported
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(appUrl));
                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            System.err.println("[Main] Fatal Error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
