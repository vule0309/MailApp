package gateway;

import common.Config;
import gateway.handler.MainHandler;
import gateway.handler.POP3Handler;
import gateway.handler.SMTPHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GatewayServer {
    private ExecutorService pool = Executors.newFixedThreadPool(100);
    
    public void start() {
        System.out.println("🌐 [GATEWAY] Đang khởi động...");
        new Thread(() -> listen(Config.MAIN_PORT, "MAIN")).start();
        new Thread(() -> listen(Config.SMTP_PORT, "SMTP")).start();
        new Thread(() -> listen(Config.POP3_PORT, "POP3")).start();
    }
    
    private void listen(int port, String type) {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("   ✅ " + type + " listening on " + port);
            while (true) {
                java.net.Socket s = server.accept();
                if (type.equals("MAIN")) pool.execute(new MainHandler(s));
                else if (type.equals("SMTP")) pool.execute(new SMTPHandler(s));
                else if (type.equals("POP3")) pool.execute(new POP3Handler(s));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}