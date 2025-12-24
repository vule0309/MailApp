package client.network;

import client.model.User;
import client. model.Email;
import common.Config;
import common. Protocol;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java. util.List;

public class MailClient {
    
    // Singleton instance
    private static MailClient instance;
    
    // Socket cho Auth (port 2525)
    private Socket authSocket;
    private BufferedReader authIn;
    private PrintWriter authOut;
    
    // Thông tin user đang đăng nhập
    private User currentUser;
    
    // ========== SINGLETON PATTERN ==========
    private MailClient() {
    }
    
    public static synchronized MailClient getInstance() {
        if (instance == null) {
            instance = new MailClient();
        }
        return instance;
    }
    
    // ========== KẾT NỐI AUTH SERVER ==========
    public boolean connect() {
        try {
            authSocket = new Socket(Config.SERVER_HOST, Config. MAIN_PORT);
            authIn = new BufferedReader(new InputStreamReader(authSocket.getInputStream()));
            authOut = new PrintWriter(authSocket.getOutputStream(), true);
            
            System.out.println("✅ [AUTH] Kết nối thành công!");
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ [AUTH] Không thể kết nối: " + e.getMessage());
            return false;
        }
    }
    
    // ========== NGẮT KẾT NỐI ==========
    public void disconnect() {
        try {
            if (authIn != null) authIn.close();
            if (authOut != null) authOut.close();
            if (authSocket != null && !authSocket.isClosed()) authSocket.close();
            System.out. println("🔌 [AUTH] Đã ngắt kết nối");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // ========== GỬI LỆNH AUTH ==========
    private synchronized String sendAuthCommand(String command) {
        try {
            if (authSocket == null || authSocket.isClosed()) {
                if (! connect()) {
                    return "ERROR||Không thể kết nối server";
                }
            }
            
            authOut.println(command);
            System.out.println("📤 [AUTH] Gửi:  " + command);
            
            String response = authIn. readLine();
            System.out.println("📥 [AUTH] Nhận: " + response);
            
            return response;
            
        } catch (IOException e) {
            System.err.println("❌ [AUTH] Lỗi:  " + e.getMessage());
            return "ERROR||Lỗi giao tiếp với server";
        }
    }
    
    // ====================================================
    //            AUTHENTICATION (Port 2525)
    // ====================================================
    
    // ========== ĐĂNG KÝ ==========
    public String register(String username, String password, String email) {
        String command = Protocol.CMD_REGISTER + "||" + username + "||" + password + "||" + email;
        return sendAuthCommand(command);
    }
    
    // ========== ĐĂNG NHẬP ==========
    public String login(String username, String password) {
        String command = Protocol.CMD_LOGIN + "||" + username + "||" + password;
        String response = sendAuthCommand(command);
        
        if (response != null && response. startsWith("OK")) {
            parseLoginResponse(response);
        }
        
        return response;
    }
    
    // Parse login response:  OK||token||id||username||email
    private void parseLoginResponse(String response) {
        try {
            String[] parts = response. split("\\|\\|");
            if (parts.length >= 5) {
                String token = parts[1];
                int id = Integer. parseInt(parts[2]);
                String username = parts[3];
                String email = parts[4];
                
                currentUser = new User(id, username, email, token);
                System.out.println("✅ Đã lưu user: " + currentUser);
            }
        } catch (Exception e) {
            System.err. println("❌ Lỗi parse login: " + e.getMessage());
        }
    }
    
    // ========== ĐĂNG XUẤT ==========
    public String logout() {
        if (currentUser == null) {
            return "ERROR||Chưa đăng nhập";
        }
        
        String command = Protocol.CMD_LOGOUT + "||" + currentUser.getSessionToken();
        String response = sendAuthCommand(command);
        
        if (response != null && response. startsWith("OK")) {
            currentUser = null;
            disconnect();
        }
        
        return response;
    }
    
    // ====================================================
    //              SMTP - GỬI EMAIL (Port 2526)
    // ====================================================
    
    public boolean sendEmail(String recipientEmail, String subject, String body) {
        if (currentUser == null) {
            System.err.println("❌ Chưa đăng nhập!");
            return false;
        }
        
        SMTPClient smtp = new SMTPClient(currentUser.getSessionToken());
        return smtp.send(currentUser.getEmail(), recipientEmail, subject, body);
    }
    
    // ====================================================
    //              POP3 - NHẬN EMAIL (Port 2527)
    // ====================================================
    
 // ========== LẤY INBOX (SỬA - DÙNG MAIN PORT) ==========
    public List<Email> getInbox() {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        List<Email> inboxEmails = new ArrayList<>();
        
        try {
            Socket tempSocket = new Socket(Config.SERVER_HOST, Config. MAIN_PORT);
            BufferedReader tempIn = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
            PrintWriter tempOut = new PrintWriter(tempSocket.getOutputStream(), true);
            
            // Gửi lệnh lấy inbox
            String command = "LIST||" + currentUser.getSessionToken() + "||inbox";
            tempOut.println(command);
            System.out.println("📤 [INBOX] Gửi:  " + command);
            
            String response = tempIn.readLine();
            System.out.println("📥 [INBOX] Nhận: " + response);
            
            // Parse response
            if (response != null && response.startsWith("OK")) {
                inboxEmails = parseInboxEmailList(response);
            }
            
            // Đóng kết nối
            tempIn.close();
            tempOut.close();
            tempSocket.close();
            
        } catch (IOException e) {
            System.err.println("❌ [INBOX] Lỗi:  " + e.getMessage());
        }
        
        return inboxEmails;
    }
 // ========== PARSE DANH SÁCH INBOX ==========
    private List<Email> parseInboxEmailList(String response) {
        List<Email> emails = new ArrayList<>();
        
        String[] parts = response.split("\\|\\|");
        
        // Kiểm tra nếu rỗng
        if (parts.length < 2 || parts[1].equals("EMPTY")) {
            return emails;
        }
        
        // Parse từng email
        // Format: OK||id;;sender;;subject;;time;;isRead||... 
        for (int i = 1; i < parts.length; i++) {
            try {
                String[] emailData = parts[i].split(";;");
                
                if (emailData.length >= 5) {
                    Email email = new Email(
                        Integer.parseInt(emailData[0]),    // id
                        emailData[1],                       // sender
                        emailData[2],                       // subject
                        emailData[3],                       // time
                        Boolean.parseBoolean(emailData[4])  // isRead
                    );
                    email.setFolder("inbox");
                    emails.add(email);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Lỗi parse inbox email: " + e.getMessage());
            }
        }
        
        return emails;
    }

    
    // ========== 🆕 LẤY EMAIL ĐÃ GỬI (SENT) ==========
    public List<Email> getSentEmails() {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        // Tạo kết nối tạm đến server để lấy sent emails
        List<Email> sentEmails = new ArrayList<>();
        
        try {
            Socket tempSocket = new Socket(Config.SERVER_HOST, Config.MAIN_PORT);
            BufferedReader tempIn = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
            PrintWriter tempOut = new PrintWriter(tempSocket.getOutputStream(), true);
            
            // Gửi lệnh lấy sent emails
            String command = "LIST||" + currentUser. getSessionToken() + "||sent";
            tempOut.println(command);
            System. out.println("📤 [SENT] Gửi:  " + command);
            
            String response = tempIn.readLine();
            System.out.println("📥 [SENT] Nhận: " + response);
            
            // Parse response
            if (response != null && response.startsWith("OK")) {
                sentEmails = parseSentEmailList(response);
            }
            
            // Đóng kết nối tạm
            tempIn.close();
            tempOut.close();
            tempSocket. close();
            
        } catch (IOException e) {
            System.err.println("❌ [SENT] Lỗi: " + e.getMessage());
        }
        
        return sentEmails;
    }
    
    // ========== 🆕 PARSE DANH SÁCH EMAIL ĐÃ GỬI ==========
    private List<Email> parseSentEmailList(String response) {
        List<Email> emails = new ArrayList<>();
        
        String[] parts = response. split("\\|\\|");
        
        // Kiểm tra nếu rỗng
        if (parts.length < 2 || parts[1].equals("EMPTY")) {
            return emails;
        }
        
        // Parse từng email
        // Format: OK||id;;recipient;;subject;;time||... 
        for (int i = 1; i < parts. length; i++) {
            try {
                String[] emailData = parts[i]. split(";;");
                
                if (emailData.length >= 4) {
                    Email email = new Email();
                    email. setId(Integer. parseInt(emailData[0]));
                    email.setRecipientEmail(emailData[1]);
                    email.setSubject(emailData[2]);
                    email.setSentAtString(emailData[3]);
                    email.setFolder("sent");
                    email.setRead(true); // Email đã gửi luôn là "đã đọc"
                    
                    emails.add(email);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Lỗi parse sent email: " + e.getMessage());
            }
        }
        
        return emails;
    }
    
 // ========== ĐỌC CHI TIẾT EMAIL (SỬA - DÙNG MAIN PORT) ==========
    public Email readEmail(int emailId) {
        if (currentUser == null) {
            return null;
        }
        
        try {
            Socket tempSocket = new Socket(Config.SERVER_HOST, Config.MAIN_PORT);
            BufferedReader tempIn = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
            PrintWriter tempOut = new PrintWriter(tempSocket.getOutputStream(), true);
            
            // Gửi lệnh đọc email
            String command = "READ||" + currentUser.getSessionToken() + "||" + emailId;
            tempOut.println(command);
            System.out.println("📤 [READ] Gửi: " + command);
            
            String response = tempIn. readLine();
            System.out.println("📥 [READ] Nhận: " + response);
            
            // Đóng kết nối
            tempIn.close();
            tempOut.close();
            tempSocket.close();
            
            // Parse response:  OK||id||sender||recipient||subject||body||time
            if (response != null && response.startsWith("OK")) {
                String[] parts = response.split("\\|\\|");
                if (parts.length >= 7) {
                    return new Email(
                        Integer.parseInt(parts[1]),  // id
                        parts[2],                     // sender
                        parts[3],                     // recipient
                        parts[4],                     // subject
                        parts[5],                     // body
                        parts[6]                      // time
                    );
                }
            }
            
        } catch (IOException e) {
            System.err.println("❌ [READ] Lỗi: " + e. getMessage());
        }
        
        return null;
    }
    
    // ========== 🆕 XÓA EMAIL (CÓ 2 THAM SỐ) ==========
    public boolean deleteEmail(int emailId, boolean isSender) {
        if (currentUser == null) {
            return false;
        }
        
        try {
            Socket tempSocket = new Socket(Config.SERVER_HOST, Config. MAIN_PORT);
            BufferedReader tempIn = new BufferedReader(new InputStreamReader(tempSocket. getInputStream()));
            PrintWriter tempOut = new PrintWriter(tempSocket.getOutputStream(), true);
            
            // Gửi lệnh xóa
            String command = "DELETE||" + currentUser.getSessionToken() + "||" + emailId + "||" + isSender;
            tempOut.println(command);
            System.out.println("📤 [DELETE] Gửi:  " + command);
            
            String response = tempIn.readLine();
            System.out.println("📥 [DELETE] Nhận:  " + response);
            
            // Đóng kết nối
            tempIn.close();
            tempOut.close();
            tempSocket. close();
            
            return response != null && response.startsWith("OK");
            
        } catch (IOException e) {
            System.err.println("❌ [DELETE] Lỗi:  " + e.getMessage());
            return false;
        }
    }
    
    // ========== XÓA EMAIL (1 THAM SỐ - dùng cho POP3) ==========
    public boolean deleteEmail(int emailId) {
        return deleteEmail(emailId, false);
    }
    
    // ====================================================
    //                  GETTERS
    // ====================================================
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public String getSessionToken() {
        return currentUser != null ? currentUser.getSessionToken() : null;
    }
    
    public String getUserEmail() {
        return currentUser != null ?  currentUser.getEmail() : null;
    }
    
    public String getUsername() {
        return currentUser != null ?  currentUser.getUsername() : null;
    }
    
    public int getUserId() {
        return currentUser != null ?  currentUser.getId() : -1;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null && currentUser. getSessionToken() != null;
    }
    
    public boolean isConnected() {
        return authSocket != null && ! authSocket. isClosed() && authSocket.isConnected();
    }
}