package users;

import database.Db;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UsersPanel extends JPanel {
    private int userID;
    JTable table = new JTable();
    JScrollPane scrollPane = new JScrollPane(table);
    DefaultTableModel model = new DefaultTableModel();
    public UsersPanel(int userID) {
        this.userID = userID;
        init();
        createTable();
        loadUsers();
    }
    private void init() {
        setLayout(null);
    }
    private void  createTable() {
        String[] usersColumns = {"UserId","Username", "Email", "password", "Role", "Created At"};
        for (var col : usersColumns) {
            model.addColumn(col);
        }
        table.setModel(model);
        scrollPane.setBounds(10, 10, 1250, 400);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(5);
        table.setRowHeight(50);
        add(scrollPane);
    }
    private void loadUsers() {
        try (var conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            var statement = conn.prepareStatement("""
                    SELECT u.user_id, u.username, j.email, u.password, u.created_at, u.role  FROM users u
                    JOIN jobseekers j ON j.jobseeker_id = u.user_id
                    WHERE NOT u.role = 'admin'""");
            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                model.addRow(new Object[]{
                        resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        resultSet.getString("role"),
                        resultSet.getString("created_at"),
                });
            }
            var anotherOne = conn.prepareStatement("""
                    SELECT u.user_id, u.username, m.email, u.password, u.created_at, u.role  FROM users u
                    JOIN managers m ON m.manager_id = u.user_id
                    WHERE NOT u.role = 'admin'""");
            var anotherOneResult = anotherOne.executeQuery();
            while (anotherOneResult.next()) {
                model.addRow(new Object[]{anotherOneResult.getInt("user_id"),
                        anotherOneResult.getString("username"),
                        anotherOneResult.getString("email"),
                        anotherOneResult.getString("password"),
                        anotherOneResult.getString("role"),
                        anotherOneResult.getString("created_at")});
            }
            conn.commit();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error For loading", JOptionPane.ERROR_MESSAGE);
        }
    }
}
