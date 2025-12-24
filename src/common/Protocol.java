package common;

public class Protocol {
    
    // ========== MAIN COMMANDS (Port 2525) ==========
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    
    // ========== SMTP COMMANDS (Port 2526) ==========
    public static final String SMTP_HELO = "HELO";           // Bắt đầu kết nối
    public static final String SMTP_AUTH = "AUTH";           // Xác thực token
    public static final String SMTP_MAIL_FROM = "MAIL FROM"; // Người gửi
    public static final String SMTP_RCPT_TO = "RCPT TO";     // Người nhận
    public static final String SMTP_DATA = "DATA";           // Bắt đầu nội dung
    public static final String SMTP_QUIT = "QUIT";           // Kết thúc
    
    // SMTP Response Codes
    public static final String SMTP_220 = "220";  // Service ready
    public static final String SMTP_221 = "221";  // Closing connection
    public static final String SMTP_235 = "235";  // Auth successful
    public static final String SMTP_250 = "250";  // OK
    public static final String SMTP_354 = "354";  // Start mail input
    public static final String SMTP_450 = "450";  // Mailbox unavailable
    public static final String SMTP_500 = "500";  // Syntax error
    public static final String SMTP_535 = "535";  // Auth failed
    
    // ========== POP3 COMMANDS (Port 2527) ==========
    public static final String POP3_USER = "USER";   // Username
    public static final String POP3_PASS = "PASS";   // Password (token)
    public static final String POP3_STAT = "STAT";   // Thống kê
    public static final String POP3_LIST = "LIST";   // Danh sách mail
    public static final String POP3_RETR = "RETR";   // Đọc mail
    public static final String POP3_DELE = "DELE";   // Xóa mail
    public static final String POP3_QUIT = "QUIT";   // Kết thúc
    public static final String POP3_UIDL = "UIDL";   // Unique ID
    
    // POP3 Response
    public static final String POP3_OK = "+OK";
    public static final String POP3_ERR = "-ERR";
    
    // ========== DELIMITERS ==========
    public static final String DELIMITER = "||";
    public static final String DATA_END = "\r\n.\r\n";  // Kết thúc DATA trong SMTP
}