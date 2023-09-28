import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigDecimal;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;


//? Basic functionality of project is complete, but there are some bugs that I do not have time to fix. 
//? userId not working as intended, plan was to use it to fetch customerId for invoice with user and customer being linked by userId.
//? Customer tab also has empty userId column, which is not needed.

//*--------------------------------------------------------- GUI ---------------------------------------------------------


public class PurchaseManager extends JFrame implements ActionListener {
    // UI components
    private JTabbedPane tabbedPane;
    private JPanel customerPanel, invoicePanel, productPanel;
    private JTable customerTable, invoiceTable, productTable;
    private JButton addCustomerBtn, deleteCustomerBtn, amendCustomerBtn;
    private JButton addInvoiceBtn, deleteInvoiceBtn, amendInvoiceBtn;
    private JButton addProductBtn, deleteProductBtn, amendProductBtn, purchaseProductBtn;
    private JFrame frame;               //added to make frame visible after login
    private static String userRole;     //added to make frame visible after login
    private Connection connection;      // Database connection and statement
    private Statement statement;        // Database connection and statement
    private JButton logoutButton;       //logout button added
    private LoginForm loginForm;        //added to make frame visible after logout
    private JPanel controlsPanel;       //added to make buttons change depending on user role
    private int userId;                 //!added to fetch CustomerId for invoice (may not work)
    private int customerId;             //!added to fetch CustomerId for invoice (may not work)


