package companies;

import address.Address;
import database.Db;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CompaniesPanel extends JPanel {
    private final int userID;
    private final String role;

    private final JTextField name = new JTextField(20);

    private final JComboBox<Address> locationCombo = new JComboBox<>();

    private final JTable table = new JTable();
    private final JScrollPane scrollPane = new JScrollPane(table);
    private final DefaultTableModel model = new DefaultTableModel();

    public CompaniesPanel(int userID, String role) {
        this.userID = userID;
        this.role = role;

        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton submitButton = new JButton("Create Company");
        JButton deleteButton = new JButton("Delete Company");
        JButton updateButton = new JButton("Update Company");
        if (role.equalsIgnoreCase("Admin")) {
            gbc.gridx = 0; gbc.gridy = 0;
            JLabel nameLabel = new JLabel("Company Name");
            formPanel.add(nameLabel, gbc);
            gbc.gridx = 1;
            formPanel.add(name, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            JLabel locationLabel = new JLabel("Location");
            formPanel.add(locationLabel, gbc);
            gbc.gridx = 1;
            formPanel.add(locationCombo, gbc);
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(submitButton, gbc);
            gbc.gridx = 1;
            formPanel.add(updateButton, gbc);
            gbc.gridx = 2;
            formPanel.add(deleteButton, gbc);
        }

        add(formPanel, BorderLayout.NORTH);

        createTable();
        add(scrollPane, BorderLayout.CENTER);


        loadLocations();
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


    private void loadLocations() {
        try (var conn = Db.getConnection()) {
            var stmt = conn.prepareStatement("SELECT address_id AS id, city, country, created_at FROM address ORDER BY city");
            var rs = stmt.executeQuery();

            locationCombo.removeAllItems();
            while (rs.next()) {
                locationCombo.addItem(new Address(
                        rs.getInt("id"),
                        rs.getString("city"),
                        rs.getString("country"),
                        rs.getString("created_at")
                ));
            }
            locationCombo.setSelectedIndex(-1);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createCompany() {
        if (name.getText().isEmpty() || locationCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Address location = (Address) locationCombo.getSelectedItem();

        try (var conn = Db.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO companies (name, location) VALUES (?,?)");
            stmt.setString(1, name.getText());
            stmt.setInt(2, location.getId());
            int created = stmt.executeUpdate();

            if (created > 0) {
                clearFields();
                model.setRowCount(0);
                loadCompanies();
                JOptionPane.showMessageDialog(this, "Company Created", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Company not created", "Error", JOptionPane.ERROR_MESSAGE);
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
            var stmt = conn.prepareStatement("UPDATE companies SET name = ?, location = ? WHERE company_id = ?");
            stmt.setString(1, name.getText().isEmpty() ? (String) table.getValueAt(selectedRow, 1) : name.getText());

            Address location = (Address) locationCombo.getSelectedItem();
            if (location != null) {
                stmt.setInt(2, location.getId());
            } else {
                stmt.setInt(2, (Integer) table.getValueAt(selectedRow, 2));
            }

            stmt.setInt(3, companyId);
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
        locationCombo.setSelectedIndex(-1);
    }

    public void loadCompanies() {
        try (var conn = Db.getConnection()) {
            String sql;
            if (role.equalsIgnoreCase("Admin")) {
                sql = """
                        SELECT c.company_id, c.name, a.city AS location, m.name AS employer_name, m.email AS employer_email, c.created_at
                        FROM companies c
                                 JOIN users u
                                 JOIN managers m
                                    ON u.user_id = m.manager_id AND m.company_id = c.company_id
                                 JOIN address a
                        ON a.address_id = c.location""";
            }
            else if (role.equalsIgnoreCase("Employer")) {
                sql = """
                        SELECT c.company_id, c.name, a.city AS location, u.username AS employer_name, m.email AS employer_email, c.created_at
                        FROM companies c
                            JOIN users u
                                JOIN managers m
                                ON m.manager_id = u.user_id
                            JOIN address a
                                ON a.address_id = c.location
                            WHERE manager_id = ?;
                        """;
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
                        rs.getString("location"), // city name
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
