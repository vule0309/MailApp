public class StartAllServices {
    public static void main(String[] args) {
        System.out.println("=== KHỞI ĐỘNG HỆ THỐNG MAIL SOA ===");
        
        // 1. Start Auth Service
        new Thread(() -> services.auth.AuthServiceMain.main(null)).start();
        
        // 2. Start Mail Data Service
        new Thread(() -> services.maildata.MailDataServiceMain.main(null)).start();
        
        // Đợi xíu cho services lên
        try { Thread.sleep(1000); } catch (Exception e) {}
        
        // 3. Start Gateway
        new Thread(() -> gateway.GatewayMain.main(null)).start();
    }
}