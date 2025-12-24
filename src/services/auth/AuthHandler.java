package services.auth;

import database.UserDAO;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuthHandler implements Runnable {
    
    private Socket socket;
    private UserDAO userDAO;
    private static Map<String, Integer> sessions = new ConcurrentHashMap<>();
    
    public AuthHandler(Socket socket) {
        this.socket = socket;
        this.userDAO = new UserDAO();
    }
    
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            String request = in.readLine();
            System.out.println("📥 [AUTH SERVICE] Nhận: " + request);
            
            String response = processRequest(request);
            out.println(response);
            System.out.println("📤 [AUTH SERVICE] Trả: " + response);
            
        } catch (IOException e) {
            System.err.println("❌ [AUTH SERVICE] Lỗi: " + e.getMessage());
        }
    }
    
    private String processRequest(String request) {
        if (request == null) return "ERROR||Null request";
        String[] parts = request.split("\\|\\|");
        String command = parts[0].toUpperCase();
        
        switch (command) {
            case "REGISTER": // REGISTER||user||pass||email
                return (parts.length >= 4 && userDAO.register(parts[1], parts[2], parts[3])) 
                       ? "OK||Đăng ký thành công" : "ERROR||Đăng ký thất bại hoặc user đã tồn tại";

            case "LOGIN": // LOGIN||user||pass
                if (parts.length < 3) return "ERROR||Thiếu thông tin";
                int userId = userDAO.authenticate(parts[1], parts[2]);
                if (userId > 0) {
                    String token = UUID.randomUUID().toString();
                    sessions.put(token, userId);
                    String[] info = userDAO.getUserByUsername(parts[1]); // returns {id, username, email}
                    return "OK||" + token + "||" + userId + "||" + info[1] + "||" + info[2];
                }
                return "ERROR||Sai thông tin đăng nhập";

            case "VALIDATE": // VALIDATE||token
                if (parts.length < 2) return "ERROR||Token missing";
                Integer uid = sessions.get(parts[1]);
                if (uid != null) {
                    String email = userDAO.getEmailByUserId(uid);
                    return "OK||" + uid + "||" + email;
                }
                return "ERROR||Token không hợp lệ";

            case "LOGOUT": // LOGOUT||token
                if (parts.length < 2) return "ERROR";
                sessions.remove(parts[1]);
                return "OK||Đăng xuất thành công";

            case "CHECK_EMAIL": // CHECK_EMAIL||email (Dùng cho SMTP RCPT TO)
                 if (parts.length < 2) return "ERROR";
                 int id = userDAO.getUserIdByEmail(parts[1]);
                 return (id > 0) ? "OK||Exist" : "ERROR||Not Found";

            default: return "ERROR||Lệnh không hợp lệ";
        }
    }
}