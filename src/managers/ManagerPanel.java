package managers;
import address.Address;
import database.Db;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ManagerPanel extends JPanel {
    private DefaultTableModel model = new DefaultTableModel();
    private JTable table = new JTable(model);
    private JScrollPane tablePane = new JScrollPane(table);
    JLabel managerNameLabel = new JLabel("Manager Name");
    JLabel usernameLabel = new JLabel("Manager Username");
    JTextField usernameField = new JTextField(20);
    JLabel passwordLabel = new JLabel("Manager Password");
    JTextField passwordField = new JTextField(20);
    JTextField nameField = new JTextField(20);
    JLabel emailLabel = new JLabel("Manager Email");
    JTextField emailField = new JTextField(20);
    JComboBox<Address> addressCombo = new JComboBox<>();
    JButton updateButton = new JButton("Update Manager");
    JButton deleteButton = new JButton("Delete Manager");

    public ManagerPanel() {
        initComponents();
        formComponents();
        fetchAddresses();
        createTable();
        loadManagers();
        updateButton.addActionListener(_-> updateManager());
        deleteButton.addActionListener(_-> deleteManager());
    }

    private void initComponents() {
        setLayout(new BorderLayout());
    }

    private void createTable(){
        String[] columns = {"Manager ID", "Name", "Email", "Address", "Company Name"};
        model.setColumnIdentifiers(columns);
        table.setRowHeight(40);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePane.setPreferredSize(new Dimension(1200, 400));
        table.getColumnModel().getColumn(0).setPreferredWidth(5);
        add(tablePane, BorderLayout.CENTER);
    }

    private void formComponents() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Manager Form"));
        panel.setPreferredSize(new Dimension(1200, 100));
        addressCombo.setPreferredSize(new Dimension(150, 25));
        addressCombo.setSelectedIndex(-1);
        panel.add(managerNameLabel);
        panel.add(nameField);
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(addressCombo);
        panel.add(updateButton);
        panel.add(deleteButton);
        add(panel, BorderLayout.NORTH);
    }
    private void deleteManager() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (var conn = Db.getConnection()) {
            int managerId = (int) model.getValueAt(selectedRow, 0);

            var stmt = conn.prepareStatement("DELETE FROM managers WHERE manager_id=?");
            stmt.setInt(1, managerId);
            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Manager deleted", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void loadManagers() {
        try (var conn = Db.getConnection()) {
            var stmt = conn.prepareStatement("""
            SELECT m.manager_id, m.name, m.email, a.city, c.name AS company, u.created_at
            FROM managers m
            JOIN users u ON m.manager_id = u.user_id
            JOIN address a ON m.address_id = a.address_id
            JOIN companies c ON m.company_id = c.company_id
        """);
            var rs = stmt.executeQuery();
            model.setRowCount(0);
            while (rs.next()) {
                int id = rs.getInt("manager_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String city = rs.getString("city");
                String company = rs.getString("company");
                String createdAt = rs.getString("created_at");
                model.addRow(new Object[]{id, name, email, city, company, createdAt});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        addressCombo.setSelectedIndex(-1);
    }
    private void updateManager() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (var conn = Db.getConnection()) {
            int managerId = (int) model.getValueAt(selectedRow, 0);
            String newName = nameField.getText().isEmpty() ? model.getValueAt(selectedRow, 1).toString() : nameField.getText();
            String newEmail = emailField.getText().isEmpty() ? model.getValueAt(selectedRow, 2).toString() : emailField.getText();
            Address newAddress = (Address) addressCombo.getSelectedItem();

            var stmt = conn.prepareStatement("UPDATE managers SET name=?, email=?, address_id=? WHERE manager_id=?");
            stmt.setString(1, newName);
            stmt.setString(2, newEmail);
            stmt.setInt(3, newAddress.getId());
            stmt.setInt(4, managerId);
            int updated = stmt.executeUpdate();

            if (updated > 0) {
                loadManagers();
                clearForm();
                JOptionPane.showMessageDialog(this, "Manager updated", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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



}
