package server;

import server.database.DatabaseConnection;

public class ServerMain {
    
    public static void main(String[] args) {
        
        System.out.println("🚀 Đang khởi động Mail Server...\n");
        
        // ===== 1. Kiểm tra kết nối Database =====
        System.out.println("📦 Kiểm tra kết nối Database...");
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            if (db.getConnection() != null) {
                System.out.println("✅ Database:  Kết nối thành công!\n");
            } else {
                System.err.println("❌ Database: Không thể kết nối!");
                System.err.println("   Hãy kiểm tra:");
                System.err.println("   - MySQL đã bật chưa (XAMPP)?");
                System. err.println("   - Database 'mail_app' đã tạo chưa?");
                System.err.println("   - Thông tin trong Config.java đúng chưa?");
                return;
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi Database: " + e. getMessage());
            return;
        }
        
        // ===== 2. Tạo và khởi động Server =====
        MailServer server = new MailServer();
        
        // ===== 3. Xử lý shutdown (Ctrl+C) =====
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n⚠️ Đang tắt server...");
            server.stop();
        }));
        
        // ===== 4. Khởi động Server =====
        server.start();
    }
}