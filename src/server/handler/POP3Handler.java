package server.handler;

import server.service.AuthService;
import server.service. MailService;
import server.database.UserDAO;
import server.database.EmailDAO;
import common.Protocol;

import java.io.*;
import java.net. Socket;
import java.util. List;
import java.util. ArrayList;

public class POP3Handler implements Runnable {
    
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    
    private AuthService authService;
    private MailService mailService;
    private UserDAO userDAO;
    private EmailDAO emailDAO;
    
    // Trạng thái POP3 session
    private String username = null;
    private int currentUserId = -1;
    private boolean authenticated = false;
    private List<String[]> emailList = null;  // Cache danh sách email
    private List<Integer> deletedIndexes = new ArrayList<>();  // Đánh dấu xóa
    
    // ========== CONSTRUCTOR ==========
    public POP3Handler(Socket socket) {
        this.clientSocket = socket;
        this.authService = new AuthService();
        this.mailService = new MailService();
        this.userDAO = new UserDAO();
        this.emailDAO = new EmailDAO();
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket. getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // Gửi greeting theo chuẩn POP3
            sendResponse("+OK POP3 Server Ready");
            System.out.println("📬 [POP3] Client kết nối: " + clientSocket.getInetAddress());
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System. out.println("📨 [POP3] Nhận: " + inputLine);
                
                // Xử lý lệnh
                String response = processCommand(inputLine);
                sendResponse(response);
                
                // Nếu QUIT thì thoát
                if (inputLine.toUpperCase().startsWith(Protocol. POP3_QUIT)) {
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("❌ [POP3] Lỗi: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }
    
    // ========== GỬI RESPONSE ==========
    private void sendResponse(String response) {
        out. println(response);
        System.out. println("📤 [POP3] Gửi: " + response);
    }
    
    // ========== GỬI MULTI-LINE RESPONSE ==========
    private void sendMultiLineResponse(String...  lines) {
        for (String line :  lines) {
            out.println(line);
            System. out.println("📤 [POP3] Gửi: " + line);
        }
    }
    
    // ========== XỬ LÝ LỆNH POP3 ==========
    private String processCommand(String command) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toUpperCase();
        String arg = parts. length > 1 ? parts[1] : null;
        
        switch (cmd) {
            case "USER":
                return handleUser(arg);
            case "PASS":
                return handlePass(arg);
            case "STAT":
                return handleStat();
            case "LIST":
                return handleList(arg);
            case "RETR": 
                return handleRetr(arg);
            case "DELE":
                return handleDele(arg);
            case "RSET":
                return handleRset();
            case "NOOP":
                return "+OK";
            case "QUIT":
                return handleQuit();
            default:
                return "-ERR Unknown command";
        }
    }
    
    // ========== USER - Khai báo username ==========
    private String handleUser(String user) {
        if (user == null || user.isEmpty()) {
            return "-ERR Syntax: USER <username>";
        }
        
        username = user;
        return "+OK User accepted";
    }
    
    // ========== PASS - Xác thực (dùng token) ==========
    private String handlePass(String token) {
        if (username == null) {
            return "-ERR USER command must come first";
        }
        
        if (token == null || token.isEmpty()) {
            return "-ERR Syntax: PASS <token>";
        }
        
        // Xác thực token
        currentUserId = authService.validateToken(token);
        
        if (currentUserId > 0) {
            authenticated = true;
            // Load danh sách email
            loadEmailList();
            System.out.println("✅ [POP3] Đăng nhập thành công, userId: " + currentUserId);
            return "+OK Logged in, " + emailList.size() + " messages";
        } else {
            return "-ERR Authentication failed";
        }
    }
    
    // ========== STAT - Thống kê mailbox ==========
    private String handleStat() {
        if (! authenticated) {
            return "-ERR Not authenticated";
        }
        
        int count = getActiveEmailCount();
        int totalSize = count * 500; // Ước tính size
        
        return "+OK " + count + " " + totalSize;
    }
    
    // ========== LIST - Danh sách email ==========
    private String handleList(String msgNum) {
        if (!authenticated) {
            return "-ERR Not authenticated";
        }
        
        // LIST với số cụ thể
        if (msgNum != null) {
            int index = parseIndex(msgNum);
            if (index < 0 || index >= emailList.size()) {
                return "-ERR No such message";
            }
            if (deletedIndexes.contains(index)) {
                return "-ERR Message deleted";
            }
            return "+OK " + (index + 1) + " 500";
        }
        
        // LIST tất cả
        StringBuilder sb = new StringBuilder();
        sb.append("+OK ").append(getActiveEmailCount()).append(" messages\r\n");
        
        for (int i = 0; i < emailList. size(); i++) {
            if (! deletedIndexes. contains(i)) {
                sb.append(i + 1).append(" 500\r\n");
            }
        }
        sb.append(".");
        
        return sb.toString();
    }
    
    // ========== RETR - Lấy nội dung email ==========
    private String handleRetr(String msgNum) {
        if (!authenticated) {
            return "-ERR Not authenticated";
        }
        
        if (msgNum == null) {
            return "-ERR Syntax: RETR <msg>";
        }
        
        int index = parseIndex(msgNum);
        if (index < 0 || index >= emailList.size()) {
            return "-ERR No such message";
        }
        
        if (deletedIndexes.contains(index)) {
            return "-ERR Message deleted";
        }
        
        // Lấy chi tiết email
        String[] emailInfo = emailList.get(index);
        int emailId = Integer.parseInt(emailInfo[0]);
        
        String[] emailDetail = emailDAO.getEmailById(emailId, currentUserId);
        
        if (emailDetail == null) {
            return "-ERR Error retrieving message";
        }
        
        // Format email theo chuẩn
        StringBuilder sb = new StringBuilder();
        sb.append("+OK Message follows\r\n");
        sb.append("From: ").append(emailDetail[1]).append("\r\n");
        sb.append("To: ").append(emailDetail[2]).append("\r\n");
        sb.append("Subject: ").append(emailDetail[3]).append("\r\n");
        sb.append("Date: ").append(emailDetail[5]).append("\r\n");
        sb.append("\r\n");
        sb.append(emailDetail[4] != null ? emailDetail[4] : "").append("\r\n");
        sb.append(".");
        
        return sb.toString();
    }
    
    // ========== DELE - Đánh dấu xóa ==========
    private String handleDele(String msgNum) {
        if (!authenticated) {
            return "-ERR Not authenticated";
        }
        
        if (msgNum == null) {
            return "-ERR Syntax:  DELE <msg>";
        }
        
        int index = parseIndex(msgNum);
        if (index < 0 || index >= emailList. size()) {
            return "-ERR No such message";
        }
        
        if (deletedIndexes.contains(index)) {
            return "-ERR Message already deleted";
        }
        
        deletedIndexes.add(index);
        return "+OK Message " + (index + 1) + " deleted";
    }
    
    // ========== RSET - Reset trạng thái xóa ==========
    private String handleRset() {
        if (!authenticated) {
            return "-ERR Not authenticated";
        }
        
        deletedIndexes.clear();
        return "+OK Reset OK";
    }
    
    // ========== QUIT - Kết thúc và thực hiện xóa ==========
    private String handleQuit() {
        // Thực hiện xóa thật các email đã đánh dấu
        if (authenticated && !deletedIndexes.isEmpty()) {
            for (int index : deletedIndexes) {
                if (index >= 0 && index < emailList.size()) {
                    String[] emailInfo = emailList.get(index);
                    int emailId = Integer. parseInt(emailInfo[0]);
                    emailDAO.deleteEmail(emailId, currentUserId, false);
                }
            }
            System.out.println("🗑️ [POP3] Đã xóa " + deletedIndexes.size() + " email");
        }
        
        return "+OK Bye";
    }
    
    // ========== HELPER METHODS ==========
    
    // Load danh sách email inbox
    private void loadEmailList() {
        emailList = emailDAO.getInbox(currentUserId);
        deletedIndexes.clear();
    }
    
    // Đếm email chưa bị xóa
    private int getActiveEmailCount() {
        return emailList.size() - deletedIndexes.size();
    }
    
    // Parse index từ số message (1-based → 0-based)
    private int parseIndex(String msgNum) {
        try {
            return Integer.parseInt(msgNum) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    // Đóng kết nối
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("🔌 [POP3] Client ngắt kết nối");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}