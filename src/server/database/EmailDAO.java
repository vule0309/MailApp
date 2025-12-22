package server.database;

import java.sql.*;
import java. util.ArrayList;
import java.util. List;

public class EmailDAO {
    private Connection conn;
    
    public EmailDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }
    
    // ========== GỬI EMAIL ==========
    public boolean sendEmail(int senderId, int recipientId, String subject, String body) {
        String sql = "INSERT INTO emails (sender_id, recipient_id, subject, body) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn. prepareStatement(sql)) {
            pstmt.setInt(1, senderId);
            pstmt.setInt(2, recipientId);
            pstmt.setString(3, subject);
            pstmt. setString(4, body);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
            return false;
        }
    }
    
    // ========== LẤY DANH SÁCH INBOX ==========
    public List<String[]> getInbox(int userId) {
        List<String[]> emails = new ArrayList<>();
        
        String sql = "SELECT e.id, u.email as sender_email, e.subject, e.sent_at, e.is_read " +
                     "FROM emails e " +
                     "JOIN users u ON e. sender_id = u.id " +
                     "WHERE e.recipient_id = ? AND e.is_deleted_recipient = FALSE " +
                     "ORDER BY e.sent_at DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                emails. add(new String[]{
                    String.valueOf(rs. getInt("id")),
                    rs.getString("sender_email"),
                    rs.getString("subject"),
                    rs.getTimestamp("sent_at").toString(),
                    String.valueOf(rs. getBoolean("is_read"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emails;
    }
    
    // ========== LẤY DANH SÁCH ĐÃ GỬI ==========
    public List<String[]> getSentEmails(int userId) {
        List<String[]> emails = new ArrayList<>();
        
        String sql = "SELECT e.id, u. email as recipient_email, e. subject, e.sent_at " +
                     "FROM emails e " +
                     "JOIN users u ON e. recipient_id = u.id " +
                     "WHERE e.sender_id = ? AND e.is_deleted_sender = FALSE " +
                     "ORDER BY e.sent_at DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                emails.add(new String[]{
                    String. valueOf(rs.getInt("id")),
                    rs. getString("recipient_email"),
                    rs.getString("subject"),
                    rs. getTimestamp("sent_at").toString()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emails;
    }
    
    // ========== ĐỌC CHI TIẾT EMAIL ==========
    public String[] getEmailById(int emailId, int userId) {
        String sql = "SELECT e.*, us.email as sender_email, ur.email as recipient_email " +
                     "FROM emails e " +
                     "JOIN users us ON e.sender_id = us.id " +
                     "JOIN users ur ON e.recipient_id = ur.id " +
                     "WHERE e.id = ? AND (e.sender_id = ? OR e.recipient_id = ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, emailId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs. next()) {
                // Đánh dấu đã đọc nếu là người nhận
                if (rs.getInt("recipient_id") == userId) {
                    markAsRead(emailId);
                }
                
                return new String[]{
                    String.valueOf(rs. getInt("id")),
                    rs.getString("sender_email"),
                    rs.getString("recipient_email"),
                    rs. getString("subject"),
                    rs.getString("body"),
                    rs. getTimestamp("sent_at").toString()
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // ========== ĐÁNH DẤU ĐÃ ĐỌC ==========
    public void markAsRead(int emailId) {
        String sql = "UPDATE emails SET is_read = TRUE WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, emailId);
            pstmt. executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // ========== XÓA EMAIL ==========
    public boolean deleteEmail(int emailId, int userId, boolean isSender) {
        String sql;
        
        if (isSender) {
            sql = "UPDATE emails SET is_deleted_sender = TRUE WHERE id = ?  AND sender_id = ?";
        } else {
            sql = "UPDATE emails SET is_deleted_recipient = TRUE WHERE id = ? AND recipient_id = ?";
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, emailId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}