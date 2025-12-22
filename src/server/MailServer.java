package server;

import server.handler.MainHandler;
import server.handler. SMTPHandler;
import server.handler. POP3Handler;
import common.Config;

import java.io. IOException;
import java. net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailServer {
    
    // Server Sockets cho 3 ports
    private ServerSocket mainSocket;    // Port 2525 - Auth
    private ServerSocket smtpSocket;    // Port 2526 - SMTP
    private ServerSocket pop3Socket;    // Port 2527 - POP3
    
    // Thread pool
    private ExecutorService threadPool;
    
    // Trạng thái server
    private boolean running = true;
    
    // ========== CONSTRUCTOR ==========
    public MailServer() {
        // Thread pool xử lý tối đa 100 clients đồng thời
        this.threadPool = Executors.newFixedThreadPool(100);
    }
    
    // ========== KHỞI ĐỘNG SERVER ==========
    public void start() {
        try {
            // Tạo 3 Server Sockets
            mainSocket = new ServerSocket(Config. MAIN_PORT);
            smtpSocket = new ServerSocket(Config.SMTP_PORT);
            pop3Socket = new ServerSocket(Config.POP3_PORT);
            
            // Hiển thị banner
            printBanner();
            
            // Tạo 3 threads lắng nghe 3 ports
            Thread mainThread = new Thread(this:: listenMain, "MainListener");
            Thread smtpThread = new Thread(this::listenSMTP, "SMTPListener");
            Thread pop3Thread = new Thread(this::listenPOP3, "POP3Listener");
            
            // Khởi động các threads
            mainThread. start();
            smtpThread.start();
            pop3Thread.start();
            
            System.out.println("✅ Tất cả services đã sẵn sàng!\n");
            
            // Chờ các threads
            mainThread.join();
            smtpThread.join();
            pop3Thread.join();
            
        } catch (IOException e) {
            System.err.println("❌ Lỗi khởi động server:  " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("❌ Server bị gián đoạn: " + e.getMessage());
        }
    }
    
    // ========== LẮNG NGHE PORT MAIN (Auth) ==========
    private void listenMain() {
        System.out.println("🔐 [AUTH] Đang lắng nghe port " + Config.MAIN_PORT + "...");
        
        while (running) {
            try {
                Socket clientSocket = mainSocket. accept();
                threadPool.execute(new MainHandler(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err. println("❌ [AUTH] Lỗi:  " + e.getMessage());
                }
            }
        }
    }
    
    // ========== LẮNG NGHE PORT SMTP ==========
    private void listenSMTP() {
        System.out.println("📧 [SMTP] Đang lắng nghe port " + Config. SMTP_PORT + "...");
        
        while (running) {
            try {
                Socket clientSocket = smtpSocket.accept();
                threadPool.execute(new SMTPHandler(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("❌ [SMTP] Lỗi: " + e. getMessage());
                }
            }
        }
    }
    
    // ========== LẮNG NGHE PORT POP3 ==========
    private void listenPOP3() {
        System.out.println("📬 [POP3] Đang lắng nghe port " + Config. POP3_PORT + "...");
        
        while (running) {
            try {
                Socket clientSocket = pop3Socket.accept();
                threadPool.execute(new POP3Handler(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("❌ [POP3] Lỗi: " + e.getMessage());
                }
            }
        }
    }
    
    // ========== DỪNG SERVER ==========
    public void stop() {
        running = false;
        threadPool.shutdown();
        
        try {
            if (mainSocket != null && ! mainSocket.isClosed()) {
                mainSocket.close();
            }
            if (smtpSocket != null && ! smtpSocket. isClosed()) {
                smtpSocket.close();
            }
            if (pop3Socket != null && !pop3Socket.isClosed()) {
                pop3Socket.close();
            }
            System.out.println("\n🛑 Server đã dừng hoàn toàn.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // ========== HIỂN THỊ BANNER ==========
    private void printBanner() {
        System.out. println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                          ║");
        System.out.println("║            📧  MAIL SERVER v1.0                          ║");
        System.out.println("║                                                          ║");
        System.out. println("║   SMTP/POP3 Protocol Implementation                      ║");
        System.out.println("║                                                          ║");
        System.out. println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║                                                          ║");
        System.out.println("║   🔐 AUTH (Main)  :  Port " + Config. MAIN_PORT + "                           ║");
        System.out.println("║   📧 SMTP         : Port " + Config.SMTP_PORT + " (RFC 5321)                ║");
        System.out.println("║   📬 POP3         :  Port " + Config. POP3_PORT + " (RFC 1939)                ║");
        System.out.println("║                                                          ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║                                                          ║");
        System.out. println("║   Database:  MySQL (mail_app)                             ║");
        System.out.println("║   Status  : Starting...                                  ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}