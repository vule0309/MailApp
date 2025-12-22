package client. ui;

import client.network.MailClient;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java. awt.event.*;

public class ComposeFrame extends JFrame {
    
    private MailClient client;
    
    private JTextField txtTo;
    private JTextField txtSubject;
    private JTextArea txtBody;
    private JButton btnSend;
    private JButton btnCancel;
    private JLabel lblStatus;
    
    // ========== CONSTRUCTOR ==========
    public ComposeFrame() {
        client = MailClient.getInstance();
        initComponents();
    }
    
    // Constructor với người nhận sẵn (dùng cho Reply)
    public ComposeFrame(String toEmail) {
        client = MailClient.getInstance();
        initComponents();
        txtTo.setText(toEmail);
        txtSubject.requestFocus();
    }
    
    // Constructor với người nhận và subject (dùng cho Reply)
    public ComposeFrame(String toEmail, String subject) {
        client = MailClient.getInstance();
        initComponents();
        txtTo.setText(toEmail);
        txtSubject.setText(subject);
        txtBody.requestFocus();
    }
    
    // ========== KHỞI TẠO GIAO DIỆN ==========
    private void initComponents() {
        setTitle("✏️ Soạn Email Mới");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame. DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 400));
        
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel. setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        
        // ===== TOP PANEL (Header) =====
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints. HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // From
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel lblFrom = new JLabel("Từ:");
        lblFrom.setFont(new Font("Arial", Font.BOLD, 13));
        topPanel.add(lblFrom, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel lblFromValue = new JLabel(client.getUserEmail());
        lblFromValue.setFont(new Font("Arial", Font.PLAIN, 13));
        lblFromValue.setForeground(Color. GRAY);
        topPanel.add(lblFromValue, gbc);
        
        // To
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel lblTo = new JLabel("Đến:");
        lblTo.setFont(new Font("Arial", Font.BOLD, 13));
        topPanel.add(lblTo, gbc);
        
        gbc.gridx = 1;
        gbc. weightx = 1;
        txtTo = new JTextField();
        txtTo.setFont(new Font("Arial", Font.PLAIN, 13));
        txtTo.setPreferredSize(new Dimension(0, 30));
        topPanel.add(txtTo, gbc);
        
        // Subject
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel lblSubject = new JLabel("Tiêu đề:");
        lblSubject.setFont(new Font("Arial", Font.BOLD, 13));
        topPanel.add(lblSubject, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtSubject = new JTextField();
        txtSubject.setFont(new Font("Arial", Font.PLAIN, 13));
        txtSubject. setPreferredSize(new Dimension(0, 30));
        topPanel. add(txtSubject, gbc);
        
        // ===== CENTER PANEL (Body) =====
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel. setBackground(Color.WHITE);
        
        JLabel lblBody = new JLabel("Nội dung:");
        lblBody.setFont(new Font("Arial", Font.BOLD, 13));
        lblBody.setBorder(BorderFactory. createEmptyBorder(5, 5, 5, 0));
        
        txtBody = new JTextArea();
        txtBody.setFont(new Font("Arial", Font.PLAIN, 14));
        txtBody.setLineWrap(true);
        txtBody. setWrapStyleWord(true);
        txtBody.setBorder(BorderFactory. createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(txtBody);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color. LIGHT_GRAY));
        
        centerPanel.add(lblBody, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // ===== BOTTOM PANEL (Buttons) =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Status
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 12));
        lblStatus.setForeground(Color.GRAY);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Arial", Font. PLAIN, 13));
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        
        btnSend = new JButton("📤 Gửi");
        btnSend.setFont(new Font("Arial", Font.BOLD, 13));
        btnSend.setBackground(new Color(70, 130, 180));
        btnSend.setForeground(Color. WHITE);
        btnSend.setPreferredSize(new Dimension(100, 35));
        btnSend.setFocusPainted(false);
        btnSend.setCursor(new Cursor(Cursor. HAND_CURSOR));
        btnSend.addActionListener(e -> sendEmail());
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSend);
        
        bottomPanel.add(lblStatus, BorderLayout. WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Add to main
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout. SOUTH);
        
        add(mainPanel);
        
        // Keyboard shortcuts
        txtTo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtSubject.requestFocus();
                }
            }
        });
        
        txtSubject.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtBody. requestFocus();
                }
            }
        });
        
        // Ctrl+Enter để gửi
        txtBody.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendEmail();
                }
            }
        });
    }
    
    // ========== GỬI EMAIL ==========
    private void sendEmail() {
        String to = txtTo.getText().trim();
        String subject = txtSubject.getText().trim();
        String body = txtBody.getText().trim();
        
        // Validate
        if (to.isEmpty()) {
            showError("Vui lòng nhập địa chỉ người nhận!");
            txtTo.requestFocus();
            return;
        }
        
        if (! to.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Địa chỉ email không hợp lệ!");
            txtTo.requestFocus();
            return;
        }
        
        if (subject.isEmpty()) {
            int confirm = JOptionPane. showConfirmDialog(this,
                "Email không có tiêu đề.  Bạn vẫn muốn gửi?",
                "Xác nhận",
                JOptionPane. YES_NO_OPTION);
            
            if (confirm != JOptionPane.YES_OPTION) {
                txtSubject.requestFocus();
                return;
            }
            subject = "(Không có tiêu đề)";
        }
        
        if (body.isEmpty()) {
            int confirm = JOptionPane. showConfirmDialog(this,
                "Email không có nội dung. Bạn vẫn muốn gửi?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm != JOptionPane.YES_OPTION) {
                txtBody.requestFocus();
                return;
            }
        }
        
        // Disable buttons
        setButtonsEnabled(false);
        lblStatus.setForeground(Color.BLUE);
        lblStatus.setText("Đang gửi.. .");
        
        // Gửi trong thread riêng
        final String finalSubject = subject;
        
        new Thread(() -> {
            boolean success = client.sendEmail(to, finalSubject, body);
            
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Gửi email thành công! ",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    showError("Gửi email thất bại!  Kiểm tra lại địa chỉ người nhận.");
                    setButtonsEnabled(true);
                }
            });
        }).start();
    }
    
    // ========== HELPER METHODS ==========
    private void showError(String message) {
        lblStatus. setForeground(Color.RED);
        lblStatus.setText(message);
    }
    
    private void setButtonsEnabled(boolean enabled) {
        btnSend.setEnabled(enabled);
        btnCancel. setEnabled(enabled);
        txtTo.setEnabled(enabled);
        txtSubject.setEnabled(enabled);
        txtBody.setEnabled(enabled);
    }
}