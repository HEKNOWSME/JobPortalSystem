package address;

import database.Db;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AddressesPanel extends JPanel {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
    JLabel addressCity = new JLabel("Address City");
    JTextField city = new JTextField(20);
    JLabel countryLabel = new JLabel("Country");
    JTextField country = new JTextField(20);
    JButton submitButton = new JButton("Submit Address");
    JButton updateButton = new JButton("Update Address");
    JButton deleteButton = new JButton("Delete Address");
    DefaultTableModel model = new DefaultTableModel();
    JTable table = new JTable(model);
    JScrollPane tablePane = new JScrollPane(table);
    public AddressesPanel() {
        initComponents();
        formComponents();
        createTable();
        fetchAddresses();
        submitButton.addActionListener(_-> createAddress());
        deleteButton.addActionListener(_-> deleteAddress());
        updateButton.addActionListener(_-> updateAddress());
    }
    private void initComponents() {
        setLayout(new BorderLayout());
    }
    private void createTable(){
        String[] columns = {"Address ID", "City", "Country", "Created_at"};
        model.setColumnIdentifiers(columns);
        table.setRowHeight(40);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePane.setPreferredSize(new Dimension(1200, 400));
        table.getColumnModel().getColumn(0).setPreferredWidth(5);
        add(tablePane, BorderLayout.CENTER);
    }
    private void formComponents() {
        panel.setBorder(BorderFactory.createTitledBorder("Address Form"));
        panel.setPreferredSize(new Dimension(1200, 100));
        panel.add(addressCity);
        panel.add(city);
        panel.add(countryLabel);
        panel.add(country);
        panel.add(submitButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        add(panel, BorderLayout.NORTH);
    }
    private void fetchAddresses() {
        try(var conn = Db.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM address");
            var rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("address_id"),
                        rs.getString("city"),
                        rs.getString("country"),
                        rs.getString("created_at")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void clearForm() {
        city.setText("");
        country.setText("");
    }
    private void createAddress() {
        if (city.getText().isEmpty() || country.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (var conn = Db.getConnection()) {
            var checkCityStatement = conn.prepareStatement("SELECT city FROM address WHERE city=?");
            checkCityStatement.setString(1, city.getText());
            var checkCity = checkCityStatement.executeQuery();
            if (checkCity.next()) {
                JOptionPane.showMessageDialog(this, "Address already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            var stmt = conn.prepareStatement("INSERT INTO address(city, country) VALUES(?, ?)");
            stmt.setString(1, city.getText());
            stmt.setString(2, country.getText());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Address created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            model.setRowCount(0);
            fetchAddresses();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void deleteAddress() {
        try (var conn = Db.getConnection()) {
            var selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Select an address first", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            var addressID = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            var stmt = conn.prepareStatement("DELETE FROM address WHERE address_id=?");
            stmt.setInt(1, addressID);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Address deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            model.setRowCount(0);
            fetchAddresses();

        }catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateAddress() {
        try(var conn = Db.getConnection()) {
            var selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Select an address first", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            var addressID = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            var cityColRow = model.getValueAt(selectedRow, 1);
            var countryColRow = model.getValueAt(selectedRow, 2);
            var stmt = conn.prepareStatement("UPDATE address SET city=?, country=? WHERE address_id=?");
            stmt.setString(1, city.getText().isEmpty() ? cityColRow.toString() : city.getText());
            stmt.setString(2, country.getText().isEmpty() ? countryColRow.toString() : country.getText());
            stmt.setInt(3, addressID);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Address updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            model.setRowCount(0);
            fetchAddresses();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
