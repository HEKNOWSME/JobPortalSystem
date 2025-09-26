package applications;

import javax.swing.*;

public class ApplicationsPanel extends JPanel {
    private int userID;
    private String role;
    public ApplicationsPanel(int userID, String role) {
        this.userID = userID;
        this.role = role;
        setLayout(null);
    }
}
