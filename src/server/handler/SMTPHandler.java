package server.handler;

import server.service.AuthService;
import server. service. MailService;
import common.Protocol;

import java.io.*;
import java.net.Socket;

public class SMTPHandler implements Runnable {
    
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    
    private AuthService authService;
    private MailService mailService;
    
    // Trạng thái SMTP session
    private boolean authenticated = false;
    private int currentUserId = -1;
    private String senderEmail = null;
    private String recipientEmail = null;
    private String subject = null;
    private StringBuilder bodyBuilder = null;
    private boolean receivingData = false;
    
    // ========== CONSTRUCTOR ==========
    public SMTPHandler(Socket socket) {
        this. clientSocket = socket;
        this.authService = new AuthService();
        this.mailService = new MailService();
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // Gửi greeting theo chuẩn SMTP
            sendResponse("220 mail.local SMTP Server Ready");
            System.out.println("📧 [SMTP] Client kết nối:  " + clientSocket. getInetAddress());
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("📨 [SMTP] Nhận: " + inputLine);
                
                // Xử lý lệnh
                String response = processCommand(inputLine);
                
                if (response != null) {
                    sendResponse(response);
                }
                
                // Nếu QUIT thì thoát
                if (inputLine.toUpperCase().startsWith(Protocol.SMTP_QUIT)) {
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("❌ [SMTP] Lỗi:  " + e.getMessage());
        } finally {
            closeConnection();
        }
    }
    
    // ========== GỬI RESPONSE ==========
    private void sendResponse(String response) {
        out.println(response);
        System.out.println("📤 [SMTP] Gửi:  " + response);
    }
    
    // ========== XỬ LÝ LỆNH SMTP ==========
    private String processCommand(String command) {
        
        // Nếu đang nhận DATA (nội dung email)
        if (receivingData) {
            return handleDataContent(command);
        }
        
        String upperCommand = command.toUpperCase();
        
        // ===== HELO =====
        if (upperCommand.startsWith(Protocol.SMTP_HELO)) {
            return handleHelo(command);
        }
        
        // ===== AUTH =====
        if (upperCommand.startsWith(Protocol. SMTP_AUTH)) {
            return handleAuth(command);
        }
        
        // ===== MAIL FROM =====
        if (upperCommand.startsWith(Protocol. SMTP_MAIL_FROM)) {
            return handleMailFrom(command);
        }
        
        // ===== RCPT TO =====
        if (upperCommand.startsWith(Protocol.SMTP_RCPT_TO)) {
            return handleRcptTo(command);
        }
        
        // ===== DATA =====
        if (upperCommand.startsWith(Protocol.SMTP_DATA)) {
            return handleData();
        }
        
        // ===== QUIT =====
        if (upperCommand.startsWith(Protocol.SMTP_QUIT)) {
            return handleQuit();
        }
        
        // Lệnh không hợp lệ
        return "500 Syntax error, command unrecognized";
    }
    
    // ========== HELO - Khởi tạo kết nối ==========
    private String handleHelo(String command) {
        // HELO client. local
        String[] parts = command.split(" ", 2);
        String clientDomain = parts. length > 1 ? parts[1] : "unknown";
        
        return "250 Hello " + clientDomain + ", pleased to meet you";
    }
    
    // ========== AUTH - Xác thực token ==========
    private String handleAuth(String command) {
        // AUTH TOKEN abc123-xyz-token
        String[] parts = command.split(" ", 3);
        
        if (parts. length < 3 || !parts[1].equalsIgnoreCase("TOKEN")) {
            return "501 Syntax:  AUTH TOKEN <token>";
        }
        
        String token = parts[2];
        
        // Xác thực token
        currentUserId = authService.validateToken(token);
        
        if (currentUserId > 0) {
            authenticated = true;
            System.out.println("✅ [SMTP] Xác thực thành công, userId: " + currentUserId);
            return "235 Authentication successful";
        } else {
            return "535 Authentication failed";
        }
    }
    
