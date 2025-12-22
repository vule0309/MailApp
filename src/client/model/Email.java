package client.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Email implements Serializable {
    private int id;
    private String senderEmail;
    private String recipientEmail;
    private String subject;
    private String body;
    private String sentAtString;      // ✅ THÊM:  Lưu string gốc từ server
    private LocalDateTime sentAt;
    private boolean isRead;
    private String folder;
    
    // ========== CONSTRUCTORS ==========
    public Email() {
    }
    
    // Constructor để soạn email mới
    public Email(String senderEmail, String recipientEmail, String subject, String body) {
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.body = body;
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
        this.folder = "inbox";
    }
    
    // ✅ THÊM: Constructor từ dữ liệu Inbox (5 trường)
    public Email(int id, String senderEmail, String subject, String sentAtString, boolean isRead) {
        this. id = id;
        this.senderEmail = senderEmail;
        this. subject = subject;
        this.sentAtString = sentAtString;
        this. sentAt = parseDateTime(sentAtString);
        this.isRead = isRead;
        this.folder = "inbox";
    }
    
    // ✅ THÊM: Constructor từ dữ liệu Sent (4 trường)
    public Email(int id, String recipientEmail, String subject, String sentAtString) {
        this. id = id;
        this.recipientEmail = recipientEmail;
        this. subject = subject;
        this.sentAtString = sentAtString;
        this. sentAt = parseDateTime(sentAtString);
        this.isRead = true;
        this. folder = "sent";
    }
    
    // ✅ THÊM: Constructor từ dữ liệu chi tiết (6 trường)
    public Email(int id, String senderEmail, String recipientEmail, 
                 String subject, String body, String sentAtString) {
        this. id = id;
        this.senderEmail = senderEmail;
        this. recipientEmail = recipientEmail;
        this.subject = subject;
        this. body = body;
        this.sentAtString = sentAtString;
        this. sentAt = parseDateTime(sentAtString);
    }
    
    // ========== PARSE DATETIME TỪ STRING ==========
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            // Format từ MySQL Timestamp:  "2024-01-15 10:30:45.0"
            String cleanStr = dateTimeStr. split("\\.")[0]; // Bỏ phần . 0
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm: ss");
            return LocalDateTime.parse(cleanStr, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("⚠️ Không parse được thời gian: " + dateTimeStr);
            return LocalDateTime.now();
        }
    }
    
    // ========== GETTERS ==========
    public int getId() {
        return id;
    }
    
    public String getSenderEmail() {
        return senderEmail;
    }
    
    public String getRecipientEmail() {
        return recipientEmail;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public String getBody() {
        return body;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public String getSentAtString() {
        return sentAtString;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public String getFolder() {
        return folder;
    }
    
    // ========== SETTERS ==========
    public void setId(int id) {
        this.id = id;
    }
    
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }
    
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
    
    public void setSubject(String subject) {
        this. subject = subject;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public void setSentAtString(String sentAtString) {
        this.sentAtString = sentAtString;
        this.sentAt = parseDateTime(sentAtString);
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public void setFolder(String folder) {
        this.folder = folder;
    }
    
    // ========== UTILITY METHODS ==========
    
    // Lấy thời gian dạng string đẹp
    public String getFormattedTime() {
        if (sentAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter. ofPattern("dd/MM/yyyy HH:mm");
        return sentAt. format(formatter);
    }
    
    // Lấy preview body (50 ký tự đầu)
    public String getBodyPreview() {
        if (body == null || body.isEmpty()) return "";
        if (body.length() <= 50) return body;
        return body.substring(0, 50) + "...";
    }
    
    // Kiểm tra subject rỗng
    public String getDisplaySubject() {
        if (subject == null || subject.trim().isEmpty()) {
            return "(Không có tiêu đề)";
        }
        return subject;
    }
    
    // Lấy trạng thái đọc
    public String getReadStatus() {
        return isRead ? "Đã đọc" :  "Chưa đọc";
    }
    
    // ========== OVERRIDE METHODS ==========
    @Override
    public String toString() {
        return "Email{" +
                "id=" + id +
                ", from='" + senderEmail + '\'' +
                ", to='" + recipientEmail + '\'' +
                ", subject='" + subject + '\'' +
                ", sentAt=" + getFormattedTime() +
                ", isRead=" + isRead +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Email email = (Email) obj;
        return id == email.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}