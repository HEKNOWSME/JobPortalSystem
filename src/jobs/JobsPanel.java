package jobs;

import database.Db;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class JobsPanel extends JPanel {
    private int userID;
    private String role;
    private final JLabel titleLabel = new JLabel("Title");
    private final JTextField jobTitle = new JTextField(20);
    private final JLabel descriptionLabel = new JLabel("Description");
    private final JTextField jobDescription = new JTextField(20);
    private final JLabel minSalaryLabel = new JLabel("Minimum Salary");
    private final JTextField minSalary = new JTextField(20);
    private final JLabel maxSalaryLabel = new JLabel("Maximum Salary");
    private final JTextField maxSalary = new JTextField(20);

    JTable table = new JTable();
    JScrollPane scrollPane = new JScrollPane(table);
    DefaultTableModel model = new DefaultTableModel();

    JButton submitButton = new JButton("Create Job");
    JButton updateButton = new JButton("Update Job");
    JButton deleteButton = new JButton("Delete Job");
    JButton applyButton = new JButton("Apply Job");

    public JobsPanel(int userID, String role) {
        this.userID = userID;
        this.role = role;
        init();
        if (this.role.equals("employer")) {
            createUIComponents();
        }
        createSizeComponents();
        createTable();
        loadJobs();

        submitButton.addActionListener(_ -> createJob());
        deleteButton.addActionListener(_ -> deleteJob());
        updateButton.addActionListener(_ -> updateJob());

        if (this.role.equals("jobseeker")) {
            add(applyButton);
            applyButton.setBounds(1100, 150, 120, 30);
            applyButton.addActionListener(_ -> applyJob());
        }
    }

    private void init() {
        setLayout(null);
    }

    private void createUIComponents() {
        add(titleLabel); add(jobTitle);
        add(descriptionLabel); add(jobDescription);
        add(minSalaryLabel); add(minSalary);
        add(maxSalaryLabel); add(maxSalary);
        add(submitButton); add(updateButton); add(deleteButton);
    }

    private void createSizeComponents() {
        titleLabel.setBounds(50, 50, 100, 30);
        jobTitle.setBounds(150, 50, 200, 30);
        descriptionLabel.setBounds(50, 90, 100, 30);
        jobDescription.setBounds(150, 90, 200, 30);
        minSalaryLabel.setBounds(50, 130, 100, 30);
        minSalary.setBounds(150, 130, 200, 30);
        maxSalaryLabel.setBounds(50, 170, 100, 30);
        maxSalary.setBounds(150, 170, 200, 30);

        submitButton.setBounds(400, 50, 120, 30);
        updateButton.setBounds(400, 90, 120, 30);
        deleteButton.setBounds(400, 130, 120, 30);
    }

    private void createTable() {
        String[] columns = {
                "ID", "Job Title", "Job Description", "Job Type", "company", "city",
                "Job Min Salary", "Job Max Salary", "Status", "Posted At"
        };
        for (var col : columns) {
            model.addColumn(col);
        }
        table.setModel(model);
        scrollPane.setBounds(10, 220, 1250, 400);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(5);
        table.setRowHeight(40);
        add(scrollPane);
    }

    private void createJob() {
        if (jobTitle.getText().isEmpty() || jobDescription.getText().isEmpty()
                || minSalary.getText().isEmpty() || maxSalary.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (Integer.parseInt(minSalary.getText()) > Integer.parseInt(maxSalary.getText())) {
            JOptionPane.showMessageDialog(this, "Minimum Salary must not be greater than Maximum Salary", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String title = jobTitle.getText();
            String description = jobDescription.getText();
            int min = Integer.parseInt(minSalary.getText());
            int max = Integer.parseInt(maxSalary.getText());
            try (var conn = Db.getConnection()) {
                var fetchEmployerCompany = conn.prepareStatement("SELECT * from companies join managers ON  manager_id = ?");
                fetchEmployerCompany.setInt(1, userID);
                var employerCompany = fetchEmployerCompany.executeQuery();
                if (employerCompany.next()) {
                    var createStatement = conn.prepareStatement(
                            "INSERT INTO jobs (title, description, salary_min, salary_max, company_id) VALUES (?,?,?,?,?)"
                    );
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
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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
            var statement = conn.prepareStatement("""
                    SELECT j.job_id, j.title AS title, j.description,
                           j.job_type, c.name AS company, a.city, j.salary_min, j.salary_max,
                           j.posted_at, j.status
                    FROM jobs j
                            JOIN companies c
                            JOIN address a ON c.location = a.address_id
                    """);
            var resultSet = statement.executeQuery();
            model.setRowCount(0); // clear old rows
            while (resultSet.next()) {
                model.addRow(new Object[]{
                        resultSet.getInt("job_id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getString("job_type"),
                        resultSet.getString("company"),
                        resultSet.getString("city"),
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
            JOptionPane.showMessageDialog(this, "Select a row first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int jobID = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
        try (var conn = Db.getConnection()) {
            var statement = conn.prepareStatement("DELETE FROM jobs WHERE job_id = ?");
            statement.setInt(1, jobID);
            int check = statement.executeUpdate();
            if (check > 0) {
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Job Deleted", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Job Not Deleted", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateJob() {
        var selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int id = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
        try (var conn = Db.getConnection()) {
            var fecthJob = conn.prepareStatement("SELECT * FROM jobs WHERE job_id = ?");
            fecthJob.setInt(1, id);
            var result = fecthJob.executeQuery();
            if (result.next()) {
                var statement = conn.prepareStatement(
                        "UPDATE jobs SET title = ?, description = ?, salary_min = ?, salary_max = ? WHERE job_id = ?"
                );
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
            } else {
                JOptionPane.showMessageDialog(this, "Job Not Found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyJob() {
        var selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job to apply", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int jobId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());

        try (var conn = Db.getConnection()) {

            var checkStmt = conn.prepareStatement("SELECT * FROM applications WHERE job_id = ? AND jobseeker_id = ?");
            checkStmt.setInt(1, jobId);
            checkStmt.setInt(2, userID);
            var rs = checkStmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "You already applied for this job", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            var stmt = conn.prepareStatement("INSERT INTO applications (job_id, jobseeker_id) VALUES (?, ?)");
            stmt.setInt(1, jobId);
            stmt.setInt(2, userID);
            int inserted = stmt.executeUpdate();
            if (inserted > 0) {
                JOptionPane.showMessageDialog(this, "Application submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to apply", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
