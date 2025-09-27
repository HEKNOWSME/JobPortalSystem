package jobs;

import applications.ApplicationsPanel;
import database.Db;
import users.UsersPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class JobsPanel extends JPanel {
    private int userID;
    private String role;
    private final JLabel titleLabel = new JLabel("Title"); private final JTextField jobTitle = new JTextField(20);
    private final JLabel descriptionLabel = new JLabel("Description"); private final JTextField jobDescription = new JTextField(20);
    private final JLabel minSalaryLabel = new JLabel("Minimum Salary"); private final JTextField minSalary = new JTextField(20);
    private final JLabel maxSalaryLabel = new JLabel("Maximum Salary"); private final JTextField maxSalary = new JTextField(20);
    JTable table = new JTable();
    JScrollPane scrollPane = new JScrollPane(table);
    DefaultTableModel model = new DefaultTableModel();
    JButton submitButton = new JButton("Create Job"); JButton updateButton = new JButton("Update Job"); JButton deleteButton = new JButton("Delete Job");
    public JobsPanel(int userID,  String role) {
        this.userID = userID;
        this.role = role;
        init();
        if (this.role.equals("employer")) {
        createUIComponents();
        }
        createSizeComponents();
        createTable();
        loadJobs();
        submitButton.addActionListener(_-> createJob());
        deleteButton.addActionListener(_-> deleteJob());
        updateButton.addActionListener(_-> updateJob());
    }
    private void init() {
        setLayout(null);
    }
    private void createUIComponents() {
        add(titleLabel);
        add(jobTitle); add(jobTitle);
        add(descriptionLabel); add(descriptionLabel);
        add(jobDescription); add(jobDescription);
        add(minSalaryLabel);add(minSalary);
        add(maxSalaryLabel);add(maxSalary);
        add(submitButton); add(submitButton);
        add(updateButton); add(updateButton);
        add(deleteButton); add(deleteButton);


    }
    private void  createSizeComponents() {
        ApplicationsPanel.setFields(titleLabel, jobTitle, descriptionLabel, jobDescription, minSalaryLabel, minSalary, maxSalaryLabel, maxSalary, submitButton, updateButton, deleteButton);
    }
    private void  createTable() {
        String[] columns = {"ID", "Job Title", "Job Description", "Job Type", "Job Min Salary", "Job Max Salary", "Status", "Posted At" };
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
    private void createJob() {
        if (jobTitle.getText().isEmpty()
                || jobDescription.getText().isEmpty()
                || minSalary.getText().isEmpty()
                || maxSalary.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (Integer.parseInt(minSalary.getText()) > Integer.parseInt(maxSalary.getText())) {
            JOptionPane.showMessageDialog(this, "Minimum Salary must not be greater than Maximum Salary", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String title = jobTitle.getText();
            String description = jobDescription.getText();
            int min = Integer.parseInt(minSalary.getText());
            int max = Integer.parseInt(maxSalary.getText());
            try(var conn = Db.getConnection()) {
                var fetchEmployerCompany = conn.prepareStatement("SELECT * FROM companies WHERE user_id = ?");
                fetchEmployerCompany.setInt(1, userID);
                var employerCompany = fetchEmployerCompany.executeQuery();
                if (employerCompany.next()) {
                    var createStatement = conn.prepareStatement("INSERT INTO jobs (title, description, salary_min, salary_max, company_id) VALUES (?,?,?,?,?)");
                    createStatement.setString(1, title);
                    createStatement.setString(2, description);
                    createStatement.setInt(3, min);
                    createStatement.setInt(4, max);
                    createStatement.setInt(5, employerCompany.getInt("company_id"));
                    var created = createStatement.executeUpdate();
                    if (created > 0) {
                        clearFields();
                        model.setRowCount(0);
                        loadJobs();
                        JOptionPane.showMessageDialog(this, "Job Created", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Job Not Created", "Error", JOptionPane.ERROR_MESSAGE);
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
        jobTitle.setText("");
        jobDescription.setText("");
        minSalary.setText("");
        maxSalary.setText("");
    }
    private void loadJobs() {
        try (var conn = Db.getConnection()) {
            var statement = conn.prepareStatement("SELECT * FROM jobs");
            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                model.addRow(new Object[]{
                        resultSet.getInt("job_id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getString("Job_type"),
                        resultSet.getInt("salary_min"),
                        resultSet.getInt("salary_max"),
                        resultSet.getString("status"),
                        resultSet.getString("posted_at")
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
            String jobID = model.getValueAt(selectedRow, 0).toString();
            int id = Integer.parseInt(jobID);
            try (var conn = Db.getConnection()) {
                var fecthJob = conn.prepareStatement("SELECT * FROM jobs where job_id = ?");
                fecthJob.setInt(1, id);
                var result = fecthJob.executeQuery();
                if (result.next()) {
                    var statement =  conn.prepareStatement("UPDATE jobs SET title = ?, description = ?, salary_min =?, salary_max =? where job_id = ?");
                    statement.setString(1, jobTitle.getText().isEmpty() ? result.getString("title") : jobTitle.getText());
                    statement.setString(2, jobDescription.getText().isEmpty() ? result.getString("description") : jobDescription.getText());
                    statement.setInt(3, Integer.parseInt(minSalary.getText().isEmpty() ? result.getString("salary_min") : minSalary.getText()));
                    statement.setInt(4, Integer.parseInt(maxSalary.getText().isEmpty() ? result.getString("salary_max") : maxSalary.getText()));
                    statement.setInt(5, id);
                    statement.executeUpdate();
                    model.setRowCount(0);
                    clearFields();
                    loadJobs();
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
