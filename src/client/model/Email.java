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
    private String sentAtString;
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
    
    // Constructor từ dữ liệu Inbox (5 trường)
    public Email(int id, String senderEmail, String subject, String sentAtString, boolean isRead) {
        this.id = id;
        this. senderEmail = senderEmail;
        this.subject = subject;
        this. sentAtString = sentAtString;
        this.sentAt = parseDateTime(sentAtString);
        this.isRead = isRead;
        this.folder = "inbox";
    }
    
    // Constructor từ dữ liệu Sent (4 trường)
    public Email(int id, String recipientEmail, String subject, String sentAtString) {
        this.id = id;
        this. recipientEmail = recipientEmail;
        this.subject = subject;
        this. sentAtString = sentAtString;
        this.sentAt = parseDateTime(sentAtString);
        this.isRead = true;
        this. folder = "sent";
    }
    
    // Constructor từ dữ liệu chi tiết (6 trường)
    public Email(int id, String senderEmail, String recipientEmail, 
                 String subject, String body, String sentAtString) {
        this.id = id;
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.body = body;
        this.sentAtString = sentAtString;
        this.sentAt = parseDateTime(sentAtString);
    }
    
    // ========== PARSE DATETIME TỪ STRING ==========
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        
        try {
            // Format từ MySQL Timestamp: "2024-01-15 10:30:45.0"
            String cleanStr = dateTimeStr.split("\\.")[0].trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm: ss");
            return LocalDateTime.parse(cleanStr, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("⚠️ Không parse được thời gian:  " + dateTimeStr);
            return null;
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
    
    // 🆕 Lấy thời gian dạng string đẹp (GIỐNG GMAIL)
    public String getFormattedTime() {
        // Nếu không có sentAt, thử dùng sentAtString gốc
        if (sentAt == null && sentAtString != null) {
            sentAt = parseDateTime(sentAtString);
        }
        
        if (sentAt == null) {
            // Fallback: hiển thị string gốc nếu có
            return sentAtString != null ? formatRawTimeString(sentAtString) : "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Hiển thị giống Gmail:
        // - Hôm nay: chỉ hiện giờ (VD: 14:30)
        // - Năm nay: hiện ngày tháng (VD: 22 Thg 12)
        // - Năm trước: hiện đầy đủ (VD: 22/12/2024)
        
        if (sentAt. toLocalDate().equals(now.toLocalDate())) {
            // Hôm nay - chỉ hiện giờ
            return sentAt.format(DateTimeFormatter. ofPattern("HH:mm"));
        } else if (sentAt.getYear() == now.getYear()) {
            // Năm nay - hiện ngày tháng
            return sentAt.format(DateTimeFormatter. ofPattern("dd 'Thg' MM"));
        } else {
            // Năm trước - hiện đầy đủ
            return sentAt. format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }
    
    // 🆕 Format string thời gian thô (fallback)
    private String formatRawTimeString(String raw) {
        if (raw == null) return "";
        // "2024-12-22 14:30:45.0" → "22/12/2024 14:30"
        try {
            String clean = raw.split("\\.")[0].trim();
            String[] parts = clean. split(" ");
            if (parts.length >= 2) {
                String[] dateParts = parts[0].split("-");
                String[] timeParts = parts[1].split(":");
                if (dateParts. length >= 3 && timeParts. length >= 2) {
                    return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0] + " " + 
                           timeParts[0] + ":" + timeParts[1];
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return raw;
    }
    
    // Lấy thời gian đầy đủ (cho chi tiết email)
    public String getFullFormattedTime() {
        if (sentAt == null && sentAtString != null) {
            sentAt = parseDateTime(sentAtString);
        }
        
        if (sentAt == null) {
            return sentAtString != null ? formatRawTimeString(sentAtString) : "";
        }
        
        return sentAt.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
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
        return isRead ?  "Đã đọc" :  "Chưa đọc";
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