package jobseekers;

import address.Address;
import database.Db;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.Statement;

public class JobSeekersPanel extends JPanel {
    JComboBox<Address> addressCombo = new JComboBox<>();
    JLabel nameLabel = new JLabel("JobSeeker Name");
    JTextField nameField = new JTextField(20);
    JLabel usernameLabel = new JLabel("Seeker Username");
    JTextField usernameField = new JTextField(20);
    JLabel emailLabel = new JLabel("Seeker Email");
    JTextField emailField = new JTextField(20);
    JLabel passwordLabel = new JLabel("Seeker Password");
    JTextField passwordField = new JTextField(20);
    JButton submitButton = new JButton("Submit Seeker");
    JButton updateButton = new JButton("Update Seeker");
    JButton deleteButton = new JButton("Delete Seeker");
    DefaultTableModel model = new DefaultTableModel();
    JTable table = new JTable(model);
    JScrollPane tablePane = new JScrollPane(table);
    public JobSeekersPanel() {
        initComponents();
        formComponents();
        createTable();
        loadJobSeekers();
        fetchAddresses();
        submitButton.addActionListener(_-> createJobSeeker());
        updateButton.addActionListener(_-> updateJobSeeker());
        deleteButton.addActionListener(_-> deleteJobSeeker());
    }


    private void initComponents() {
        setLayout(new BorderLayout());
    }
    private void createTable(){
        String[] columns = {"Seeker ID", "Name", "Email", "Address", "created_at"};
        model.setColumnIdentifiers(columns);
        table.setRowHeight(40);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePane.setPreferredSize(new Dimension(1200, 400));
        table.getColumnModel().getColumn(0).setPreferredWidth(5);
        add(tablePane, BorderLayout.CENTER);
    }
    private void formComponents() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Jobseeker Form"));
        panel.setPreferredSize(new Dimension(1200, 100));
        addressCombo.setSelectedIndex(-1);
        addressCombo.setPreferredSize(new Dimension(150, 25));
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(addressCombo);
        panel.add(submitButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        add(panel, BorderLayout.NORTH);
    }
    private void fetchAddresses() {
        try (var conn = Db.getConnection()) {
            var stmt = conn.prepareStatement("SELECT address_id AS id, city, country, created_at FROM address ORDER BY city");
            var result = stmt.executeQuery();
            addressCombo.removeAllItems();
            while (result.next()) {
                addressCombo.addItem(new Address(
                        result.getInt("id"),
                        result.getString("city"),
                        result.getString("country"),
                        result.getString("created_at")
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
    private void  createJobSeeker() {
        if (nameField.getText().isEmpty() ||
                usernameField.getText().isEmpty() ||
                addressCombo.getSelectedItem() == null ||
                emailField.getText().isEmpty() ||
                passwordField.getText().isEmpty()
        ) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try (var conn = Db.getConnection()) {
                conn.setAutoCommit(false);
                Address address = (Address) addressCombo.getSelectedItem();
                String name = nameField.getText();
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = passwordField.getText();
                int userId = -1;
                try (var createUserStatement = conn.prepareStatement(
                        "INSERT INTO users (username, password) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                )) {
                    createUserStatement.setString(1, username);
                    createUserStatement.setString(2, password);

                    int created = createUserStatement.executeUpdate();

                    if (created > 0) {
                        try (ResultSet rs = createUserStatement.getGeneratedKeys()) {
                            if (rs.next()) {
                                userId = rs.getInt(1);
                            }
                        }
                    }
                }
                if (userId > 0) {
                    try (var stmt = conn.prepareStatement(
                            "INSERT INTO jobseekers (jobseeker_id, name, email, address_id) VALUES (?, ?, ?, ?)"
                    )) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, name);
                        stmt.setString(3, email);
                        stmt.setInt(4, address.getId());

                        int res = stmt.executeUpdate();

                        if (res > 0) {
                            conn.commit();
                            model.setRowCount(0);
                            loadJobSeekers();
                            clearForm();
                            JOptionPane.showMessageDialog(this, "Jobseeker Created", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this, "Jobseeker Not Created", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "User not created", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        }

    }
    private void loadJobSeekers() {
        try (var conn = Db.getConnection()) {
            var stmt = conn.prepareStatement("""
                    SELECT j.jobseeker_id, j.name, j.email, a.city, j.created_at FROM jobseekers j JOIN users u
                    ON j.jobseeker_id = u.user_id JOIN address a ON a.address_id = j.address_id
                """);
            var result = stmt.executeQuery();
            while (result.next()) {
                int id = result.getInt("jobseeker_id");
                String name = result.getString("name");
                String email = result.getString("email");
                String city = result.getString("city");
                String createdAt = result.getString("created_at");
                model.addRow(new Object[]{id, name, email, city, createdAt});
            }
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }    }
    private void clearForm() {
        nameField.setText("");
        usernameField.setText("");
        emailField.setText("");
        addressCombo.setSelectedIndex(-1);
    }
    private void updateJobSeeker() {
        try (var conn = Db.getConnection()) {
            int  selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a row first", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int jobSeekerId = (int) model.getValueAt(selectedRow, 0);
            String nameColumn = model.getValueAt(selectedRow, 1).toString();
            String emailColumn = model.getValueAt(selectedRow, 2).toString();
            String name = nameField.getText();
            String email = emailField.getText();
            Address address = (Address) addressCombo.getSelectedItem();
            var stmt = conn.prepareStatement("UPDATE jobseekers SET name = ?, email = ?, address_id = ? WHERE jobseeker_id = ?");
            stmt.setString(1, name.isEmpty() ? nameColumn : name);
            stmt.setString(2, email.isEmpty() ? emailColumn : email);
            stmt.setInt(3, address.getId());
            stmt.setInt(4, jobSeekerId);
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                model.removeRow(selectedRow);
                model.setRowCount(0);
                loadJobSeekers();
                clearForm();
                JOptionPane.showMessageDialog(this, "Jobseeker Updated", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void deleteJobSeeker() {
        var selectedRow = table.getSelectedRow();
        var seekerId = model.getValueAt(selectedRow, 0);
        var id = Integer.parseInt(seekerId.toString());
        try (var conn = Db.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM jobseekers WHERE jobseeker_id = ?");
            stmt.setInt(1, id);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                model.removeRow(selectedRow);
                model.setRowCount(0);
                loadJobSeekers();
                clearForm();
                JOptionPane.showMessageDialog(this, "Jobseeker Deleted", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
