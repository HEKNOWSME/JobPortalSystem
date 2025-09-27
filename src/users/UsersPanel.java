package users;

import database.Db;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UsersPanel extends JPanel {
    private int userID;
    private String role;
    private final JLabel usernameLabel = new JLabel("Username "); private final JTextField username = new JTextField(20);
    private final JLabel emailLabel = new JLabel("Email"); private final JTextField email = new JTextField(20);
    private final JLabel passwordLabel = new JLabel("Password"); private final JTextField password = new JTextField(20);
    JTable table = new JTable();
    JScrollPane scrollPane = new JScrollPane(table);
    DefaultTableModel model = new DefaultTableModel();
    JButton submitButton = new JButton("Create User"); JButton updateButton = new JButton("Update User"); JButton deleteButton = new JButton("Delete Userf");
    public UsersPanel(int userID,  String role) {
        this.userID = userID;
        this.role = role;
        init();
        createUIComponents();
        createSizeComponents();
        createTable();
        loadUsers();
        submitButton.addActionListener(_-> createJob());
        deleteButton.addActionListener(_-> deleteJob());
        updateButton.addActionListener(_-> updateJob());
    }
    private void init() {
        setLayout(null);
    }
    private void createUIComponents() {
        add(usernameLabel); add(username);
        add(emailLabel); add(email);
        add(passwordLabel);add(password); add(submitButton);
        add(deleteButton); add(updateButton);


    }
    private void  createSizeComponents() {
        usernameLabel.setBounds(10, 10, 100, 30);
        username.setBounds(130, 10, 200, 30);
        emailLabel.setBounds(10, 50, 100, 30);
        email.setBounds(130, 50, 200, 30);
        passwordLabel.setBounds(10, 90, 100, 30);
        password.setBounds(130, 90, 200, 30);
        submitButton.setBounds(350, 10, 200, 30);
        updateButton.setBounds(350, 50, 200, 30);
        deleteButton.setBounds(350, 90, 200, 30);
    }
    private void  createTable() {
        String[] usersColumns = {"UserId","Username", "Email", "password", "Role", "Created At"};
        for (var col : usersColumns) {
            model.addColumn(col);
        }
        table.setModel(model);
        scrollPane.setBounds(10, 200, 1250, 400);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(5);
        table.setRowHeight(50);
        add(scrollPane);
    }
    private void createJob() {
        if (username.getText().isEmpty()
                || email.getText().isEmpty()
                || password.getText().isEmpty()
                || username.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String username = this.username.getText();
            String emailText = email.getText();
            String password = this.password.getText();
            try(var conn = Db.getConnection()) {
                var fetchAllUsersExceptAdmin = conn.prepareStatement("SELECT * FROM users WHERE NOT user_id = ?");
                fetchAllUsersExceptAdmin.setInt(1, userID);
                var employerCompany = fetchAllUsersExceptAdmin.executeQuery();
                if (employerCompany.next()) {
                    var createStatement = conn.prepareStatement("INSERT INTO users (username, email, password) VALUES (?,?,?)");
                    createStatement.setString(1, username);
                    createStatement.setString(2, emailText);
                    createStatement.setString(3, password);
                    var created = createStatement.executeUpdate();
                    if (created > 0) {
                        clearFields();
                        model.setRowCount(0);
                        loadUsers();
                        JOptionPane.showMessageDialog(this, "User Created", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "User Not Created", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Employer Not Found", "Error", JOptionPane.ERROR_MESSAGE);

                }

            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);}
        }
    }
    private void clearFields() {
        username.setText("");
        email.setText("");
        password.setText("");
    }
    private void loadUsers() {
        try (var conn = Db.getConnection()) {
            var statement = conn.prepareStatement("SELECT * FROM users WHERE NOT user_id = ?");
            statement.setInt(1, userID);
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

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void deleteJob() {
        var selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selected Row Not Found", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String jobID = model.getValueAt(selectedRow, 0).toString();
            try (var conn = Db.getConnection()) {
                var statement = conn.prepareStatement("DELETE FROM jobs where job_id = ?");
                int id = Integer.parseInt(jobID);
                statement.setInt(1, id);
                var check = statement.executeUpdate();
                if (check < 0) {
                    JOptionPane.showMessageDialog(this, "Job Not Deleted Successfully", "Error", JOptionPane.ERROR_MESSAGE);

                } else {
                    model.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Job Deleted Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void updateJob() {
        var selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selected Row Not Found", "Error", JOptionPane.ERROR_MESSAGE);
        } else  {
            String selectedId = model.getValueAt(selectedRow, 0).toString();
            int id = Integer.parseInt(selectedId);
            try (var conn = Db.getConnection()) {
                var fetchJob = conn.prepareStatement("SELECT * FROM users where user_id = ?");
                fetchJob.setInt(1, id);
                var result = fetchJob.executeQuery();
                if (result.next()) {
                    var statement =  conn.prepareStatement("UPDATE users SET username = ?, email = ?, password =? WHERE user_id = ?");
                    statement.setString(1, username.getText().isEmpty() ? result.getString("username") : username.getText());
                    statement.setString(2, email.getText().isEmpty() ? result.getString("email") : email.getText());
                    statement.setString(3, password.getText().isEmpty() ? result.getString("password") : password.getText());
                    statement.setInt(4, id);
                    statement.executeUpdate();
                    model.setRowCount(0);
                    clearFields();
                    loadUsers();
                    JOptionPane.showMessageDialog(this, "User Updated", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else  {
                    JOptionPane.showMessageDialog(this, "User Not Found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
