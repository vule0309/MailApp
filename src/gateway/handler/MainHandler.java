package gateway.handler;

import common.Protocol;
import gateway.ServiceClient;
import java.io.*;
import java.net.Socket;

public class MainHandler implements Runnable {
    private Socket socket;

    public MainHandler(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
             
            System.out.println("🔐 [GATEWAY-MAIN] Client connected");
            String line;
            while ((line = in.readLine()) != null) {
                String response = processCommand(line);
                out.println(response);
                if (line.startsWith(Protocol.CMD_LOGOUT)) break;
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String processCommand(String cmd) {
        String[] parts = cmd.split("\\|\\|");
        String type = parts[0].toUpperCase();

        // AUTH commands -> Chuyển tiếp sang Auth Service
        if (type.equals("REGISTER") || type.equals("LOGIN") || type.equals("LOGOUT")) {
            return ServiceClient.callAuthService(cmd);
        }
        
        // Mail commands -> Cần userId từ token trước
        if (parts.length >= 2) {
            String token = parts[1];
            // 1. Validate token với Auth Service
            String authResp = ServiceClient.callAuthService("VALIDATE||" + token);
            if (!authResp.startsWith("OK")) return "ERROR||Token invalid";
            
            String userId = authResp.split("\\|\\|")[1];
            
            // 2. Gọi Mail Data Service
            switch (type) {
                case "LIST": // LIST||token||folder
                    String folder = parts[2].toUpperCase();
                    return ServiceClient.callMailDataService(folder.equals("INBOX") ? "INBOX||" + userId : "SENT||" + userId);
                case "READ": // READ||token||emailId
                    return ServiceClient.callMailDataService("READ||" + parts[2] + "||" + userId);
                case "DELETE": // DELETE||token||emailId||isSender
                    return ServiceClient.callMailDataService("DELETE||" + parts[2] + "||" + userId + "||" + parts[3]);
            }
        }
        return "ERROR||Unknown command";
    }
}