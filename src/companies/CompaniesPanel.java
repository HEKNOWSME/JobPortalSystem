package companies;

import database.Db;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CompaniesPanel extends JPanel {
    private final int userID;
    private final String role;

    private final JLabel nameLabel = new JLabel("Company Name");
    private final JTextField name = new JTextField(20);

    private final JLabel locationLabel = new JLabel("Location");
    private final JTextField location = new JTextField(20);

    private final JLabel employerIdLabel = new JLabel("Employer ID");
    private final JTextField employer_id = new JTextField(20);

    private JTable table = new JTable();
    private JScrollPane scrollPane = new JScrollPane(table);
    private DefaultTableModel model = new DefaultTableModel();

    private JButton submitButton = new JButton("Create Company");
    private JButton updateButton = new JButton("Update Company");
    private JButton deleteButton = new JButton("Delete Company");

    public CompaniesPanel(int userID, String role) {
        this.userID = userID;
        this.role = role;

        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        if (role.equalsIgnoreCase("Admin")) {
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(nameLabel, gbc);
            gbc.gridx = 1;
            formPanel.add(name, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(locationLabel, gbc);
            gbc.gridx = 1;
            formPanel.add(location, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(employerIdLabel, gbc);
            gbc.gridx = 1;
            formPanel.add(employer_id, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(submitButton, gbc);
            gbc.gridx = 1;
            formPanel.add(updateButton, gbc);
            gbc.gridx = 2;
            formPanel.add(deleteButton, gbc);
        }

        add(formPanel, BorderLayout.NORTH);


        createTable();
        add(scrollPane, BorderLayout.CENTER);

        if (role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Employer")) {
            loadCompanies();
        }

        submitButton.addActionListener(e -> createCompany());
        updateButton.addActionListener(e -> updateCompany());
        deleteButton.addActionListener(e -> deleteCompany());
    }

    private void createTable() {
        String[] columns = {"Company ID", "Name", "Location", "Employer Name", "Employer Email", "Created At"};
        model.setColumnIdentifiers(columns);
        table.setModel(model);
        table.setRowHeight(40);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setPreferredSize(new Dimension(1200, 400));
    }

    private void createCompany() {
        if (name.getText().isEmpty() || location.getText().isEmpty() || employer_id.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (var conn = Db.getConnection()) {
            int employer = Integer.parseInt(employer_id.getText());
            var checkEmployer = conn.prepareStatement("SELECT * FROM users WHERE user_id = ? AND role = 'employer'");
            checkEmployer.setInt(1, employer);
            var rs = checkEmployer.executeQuery();

            if (rs.next()) {
                var stmt = conn.prepareStatement("INSERT INTO companies (name, location, user_id) VALUES (?,?,?)");
                stmt.setString(1, name.getText());
                stmt.setString(2, location.getText());
                stmt.setInt(3, employer);
                int created = stmt.executeUpdate();

                if (created > 0) {
                    clearFields();
                    model.setRowCount(0);
                    loadCompanies();
                    JOptionPane.showMessageDialog(this, "Company Created", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Company not created", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Employer not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCompany() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a company first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int companyId = (int) table.getValueAt(selectedRow, 0);

        try (var conn = Db.getConnection()) {
            int employer = employer_id.getText().isEmpty() ? -1 : Integer.parseInt(employer_id.getText());

            if (employer != -1) {
                var checkEmployer = conn.prepareStatement("SELECT * FROM users WHERE user_id = ? AND role = 'employer'");
                checkEmployer.setInt(1, employer);
                var rs = checkEmployer.executeQuery();
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Employer not found", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            var stmt = conn.prepareStatement("UPDATE companies SET name = ?, location = ?, user_id = ? WHERE company_id = ?");
            stmt.setString(1, name.getText().isEmpty() ? (String) table.getValueAt(selectedRow, 1) : name.getText());
            stmt.setString(2, location.getText().isEmpty() ? (String) table.getValueAt(selectedRow, 2) : location.getText());
            stmt.setInt(3, employer != -1 ? employer : (int) table.getValueAt(selectedRow, 0));
            stmt.setInt(4, companyId);
            stmt.executeUpdate();

            clearFields();
            model.setRowCount(0);
            loadCompanies();
            JOptionPane.showMessageDialog(this, "Company Updated", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCompany() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a company first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int companyId = (int) table.getValueAt(selectedRow, 0);

        try (var conn = Db.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM companies WHERE company_id = ?");
            stmt.setInt(1, companyId);
            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Company Deleted", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Company Not Deleted", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        name.setText("");
        location.setText("");
        employer_id.setText("");
    }

    public void loadCompanies() {
        try (var conn = Db.getConnection()) {
            String sql;
            if (role.equalsIgnoreCase("Admin")) {
                sql = "SELECT c.company_id, c.name, c.location, u.username AS employer_name, u.email AS employer_email, c.created_at " +
                        "FROM companies c JOIN users u ON c.user_id = u.user_id";
            } else if (role.equalsIgnoreCase("Employer")) {
                sql = "SELECT c.company_id, c.name, c.location, u.username AS employer_name, u.email AS employer_email, c.created_at " +
                        "FROM companies c JOIN users u ON c.user_id = u.user_id WHERE c.user_id = ?";
            } else {
                return;
            }

            var stmt = conn.prepareStatement(sql);
            if (role.equalsIgnoreCase("Employer")) stmt.setInt(1, userID);

            var rs = stmt.executeQuery();
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("company_id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("employer_name"),
                        rs.getString("employer_email"),
                        rs.getTimestamp("created_at")
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
