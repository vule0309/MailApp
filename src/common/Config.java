package common;

public class Config {
	// ========== SERVER HOST ==========
    public static final String SERVER_HOST = "localhost";
    
    // ===== GATEWAY PORTS (Client kết nối vào đây) =====
    public static final int MAIN_PORT = 2525;    // Auth từ Client
    public static final int SMTP_PORT = 2526;    // Gửi mail
    public static final int POP3_PORT = 2527;    // Nhận mail
    
 // ===== INTERNAL SERVICE PORTS (Nội bộ) =====
    public static final int AUTH_SERVICE_PORT = 9001;      // 🆕 Auth Service
    public static final int MAILDATA_SERVICE_PORT = 9002;  // 🆕 Mail Data Service
    
    // Database - SỬA THEO MÁY BẠN
    public static final String DB_URL = "jdbc:mysql://localhost:3307/mail_app";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = ""; // Để trống nếu dùng XAMPP mặc định
 // ========== SESSION ==========
    public static final int SESSION_TIMEOUT_HOURS = 24;
    
}