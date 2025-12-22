package server.database;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util. Base64;

public class UserDAO {
    private Connection conn;
    
    public UserDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }
    
    // ========== MÃ HÓA PASSWORD ==========
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password. getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    // ========== ĐĂNG KÝ USER MỚI ==========
    public boolean register(String username, String password, String email) {
        String sql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt. setString(3, email);
            
            int result = pstmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            System.err. println("Lỗi đăng ký: " + e.getMessage());
            return false;
        }
    }
    
    // ========== XÁC THỰC ĐĂNG NHẬP ==========
    public int authenticate(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password_hash = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt. setString(2, hashPassword(password));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id"); // Trả về user_id nếu đúng
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xác thực: " + e.getMessage());
        }
        return -1; // Trả về -1 nếu sai
    }
    
    // ========== LẤY THÔNG TIN USER ==========
    public String[] getUserByUsername(String username) {
        String sql = "SELECT id, username, email FROM users WHERE username = ? ";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new String[]{
                    String.valueOf(rs. getInt("id")),
                    rs.getString("username"),
                    rs. getString("email")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // ========== LẤY USER ID TỪ EMAIL ==========
    public int getUserIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        
        try (PreparedStatement pstmt = conn. prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt. executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e. printStackTrace();
        }
        return -1;
    }
    
    // ========== KIỂM TRA USERNAME ĐÃ TỒN TẠI ==========
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs. next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // ========== KIỂM TRA EMAIL ĐÃ TỒN TẠI ==========
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs. next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // ========== LẤY EMAIL TỪ USER ID ==========
    public String getEmailByUserId(int userId) {
        String sql = "SELECT email FROM users WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs. next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}