    // MAIN METHOD
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setLoginSuccessListener(userRole -> {
                PurchaseManager purchaseManager = new PurchaseManager(userRole);
                purchaseManager.frame.setVisible(true);
                loginForm.dispose();
            });
        });
    }
    

    public PurchaseManager(String userRole) {
        this.userRole = userRole;
        initComponents();
        setUpConnection(); // Move this line before calling getCustomerId
        populateTables(); // Move this line before calling getCustomerId
        this.userId = userId;
        this.customerId = getCustomerId(userId);
        
        //logout button
        controlsPanel = new JPanel();
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(this);
        controlsPanel.add(logoutButton);

        add(controlsPanel, BorderLayout.SOUTH);
        
        // Set JFrame properties
        setTitle("Purchase Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    

    
    private void initComponents() {
        setTitle("Purchase Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Initialize customer UI components
        customerTable = new JTable();
        JScrollPane customerScrollPane = new JScrollPane(customerTable);
        addCustomerBtn = new JButton("Add");
        deleteCustomerBtn = new JButton("Delete");
        amendCustomerBtn = new JButton("Amend");
        addCustomerBtn.addActionListener(this);
        deleteCustomerBtn.addActionListener(this);
        amendCustomerBtn.addActionListener(this);

        customerPanel = new JPanel(new BorderLayout());
        JPanel customerBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        customerBtnPanel.add(addCustomerBtn);
        customerBtnPanel.add(deleteCustomerBtn);
        customerBtnPanel.add(amendCustomerBtn);
        customerPanel.add(customerScrollPane, BorderLayout.CENTER);
        customerPanel.add(customerBtnPanel, BorderLayout.SOUTH);

        // Initialize invoice UI components
        invoiceTable = new JTable();
        JScrollPane invoiceScrollPane = new JScrollPane(invoiceTable);
        addInvoiceBtn = new JButton("Add");
        deleteInvoiceBtn = new JButton("Delete");
        amendInvoiceBtn = new JButton("Amend");
        addInvoiceBtn.addActionListener(this);
        deleteInvoiceBtn.addActionListener(this);
        amendInvoiceBtn.addActionListener(this);

        invoicePanel = new JPanel(new BorderLayout());
        JPanel invoiceBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        invoiceBtnPanel.add(addInvoiceBtn);
        invoiceBtnPanel.add(deleteInvoiceBtn);
        invoiceBtnPanel.add(amendInvoiceBtn);
        invoicePanel.add(invoiceScrollPane, BorderLayout.CENTER);
        invoicePanel.add(invoiceBtnPanel, BorderLayout.SOUTH);

        // Initialize product UI components
        productTable = new JTable();
        JScrollPane productScrollPane = new JScrollPane(productTable);
        addProductBtn = new JButton("Add");
        deleteProductBtn = new JButton("Delete");
        amendProductBtn = new JButton("Amend");
        purchaseProductBtn = new JButton("Purchase");
        addProductBtn.addActionListener(this);
        deleteProductBtn.addActionListener(this);
        amendProductBtn.addActionListener(this);
        purchaseProductBtn.addActionListener(this);

        productPanel = new JPanel(new BorderLayout());
        JPanel productBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        productBtnPanel.add(addProductBtn);
        productBtnPanel.add(deleteProductBtn);
        productBtnPanel.add(amendProductBtn);
        productBtnPanel.add(purchaseProductBtn);
        productPanel.add(productScrollPane, BorderLayout.CENTER);
        productPanel.add(productBtnPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Customers", customerPanel);
        tabbedPane.addTab("Invoices", invoicePanel);
        tabbedPane.addTab("Products", productPanel);

        add(tabbedPane);
        setVisible(true);


        // Hide buttons and tabs based on user role
        if ("customer".equalsIgnoreCase(userRole)) {
            // Hide Add, Delete, Amend buttons for Customers, Invoices, and Products
            addCustomerBtn.setVisible(false);
            deleteCustomerBtn.setVisible(false);
            amendCustomerBtn.setVisible(false);
            
            addInvoiceBtn.setVisible(false);
            deleteInvoiceBtn.setVisible(false);
            amendInvoiceBtn.setVisible(false);
            
            addProductBtn.setVisible(false);
            deleteProductBtn.setVisible(false);
            amendProductBtn.setVisible(false);
    
            // Remove the Customers and Invoices tabs
            tabbedPane.removeTabAt(0); // Remove Customers tab
            
            int tabCount = tabbedPane.getTabCount();    //?Had to make loop through each tab to check for "Invoices" tab as index was being changed (Now working)
            for (int i = 0; i < tabCount; i++) {
            if (tabbedPane.getTitleAt(i).equals("Invoices")) {
            tabbedPane.removeTabAt(i);
            break;
                }
            }

        }
    }
    

//*--------------------------------------------------------- DATABASE CONNECTION ---------------------------------------------------------
    // Connect to the database
    private void setUpConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/purchases", "root", "P0rtf0l10s;23");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    // Populate the tables with data from the database
    private void populateTables() {
        try {
            // Populate the customer table
            ResultSet customerResultSet = statement.executeQuery("SELECT * FROM customer");
            ResultSetMetaData customerMetaData = customerResultSet.getMetaData();
            int customerColumnCount = customerMetaData.getColumnCount();
            Vector<String> customerColumnNames = new Vector<>();
            for (int i = 1; i <= customerColumnCount; i++) {
                customerColumnNames.add(customerMetaData.getColumnName(i));
            }
            customerTable.setModel(new DefaultTableModel(getData(customerResultSet, customerColumnCount), customerColumnNames));
    
            // Populate the invoice table
            ResultSet invoiceResultSet = statement.executeQuery("SELECT * FROM invoice");
            ResultSetMetaData invoiceMetaData = invoiceResultSet.getMetaData();
            int invoiceColumnCount = invoiceMetaData.getColumnCount();
            Vector<String> invoiceColumnNames = new Vector<>();
            for (int i = 1; i <= invoiceColumnCount; i++) {
                invoiceColumnNames.add(invoiceMetaData.getColumnName(i));
            }
            invoiceTable.setModel(new DefaultTableModel(getData(invoiceResultSet, invoiceColumnCount), invoiceColumnNames));

    
            // Update the SQL query to include the 'description' and 'stock' columns for the product table
            ResultSet productResultSet = statement.executeQuery("SELECT id, name, description, price, stock FROM product");
            ResultSetMetaData productMetaData = productResultSet.getMetaData();
            int productColumnCount = productMetaData.getColumnCount();
            Vector<String> productColumnNames = new Vector<>();
            for (int i = 1; i <= productColumnCount; i++) {
                productColumnNames.add(productMetaData.getColumnName(i));
            }
            productTable.setModel(new DefaultTableModel(getData(productResultSet, productColumnCount), productColumnNames));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get data from the database and return it as a Vector
    private Vector<Vector<Object>> getData(ResultSet resultSet, int columnCount) {  //? takes in the result set and the number of columns
        Vector<Vector<Object>> data = new Vector<>();                               //? create a new vector to hold the data
        try {                                               //? try to get the data
            while (resultSet.next()) {                      //? while there is a next row
                Vector<Object> rowData = new Vector<>();    //? create a new vector for each row
                for (int i = 1; i <= columnCount; i++) {    //? for each column in the row
                    rowData.add(resultSet.getObject(i));    //? add the data from the column to the row vector
                }
                data.add(rowData);                          //? add the row vector to the data vector
            }
        } catch (SQLException e) {                          //? if there is an error
            e.printStackTrace();                            //? print the error
        }
        return data;                                        //? return the data vector
    }

//*--------------------------------------------------------- BUTTON ACTIONS ---------------------------------------------------------

    //* Actions
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        // Calls Add, Delete, and Amend methods for each table
        if (source == addCustomerBtn) {
            addCustomer();                              //? works
        } else if (source == deleteCustomerBtn) {
            deleteCustomer();                           //? works
        } else if (source == amendCustomerBtn) {
            amendCustomer();                            //? works
        } else if (source == addInvoiceBtn) {
            // Call the method to add an invoice        //! not neccessarilly necessary
        } else if (source == deleteInvoiceBtn) {
            // Call the method to delete an invoice     //! not neccessarilly necessary
        } else if (source == amendInvoiceBtn) {
            // Call the method to amend an invoice      //! not neccessarilly necessary
        } else if (source == addProductBtn) {
            addProduct();                               //? works
        } else if (source == deleteProductBtn) {
            deleteProduct();                            //? works
        } else if (source == amendProductBtn) {
            amendProduct();                             //? works
        } else if (e.getSource() == logoutButton) {
            handleLogout();                             //? works
        } else if (source == purchaseProductBtn) {
            handlePurchase();
        }
    }

//*--------------------------------------------------------- METHODS ---------------------------------------------------------

    //* Customer methods 
    // Method to add customer to database
    private void addCustomer() {
        // Create a form panel to add customer details
        JTextField nameField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JTextField addressField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
    
        JPanel customerFormPanel = new JPanel(new GridLayout(0, 2));
        customerFormPanel.add(new JLabel("Name:"));
        customerFormPanel.add(nameField);
        customerFormPanel.add(new JLabel("Email:"));
        customerFormPanel.add(emailField);
        customerFormPanel.add(new JLabel("Address:"));
        customerFormPanel.add(addressField);
        customerFormPanel.add(new JLabel("Phone:"));
        customerFormPanel.add(phoneField);
    
        // Flag to check if the input is valid
        boolean validInput = false; 
    
        // Loop until the user enters valid input
        while (!validInput) {
            int result = JOptionPane.showConfirmDialog(null, customerFormPanel, "Add Customer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
            // If the user clicks OK, validate the input
            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText();
                String email = emailField.getText();
                String address = addressField.getText();
                String phone = phoneField.getText();
    
                // Check if the input is not valid and display an error message
                if (!isValidEmail(email)) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid email address.", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                try {
                    Integer.parseInt(phone);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid phone number (only integers).", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
    
                // If validation passes, insert the new customer into the database
                try {
                    String query = "INSERT INTO customer (name, email, address, phone) VALUES (?, ?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, name);
                    preparedStatement.setString(2, email);
                    preparedStatement.setString(3, address);
                    preparedStatement.setString(4, phone);
                    preparedStatement.executeUpdate();
    
                    // Refresh the customer table
                    populateTables();
    
                    // Set validInput to true to exit the loop
                    validInput = true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                // If the user clicks "Cancel," exit the loop
                break;
            }
        }
    }
    
    

    // Method to delete customer from database
    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a customer to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        int customerId = (int) customerTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this customer?", "Delete Customer", JOptionPane.YES_NO_OPTION);
    
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM customer WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, customerId);
                preparedStatement.executeUpdate();
    
                // Refresh the customer table
                populateTables();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to amend customer in database
    private void amendCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a customer to amend.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        int customerId = (int) customerTable.getValueAt(selectedRow, 0);
        String currentName = (String) customerTable.getValueAt(selectedRow, 1);
        String currentEmail = (String) customerTable.getValueAt(selectedRow, 2);
        String currentAddress = (String) customerTable.getValueAt(selectedRow, 3);
        String currentPhone = (String) customerTable.getValueAt(selectedRow, 4);
    
        JTextField nameField = new JTextField(currentName, 15);
        JTextField emailField = new JTextField(currentEmail, 15);
        JTextField addressField = new JTextField(currentAddress, 15);
        JTextField phoneField = new JTextField(currentPhone, 15);
    
        JPanel customerFormPanel = new JPanel(new GridLayout(0, 2));
        customerFormPanel.add(new JLabel("Name:"));
        customerFormPanel.add(nameField);
        customerFormPanel.add(new JLabel("Email:"));
        customerFormPanel.add(emailField);
        customerFormPanel.add(new JLabel("Address:"));
        customerFormPanel.add(addressField);
        customerFormPanel.add(new JLabel("Phone:"));
        customerFormPanel.add(phoneField);

        // Flag to check if the input is valid
        int result = JOptionPane.OK_OPTION;
        while (result == JOptionPane.OK_OPTION) {
        result = JOptionPane.showConfirmDialog(null, customerFormPanel, "Amend Customer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String updatedName = nameField.getText();
            String updatedEmail = emailField.getText();
            String updatedAddress = addressField.getText();
            String updatedPhone = phoneField.getText();

        // Check if the input is not valid and display an error message
        if (!isValidEmail(updatedEmail)) {
            JOptionPane.showMessageDialog(null, "Please enter a valid email address.", "Error", JOptionPane.ERROR_MESSAGE);
            result = JOptionPane.OK_OPTION;
            continue;
        }

        try {
            Integer.parseInt(updatedPhone);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid phone number (only integers).", "Error", JOptionPane.ERROR_MESSAGE);
            result = JOptionPane.OK_OPTION;
            continue;
        }

        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to save?", "Save Changes", JOptionPane.YES_NO_OPTION);

        // If validation passes, insert the new customer into the database
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "UPDATE customer SET name = ?, email = ?, address = ?, phone = ? WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, updatedName);
                preparedStatement.setString(2, updatedEmail);
                preparedStatement.setString(3, updatedAddress);
                preparedStatement.setString(4, updatedPhone);
                preparedStatement.setInt(5, customerId);
                preparedStatement.executeUpdate();

                    // Refresh the customer table
                    populateTables();
                } 
                catch (SQLException e) {
                e.printStackTrace();
                }
            }
        }
    }
}

    // Method to check if email input is valid
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Method to add product to database
    private void addProduct() {
        JTextField nameField = new JTextField(15);
        JTextField descriptionField = new JTextField(15);
        JTextField priceField = new JTextField(15);
        JTextField stockField = new JTextField(15);
    
        JPanel productFormPanel = new JPanel(new GridLayout(0, 2));
        productFormPanel.add(new JLabel("Name:"));
        productFormPanel.add(nameField);
        productFormPanel.add(new JLabel("Description:"));
        productFormPanel.add(descriptionField);
        productFormPanel.add(new JLabel("Price:"));
        productFormPanel.add(priceField);
        productFormPanel.add(new JLabel("Stock:"));
        productFormPanel.add(stockField);
    
        int result = JOptionPane.showConfirmDialog(null, productFormPanel, "Add Product", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String description = descriptionField.getText();
            String price = priceField.getText();
            String stock = stockField.getText();
    
            // Validate price input
            try {
                double priceValue = Double.parseDouble(price);
                if (priceValue <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Price must be a number and be greater than 0.00", "Invalid price", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // Validate stock input
            int stockValue;
            try {
                stockValue = Integer.parseInt(stock);
                if (stockValue < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Stock must be a positive number", "Invalid stock", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // If validation passes, insert the new product into the database
            try {
                String query = "INSERT INTO product (name, description, price, stock) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, description);
                preparedStatement.setDouble(3, Double.parseDouble(price));
                preparedStatement.setInt(4, stockValue); // Set the stock value
                preparedStatement.executeUpdate();
    
                // Refresh the product table
                populateTables();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    

    // Method to delete product from database
    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a product to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        int productId = (int) productTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this product?", "Delete Product", JOptionPane.YES_NO_OPTION);
    
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM product WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, productId);
                preparedStatement.executeUpdate();
    
                // Refresh the product table
                populateTables();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to amend product in database
    private void amendProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a product to amend.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        int productId = (int) productTable.getValueAt(selectedRow, 0);
        String currentName = (String) productTable.getValueAt(selectedRow, 1);
        String currentDescription = (String) productTable.getValueAt(selectedRow, 2);
        BigDecimal currentPriceBD = (BigDecimal) productTable.getValueAt(selectedRow, 3);
        double currentPrice = currentPriceBD.doubleValue();
        int currentStock = (int) productTable.getValueAt(selectedRow, 4);
    
        JTextField nameField = new JTextField(currentName, 15);
        JTextField descriptionField = new JTextField(currentDescription, 15);
        JTextField priceField = new JTextField(Double.toString(currentPrice), 15);
        JTextField stockField = new JTextField(Integer.toString(currentStock), 15);
    
        JPanel productFormPanel = new JPanel(new GridLayout(0, 2));
        productFormPanel.add(new JLabel("Name:"));
        productFormPanel.add(nameField);
        productFormPanel.add(new JLabel("Description:"));
        productFormPanel.add(descriptionField);
        productFormPanel.add(new JLabel("Price:"));
        productFormPanel.add(priceField);
        productFormPanel.add(new JLabel("Stock:"));
        productFormPanel.add(stockField);
    
        int result = JOptionPane.showConfirmDialog(null, productFormPanel, "Amend Product", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String updatedName = nameField.getText();
            String updatedDescription = descriptionField.getText();
            String updatedPrice = priceField.getText();
            String updatedStock = stockField.getText();
    
            // Validate price input
            try {
                double updatedPriceValue = Double.parseDouble(updatedPrice);
                if (updatedPriceValue <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Price must be a number and be greater than 0.00", "Invalid price", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // Validate stock input
            int updatedStockValue;
            try {
                updatedStockValue = Integer.parseInt(updatedStock);
                if (updatedStockValue < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Stock must be a non-negative integer", "Invalid stock", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // If validation passes, asks user for confirmation and updates the product in the database
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to save?", "Save Changes", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String query = "UPDATE product SET name = ?, description = ?, price = ?, stock = ? WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, updatedName);
                    preparedStatement.setString(2, updatedDescription);
                    preparedStatement.setDouble(3, Double.parseDouble(updatedPrice));
                    preparedStatement.setInt(4, updatedStockValue);
                    preparedStatement.setInt(5, productId);
                    preparedStatement.executeUpdate();
    
                    // Refresh the product table
                    populateTables();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Logout Method
    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            dispose(); // Close the PurchaseManager
            loginForm.setVisible(true); // Show the login form again
        }
    }

    // getCustomerId method (from userId to customerId)
    private int getCustomerId(int userId) {
        System.out.println("userId: " + userId); //! Print userId for debugging
        int customerId = -1;
        String query = "SELECT id FROM customer WHERE user_id = ?";
    
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
    
            if (resultSet.next()) {
                customerId = resultSet.getInt("id");
            }
    
            resultSet.close();
            preparedStatement.close(); // Use preparedStatement.close() instead of statement.close()
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return customerId;
    }
    
    
    // Method to handle purchase 
    private void handlePurchase() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to purchase.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        int productId = Integer.parseInt(productTable.getValueAt(selectedRow, 0).toString());
        String productName = productTable.getValueAt(selectedRow, 1).toString();
        double productPrice = Double.parseDouble(productTable.getValueAt(selectedRow, 3).toString());
    
        int confirmPurchase = JOptionPane.showConfirmDialog(this, "Are you sure you want to purchase " + productName + "?", "Confirm Purchase", JOptionPane.YES_NO_OPTION);
    
        if (confirmPurchase == JOptionPane.YES_OPTION) {
            try {
                // Insert the purchase into the invoice table
                String sql = "INSERT INTO invoice (invoice_date, total_amount) VALUES (?, ?);";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setDate(1, new java.sql.Date(System.currentTimeMillis()));
                preparedStatement.setDouble(2, productPrice);
                preparedStatement.executeUpdate();
    
                // Update the stock value of the purchased product
                sql = "UPDATE product SET stock = stock - 1 WHERE id = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, productId);
                preparedStatement.executeUpdate();
    
                JOptionPane.showMessageDialog(this, "Product purchased successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Refresh the product table to reflect the updated stock value
                refreshProductTable();
    
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error purchasing the product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    


    // Method to refresh the product table
    private void refreshProductTable() {
        DefaultTableModel model = (DefaultTableModel) productTable.getModel();
        model.setRowCount(0); // clear existing rows
        
        try {
            String sql = "SELECT * FROM product";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                double price = resultSet.getDouble("price");
                int stock = resultSet.getInt("stock");
                
                model.addRow(new Object[]{id, name, description, price, stock});
            }
            
            resultSet.close();
            statement.close();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error refreshing product table.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    


}