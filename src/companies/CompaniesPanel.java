package companies;

import javax.swing.*;

public class CompaniesPanel extends JPanel {
    private int userID;
    private String role;
    public CompaniesPanel(int userID,  String role) {
        this.userID = userID;
        this.role = role;
        setLayout(null);
    }
}
