package client.network;

import client.model.Email;
import common. Config;

import java.io.*;
import java.net.Socket;
import java.util. ArrayList;
import java. util.List;

public class POP3Client {
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private String sessionToken;
    
    // ========== CONSTRUCTOR ==========
    public POP3Client(String username, String sessionToken) {
        this.username = username;
        this.sessionToken = sessionToken;
    }
    
    // ========== KẾT NỐI POP3 SERVER ==========
    public boolean connect() {
        try {
            socket = new Socket(Config.SERVER_HOST, Config. POP3_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket. getOutputStream(), true);
            
            // Nhận greeting từ server
            String greeting = in.readLine();
            System.out.println("📬 [POP3] " + greeting);
            
            if (greeting != null && greeting.startsWith("+OK")) {
                System.out.println("✅ [POP3] Kết nối thành công!");
                return true;
            }
            
        } catch (IOException e) {
            System. err.println("❌ [POP3] Không thể kết nối: " + e. getMessage());
        }
        return false;
    }
    
    // ========== NGẮT KẾT NỐI ==========
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out. println("🔌 [POP3] Đã ngắt kết nối");
        } catch (IOException e) {
            e. printStackTrace();
        }
    }
    
    // ========== GỬI LỆNH VÀ NHẬN RESPONSE ==========
    private String sendCommand(String command) {
        try {
            out.println(command);
            System. out.println("📤 [POP3] Gửi: " + command);
            
            String response = in. readLine();
            System.out.println("📥 [POP3] Nhận: " + response);
            
            return response;
            
        } catch (IOException e) {
            System.err.println("❌ [POP3] Lỗi: " + e.getMessage());
            return null;
        }
    }
    
    // ========== ĐĂNG NHẬP POP3 ==========
    public boolean login() {
        try {
            // Bước 1: USER
            String response = sendCommand("USER " + username);
            if (response == null || !response.startsWith("+OK")) {
                return false;
            }
            
            // Bước 2: PASS (dùng token)
            response = sendCommand("PASS " + sessionToken);
            if (response != null && response.startsWith("+OK")) {
                System. out.println("✅ [POP3] Đăng nhập thành công!");
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("❌ [POP3] Lỗi đăng nhập:  " + e.getMessage());
        }
        return false;
    }
    
    // ========== LẤY THỐNG KÊ MAILBOX ==========
    public int[] getStats() {
        // Trả về [số lượng, tổng size]
        String response = sendCommand("STAT");
        
        if (response != null && response.startsWith("+OK")) {
            String[] parts = response.split(" ");
            if (parts.length >= 3) {
                int count = Integer.parseInt(parts[1]);
                int size = Integer.parseInt(parts[2]);
                return new int[]{count, size};
            }
        }
        return new int[]{0, 0};
    }
    
    // ========== LẤY DANH SÁCH EMAIL ==========
    public List<Email> getEmailList() {
        List<Email> emails = new ArrayList<>();
        
        String response = sendCommand("LIST");
        
        if (response != null && response.startsWith("+OK")) {
            try {
                // Đọc danh sách cho đến khi gặp "."
                String line;
                while ((line = in.readLine()) != null && !line.equals(". ")) {
                    System.out.println("📥 [POP3] " + line);
                    
                    // Parse:  "1 500" -> msgNum = 1, size = 500
                    String[] parts = line. split(" ");
                    if (parts. length >= 2) {
                        int msgNum = Integer. parseInt(parts[0]);
                        
                        // Lấy chi tiết email
                        Email email = retrieveEmail(msgNum);
                        if (email != null) {
                            emails.add(email);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("❌ [POP3] Lỗi đọc danh sách: " + e.getMessage());
            }
        }
        
        return emails;
    }
    
    // ========== LẤY CHI TIẾT 1 EMAIL ==========
    public Email retrieveEmail(int msgNum) {
        String response = sendCommand("RETR " + msgNum);
        
        if (response != null && response. startsWith("+OK")) {
            try {
                StringBuilder content = new StringBuilder();
                String line;
                
                // Headers
                String from = "";
                String to = "";
                String subject = "";
                String date = "";
                boolean inBody = false;
                StringBuilder bodyBuilder = new StringBuilder();
                
                while ((line = in.readLine()) != null && !line.equals(". ")) {
                    
                    if (line.isEmpty()) {
                        inBody = true;  // Dòng trống = bắt đầu body
                        continue;
                    }
                    
                    if (! inBody) {
                        // Parse headers
                        if (line.toUpperCase().startsWith("FROM:")) {
                            from = line.substring(5).trim();
                        } else if (line.toUpperCase().startsWith("TO:")) {
                            to = line. substring(3).trim();
                        } else if (line. toUpperCase().startsWith("SUBJECT: ")) {
                            subject = line.substring(8).trim();
                        } else if (line.toUpperCase().startsWith("DATE:")) {
                            date = line. substring(5).trim();
                        }
                    } else {
                        // Body
                        if (bodyBuilder.length() > 0) {
                            bodyBuilder. append("\n");
                        }
                        bodyBuilder.append(line);
                    }
                }
                
                // Tạo Email object
                Email email = new Email();
                email.setId(msgNum);
                email.setSenderEmail(from);
                email.setRecipientEmail(to);
                email. setSubject(subject);
                email. setBody(bodyBuilder.toString());
                email.setSentAtString(date);
                email.setFolder("inbox");
                
                return email;
                
            } catch (IOException e) {
                System.err. println("❌ [POP3] Lỗi đọc email:  " + e.getMessage());
            }
        }
        
        return null;
    }
    
    // ========== XÓA EMAIL ==========
    public boolean deleteEmail(int msgNum) {
        String response = sendCommand("DELE " + msgNum);
        return response != null && response.startsWith("+OK");
    }
    
    // ========== RESET (HỦY XÓA) ==========
    public boolean reset() {
        String response = sendCommand("RSET");
        return response != null && response.startsWith("+OK");
    }
    
    // ========== ĐÓNG KẾT NỐI ==========
    public void quit() {
        sendCommand("QUIT");
        disconnect();
    }
    
    // ========== LẤY INBOX (PHƯƠNG THỨC TIỆN LỢI) ==========
    public List<Email> fetchInbox() {
        List<Email> emails = new ArrayList<>();
        
        if (connect() && login()) {
            emails = getEmailList();
            quit();
        }
        
        return emails;
    }
}