package database;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db extends JOptionPane {
    public static Connection getConnection(){
        try {
            String url = "jdbc:mysql://localhost:3306/jobPortalSystem?createDatabaseIfNotExist=true";
            String user = "root";
            String password = "";
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed To connect to database");
            System.exit(0);
            return  null;
        }
    }
}
