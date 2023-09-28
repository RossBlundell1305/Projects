import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.function.Consumer;

//*--------------------------------------------------------- LoginForm ---------------------------------------------------------

public class LoginForm extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, createAccountButton;
    private JLabel messageLabel;
    private String userRole;
    

    // LoginForm Constructor
    public LoginForm() {
        setTitle("Login Form");
        setLayout(new GridLayout(4, 2));

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");
        createAccountButton = new JButton("Create Account");
        messageLabel = new JLabel("");

        loginButton.addActionListener(this);
        createAccountButton.addActionListener(this);

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);
        add(createAccountButton);
        add(messageLabel);

        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Main Method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            handleLogin();
        } else if (e.getSource() == createAccountButton) {
            createAccount();
        }
    }

    private void createAccount() {
        new CreateAccountForm(this);  
    }

    // loginSuccessListener
    private Consumer<String> loginSuccessListener;

    // setloginSuccessListener
    public void setLoginSuccessListener(Consumer<String> loginSuccessListener) {
        this.loginSuccessListener = loginSuccessListener;
    }

    // handleLogin Method
    private void handleLogin() {
        String enteredUsername = usernameField.getText().trim();
        String enteredPassword = new String(passwordField.getPassword());
    
        boolean validUser = validateUser(enteredUsername, enteredPassword);
        
    
        if (validUser) {
            // Get the userRole
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/purchases", "root", "P0rtf0l10s;23");
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT role FROM user WHERE username = ?")) {
        
                preparedStatement.setString(1, enteredUsername);
                ResultSet resultSet = preparedStatement.executeQuery();
        
                if (resultSet.next()) {
                    userRole = resultSet.getString("role");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        
            // Call the loginSuccessListener with userRole if it's not null
            if (loginSuccessListener != null) {
                loginSuccessListener.accept(userRole);
            }


            // Close the login form
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Username or Password incorrect.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
        
    

    // Validateuser Method
    private boolean validateUser(String enteredUsername, String enteredPassword) {
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
    
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/purchases", "root", "P0rtf0l10s;23");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
    
            preparedStatement.setString(1, enteredUsername);
            preparedStatement.setString(2, enteredPassword);
    
            ResultSet resultSet = preparedStatement.executeQuery();
    
            if (resultSet.next()) {
                return true;
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return false;
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getUserRole() {
        return userRole;
    }

    // clear the fields of login form
    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }
    
}

//*--------------------------------------------------------- CreateAccountForm ---------------------------------------------------------

// CreateAccountForm
class CreateAccountForm extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton customerRadioButton, adminRadioButton;
    private ButtonGroup roleButtonGroup;
    private JButton createButton;
    private JLabel messageLabel;

    public CreateAccountForm(JFrame parent) {
        super(parent, "Create Account", true);

        setLayout(new GridLayout(5, 2));

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        customerRadioButton = new JRadioButton("Customer", true);
        adminRadioButton = new JRadioButton("Admin", false);
        roleButtonGroup = new ButtonGroup();
        createButton = new JButton("Create");
        messageLabel = new JLabel("");

        roleButtonGroup.add(customerRadioButton);
        roleButtonGroup.add(adminRadioButton);

        adminRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String adminPassword = JOptionPane.showInputDialog("Enter the admin password:");
                if (!"@dm1n".equals(adminPassword)) {
                    JOptionPane.showMessageDialog(this, "Incorrect admin password. Switching back to customer.", "Error", JOptionPane.ERROR_MESSAGE);
                    customerRadioButton.setSelected(true);
                }
            }});
    
            createButton.addActionListener(e -> createAccount());
    
            add(new JLabel("Username:"));
            add(usernameField);
            add(new JLabel("Password:"));
            add(passwordField);
            add(customerRadioButton);
            add(adminRadioButton);
            add(createButton);
            add(messageLabel);
    
            pack();
            setLocationRelativeTo(parent);
            setVisible(true);
        }
    
        // Create Account Method
        private void createAccount() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = customerRadioButton.isSelected() ? "customer" : "admin";
    
            //database connection
            String url = "jdbc:mysql://localhost:3306/purchases";
            String user = "root";
            String pass = "P0rtf0l10s;23";
    
            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                String checkQuery = "SELECT * FROM user WHERE username = ?";
                PreparedStatement checkPreparedStatement = connection.prepareStatement(checkQuery);
                checkPreparedStatement.setString(1, username);
                ResultSet checkResultSet = checkPreparedStatement.executeQuery();
    
                if (checkResultSet.next()) {
                    messageLabel.setText("Username has been taken, please choose another username.");
                } else {
                    String insertQuery = "INSERT INTO user (username, password, role) VALUES (?, ?, ?)";
                    PreparedStatement insertPreparedStatement = connection.prepareStatement(insertQuery);
                    insertPreparedStatement.setString(1, username);
                    insertPreparedStatement.setString(2, password);
                    insertPreparedStatement.setString(3, role);
                    insertPreparedStatement.executeUpdate();
    
                    JOptionPane.showMessageDialog(this, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                messageLabel.setText("Error connecting to the database.");
            }
        }

    
    }
    
