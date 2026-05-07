package com.pathfinder.db;

import com.pathfinder.model.User;
import java.sql.*;
import java.util.*;

public class UserDAO {

    // ── Register ──────────────────────────────────────────────────
    public static int register(String name, String email, String password, String role)
            throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, role, created_at) VALUES (?,?,?,?,NOW())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, role);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // ── Login ─────────────────────────────────────────────────────
    public static User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verify(password, storedHash)) {
                        User u = new User();
                        u.setId(rs.getInt("id"));
                        u.setName(rs.getString("name"));
                        u.setEmail(rs.getString("email"));
                        u.setRole(rs.getString("role"));
                        return u;
                    }
                }
            }
        }
        return null;
    }

    // ── Email exists ───────────────────────────────────────────────
    public static boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── Save student profile ───────────────────────────────────────
    public static void saveStudentProfile(int userId, String standard, String stream,
            String state, String district, String institution,
            String interests, String marks) throws SQLException {
        String sql = "INSERT INTO student_profiles (user_id, standard_level, stream, state, district, institution, interests, marks_json) "
                   + "VALUES (?,?,?,?,?,?,?,?) "
                   + "ON DUPLICATE KEY UPDATE standard_level=VALUES(standard_level), stream=VALUES(stream), "
                   + "state=VALUES(state), district=VALUES(district), institution=VALUES(institution), "
                   + "interests=VALUES(interests), marks_json=VALUES(marks_json), updated_at=NOW()";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, standard);
            ps.setString(3, stream);
            ps.setString(4, state);
            ps.setString(5, district);
            ps.setString(6, institution);
            ps.setString(7, interests);
            ps.setString(8, marks);
            ps.executeUpdate();
        }
    }

    // ── Save professional profile ─────────────────────────────────
    public static void saveProfessionalProfile(int userId, String domain,
            String experience, String skills, String interests,
            String currentRole, String company) throws SQLException {
        String sql = "INSERT INTO professional_profiles (user_id, domain, years_experience, skills, interests, current_role, company) "
                   + "VALUES (?,?,?,?,?,?,?) "
                   + "ON DUPLICATE KEY UPDATE domain=VALUES(domain), years_experience=VALUES(years_experience), "
                   + "skills=VALUES(skills), interests=VALUES(interests), current_role=VALUES(current_role), "
                   + "company=VALUES(company), updated_at=NOW()";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, domain);
            ps.setString(3, experience);
            ps.setString(4, skills);
            ps.setString(5, interests);
            ps.setString(6, currentRole);
            ps.setString(7, company);
            ps.executeUpdate();
        }
    }

    // ── Save AI result ────────────────────────────────────────────
    public static void saveResult(int userId, String type, String resultJson) throws SQLException {
        String sql = "INSERT INTO ai_results (user_id, result_type, result_json, created_at) VALUES (?,?,?,NOW())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, resultJson);
            ps.executeUpdate();
        }
    }

    // ── Get latest result ─────────────────────────────────────────
    public static String getLatestResult(int userId, String type) throws SQLException {
        String sql = "SELECT result_json FROM ai_results WHERE user_id=? AND result_type=? ORDER BY created_at DESC LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("result_json");
            }
        }
        return null;
    }

    // ── Save Unified Profile (v2) ──────────────────────────────────
    public static void saveUnifiedProfile(String email, String standard, String stream,
            String state, String district, String institution, String interests, String extraJson) throws SQLException {
        String sql = "INSERT INTO user_profiles_v2 (email, class_level, stream, state, district, institution, interests, extra_info_json) "
                   + "VALUES (?,?,?,?,?,?,?,?) "
                   + "ON DUPLICATE KEY UPDATE class_level=VALUES(class_level), stream=VALUES(stream), "
                   + "state=VALUES(state), district=VALUES(district), institution=VALUES(institution), "
                   + "interests=VALUES(interests), extra_info_json=VALUES(extra_info_json)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, standard);
            ps.setString(3, stream);
            ps.setString(4, state);
            ps.setString(5, district);
            ps.setString(6, institution);
            ps.setString(7, interests);
            ps.setString(8, extraJson);
            ps.executeUpdate();
        }
    }

    // ── Get Unified Profile (v2) ──────────────────────────────────
    public static Map<String, String> getUnifiedProfile(String email) throws SQLException {
        String sql = "SELECT * FROM user_profiles_v2 WHERE email = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> p = new HashMap<>();
                    p.put("class_level", rs.getString("class_level"));
                    p.put("stream", rs.getString("stream"));
                    p.put("state", rs.getString("state"));
                    p.put("district", rs.getString("district"));
                    p.put("institution", rs.getString("institution"));
                    p.put("interests", rs.getString("interests"));
                    p.put("extra_info_json", rs.getString("extra_info_json"));
                    return p;
                }
            }
        }
        return null;
    }

    public static void saveProfessionalProfileV2(String email, String domain, String role, String company, 
            String exp, String skills, String interests, String state, String district) throws SQLException {
        String sql = "INSERT INTO professional_profiles_v2 (email, domain, current_role, company, experience, skills, interests, state, district) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE domain=VALUES(domain), current_role=VALUES(current_role), " +
                     "company=VALUES(company), experience=VALUES(experience), skills=VALUES(skills), " +
                     "interests=VALUES(interests), state=VALUES(state), district=VALUES(district)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, domain);
            ps.setString(3, role);
            ps.setString(4, company);
            ps.setString(5, exp);
            ps.setString(6, skills);
            ps.setString(7, interests);
            ps.setString(8, state);
            ps.setString(9, district);
            ps.executeUpdate();
        }
    }

    public static Map<String, String> getProfessionalProfileV2(String email) throws SQLException {
        String sql = "SELECT * FROM professional_profiles_v2 WHERE email = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> p = new HashMap<>();
                    p.put("email", rs.getString("email"));
                    p.put("domain", rs.getString("domain"));
                    p.put("current_role", rs.getString("current_role"));
                    p.put("company", rs.getString("company"));
                    p.put("experience", rs.getString("experience"));
                    p.put("skills", rs.getString("skills"));
                    p.put("interests", rs.getString("interests"));
                    p.put("state", rs.getString("state"));
                    p.put("district", rs.getString("district"));
                    return p;
                }
            }
        }
        return null;
    }
}
