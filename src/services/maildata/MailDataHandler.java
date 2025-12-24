package services.maildata;

import database.EmailDAO;
import database.UserDAO;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class MailDataHandler implements Runnable {
    private Socket socket;
    private EmailDAO emailDAO;
    private UserDAO userDAO;

    public MailDataHandler(Socket socket) {
        this.socket = socket;
        this.emailDAO = new EmailDAO();
        this.userDAO = new UserDAO();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            String request = in.readLine();
            System.out.println("📥 [MAIL DATA] Nhận: " + request);
            String response = processRequest(request);
            // Cắt ngắn log nếu response quá dài
            String logResponse = (response.length() > 100) ? response.substring(0, 100) + "..." : response;
            out.println(response);
            System.out.println("📤 [MAIL DATA] Trả: " + logResponse);
            
        } catch (IOException e) {
            System.err.println("❌ [MAIL DATA] Lỗi: " + e.getMessage());
        }
    }

    private String processRequest(String request) {
        if (request == null) return "ERROR||Null";
        String[] parts = request.split("\\|\\|");
        String command = parts[0].toUpperCase();

        switch (command) {
            case "SEND": // SEND||senderId||recipientEmail||subject||body
                if (parts.length < 5) return "ERROR||Thiếu thông tin";
                int recipientId = userDAO.getUserIdByEmail(parts[2]);
                if (recipientId == -1) return "ERROR||Người nhận không tồn tại";
                boolean sent = emailDAO.sendEmail(Integer.parseInt(parts[1]), recipientId, parts[3], parts[4]);
                return sent ? "OK||Gửi thành công" : "ERROR||Lỗi database";

            case "INBOX": // INBOX||userId
                List<String[]> inbox = emailDAO.getInbox(Integer.parseInt(parts[1]));
                if (inbox.isEmpty()) return "OK||EMPTY";
                StringBuilder sbInbox = new StringBuilder("OK");
                for (String[] mail : inbox) sbInbox.append("||").append(String.join(";;", mail));
                return sbInbox.toString();

            case "SENT": // SENT||userId
                List<String[]> sentList = emailDAO.getSentEmails(Integer.parseInt(parts[1]));
                if (sentList.isEmpty()) return "OK||EMPTY";
                StringBuilder sbSent = new StringBuilder("OK");
                for (String[] mail : sentList) sbSent.append("||").append(String.join(";;", mail));
                return sbSent.toString();

            case "READ": // READ||emailId||userId
                String[] mail = emailDAO.getEmailById(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                return (mail != null) ? "OK||" + String.join("||", mail) : "ERROR||Không tìm thấy";

            case "DELETE": // DELETE||emailId||userId||isSender
                boolean del = emailDAO.deleteEmail(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3]));
                return del ? "OK||Đã xóa" : "ERROR||Xóa thất bại";

            default: return "ERROR||Unknown command";
        }
    }
}