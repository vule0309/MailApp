package server.handler;

import server. service.AuthService;
import server.service.MailService;
import common.Protocol;

import java.io.*;
import java.net. Socket;

public class MainHandler implements Runnable {
    
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private AuthService authService;
    
    // ========== CONSTRUCTOR ==========
    public MainHandler(Socket socket) {
        this. clientSocket = socket;
        this.authService = new AuthService();
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            System.out.println("🔐 [AUTH] Client kết nối: " + clientSocket.getInetAddress());
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out. println("📨 [AUTH] Nhận: " + inputLine);
                
                String response = processCommand(inputLine);
                out.println(response);
                System.out.println("📤 [AUTH] Gửi: " + response);
                
                // Nếu LOGOUT thì ngắt kết nối
                if (inputLine.toUpperCase().startsWith(Protocol.CMD_LOGOUT)) {
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("❌ [AUTH] Lỗi: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }
    
    // ========== XỬ LÝ LỆNH ==========
    private String processCommand(String command) {
        String[] parts = command. split("\\|\\|");
        String cmd = parts[0].toUpperCase();
        
        try {
            switch (cmd) {
                
                // REGISTER||username||password||email
                case Protocol.CMD_REGISTER:
                    if (parts.length == 4) {
                        return authService.register(parts[1], parts[2], parts[3]);
                    }
                    return "ERROR||Thiếu thông tin đăng ký";
                
                // LOGIN||username||password
                case Protocol.CMD_LOGIN:
                    if (parts.length == 3) {
                        return authService.login(parts[1], parts[2]);
                    }
                    return "ERROR||Thiếu thông tin đăng nhập";
                
                // LOGOUT||token
                case Protocol. CMD_LOGOUT: 
                    if (parts.length == 2) {
                        return authService. logout(parts[1]);
                    }
                    return "ERROR||Token không hợp lệ";
                 // ===== LIST (lấy danh sách email) =====
                case "LIST":
                    if (parts.length == 3) {
                        int userId = authService.validateToken(parts[1]);
                        if (userId > 0) {
                            MailService mailService = new MailService();
                            if (parts[2].equals("inbox")) {
                                return mailService.getInbox(userId);
                            } else if (parts[2].equals("sent")) {
                                return mailService.getSentEmails(userId);
                            }
                            return "ERROR||Folder không hợp lệ";
                        }
                        return "ERROR||Token không hợp lệ";
                    }
                    return "ERROR||Thiếu thông tin";

                // ===== DELETE (xóa email) =====
                case "DELETE": 
                    if (parts. length == 4) {
                        int userId = authService. validateToken(parts[1]);
                        if (userId > 0) {
                            MailService mailService = new MailService();
                            int emailId = Integer. parseInt(parts[2]);
                            boolean isSender = Boolean.parseBoolean(parts[3]);
                            return mailService.deleteEmail(emailId, userId, isSender);
                        }
                        return "ERROR||Token không hợp lệ";
                    }
                    return "ERROR||Thiếu thông tin";
                    
                default:
                    return "ERROR||Lệnh không hợp lệ:  " + cmd;
            }
            
        } catch (Exception e) {
            System.err.println("❌ [AUTH] Lỗi xử lý: " + e. getMessage());
            return "ERROR||Lỗi server";
        }
    }
    
    // ========== ĐÓNG KẾT NỐI ==========
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("🔌 [AUTH] Client ngắt kết nối");
        } catch (IOException e) {
            e. printStackTrace();
        }
    }
}