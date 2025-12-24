package gateway.handler;

import gateway.ServiceClient;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class POP3Handler implements Runnable {
    private Socket socket;
    private boolean authenticated = false;
    private int currentUserId = -1;
    private List<String> cachedIds = new ArrayList<>(); // Cache ID để mapping index

    public POP3Handler(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            out.println("+OK POP3 Gateway Ready");
            String line;
            while ((line = in.readLine()) != null) {
                String resp = processCommand(line);
                out.println(resp);
                if (line.toUpperCase().startsWith("QUIT")) break;
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String processCommand(String cmd) {
        String[] parts = cmd.split(" ");
        String c = parts[0].toUpperCase();
        
        if (c.equals("USER")) return "+OK User accepted";
        if (c.equals("PASS")) {
            String token = parts[1];
            String val = ServiceClient.callAuthService("VALIDATE||" + token);
            if (val.startsWith("OK")) {
                authenticated = true;
                currentUserId = Integer.parseInt(val.split("\\|\\|")[1]);
                return "+OK Logged in";
            }
            return "-ERR Auth failed";
        }
        
        if (!authenticated) return "-ERR Auth required";
        
        if (c.equals("STAT")) {
            // Lấy inbox để đếm
            String inboxRaw = ServiceClient.callMailDataService("INBOX||" + currentUserId);
            if (inboxRaw.contains("EMPTY")) return "+OK 0 0";
            // Format: OK||id;;sender...||id;;sender...
            String[] mails = inboxRaw.split("\\|\\|");
            return "+OK " + (mails.length - 1) + " " + ((mails.length - 1) * 500);
        }
        
        if (c.equals("LIST")) {
             String inboxRaw = ServiceClient.callMailDataService("INBOX||" + currentUserId);
             if (inboxRaw.contains("EMPTY")) return "+OK 0 messages\r\n.";
             String[] mails = inboxRaw.split("\\|\\|");
             cachedIds.clear();
             StringBuilder sb = new StringBuilder("+OK " + (mails.length - 1) + " messages");
             for (int i = 1; i < mails.length; i++) {
                 String[] m = mails[i].split(";;"); // m[0] is ID
                 cachedIds.add(m[0]);
                 sb.append("\r\n").append(i).append(" 500");
             }
             sb.append("\r\n.");
             return sb.toString();
        }
        
        if (c.equals("RETR")) {
            int idx = Integer.parseInt(parts[1]) - 1;
            if (idx < 0 || idx >= cachedIds.size()) return "-ERR Invalid index";
            String emailId = cachedIds.get(idx);
            
            String mailRaw = ServiceClient.callMailDataService("READ||" + emailId + "||" + currentUserId);
            if (!mailRaw.startsWith("OK")) return "-ERR Read error";
            
            // Format: OK||id||sender||recipient||subject||body||time
            String[] data = mailRaw.split("\\|\\|");
            return "+OK\r\nFrom: " + data[2] + "\r\nSubject: " + data[4] + "\r\n\r\n" + data[5] + "\r\n.";
        }
        
        if (c.equals("DELE")) {
            int idx = Integer.parseInt(parts[1]) - 1;
            if (idx < 0 || idx >= cachedIds.size()) return "-ERR Invalid index";
            String emailId = cachedIds.get(idx);
            ServiceClient.callMailDataService("DELETE||" + emailId + "||" + currentUserId + "||false");
            return "+OK Deleted";
        }
        
        if (c.equals("QUIT")) return "+OK Bye";
        
        return "-ERR Unknown";
    }
}