    // ========== MAIL FROM - Khai báo người gửi ==========
    private String handleMailFrom(String command) {
        // MAIL FROM: <sender@mail.local>
        
        if (! authenticated) {
            return "530 Authentication required";
        }
        
        // Parse email từ command
        senderEmail = extractEmail(command);
        
        if (senderEmail == null) {
            return "501 Syntax:  MAIL FROM: <email>";
        }
        
        System.out.println("📧 [SMTP] Sender: " + senderEmail);
        return "250 OK";
    }
    
    // ========== RCPT TO - Khai báo người nhận ==========
    private String handleRcptTo(String command) {
        // RCPT TO: <recipient@mail.local>
        
        if (!authenticated) {
            return "530 Authentication required";
        }
        
        if (senderEmail == null) {
            return "503 Need MAIL command first";
        }
        
        // Parse email từ command
        recipientEmail = extractEmail(command);
        
        if (recipientEmail == null) {
            return "501 Syntax: RCPT TO:<email>";
        }
        
        // Kiểm tra người nhận có tồn tại không
        if (! isValidRecipient(recipientEmail)) {
            return "450 Mailbox not found:  " + recipientEmail;
        }
        
        System. out.println("📧 [SMTP] Recipient: " + recipientEmail);
        return "250 OK";
    }
    
    // ========== DATA - Bắt đầu nhận nội dung ==========
    private String handleData() {
        if (!authenticated) {
            return "530 Authentication required";
        }
        
        if (senderEmail == null || recipientEmail == null) {
            return "503 Need MAIL and RCPT commands first";
        }
        
        receivingData = true;
        bodyBuilder = new StringBuilder();
        subject = "";
        
        return "354 Start mail input; end with <CRLF>. <CRLF>";
    }
    
    // ========== XỬ LÝ NỘI DUNG DATA ==========
    private String handleDataContent(String line) {
        // Kết thúc DATA khi gặp dấu "."
        if (line.equals(".")) {
            receivingData = false;
            
            // Gửi email
            String result = sendEmail();
            
            // Reset trạng thái
            resetState();
            
            return result;
        }
        
        // Parse Subject từ header
        if (line.toUpperCase().startsWith("SUBJECT:")) {
            subject = line.substring(8).trim();
        }
        // Thêm vào body (bỏ qua headers)
        else if (! line.contains(":") || bodyBuilder. length() > 0) {
            if (bodyBuilder.length() > 0) {
                bodyBuilder.append("\n");
            }
            bodyBuilder.append(line);
        }
        
        return null; // Không gửi response khi đang nhận data
    }
    
    // ========== GỬI EMAIL ==========
    private String sendEmail() {
        String body = bodyBuilder.toString().trim();
        
        String result = mailService.sendEmail(currentUserId, recipientEmail, subject, body);
        
        if (result.startsWith("OK")) {
            System.out.println("✅ [SMTP] Email đã gửi thành công!");
            return "250 OK Message accepted for delivery";
        } else {
            System.out.println("❌ [SMTP] Gửi email thất bại!");
            return "554 Transaction failed:  " + result;
        }
    }
    
    // ========== QUIT - Kết thúc ==========
    private String handleQuit() {
        return "221 mail.local closing connection";
    }
    
    // ========== HELPER METHODS ==========
    
    // Trích xuất email từ command:  MAIL FROM:<email> hoặc RCPT TO:<email>
    private String extractEmail(String command) {
        int start = command.indexOf('<');
        int end = command.indexOf('>');
        
        if (start != -1 && end != -1 && end > start) {
            return command.substring(start + 1, end).trim();
        }
        
        // Thử parse không có dấu <>
        String[] parts = command.split(":", 2);
        if (parts.length > 1) {
            return parts[1].trim().replace("<", "").replace(">", "");
        }
        
        return null;
    }
    
    // Kiểm tra người nhận có tồn tại
    private boolean isValidRecipient(String email) {
        // Dùng UserDAO để kiểm tra
        server.database.UserDAO userDAO = new server.database.UserDAO();
        return userDAO. getUserIdByEmail(email) > 0;
    }
    
    // Reset trạng thái sau khi gửi
    private void resetState() {
        senderEmail = null;
        recipientEmail = null;
        subject = null;
        bodyBuilder = null;
    }
    
    // Đóng kết nối
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("🔌 [SMTP] Client ngắt kết nối");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}