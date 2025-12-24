package gateway.handler;

import gateway.ServiceClient;
import java.io.*;
import java.net.Socket;

public class SMTPHandler implements Runnable {
    private Socket socket;
    private boolean authenticated = false;
    private int currentUserId = -1;
    private String recipientEmail;
    private String senderEmail;
    private StringBuilder dataBuffer;
    private boolean isDataMode = false;

    public SMTPHandler(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            out.println("220 Gateway SMTP Ready");
            String line;
            while ((line = in.readLine()) != null) {
                if (isDataMode) {
                    if (line.equals(".")) {
                        isDataMode = false;
                        // Gửi mail qua Mail Data Service
                        String fullData = dataBuffer.toString();
                        String subject = "(No Subject)";
                        
                        // Parse subject
                        for(String l : fullData.split("\n")) {
                            if(l.startsWith("Subject:")) {
                                subject = l.substring(8).trim();
                                break;
                            }
                        }
                        
                        // === FIX: Tách Header để lấy đúng nội dung Body ===
                        String bodyContent = fullData;
                        // Tìm vị trí dòng trống (2 dấu xuống dòng liên tiếp) ngăn cách Header và Body
                        int splitIndex = fullData.indexOf("\n\n");
                        if (splitIndex != -1) {
                            // Lấy phần sau dòng trống (+2 ký tự \n\n)
                            bodyContent = fullData.substring(splitIndex + 2);
                        }
                        // ==================================================
                        
                        // Gửi bodyContent (nội dung thật) thay vì fullData
                        String resp = ServiceClient.callMailDataService("SEND||" + currentUserId + "||" + recipientEmail + "||" + subject + "||" + bodyContent);
                        out.println(resp.startsWith("OK") ? "250 OK" : "554 Transaction failed");
                    } else {
                        dataBuffer.append(line).append("\n");
                    }
                } else {
                    String resp = processCommand(line);
                    if (resp != null) out.println(resp);
                    if (line.toUpperCase().startsWith("QUIT")) break;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String processCommand(String cmd) {
        String upper = cmd.toUpperCase();
        if (upper.startsWith("HELO")) return "250 Hello";
        if (upper.startsWith("AUTH TOKEN")) {
            String token = cmd.split(" ")[2];
            String authResp = ServiceClient.callAuthService("VALIDATE||" + token);
            if (authResp.startsWith("OK")) {
                authenticated = true;
                currentUserId = Integer.parseInt(authResp.split("\\|\\|")[1]);
                return "235 Auth successful";
            }
            return "535 Auth failed";
        }
        if (upper.startsWith("MAIL FROM:")) {
             senderEmail = cmd.substring(10).replace("<","").replace(">","").trim();
             return "250 OK";
        }
        if (upper.startsWith("RCPT TO:")) {
            if (!authenticated) return "530 Auth required";
            String email = cmd.substring(8).replace("<","").replace(">","").trim();
            // Check email tồn tại qua Auth Service
            String check = ServiceClient.callAuthService("CHECK_EMAIL||" + email);
            if (check.startsWith("OK")) {
                recipientEmail = email;
                return "250 OK";
            }
            return "550 User not found";
        }
        if (upper.startsWith("DATA")) {
            if (!authenticated || recipientEmail == null) return "503 Sequence error";
            isDataMode = true;
            dataBuffer = new StringBuilder();
            return "354 End data with <CR><LF>.<CR><LF>";
        }
        if (upper.startsWith("QUIT")) return "221 Bye";
        return "500 Unknown";
    }
}