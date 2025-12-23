package common;

public class Config {
	// ========== SERVER HOST ==========
    public static final String SERVER_HOST = "localhost";
    
    // ========== PORTS ==========
    public static final int MAIN_PORT = 2525;      // Port chính (Auth:  đăng ký, đăng nhập, đăng xuất)
    public static final int SMTP_PORT = 2526;      // Port SMTP (Gửi mail)
    public static final int POP3_PORT = 2527;      // Port POP3 (Nhận mail)
    
    // Database - SỬA THEO MÁY BẠN
    public static final String DB_URL = "jdbc:mysql://localhost:3307/mail_app";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = ""; // Để trống nếu dùng XAMPP mặc định
 // ========== SESSION ==========
    public static final int SESSION_TIMEOUT_HOURS = 24;
    
}