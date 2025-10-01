package form;
import database.Db;
import database.JobPortalSystemUI;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;


public class LoginForm extends JFrame {
    private final JLabel usernameLabel = new JLabel("Username "); private final JTextField usernameField = new JTextField(20);
    private final JLabel passwordLabel = new JLabel("Password "); private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginBtn = new JButton("Login");  private final JButton registerButton = new JButton("Sign Up");
    public LoginForm() {
        createUI();
        createUIComponents();
        setComponentsSize();
        registerButton.addActionListener(_ -> registerUser());
        loginBtn.addActionListener(_ -> loginUserIntoSystem());

    }
    private void createUI() {
        setLayout(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setTitle("Login Form");
        getContentPane().setBackground(new Color(236, 221, 221));
        setVisible(true);
    }
    private void  createUIComponents() {
        add(usernameLabel); add(usernameField); add(passwordLabel); add(passwordField);
        add(loginBtn); add(registerButton);
    }
    private void setComponentsSize() {
        usernameLabel.setBounds(10, 30, 100, 30); usernameField.setBounds(120, 30, 260, 30);
        passwordLabel.setBounds(10, 100, 100, 30); passwordField.setBounds(120, 100, 260, 30);
        loginBtn.setBounds(10, 170, 170, 30); registerButton.setBounds(190, 170, 180, 30);
    }
    private void registerUser() {
        dispose();
        new RegisterForm();
    }
    private void loginUserIntoSystem() {
        if (usernameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email", "Email", JOptionPane.ERROR_MESSAGE);
        } else if (new String(passwordField.getPassword()).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter password", "Password", JOptionPane.ERROR_MESSAGE);
        } else {
            try (Connection conn =  Db.getConnection()) {
                var statement = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
                statement.setString(1, usernameField.getText());
                var user = statement.executeQuery();
                if (user.next()) {
                    var checkPassword = user.getString("password").equals(new String(passwordField.getPassword()));
                    if (checkPassword) {
                        JOptionPane.showMessageDialog(this, "Login Successful", "Login", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        var user_id = user.getInt("user_id");
                        var role = user.getString("role");
                        new JobPortalSystemUI(user_id, role);
                    } else {
                        JOptionPane.showMessageDialog(this, "Wrong Password", "Login", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong Email", "Login", JOptionPane.ERROR_MESSAGE);
                }

            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
