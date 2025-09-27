package database;

import applications.ApplicationsPanel;
import companies.CompaniesPanel;
import form.LoginForm;
import jobs.JobsPanel;
import users.UsersPanel;

import javax.swing.*;
import java.awt.*;

public class JobPortalSystemUI extends JFrame {
    private final int userID;
    private final String role;
    private JTabbedPane tabbedPane;
    private JButton logoutButton;

    public JobPortalSystemUI(int userID, String role) {
        this.userID = userID;
        this.role = role;
        init();
        addHeader();
        addPanels();
    }

    private void init() {
        setTitle("Job Portal");
        setSize(1800, 1000);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setVisible(true);

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void addHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutButton = new JButton("Logout");

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginForm();
            }
        });

        headerPanel.add(logoutButton);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void addPanels() {
        switch (role.toLowerCase()) {
            case "jobseeker" -> {
                tabbedPane.addTab("Jobs", new JobsPanel(userID, role));
                tabbedPane.addTab("Applications", new ApplicationsPanel(userID, role));
            }
            case "employer" -> {
                tabbedPane.addTab("Jobs", new JobsPanel(userID, role));
                tabbedPane.addTab("Applications", new ApplicationsPanel(userID, role));
                tabbedPane.addTab("Companies", new CompaniesPanel(userID, role));
            }
            case "admin" -> {
                tabbedPane.addTab("Users", new UsersPanel(userID, role));
                tabbedPane.addTab("Companies", new CompaniesPanel(userID, role));
            }
            default -> {
                JOptionPane.showMessageDialog(null, "Login Failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
