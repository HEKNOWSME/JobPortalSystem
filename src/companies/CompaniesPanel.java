package companies;

import database.Db;
import users.UsersPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CompaniesPanel extends JPanel {
    private int userID;
    private String role;
    private final JLabel nameLabel = new JLabel("Name"); private final JTextField name = new JTextField(20);
    private final JLabel locationLabel = new JLabel("Location"); private final JTextField location = new JTextField(20);
    private final JLabel employerIdLabel = new JLabel("Employer Id"); private final JTextField employer_id = new JTextField(20);
    JTable table = new JTable();
    JScrollPane scrollPane = new JScrollPane(table);
    DefaultTableModel model = new DefaultTableModel();
    JButton submitButton = new JButton("Create Company"); JButton updateButton = new JButton("Update Company"); JButton deleteButton = new JButton("Delete Company");
    public CompaniesPanel(int userID,  String role) {
        this.userID = userID;
        this.role = role;
        init();
        createUIComponents();
        createSizeComponents();
        if (role.equalsIgnoreCase("Admin")) {
            createTable();
            loadCompanies();
        }
        submitButton.addActionListener(_-> createCompany());
        deleteButton.addActionListener(_-> deleteCompany());
        updateButton.addActionListener(_-> updateCompany());
    }
    private void init() {
        setLayout(null);
    }
    private void createUIComponents() {
        if (role.equalsIgnoreCase("Admin")) {
            add(nameLabel); add(name);
            add(locationLabel); add(locationLabel);
            add(location); add(location);
            add(employerIdLabel);add(employer_id);
            add(submitButton); add(submitButton);
            add(updateButton); add(updateButton);
            add(deleteButton); add(deleteButton);
        }
    }
    private void  createSizeComponents() {
        UsersPanel.setFields(nameLabel, name, locationLabel, location, employerIdLabel, employer_id);
        submitButton.setBounds(350, 10, 200, 30);
        updateButton.setBounds(350, 50, 200, 30);
        deleteButton.setBounds(350, 90, 200, 30);
    }
    private void  createTable() {
        String[] columns = {"ID", "Name", "Location","Created_at" };
        for (var col : columns) {
            model.addColumn(col);
        }
        table.setModel(model);
        scrollPane.setBounds(10, 200, 1250, 400);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(5);
        table.setRowHeight(50);
        add(scrollPane);
    }
    private void createCompany() {
        if (name.getText().isEmpty()
                || location.getText().isEmpty()
                || employer_id.getText().isEmpty()
        ) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String nameText = name.getText();
            String locationText = location.getText();
            int employer = Integer.parseInt(employer_id.getText());
            try(var conn = Db.getConnection()) {
                var fetchEmployer = conn.prepareStatement("select * from users where user_id = ?");
                fetchEmployer.setInt(1, employer);
                var result = fetchEmployer.executeQuery();
                if (result.next() && result.getString("role").equalsIgnoreCase("employer")) {
                    var createStatement = conn.prepareStatement("INSERT INTO companies (name, location, user_id) VALUES (?,?,?)");
                    createStatement.setString(1, nameText);
                    createStatement.setString(2, locationText);
                    createStatement.setInt(3, employer);
                    var created = createStatement.executeUpdate();
                    if (created > 0) {
                        clearFields();
                        model.setRowCount(0);
                        loadCompanies();
                        JOptionPane.showMessageDialog(this, "Job Created", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Job Not Created", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "User does not exist", "Error", JOptionPane.ERROR_MESSAGE);
                }


            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);}
        }
    }
    private void clearFields() {
        name.setText("");
        location.setText("");
        employer_id.setText("");
    }
    public void loadCompanies() {
            try (var conn = Db.getConnection()) {
                var statement = conn.prepareStatement("SELECT * FROM companies");
                var resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    model.addRow(new Object[]{
                            resultSet.getInt("company_id"),
                            resultSet.getString("name"),
                            resultSet.getString("location"),
                            resultSet.getString("created_at"),

                    });
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    private void deleteCompany() {
        var selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selected Row Not Found", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String company = model.getValueAt(selectedRow, 0).toString();
            try (var conn = Db.getConnection()) {
                var statement = conn.prepareStatement("DELETE FROM companies where company_id = ?");
                int id = Integer.parseInt(company);
                statement.setInt(1, id);
                var check = statement.executeUpdate();
                if (check < 0) {
                    JOptionPane.showMessageDialog(this, "Company Not Deleted Successfully", "Error", JOptionPane.ERROR_MESSAGE);

                } else {
                    model.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Company Deleted Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void updateCompany() {
        var selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selected Row Not Found", "Error", JOptionPane.ERROR_MESSAGE);
        } else  {
            String companyID = model.getValueAt(selectedRow, 0).toString();
            int id = Integer.parseInt(companyID);

            try (var conn = Db.getConnection()) {
                var fetchCompany = conn.prepareStatement("SELECT * FROM companies where company_id = ?");
                fetchCompany.setInt(1, id);
                var result = fetchCompany.executeQuery();
                if (result.next()) {
                    var fetchEmployer = conn.prepareStatement("select * from users where user_id = ?");
                    fetchEmployer.setInt(1, employer_id.getText().isEmpty() ? result.getInt("user_id") : Integer.parseInt(employer_id.getText()));
                    var result1 = fetchEmployer.executeQuery();
                    if (!result1.next()) {
                        JOptionPane.showMessageDialog(this, "Employer Not Found", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    var statement =  conn.prepareStatement("UPDATE companies SET name = ?, location = ?, user_id = ? WHERE company_id =?");
                    statement.setString(1, name.getText().isEmpty() ? result.getString("name") : name.getText());
                    statement.setString(2, location.getText().isEmpty() ? result.getString("location") : location.getText());
                    statement.setInt(3, Integer.parseInt(employer_id.getText().isEmpty() ? result.getString("user_id") : employer_id.getText()));
                    statement.setInt(4, id);
                    statement.executeUpdate();
                    model.setRowCount(0);
                    clearFields();
                    loadCompanies();
                    JOptionPane.showMessageDialog(this, "Job Updated", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else  {
                    JOptionPane.showMessageDialog(this, "Job Not Updated", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
