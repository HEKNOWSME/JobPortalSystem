package applications;

import database.Db;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ApplicationsPanel extends JPanel {
    private final int userID;
    private final String role;
    private int companyID;

    private final JTable table = new JTable();
    private final JScrollPane scrollPane = new JScrollPane(table);
    private final DefaultTableModel model = new DefaultTableModel();

    private final JButton hireButton = new JButton("Hire Applicant");
    private final JButton deleteButton = new JButton("Delete Application");

    public ApplicationsPanel(int userID, String role) {
        this.userID = userID;
        this.role = role.toLowerCase();

        setLayout(new BorderLayout(10, 10));
        createTable();
        add(scrollPane, BorderLayout.CENTER);

        if ("employer".equals(this.role)) {
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            btnPanel.add(hireButton);
            btnPanel.add(deleteButton);
            add(btnPanel, BorderLayout.NORTH);

            hireButton.addActionListener(e -> hireApplicant());
            deleteButton.addActionListener(e -> deleteApplication());
        }

        loadApplications();
    }

    private void createTable() {
        String[] columns = {"Application ID", "Job Title", "Applicant", "Status", "Applied At"};
        model.setColumnIdentifiers(columns);
        table.setModel(model);
        table.setRowHeight(40);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void loadApplications() {
        try (var conn = Db.getConnection()) {
            String query;
            if ("jobseeker".equals(role)) {
                query = "SELECT a.application_id, j.title, u.username AS applicant, a.status, a.applied_at " +
                        "FROM applications a " +
                        "JOIN jobs j ON a.job_id = j.job_id " +
                        "JOIN users u ON a.jobseeker_id = u.user_id " +
                        "WHERE a.jobseeker_id = ?";
            } else if ("employer".equals(role)) {
                String company = """
                        SELECT c.name, c.company_id, m.email, a.city
                        FROM companies c
                            JOIN managers m
                            JOIN address a
                        ON
                           a.address_id = c.location
                        WHERE m.manager_id = ?
                        """;
                var stmt1 = conn.prepareStatement(company);
                stmt1.setInt(1, userID);
                var rs = stmt1.executeQuery();
                if (rs.next()) {
                    companyID = rs.getInt("company_id");
                    query = """
                          
                            SELECT a.application_id, j.title, u.username, js.email  AS applicant, a.status, a.applied_at
                          FROM applications a
                                   JOIN jobs j ON a.job_id = j.job_id
                                   JOIN users u ON a.jobseeker_id = u.user_id
                                   JOIN jobseekers js
                                  ON a.jobseeker_id = js.jobseeker_id
                                   JOIN companies c ON j.company_id = c.company_id
                                   JOIN managers m on C.company_id = m.company_id
                          WHERE m.manager_id = ?;
                            """;
                } else {
                    return;
                }

            } else {
                return;
            }

            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, userID);
            var rs = stmt.executeQuery();

            model.setRowCount(0); // clear table first
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("application_id"),
                        rs.getString("title"),
                        rs.getString("applicant"),
                        rs.getString("status"),
                        rs.getTimestamp("applied_at")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hireApplicant() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an application first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int applicationID = (int) model.getValueAt(selectedRow, 0);

        try (var conn = Db.getConnection()) {
            var stmt1 = conn.prepareStatement("UPDATE applications SET status = 'Hired' WHERE application_id = ?");
            stmt1.setInt(1, applicationID);
            int updatedApps = stmt1.executeUpdate();

            if (updatedApps > 0) {
                var stmt2 = conn.prepareStatement(
                        "UPDATE jobs SET status = 'Closed' WHERE job_id = (SELECT job_id FROM applications WHERE application_id = ?)"
                );
                stmt2.setInt(1, applicationID);
                stmt2.executeUpdate();

                loadApplications();
                JOptionPane.showMessageDialog(this, "Applicant Hired! Job Closed.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Could not update application status", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteApplication() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an application first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int applicationID = (int) model.getValueAt(selectedRow, 0);

        try (var conn = Db.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM applications WHERE application_id = ?");
            stmt.setInt(1, applicationID);
            if (stmt.executeUpdate() > 0) {
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Application Deleted", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
