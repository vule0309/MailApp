package services.maildata;

import common.Config;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailDataServer {
    private ExecutorService threadPool = Executors.newFixedThreadPool(20);

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(Config.MAILDATA_SERVICE_PORT)) {
            System.out.println("📧 [MAIL DATA SERVICE] Đang lắng nghe port " + Config.MAILDATA_SERVICE_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new MailDataHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}