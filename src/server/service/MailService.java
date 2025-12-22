package server.service;

import server. database.EmailDAO;
import server.database.UserDAO;
import java.util.List;

public class MailService {
    private EmailDAO emailDAO;
    private UserDAO userDAO;
    
    public MailService() {
        this.emailDAO = new EmailDAO();
        this.userDAO = new UserDAO();
    }
    
    // ========== GỬI EMAIL ==========
    public String sendEmail(int senderId, String recipientEmail, String subject, String body) {
        // Tìm người nhận theo email
        int recipientId = userDAO.getUserIdByEmail(recipientEmail);
        
        if (recipientId == -1) {
            return "ERROR||Không tìm thấy người nhận: " + recipientEmail;
        }
        
        // Không cho gửi cho chính mình
        if (senderId == recipientId) {
            return "ERROR||Không thể gửi email cho chính mình";
        }
        
        // Gửi email
        if (emailDAO.sendEmail(senderId, recipientId, subject, body)) {
            System.out. println("📧 Email đã gửi từ " + senderId + " đến " + recipientEmail);
            return "OK||Gửi email thành công";
        }
        
        return "ERROR||Gửi email thất bại";
    }
    
    // ========== LẤY INBOX ==========
    public String getInbox(int userId) {
        List<String[]> emails = emailDAO.getInbox(userId);
        
        if (emails.isEmpty()) {
            return "OK||EMPTY";
        }
        
        // Format: OK||id;;sender;;subject;;time;;isRead||id;;sender;;subject;;time;;isRead||... 
        StringBuilder sb = new StringBuilder("OK");
        
        for (String[] email : emails) {
            sb.append("||");
            sb.append(String.join(";;", email));
        }
        
        return sb.toString();
    }
    
    // ========== LẤY EMAIL ĐÃ GỬI ==========
    public String getSentEmails(int userId) {
        List<String[]> emails = emailDAO.getSentEmails(userId);
        
        if (emails.isEmpty()) {
            return "OK||EMPTY";
        }
        
        StringBuilder sb = new StringBuilder("OK");
        
        for (String[] email : emails) {
            sb.append("||");
            sb.append(String. join(";;", email));
        }
        
        return sb.toString();
    }
    
    // ========== ĐỌC CHI TIẾT EMAIL ==========
    public String readEmail(int emailId, int userId) {
        String[] email = emailDAO.getEmailById(emailId, userId);
        
        if (email != null) {
            // Format: OK||id||sender||recipient||subject||body||time
            return "OK||" + String.join("||", email);
        }
        
        return "ERROR||Không tìm thấy email";
    }
    
    // ========== XÓA EMAIL ==========
    public String deleteEmail(int emailId, int userId, boolean isSender) {
        if (emailDAO.deleteEmail(emailId, userId, isSender)) {
            return "OK||Xóa email thành công";
        }
        return "ERROR||Xóa email thất bại";
    }
}