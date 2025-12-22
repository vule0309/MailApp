package server.service;

import server.database.UserDAO;

public class AuthService {
    private UserDAO userDAO;
    private SessionManager sessionManager;
    
    public AuthService() {
        this.userDAO = new UserDAO();
        this.sessionManager = new SessionManager();
    }
    
    // ========== ĐĂNG KÝ ==========
    public String register(String username, String password, String email) {
        // Kiểm tra username đã tồn tại
        if (userDAO.usernameExists(username)) {
            return "ERROR||Username đã tồn tại";
        }
        
        // Kiểm tra email đã tồn tại
        if (userDAO.emailExists(email)) {
            return "ERROR||Email đã được sử dụng";
        }
        
        // Kiểm tra độ dài password
        if (password. length() < 6) {
            return "ERROR||Mật khẩu phải có ít nhất 6 ký tự";
        }
        
        // Kiểm tra định dạng email đơn giản
        if (!email.contains("@")) {
            return "ERROR||Email không hợp lệ";
        }
        
        // Thực hiện đăng ký
        if (userDAO.register(username, password, email)) {
            System.out.println("✅ Đăng ký thành công: " + username);
            return "OK||Đăng ký thành công";
        }
        
        return "ERROR||Đăng ký thất bại";
    }
    
    // ========== ĐĂNG NHẬP ==========
    public String login(String username, String password) {
        // Xác thực username và password
        int userId = userDAO.authenticate(username, password);
        
        if (userId > 0) {
            // Tạo session token
            String token = sessionManager.createSession(userId);
            
            // Lấy thông tin user
            String[] userInfo = userDAO.getUserByUsername(username);
            
            // Trả về: OK||token||userId||username||email
            System.out.println("✅ Đăng nhập thành công: " + username);
            return "OK||" + token + "||" + userInfo[0] + "||" + userInfo[1] + "||" + userInfo[2];
        }
        
        return "ERROR||Sai tên đăng nhập hoặc mật khẩu";
    }
    
    // ========== ĐĂNG XUẤT ==========
    public String logout(String token) {
        if (sessionManager.invalidateSession(token)) {
            return "OK||Đăng xuất thành công";
        }
        return "ERROR||Đăng xuất thất bại";
    }
    
    // ========== XÁC THỰC TOKEN ==========
    public int validateToken(String token) {
        return sessionManager.validateSession(token);
    }
}