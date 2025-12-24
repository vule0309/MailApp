package database;


import common.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    
    // Constructor - kết nối database
    private DatabaseConnection() {
        try {
            // Load MySQL Driver
            Class. forName("com.mysql.cj.jdbc.Driver");
            
            // Tạo kết nối
            this.connection = DriverManager.getConnection(
                Config.DB_URL, 
                Config.DB_USER, 
                Config.DB_PASSWORD
            );
            System.out.println("✅ Kết nối database thành công!");
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Không tìm thấy MySQL Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối database!");
            e.printStackTrace();
        }
    }
    
    // Singleton pattern - chỉ tạo 1 instance duy nhất
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    // Lấy connection
    public Connection getConnection() {
        try {
            // Kiểm tra nếu connection bị đóng thì tạo lại
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(
                    Config.DB_URL, 
                    Config.DB_USER, 
                    Config.DB_PASSWORD
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
    // Đóng kết nối
    public void closeConnection() {
        try {
            if (connection != null && ! connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Đã đóng kết nối database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
