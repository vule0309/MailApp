//package server;
//
//import server.service.AuthService;
//import server.service.MailService;
//import common.Protocol;
//
//import java. io.*;
//import java.net.Socket;
//
//public class ClientHandler implements Runnable {
//    private Socket clientSocket;
//    private BufferedReader in;
//    private PrintWriter out;
//    private AuthService authService;
//    private MailService mailService;
//    
//    // Constructor
//    public ClientHandler(Socket socket) {
//        this. clientSocket = socket;
//        this.authService = new AuthService();
//        this. mailService = new MailService();
//    }
//    
//    @Override
//    public void run() {
//        try {
//            // Tạo luồng đọc/ghi
//            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//            out = new PrintWriter(clientSocket. getOutputStream(), true);
//            
//            System.out.println("📥 Client kết nối: " + clientSocket.getInetAddress());
//            
//            String inputLine;
//            // Đọc lệnh từ client
//            while ((inputLine = in.readLine()) != null) {
//                System.out.println("📨 Nhận: " + inputLine);
//                
//                // Xử lý lệnh và trả kết quả
//                String response = processCommand(inputLine);
//                out.println(response);
//                
//                System.out.println("📤 Gửi: " + response);
//                
//                // Nếu logout thì ngắt kết nối
//                if (inputLine.startsWith(Protocol.CMD_LOGOUT)) {
//                    break;
//                }
//            }
//            
//        } catch (IOException e) {
//            System.err.println("❌ Lỗi xử lý client: " + e.getMessage());
//        } finally {
//            closeConnection();
//        }
//    }
//    
//    // ========== XỬ LÝ CÁC LỆNH ==========
//    private String processCommand(String command) {
//        String[] parts = command. split("\\|\\|");
//        String cmd = parts[0]. toUpperCase();
//        
//        try {
//            switch (cmd) {
//                
//                // ===== ĐĂNG KÝ =====
//                // Format: REGISTER||username||password||email
//                case Protocol.CMD_REGISTER:
//                    if (parts.length == 4) {
//                        return authService.register(parts[1], parts[2], parts[3]);
//                    }
//                    return "ERROR||Thiếu thông tin đăng ký";
//                
//                // ===== ĐĂNG NHẬP =====
//                // Format: LOGIN||username||password
//                case Protocol.CMD_LOGIN:
//                    if (parts. length == 3) {
//                        return authService.login(parts[1], parts[2]);
//                    }
//                    return "ERROR||Thiếu thông tin đăng nhập";
//                
//                // ===== ĐĂNG XUẤT =====
//                // Format: LOGOUT||token
//                case Protocol. CMD_LOGOUT:
//                    if (parts.length == 2) {
//                        return authService. logout(parts[1]);
//                    }
//                    return "ERROR||Token không hợp lệ";
//                
//                // ===== GỬI EMAIL =====
//                // Format: SEND||token||recipientEmail||subject||body
//                case Protocol.CMD_SEND:
//                    if (parts. length >= 5) {
//                        int userId = authService.validateToken(parts[1]);
//                        if (userId > 0) {
//                            // Ghép body nếu có chứa ||
//                            String body = parts[4];
//                            if (parts.length > 5) {
//                                StringBuilder sb = new StringBuilder(parts[4]);
//                                for (int i = 5; i < parts.length; i++) {
//                                    sb.append("||").append(parts[i]);
//                                }
//                                body = sb.toString();
//                            }
//                            return mailService.sendEmail(userId, parts[2], parts[3], body);
//                        }
//                        return "ERROR||Phiên đăng nhập không hợp lệ";
//                    }
//                    return "ERROR||Thiếu thông tin email";
//                
//                // ===== LẤY DANH SÁCH EMAIL =====
//                // Format: LIST||token||folder (inbox/sent)
//                case Protocol. CMD_LIST:
//                    if (parts.length == 3) {
//                        int userId = authService. validateToken(parts[1]);
//                        if (userId > 0) {
//                            if (parts[2]. equals("inbox")) {
//                                return mailService.getInbox(userId);
//                            } else if (parts[2].equals("sent")) {
//                                return mailService.getSentEmails(userId);
//                            }
//                            return "ERROR||Folder không hợp lệ";
//                        }
//                        return "ERROR||Phiên đăng nhập không hợp lệ";
//                    }
//                    return "ERROR||Thiếu thông tin";
//                
//                // ===== ĐỌC EMAIL =====
//                // Format: READ||token||emailId
//                case Protocol.CMD_READ:
//                    if (parts.length == 3) {
//                        int userId = authService.validateToken(parts[1]);
//                        if (userId > 0) {
//                            int emailId = Integer.parseInt(parts[2]);
//                            return mailService.readEmail(emailId, userId);
//                        }
//                        return "ERROR||Phiên đăng nhập không hợp lệ";
//                    }
//                    return "ERROR||Thiếu thông tin";
//                
//                // ===== XÓA EMAIL =====
//                // Format: DELETE||token||emailId||isSender
//                case Protocol.CMD_DELETE:
//                    if (parts.length == 4) {
//                        int userId = authService.validateToken(parts[1]);
//                        if (userId > 0) {
//                            int emailId = Integer. parseInt(parts[2]);
//                            boolean isSender = Boolean.parseBoolean(parts[3]);
//                            return mailService.deleteEmail(emailId, userId, isSender);
//                        }
//                        return "ERROR||Phiên đăng nhập không hợp lệ";
//                    }
//                    return "ERROR||Thiếu thông tin";
//                
//                default:
//                    return "ERROR||Lệnh không hợp lệ: " + cmd;
//            }
//            
//        } catch (Exception e) {
//            System.err.println("❌ Lỗi xử lý lệnh: " + e.getMessage());
//            return "ERROR||Lỗi server: " + e.getMessage();
//        }
//    }
//    
//    // ========== ĐÓNG KẾT NỐI ==========
//    private void closeConnection() {
//        try {
//            if (in != null) in.close();
//            if (out != null) out.close();
//            if (clientSocket != null) clientSocket.close();
//            System.out.println("🔌 Client ngắt kết nối");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}