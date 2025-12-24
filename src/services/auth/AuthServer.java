package services.auth;

import common.Config;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthServer {
    private ExecutorService threadPool = Executors.newFixedThreadPool(20);
    
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(Config.AUTH_SERVICE_PORT)) {
            System.out.println("🔐 [AUTH SERVICE] Đang lắng nghe port " + Config.AUTH_SERVICE_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new AuthHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}