package client.network;

import common.Config;

import java.io.*;
import java.net.Socket;

public class SMTPClient {
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String sessionToken;
    
    // ========== CONSTRUCTOR ==========
    public SMTPClient(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    // ========== KẾT NỐI SMTP SERVER ==========
    public boolean connect() {
        try {
            socket = new Socket(Config.SERVER_HOST, Config. SMTP_PORT);
            in = new BufferedReader(new InputStreamReader(socket. getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Nhận greeting từ server
            String greeting = in.readLine();
            System.out.println("📧 [SMTP] " + greeting);
            
            if (greeting != null && greeting.startsWith("220")) {
                System.out.println("✅ [SMTP] Kết nối thành công!");
                return true;
            }
            
        } catch (IOException e) {
            System.err.println("❌ [SMTP] Không thể kết nối:  " + e.getMessage());
        }
        return false;
    }
    
    // ========== NGẮT KẾT NỐI ==========
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("🔌 [SMTP] Đã ngắt kết nối");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // ========== GỬI LỆNH VÀ NHẬN RESPONSE ==========
    private String sendCommand(String command) {
        try {
            out.println(command);
            System.out.println("📤 [SMTP] Gửi: " + command);
            
            String response = in.readLine();
            System.out. println("📥 [SMTP] Nhận: " + response);
            
            return response;
            
        } catch (IOException e) {
            System.err. println("❌ [SMTP] Lỗi:  " + e.getMessage());
            return null;
        }
    }
    
    // ========== GỬI EMAIL THEO CHUẨN SMTP ==========
    public boolean sendEmail(String senderEmail, String recipientEmail, 
                             String subject, String body) {
        
        try {
            // Bước 1: HELO
            String response = sendCommand("HELO client.local");
            if (response == null || !response.startsWith("250")) {
                System.err.println("❌ [SMTP] HELO thất bại");
                return false;
            }
            
            // Bước 2: AUTH TOKEN
            response = sendCommand("AUTH TOKEN " + sessionToken);
            if (response == null || !response.startsWith("235")) {
                System.err.println("❌ [SMTP] Xác thực thất bại");
                return false;
            }
            
            // Bước 3: MAIL FROM
            response = sendCommand("MAIL FROM: <" + senderEmail + ">");
            if (response == null || ! response.startsWith("250")) {
                System.err.println("❌ [SMTP] MAIL FROM thất bại");
                return false;
            }
            
            // Bước 4: RCPT TO
            response = sendCommand("RCPT TO:<" + recipientEmail + ">");
            if (response == null || !response.startsWith("250")) {
                System.err.println("❌ [SMTP] RCPT TO thất bại:  " + response);
                return false;
            }
            
            // Bước 5: DATA
            response = sendCommand("DATA");
            if (response == null || !response.startsWith("354")) {
                System.err.println("❌ [SMTP] DATA thất bại");
                return false;
            }
            
            // Bước 6: Gửi nội dung email
            out.println("Subject: " + subject);
            out.println("From: " + senderEmail);
            out.println("To: " + recipientEmail);
            out.println("");  // Dòng trống phân cách header và body
            out. println(body);
            out.println(".");  // Kết thúc DATA
            
            System.out.println("📤 [SMTP] Gửi nội dung email.. .");
            
            response = in.readLine();
            System. out.println("📥 [SMTP] Nhận:  " + response);
            
            if (response != null && response.startsWith("250")) {
                System.out.println("✅ [SMTP] Gửi email thành công!");
                return true;
            }
            
            // Bước 7:  QUIT
            sendCommand("QUIT");
            
        } catch (IOException e) {
            System. err.println("❌ [SMTP] Lỗi gửi email: " + e.getMessage());
        }
        
        return false;
    }
    
    // ========== GỬI EMAIL (PHƯƠNG THỨC TIỆN LỢI) ==========
    public boolean send(String senderEmail, String recipientEmail, 
                        String subject, String body) {
        
        boolean success = false;
        
        if (connect()) {
            success = sendEmail(senderEmail, recipientEmail, subject, body);
            
            // Gửi QUIT trước khi đóng
            sendCommand("QUIT");
            disconnect();
        }
        
        return success;
    }
}