package server.service;

import server.database.DatabaseConnection;
import java.sql.*;
import java.util.UUID;

public class SessionManager {
    private Connection conn;
    
    public SessionManager() {
        this.conn = DatabaseConnection.getInstance(). getConnection();
    }
    
    // ========== TẠO SESSION MỚI ==========
    public String createSession(int userId) {
        // Tạo token ngẫu nhiên
        String token = UUID.randomUUID().toString();
        
        String sql = "INSERT INTO sessions (user_id, token, expires_at) " +
                     "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 24 HOUR))";
        
        try (PreparedStatement pstmt = conn. prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, token);
            pstmt.executeUpdate();
            
            System.out.println("✅ Tạo session cho user " + userId);
            return token;
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi tạo session: " + e.getMessage());
            return null;
        }
    }
    
    // ========== XÁC THỰC SESSION ==========
    public int validateSession(String token) {
        String sql = "SELECT user_id FROM sessions WHERE token = ?  AND expires_at > NOW()";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("user_id"); // Token hợp lệ
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Token không hợp lệ
    }
    
    // ========== HỦY SESSION (LOGOUT) ==========
    public boolean invalidateSession(String token) {
        String sql = "DELETE FROM sessions WHERE token = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt. setString(1, token);
            int result = pstmt. executeUpdate();
            
            System.out.println("🚪 Đã hủy session");
            return result > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ========== XÓA SESSION HẾT HẠN ==========
    public void cleanExpiredSessions() {
        String sql = "DELETE FROM sessions WHERE expires_at < NOW()";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int deleted = pstmt. executeUpdate();
            if (deleted > 0) {
                System.out.println("🧹 Đã xóa " + deleted + " session hết hạn");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}