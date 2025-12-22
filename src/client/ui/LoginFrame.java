package client.ui;

import client.network.MailClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private JLabel lblStatus;
    
    private MailClient client;
    
    // ========== CONSTRUCTOR ==========
    public LoginFrame() {
        client = MailClient.getInstance();
        initComponents();
    }
    
    // ========== KHỞI TẠO GIAO DIỆN ==========
    private void initComponents() {
        setTitle("📧 Mail App - Đăng Nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 245, 250));
        mainPanel.setBorder(BorderFactory. createEmptyBorder(30, 40, 30, 40));
        
        // ===== TITLE =====
        JLabel lblTitle = new JLabel("📧 MAIL APPLICATION");
        lblTitle.setFont(new Font("Arial", Font. BOLD, 22));
        lblTitle.setForeground(new Color(50, 100, 150));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSubtitle = new JLabel("SMTP/POP3 Client");
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubtitle. setForeground(Color. GRAY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // ===== USERNAME =====
        JPanel usernamePanel = new JPanel(new BorderLayout(5, 5));
        usernamePanel.setOpaque(false);
        usernamePanel.setMaximumSize(new Dimension(300, 60));
        
        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 13));
        
        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(300, 35));
        
        usernamePanel.add(lblUsername, BorderLayout.NORTH);
        usernamePanel. add(txtUsername, BorderLayout. CENTER);
        
        // ===== PASSWORD =====
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 5));
        passwordPanel.setOpaque(false);
        passwordPanel.setMaximumSize(new Dimension(300, 60));
        
        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 13));
        
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Arial", Font. PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(300, 35));
        
        passwordPanel. add(lblPassword, BorderLayout. NORTH);
        passwordPanel. add(txtPassword, BorderLayout. CENTER);
        
        // ===== BUTTONS =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        buttonPanel.setOpaque(false);
        
        btnLogin = new JButton("Đăng Nhập");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 13));
        btnLogin.setBackground(new Color(70, 130, 180));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(120, 35));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnRegister = new JButton("Đăng Ký");
        btnRegister.setFont(new Font("Arial", Font.BOLD, 13));
        btnRegister.setBackground(new Color(60, 179, 113));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(120, 35));
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor. HAND_CURSOR));
        
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        
        // ===== STATUS =====
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Arial", Font. ITALIC, 12));
        lblStatus.setForeground(Color. RED);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // ===== ADD COMPONENTS =====
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(lblSubtitle);
        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(usernamePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(passwordPanel);
        mainPanel. add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);
        mainPanel. add(Box.createVerticalStrut(15));
        mainPanel.add(lblStatus);
        
        add(mainPanel);
        
        // ===== EVENT LISTENERS =====
        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> openRegister());
        
        // Enter để đăng nhập
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });
        
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocus();
                }
            }
        });
    }
    
    // ========== XỬ LÝ ĐĂNG NHẬP ==========
    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Validate
        if (username.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập!");
            txtUsername.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu!");
            txtPassword.requestFocus();
            return;
        }
        
        // Disable buttons
        setButtonsEnabled(false);
        lblStatus.setForeground(Color.BLUE);
        lblStatus.setText("Đang kết nối...");
        
        // Thực hiện đăng nhập trong thread riêng
        new Thread(() -> {
            try {
                // Kết nối server
                if (!client.connect()) {
                    SwingUtilities.invokeLater(() -> {
                        showError("Không thể kết nối đến server!");
                        setButtonsEnabled(true);
                    });
                    return;
                }
                
                // Đăng nhập
                String response = client.login(username, password);
                
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response. startsWith("OK")) {
                        // Thành công - mở MainFrame
                        JOptionPane.showMessageDialog(this,
                            "Đăng nhập thành công!\nChào mừng " + client.getUsername(),
                            "Thành công",
                            JOptionPane. INFORMATION_MESSAGE);
                        
                        openMainFrame();
                    } else {
                        // Thất bại
                        String errorMsg = parseError(response);
                        showError(errorMsg);
                        setButtonsEnabled(true);
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Lỗi:  " + e.getMessage());
                    setButtonsEnabled(true);
                });
            }
        }).start();
    }
    
    // ========== MỞ FORM ĐĂNG KÝ ==========
    private void openRegister() {
        new RegisterFrame().setVisible(true);
        this.dispose();
    }
    
    // ========== MỞ MAIN FRAME ==========
    private void openMainFrame() {
        new MainFrame().setVisible(true);
        this.dispose();
    }
    
    // ========== HELPER METHODS ==========
    private void showError(String message) {
        lblStatus.setForeground(Color.RED);
        lblStatus. setText(message);
    }
    
    private void setButtonsEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnRegister.setEnabled(enabled);
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
    }
    
    private String parseError(String response) {
        if (response == null) return "Không nhận được phản hồi từ server";
        String[] parts = response.split("\\|\\|");
        return parts. length > 1 ? parts[1] : "Đăng nhập thất bại";
    }
    
    // ========== MAIN ==========
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame().setVisible(true);
        });
    }
}