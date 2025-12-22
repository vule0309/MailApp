package client.ui;

import client.network. MailClient;
import client. model.Email;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java. awt.event.*;
import java.util.List;

public class MainFrame extends JFrame {
    
    private MailClient client;
    
    // Components
    private JTable emailTable;
    private DefaultTableModel tableModel;
    private JButton btnCompose;
    private JButton btnRefresh;
    private JButton btnDelete;
    private JButton btnLogout;
    private JButton btnInbox;
    private JButton btnSent;
    private JLabel lblStatus;
    private JLabel lblUser;
    
    // Trạng thái hiện tại
    private String currentFolder = "inbox";
    private List<Email> currentEmails;
    
    // ========== CONSTRUCTOR ==========
    public MainFrame() {
        client = MailClient.getInstance();
        initComponents();
        loadEmails();
    }
    
    // ========== KHỞI TẠO GIAO DIỆN ==========
    private void initComponents() {
        setTitle("📧 Mail App - " + client.getUserEmail());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 500));
        
        // Layout chính
        setLayout(new BorderLayout());
        
        // ===== TOP PANEL (Header) =====
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // ===== LEFT PANEL (Sidebar) =====
        JPanel leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout. WEST);
        
        // ===== CENTER PANEL (Email List) =====
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // ===== BOTTOM PANEL (Status) =====
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout. SOUTH);
    }
    
    // ========== TOP PANEL ==========
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(70, 130, 180));
        panel.setPreferredSize(new Dimension(0, 60));
        panel.setBorder(BorderFactory. createEmptyBorder(10, 15, 10, 15));
        
        // Logo & Title
        JLabel lblTitle = new JLabel("📧 MAIL APPLICATION");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        
        // User info & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);
        
        lblUser = new JLabel("👤 " + client.getUsername());
        lblUser.setFont(new Font("Arial", Font.PLAIN, 14));
        lblUser.setForeground(Color.WHITE);
        
        btnLogout = new JButton("Đăng xuất");
        btnLogout. setFont(new Font("Arial", Font. PLAIN, 12));
        btnLogout.setBackground(new Color(220, 80, 80));
        btnLogout. setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor. HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());
        
        userPanel.add(lblUser);
        userPanel.add(Box.createHorizontalStrut(15));
        userPanel.add(btnLogout);
        
        panel.add(lblTitle, BorderLayout. WEST);
        panel.add(userPanel, BorderLayout. EAST);
        
        return panel;
    }
    
    // ========== LEFT PANEL (Sidebar) ==========
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 250));
        panel.setPreferredSize(new Dimension(180, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        // Compose Button
        btnCompose = new JButton("✏️ Soạn Email");
        btnCompose. setFont(new Font("Arial", Font. BOLD, 14));
        btnCompose. setBackground(new Color(60, 179, 113));
        btnCompose. setForeground(Color.WHITE);
        btnCompose. setMaximumSize(new Dimension(160, 40));
        btnCompose. setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCompose.setFocusPainted(false);
        btnCompose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCompose. addActionListener(e -> openCompose());
        
        // Separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(160, 1));
        
        // Inbox Button
        btnInbox = new JButton("📥 Hộp thư đến");
        styleFolderButton(btnInbox, true);
        btnInbox.addActionListener(e -> switchFolder("inbox"));
        
        // Sent Button
        btnSent = new JButton("📤 Đã gửi");
        styleFolderButton(btnSent, false);
        btnSent.addActionListener(e -> switchFolder("sent"));
        
        // Refresh Button
        btnRefresh = new JButton("🔄 Làm mới");
        btnRefresh.setFont(new Font("Arial", Font.PLAIN, 13));
        btnRefresh.setMaximumSize(new Dimension(160, 35));
        btnRefresh.setAlignmentX(Component. CENTER_ALIGNMENT);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadEmails());
        
        // Add components
        panel.add(btnCompose);
        panel.add(Box.createVerticalStrut(20));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(15));
        panel.add(btnInbox);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnSent);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnRefresh);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    // ========== CENTER PANEL (Email List) ==========
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory. createEmptyBorder(10, 10, 10, 10));
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(new Color(250, 250, 252));
        toolbar.setBorder(BorderFactory. createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        
        btnDelete = new JButton("🗑️ Xóa");
        btnDelete. setFont(new Font("Arial", Font. PLAIN, 12));
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> deleteSelectedEmail());
        
        toolbar.add(btnDelete);
        
        // Table
        String[] columns = {"", "Từ/Đến", "Tiêu đề", "Thời gian", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        emailTable = new JTable(tableModel);
        emailTable.setFont(new Font("Arial", Font.PLAIN, 13));
        emailTable.setRowHeight(35);
        emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailTable.setShowGrid(false);
        emailTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Column widths
        emailTable.getColumnModel().getColumn(0).setPreferredWidth(30);  // ID (hidden)
        emailTable.getColumnModel().getColumn(1).setPreferredWidth(180); // From/To
        emailTable.getColumnModel().getColumn(2).setPreferredWidth(300); // Subject
        emailTable.getColumnModel().getColumn(3).setPreferredWidth(130); // Time
        emailTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
        
        // Ẩn cột ID
        emailTable.getColumnModel().getColumn(0).setMinWidth(0);
        emailTable.getColumnModel().getColumn(0).setMaxWidth(0);
        emailTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Header style
        JTableHeader header = emailTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(new Color(240, 240, 245));
        header.setPreferredSize(new Dimension(0, 35));
        
        // Alternate row colors
        emailTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);
                
                if (! isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 252));
                }
                
                // Bold cho email chưa đọc
                if (column == 2 && currentEmails != null && row < currentEmails. size()) {
                    Email email = currentEmails.get(row);
                    if (! email.isRead() && currentFolder. equals("inbox")) {
                        c. setFont(new Font("Arial", Font. BOLD, 13));
                    }
                }
                
                return c;
            }
        });
        
        // Double-click để mở email
        emailTable. addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedEmail();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(emailTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ========== BOTTOM PANEL (Status) ==========
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        lblStatus = new JLabel("Sẵn sàng");
        lblStatus. setFont(new Font("Arial", Font. PLAIN, 12));
        lblStatus.setForeground(Color. GRAY);
        
        panel.add(lblStatus, BorderLayout.WEST);
        
        return panel;
    }
    
    // ========== STYLE FOLDER BUTTON ==========
    private void styleFolderButton(JButton button, boolean selected) {
        button.setFont(new Font("Arial", Font.PLAIN, 13));
        button.setMaximumSize(new Dimension(160, 35));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor. HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory. createEmptyBorder(8, 15, 8, 15));
        
        if (selected) {
            button.setBackground(new Color(200, 220, 240));
            button. setForeground(new Color(50, 100, 150));
        } else {
            button. setBackground(null);
            button. setForeground(Color. DARK_GRAY);
        }
    }
    
    // ========== CHUYỂN FOLDER ==========
    private void switchFolder(String folder) {
        currentFolder = folder;
        
        // Update button styles
        styleFolderButton(btnInbox, folder.equals("inbox"));
        styleFolderButton(btnSent, folder.equals("sent"));
        
        // Update table header
        if (folder.equals("inbox")) {
            tableModel.setColumnIdentifiers(new String[]{"", "Từ", "Tiêu đề", "Thời gian", "Trạng thái"});
        } else {
            tableModel.setColumnIdentifiers(new String[]{"", "Đến", "Tiêu đề", "Thời gian", ""});
        }
        
        // Reload emails
        loadEmails();
    }
    
    // ========== LOAD EMAILS ==========
    private void loadEmails() {
        setStatus("Đang tải.. .");
        btnRefresh.setEnabled(false);
        
        new Thread(() -> {
            try {
                if (currentFolder.equals("inbox")) {
                    currentEmails = client.getInbox();
                } else {
                    currentEmails = client.getSentEmails();
                }
                
                SwingUtilities.invokeLater(() -> {
                    updateTable();
                    setStatus("Tổng cộng: " + currentEmails.size() + " email");
                    btnRefresh.setEnabled(true);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Lỗi tải email: " + e. getMessage());
                    btnRefresh.setEnabled(true);
                });
            }
        }).start();
    }
    
    // ========== CẬP NHẬT BẢNG ==========
    private void updateTable() {
        tableModel.setRowCount(0);
        
        if (currentEmails == null || currentEmails.isEmpty()) {
            return;
        }
        
        for (Email email : currentEmails) {
            Object[] row;
            
            if (currentFolder.equals("inbox")) {
                row = new Object[]{
                    email. getId(),
                    email.getSenderEmail(),
                    email.getDisplaySubject(),
                    email.getFormattedTime(),
                    email.isRead() ? "Đã đọc" : "⚫ Mới"
                };
            } else {
                row = new Object[]{
                    email.getId(),
                    email.getRecipientEmail(),
                    email.getDisplaySubject(),
                    email.getFormattedTime(),
                    ""
                };
            }
            
            tableModel.addRow(row);
        }
    }
    
    // ========== MỞ EMAIL ĐÃ CHỌN ==========
    private void openSelectedEmail() {
        int selectedRow = emailTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn một email!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Email email = currentEmails.get(selectedRow);
        new MailDetailFrame(email, currentFolder).setVisible(true);
        
        // Refresh sau khi đọc
        loadEmails();
    }
    
    // ========== XÓA EMAIL ==========
    private void deleteSelectedEmail() {
        int selectedRow = emailTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn email cần xóa!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane. showConfirmDialog(this,
            "Bạn có chắc muốn xóa email này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane. YES_OPTION) {
            Email email = currentEmails. get(selectedRow);
            boolean isSender = currentFolder.equals("sent");
            
            new Thread(() -> {
                boolean success = client.deleteEmail(email.getId(), isSender);
                
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        setStatus("Đã xóa email");
                        loadEmails();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Không thể xóa email! ",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
        }
    }
    
    // ========== MỞ SOẠN EMAIL ==========
    private void openCompose() {
        new ComposeFrame().setVisible(true);
    }
    
    // ========== ĐĂNG XUẤT ==========
    private void logout() {
        int confirm = JOptionPane. showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane. YES_OPTION) {
            client. logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }
    
    // ========== SET STATUS ==========
    private void setStatus(String status) {
        lblStatus.setText(status);
    }
    
    // ========== LẤY DANH SÁCH ĐÃ GỬI (Thêm vào MailClient nếu chưa có) ==========
    private List<Email> getSentEmails() {
        // Sẽ implement trong MailClient
        return client.getInbox(); // Tạm thời
    }
}