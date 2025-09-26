package database;

import applications.ApplicationsPanel;
import companies.CompaniesPanel;
import jobs.JobsPanel;
import users.UsersPanel;

import javax.swing.*;
import java.awt.*;

public class JobPortalSystemUI extends JFrame {
    private int userID;
    private String role;
    JTabbedPane tabbedPane;
    public JobPortalSystemUI(int userID, String role) {
        this.userID = userID;
        this.role = role;
        init();
        addPanels();
    }
    private void init() {
        setLayout(new BorderLayout());
        setTitle("Job Portal");
        setVisible(true);
        setResizable(false);
        setSize(1800, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(tabbedPane = new JTabbedPane());
    }
    private void addPanels() {
        switch (role) {
            case "jobseeker" -> {
                tabbedPane.add("Jobs", new JobsPanel(userID, role));
                tabbedPane.add("Applications", new ApplicationsPanel(userID, role));
            }
            case "employer" -> {
                tabbedPane.add("Jobs", new JobsPanel(userID, role));
                tabbedPane.add("Applications", new ApplicationsPanel(userID, role));
                tabbedPane.add("Companies", new CompaniesPanel(userID, role));
            }
            case "admin" -> {
                tabbedPane.add("Users", new UsersPanel(userID, role));
                tabbedPane.add("Companies", new CompaniesPanel(userID, role));
            }default -> {
                JOptionPane.showMessageDialog(null, "Login Failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
