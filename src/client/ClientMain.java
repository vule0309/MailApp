package client;

import client. ui.LoginFrame;
import javax.swing.*;

public class ClientMain {
    
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Chạy giao diện trong EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            System.out.println("🚀 Đang khởi động Mail Client...");
            new LoginFrame().setVisible(true);
        });
    }
}