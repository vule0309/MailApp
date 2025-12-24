package gateway;

import common.Config;
import java.io.*;
import java.net.Socket;

public class ServiceClient {
    public static String callAuthService(String request) {
        return callService(Config.SERVER_HOST, Config.AUTH_SERVICE_PORT, request);
    }
    
    public static String callMailDataService(String request) {
        return callService(Config.SERVER_HOST, Config.MAILDATA_SERVICE_PORT, request);
    }
    
    private static String callService(String host, int port, String request) {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            out.println(request);
            return in.readLine();
            
        } catch (IOException e) {
            System.err.println("❌ Lỗi gọi service [" + port + "]: " + e.getMessage());
            return "ERROR||Service unavailable";
        }
    }
}