package form;

import database.Db;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class RegisterForm extends JFrame {
    private final JLabel usernameLabel = new JLabel("Username"); private final JTextField username = new JTextField(20);
    private final JLabel emailLabel = new JLabel("Email "); private final JTextField email = new JTextField(20);
    private final JLabel passwordLabel = new JLabel("Password "); private final JPasswordField password = new JPasswordField(20);
    private final JButton loginBtn = new JButton("Login");   private final JButton registerButton = new JButton("Register");
    public RegisterForm() {
        createUI();
        createUIComponents();
        setComponentsSize();
        loginBtn.addActionListener(_ -> loginRedirect());
        registerButton.addActionListener(_ -> registerUserToLogin());

    }
    private void createUI() {
        setLayout(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setTitle("Register Form");
        getContentPane().setBackground(new Color(236, 221, 221));
        setVisible(true);
    }
    private void  createUIComponents() {
        add(emailLabel); add(email); add(passwordLabel); add(password);
        add(loginBtn); add(registerButton); add(usernameLabel); add(username);
    }
    private void setComponentsSize() {
        usernameLabel.setBounds(10, 30, 100, 30); username.setBounds(120, 30, 260, 30);
        emailLabel.setBounds(10, 100, 100, 30); email.setBounds(120, 100, 260, 30);
        passwordLabel.setBounds(10, 170, 100, 30); password.setBounds(120, 170, 260, 30);
        registerButton.setBounds(10, 220, 180, 30); loginBtn.setBounds(195, 220, 180, 30);
    }
    private void loginRedirect() {
        dispose();
        new LoginForm();
    }
    private void registerUserToLogin()  {
        if (username.getText().isEmpty() || email.getText().isEmpty() || new String(password.getPassword()).isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try (Connection conn = Db.getConnection()) {
                var checkEmailStatement  = conn.prepareStatement("SELECT email FROM users WHERE email=?");
                checkEmailStatement.setString(1, email.getText());
                var emailCheck = checkEmailStatement.executeQuery();
                if (emailCheck.next()) {
                    JOptionPane.showMessageDialog(null, "User already exists", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    var registerStatement  = conn.prepareStatement("INSERT INTO users (username, email,password) VALUES (?,?,?)");
                    registerStatement.setString(1, username.getText());
                    registerStatement.setString(2, email.getText());
                    registerStatement.setString(3, new String(password.getPassword()));
                    registerStatement.executeUpdate();
                    JOptionPane.showMessageDialog(null, "User registered successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    new LoginForm();
                }

            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
