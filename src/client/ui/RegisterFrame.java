package client.ui;

import client.network.MailClient;

import javax.swing.*;
import java.awt.*;
import java. awt.event.*;

public class RegisterFrame extends JFrame {
    
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnRegister;
    private JButton btnBack;
    private JLabel lblStatus;
    
    private MailClient client;
    
    // ========== CONSTRUCTOR ==========
    public RegisterFrame() {
        client = MailClient.getInstance();
        initComponents();
    }
    
    // ========== KHỞI TẠO GIAO DIỆN ==========
    private void initComponents() {
        setTitle("Mail App - Đăng Ký");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 250, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        
        // ===== TITLE =====
        JLabel lblTitle = new JLabel("ĐĂNG KÝ");
        lblTitle.setFont(new Font("Arial", Font. BOLD, 20));
        lblTitle.setForeground(new Color(60, 179, 113));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // ===== USERNAME =====
        JPanel usernamePanel = createInputPanel("Tên đăng nhập:");
        txtUsername = new JTextField(20);
        styleTextField(txtUsername);
        usernamePanel.add(txtUsername, BorderLayout.CENTER);
        
        // ===== EMAIL =====
        JPanel emailPanel = createInputPanel("Email:");
        txtEmail = new JTextField(20);
        styleTextField(txtEmail);
        emailPanel.add(txtEmail, BorderLayout.CENTER);
        
        // ===== PASSWORD =====
        JPanel passwordPanel = createInputPanel("Mật khẩu:");
        txtPassword = new JPasswordField(20);
        styleTextField(txtPassword);
        passwordPanel.add(txtPassword, BorderLayout.CENTER);
        
        // ===== CONFIRM PASSWORD =====
        JPanel confirmPanel = createInputPanel("Xác nhận mật khẩu:");
        txtConfirmPassword = new JPasswordField(20);
        styleTextField(txtConfirmPassword);
        confirmPanel.add(txtConfirmPassword, BorderLayout.CENTER);
        
        // ===== BUTTONS =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        btnRegister = new JButton("Đăng Ký");
        btnRegister.setFont(new Font("Arial", Font.BOLD, 13));
        btnRegister. setBackground(new Color(60, 179, 113));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(120, 35));
        btnRegister.setFocusPainted(false);
        btnRegister. setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnBack = new JButton("Quay Lại");
        btnBack.setFont(new Font("Arial", Font.BOLD, 13));
        btnBack.setBackground(new Color(150, 150, 150));
        btnBack.setForeground(Color.WHITE);
        btnBack.setPreferredSize(new Dimension(120, 35));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnBack);
        
        // ===== STATUS =====
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Arial", Font. ITALIC, 12));
        lblStatus.setForeground(Color.RED);
        lblStatus. setAlignmentX(Component. CENTER_ALIGNMENT);
        
        // ===== ADD COMPONENTS =====
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(usernamePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(emailPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(passwordPanel);
        mainPanel. add(Box.createVerticalStrut(10));
        mainPanel.add(confirmPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);
        mainPanel. add(Box.createVerticalStrut(15));
        mainPanel.add(lblStatus);
        
        add(mainPanel);
        
        // ===== EVENT LISTENERS =====
        btnRegister.addActionListener(e -> register());
        btnBack.addActionListener(e -> backToLogin());
        
        // Enter để chuyển field
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e. getKeyCode() == KeyEvent.VK_ENTER) txtEmail.requestFocus();
            }
        });
        txtEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e. getKeyCode() == KeyEvent.VK_ENTER) txtPassword.requestFocus();
            }
        });
        txtPassword. addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) txtConfirmPassword.requestFocus();
            }
        });
        txtConfirmPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) register();
            }
        });
    }
    
    // ========== XỬ LÝ ĐĂNG KÝ ==========
    private void register() {
        String username = txtUsername. getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        // Validate
        if (username.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập!");
            txtUsername.requestFocus();
            return;
        }
        
        if (username.length() < 3) {
            showError("Tên đăng nhập phải có ít nhất 3 ký tự!");
            txtUsername.requestFocus();
            return;
        }
        
        if (email.isEmpty()) {
            showError("Vui lòng nhập email!");
            txtEmail.requestFocus();
            return;
        }
        
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Email không hợp lệ!");
            txtEmail.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu!");
            txtPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự!");
            txtPassword.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp!");
            txtConfirmPassword.requestFocus();
            return;
        }
        
        // Disable buttons
        setButtonsEnabled(false);
        lblStatus.setForeground(Color.BLUE);
        lblStatus.setText("Đang xử lý...");
        
        // Đăng ký trong thread riêng
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
                
                // Đăng ký
                String response = client.register(username, password, email);
                
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("OK")) {
                        JOptionPane.showMessageDialog(this,
                            "Đăng ký thành công!\nVui lòng đăng nhập.",
                            "Thành công",
                            JOptionPane. INFORMATION_MESSAGE);
                        backToLogin();
                    } else {
                        String errorMsg = parseError(response);
                        showError(errorMsg);
                        setButtonsEnabled(true);
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Lỗi: " + e. getMessage());
                    setButtonsEnabled(true);
                });
            }
        }).start();
    }
    
    // ========== QUAY LẠI LOGIN ==========
    private void backToLogin() {
        new LoginFrame().setVisible(true);
        this.dispose();
    }
    
    // ========== HELPER METHODS ==========
    private JPanel createInputPanel(String labelText) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(320, 55));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(label, BorderLayout. NORTH);
        
        return panel;
    }
    
    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(320, 32));
    }
    
    private void showError(String message) {
        lblStatus.setForeground(Color.RED);
        lblStatus.setText(message);
    }
    
    private void setButtonsEnabled(boolean enabled) {
        btnRegister.setEnabled(enabled);
        btnBack.setEnabled(enabled);
        txtUsername.setEnabled(enabled);
        txtEmail.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        txtConfirmPassword.setEnabled(enabled);
    }
    
    private String parseError(String response) {
        if (response == null) return "Không nhận được phản hồi từ server";
        String[] parts = response. split("\\|\\|");
        return parts.length > 1 ? parts[1] : "Đăng ký thất bại";
    }
}