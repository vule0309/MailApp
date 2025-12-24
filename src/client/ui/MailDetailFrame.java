package client.ui;

import client.network.MailClient;
import client.model.Email;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MailDetailFrame extends JFrame {
    
    private MailClient client;
    private Email email;
    private String folder;
    
    private JButton btnReply;
    private JButton btnDelete;
    private JButton btnClose;
    
    // ========== CONSTRUCTOR ==========
    public MailDetailFrame(Email email, String folder) {
        this.client = MailClient. getInstance();
        this.email = email;
        this.folder = folder;
        
        // Load chi tiết email từ server
        loadEmailDetail();
        initComponents();
    }
    
    // ========== LOAD CHI TIẾT EMAIL ==========
    private void loadEmailDetail() {
        Email detailEmail = client. readEmail(email.getId());
        if (detailEmail != null) {
            email.setBody(detailEmail. getBody());
            email.setRecipientEmail(detailEmail.getRecipientEmail());
            email.setSenderEmail(detailEmail.getSenderEmail());
        }
    }
    
    // ========== KHỞI TẠO GIAO DIỆN ==========
    private void initComponents() {
        setTitle("📧 " + email.getDisplaySubject());
        setSize(650, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 400));
        
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        
        // ===== TOP PANEL (Header Info) =====
        JPanel topPanel = new JPanel();
        topPanel. setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(248, 248, 252));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 235)),
            BorderFactory. createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Subject
        JLabel lblSubject = new JLabel(email.getDisplaySubject());
        lblSubject.setFont(new Font("Arial", Font.BOLD, 18));
        lblSubject.setForeground(new Color(50, 50, 50));
        lblSubject.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // From
        JPanel fromPanel = createInfoRow("Từ:", email. getSenderEmail());
        
        // To
        JPanel toPanel = createInfoRow("Đến:", 
            email.getRecipientEmail() != null ? email. getRecipientEmail() : client.getUserEmail());
        
        // Date
        JPanel datePanel = createInfoRow("Thời gian:", email.getFormattedTime());
        
        topPanel.add(lblSubject);
        topPanel.add(Box.createVerticalStrut(12));
        topPanel.add(fromPanel);
        topPanel.add(Box. createVerticalStrut(5));
        topPanel.add(toPanel);
        topPanel.add(Box. createVerticalStrut(5));
        topPanel.add(datePanel);
        
        // ===== CENTER PANEL (Body) =====
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel. setBackground(Color. WHITE);
        
        JTextArea txtBody = new JTextArea();
        txtBody.setText(email.getBody() != null ? email. getBody() : "(Không có nội dung)");
        txtBody.setFont(new Font("Arial", Font. PLAIN, 14));
        txtBody. setEditable(false);
        txtBody.setLineWrap(true);
        txtBody. setWrapStyleWord(true);
        txtBody.setBackground(Color.WHITE);
        txtBody.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        txtBody.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(txtBody);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235)));
        
        centerPanel.add(scrollPane, BorderLayout. CENTER);
        
        // ===== BOTTOM PANEL (Buttons) =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel. setBackground(Color. WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Arial", Font.PLAIN, 13));
        btnClose.setPreferredSize(new Dimension(90, 35));
        btnClose.setFocusPainted(false);
        btnClose. setCursor(new Cursor(Cursor. HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        
        btnDelete = new JButton("Xóa");
        btnDelete. setFont(new Font("Arial", Font. PLAIN, 13));
        btnDelete.setPreferredSize(new Dimension(90, 35));
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> deleteEmail());
        
        btnReply = new JButton("Trả lời");
        btnReply. setFont(new Font("Arial", Font. BOLD, 13));
        btnReply. setBackground(new Color(70, 130, 180));
        btnReply.setForeground(Color. WHITE);
        btnReply.setPreferredSize(new Dimension(100, 35));
        btnReply. setFocusPainted(false);
        btnReply. setCursor(new Cursor(Cursor. HAND_CURSOR));
        btnReply.addActionListener(e -> replyEmail());
        
        // Chỉ hiện nút Reply nếu đang ở inbox
        if (folder.equals("inbox")) {
            bottomPanel.add(btnReply);
        }
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnClose);
        
        // Add to main
        mainPanel.add(topPanel, BorderLayout. NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    // ========== TẠO ROW THÔNG TIN ==========
    private JPanel createInfoRow(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblLabel = new JLabel(label + " ");
        lblLabel. setFont(new Font("Arial", Font. BOLD, 13));
        lblLabel.setForeground(Color.GRAY);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 13));
        lblValue.setForeground(Color.DARK_GRAY);
        
        panel. add(lblLabel);
        panel.add(lblValue);
        
        return panel;
    }
    
    // ========== TRẢ LỜI EMAIL ==========
    private void replyEmail() {
        String replyTo = email.getSenderEmail();
        String replySubject = "Re: " + email. getDisplaySubject();
        
        new ComposeFrame(replyTo, replySubject).setVisible(true);
    }
    
    // ========== XÓA EMAIL ==========
    private void deleteEmail() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa email này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean isSender = folder.equals("sent");
            boolean success = client.deleteEmail(email.getId(), isSender);
            
            if (success) {
                JOptionPane. showMessageDialog(this,
                    "Đã xóa email! ",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Không thể xóa email!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}