package com.pathfinder.db;

import java.sql.*;

public class DbInit {
    public static void main(String[] args) {
        String sql = "CREATE TABLE IF NOT EXISTS user_profiles_v2 (" +
                     "email VARCHAR(100) PRIMARY KEY," +
                     "class_level VARCHAR(50)," +
                     "stream VARCHAR(50)," +
                     "state VARCHAR(100)," +
                     "district VARCHAR(100)," +
                     "institution VARCHAR(100)," +
                     "interests TEXT," +
                     "extra_info_json TEXT," +
                     "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                     "FOREIGN KEY (email) REFERENCES users(email) ON DELETE CASCADE" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";
        String proSql = "CREATE TABLE IF NOT EXISTS professional_profiles_v2 (" +
                        "email VARCHAR(100) PRIMARY KEY," +
                        "domain VARCHAR(100)," +
                        "current_role VARCHAR(100)," +
                        "company VARCHAR(100)," +
                        "experience VARCHAR(50)," +
                        "skills TEXT," +
                        "interests TEXT," +
                        "state VARCHAR(100)," +
                        "district VARCHAR(100)," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (email) REFERENCES users(email) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement()) {
            
            s.execute(sql);
            s.execute(proSql);
            
            System.out.println("✓ Tables ready (user_profiles_v2, professional_profiles_v2).");
        } catch (SQLException e) {
            System.err.println("Database init failed: " + e.getMessage());
        }
    }
}
