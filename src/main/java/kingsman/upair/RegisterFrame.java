/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package kingsman.upair;

import javax.swing.JOptionPane;
import kingsman.upair.model.Account;
import kingsman.upair.model.Passenger;
import kingsman.upair.service.RegistrationService;
import kingsman.upair.utils.ValidationUtils;

/**
 * Registration Frame for new passenger registration
 * Uses service layer and model classes following OOP principles
 * 
 * @author admin
 */
public class RegisterFrame extends javax.swing.JFrame {

    /**
     * Creates new form RegisterFrame
     */
    public RegisterFrame() {
        initComponents();
        setLocationRelativeTo(null);
        populateIDTypeComboBox();
    }
    
    /**
     * Populates the ID Type combo box with Philippine ID types
     */
    private void populateIDTypeComboBox() {
        idType.removeAllItems();
        String[] idTypes = {
            "Select ID Type",
            "Driver's License",
            "Passport",
            "SSS ID",
            "UMID",
            "PhilHealth ID",
            "Voter's ID",
            "Senior Citizen ID",
            "Student ID",
            "TIN ID",
            "Postal ID",
            "National ID",
            "Others"
        };
        for (String type : idTypes) {
            idType.addItem(type);
        }
        idType.setSelectedIndex(0); // Select first item as default
    }
    
    /**
     * Validates all required fields are filled
     * Uses ValidationUtils for validation logic
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateAllFields() {
        // Check personal information
        if (firstName.getText().trim().isEmpty()) {
            showError("First Name is required!", firstName);
            return false;
        }
        
        if (lastName.getText().trim().isEmpty()) {
            showError("Last Name is required!", lastName);
            return false;
        }
        
        if (cellphoneNumber.getText().trim().isEmpty()) {
            showError("Cellphone Number is required!", cellphoneNumber);
            return false;
        }
        
        // Check address information
        if (userProvince.getText().trim().isEmpty()) {
            showError("Province is required!", userProvince);
            return false;
        }
        
        if (userCity.getText().trim().isEmpty()) {
            showError("City/Municipality is required!", userCity);
            return false;
        }
        
        if (userBarangay.getText().trim().isEmpty()) {
            showError("Barangay is required!", userBarangay);
            return false;
        }
        
        // Check identification
        String selectedIDType = (String) idType.getSelectedItem();
        if (selectedIDType == null || selectedIDType.equals("Select ID Type")) {
            showError("Please select an ID Type!", idType);
            return false;
        }
        
        if (idNumber.getText().trim().isEmpty()) {
            showError("ID Number is required!", idNumber);
            return false;
        }
        
        // Check account information
        String username = userName.getText().trim();
        if (username.isEmpty()) {
            showError("Username is required!", userName);
            return false;
        }
        
        // Validate username format using ValidationUtils
        ValidationUtils.ValidationResult usernameValidation = ValidationUtils.validateUsername(username);
        if (!usernameValidation.isValid()) {
            showError(usernameValidation.getMessage(), userName);
            return false;
        }
        
        String password = new String(userPassword.getPassword());
        if (password.isEmpty()) {
            showError("Password is required!", userPassword);
            return false;
        }
        
        // Validate password format using ValidationUtils
        ValidationUtils.ValidationResult passwordValidation = ValidationUtils.validatePassword(password);
        if (!passwordValidation.isValid()) {
            showError(passwordValidation.getMessage(), userPassword);
            return false;
        }
        
        String confirmPassword = new String(userConfirmPassword.getPassword());
        // Validate password match using RegistrationService
        ValidationUtils.ValidationResult passwordMatchValidation = 
            RegistrationService.validatePasswordConfirmation(password, confirmPassword);
        if (!passwordMatchValidation.isValid()) {
            showError(passwordMatchValidation.getMessage(), userConfirmPassword);
            return false;
        }
        
        return true;
    }
    
    /**
     * Helper method to show error message and focus on component
     */
    private void showError(String message, javax.swing.JComponent component) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
        component.requestFocus();
    }
    
    /**
     * Handles the registration process using service layer
     */
    private void performRegistration() {
        if (!validateAllFields()) {
            return;
        }
        
        // Create model objects
        String username = userName.getText().trim();
        Passenger passenger = new Passenger();
        passenger.setUsername(username); // Set username first for validation
        passenger.setFirstName(firstName.getText().trim());
        passenger.setLastName(lastName.getText().trim());
        passenger.setCellphoneNumber(cellphoneNumber.getText().trim());
        passenger.setProvince(userProvince.getText().trim());
        passenger.setCity(userCity.getText().trim());
        passenger.setBarangay(userBarangay.getText().trim());
        passenger.setIdType((String) idType.getSelectedItem());
        passenger.setIdNumber(idNumber.getText().trim());
        
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(new String(userPassword.getPassword()));
        
        // Use service layer for registration
        RegistrationService.RegistrationResult result = 
            RegistrationService.registerPassenger(passenger, account);
        
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear all fields
            clearFields();
            
            // Return to login frame
            this.dispose();
            new LogInFrame().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Clears all input fields
     */
    private void clearFields() {
        firstName.setText("");
        lastName.setText("");
        cellphoneNumber.setText("");
        userProvince.setText("");
        userCity.setText("");
        userBarangay.setText("");
        idType.setSelectedIndex(0);
        idNumber.setText("");
        userName.setText("");
        userPassword.setText("");
        userConfirmPassword.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        userRegistrationScrollPane = new javax.swing.JScrollPane();
        userRegistrationPanel = new javax.swing.JPanel();
        createNewAccountLabel = new javax.swing.JLabel();
        personalInfoLabel = new javax.swing.JLabel();
        lastNameLabel = new javax.swing.JLabel();
        lastName = new javax.swing.JTextField();
        firstNameLabel = new javax.swing.JLabel();
        cellphoneNumber = new javax.swing.JTextField();
        cellphoneNumberLabel = new javax.swing.JLabel();
        firstName = new javax.swing.JTextField();
        addressInfoLabel = new javax.swing.JLabel();
        provinceLabel = new javax.swing.JLabel();
        userProvince = new javax.swing.JTextField();
        cityLabel = new javax.swing.JLabel();
        userCity = new javax.swing.JTextField();
        barangayLabel = new javax.swing.JLabel();
        identificationLabel = new javax.swing.JLabel();
        idTypeLabel = new javax.swing.JLabel();
        userBarangay = new javax.swing.JTextField();
        idNumberLabel = new javax.swing.JLabel();
        idType = new javax.swing.JComboBox<>();
        idNumber = new javax.swing.JTextField();
        accountInfoLabel = new javax.swing.JLabel();
        userName = new javax.swing.JTextField();
        usernameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        textLabel = new javax.swing.JLabel();
        someTextLabel = new javax.swing.JLabel();
        confirmPasswordLabel = new javax.swing.JLabel();
        registerButton = new javax.swing.JButton();
        backToLogInButton = new javax.swing.JButton();
        userPassword = new javax.swing.JPasswordField();
        userConfirmPassword = new javax.swing.JPasswordField();
        somePanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1200, 800));
        setSize(new java.awt.Dimension(1200, 800));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        userRegistrationPanel.setBackground(new java.awt.Color(255, 255, 255));
        userRegistrationPanel.setAutoscrolls(true);

        createNewAccountLabel.setFont(new java.awt.Font("Arial Black", 1, 32)); // NOI18N
        createNewAccountLabel.setForeground(new java.awt.Color(11, 56, 118));
        createNewAccountLabel.setText("Create New Account");

        personalInfoLabel.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        personalInfoLabel.setForeground(new java.awt.Color(11, 56, 118));
        personalInfoLabel.setText("Personal Information");

        lastNameLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lastNameLabel.setForeground(new java.awt.Color(102, 102, 102));
        lastNameLabel.setText("Last  Name *");

        lastName.setBackground(new java.awt.Color(255, 255, 255));
        lastName.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lastName.setForeground(new java.awt.Color(0, 0, 0));

        firstNameLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        firstNameLabel.setForeground(new java.awt.Color(102, 102, 102));
        firstNameLabel.setText("First Name *");

        cellphoneNumber.setBackground(new java.awt.Color(255, 255, 255));
        cellphoneNumber.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        cellphoneNumber.setForeground(new java.awt.Color(0, 0, 0));

        cellphoneNumberLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        cellphoneNumberLabel.setForeground(new java.awt.Color(102, 102, 102));
        cellphoneNumberLabel.setText("Cellphone Number *");

        firstName.setBackground(new java.awt.Color(255, 255, 255));
        firstName.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        firstName.setForeground(new java.awt.Color(0, 0, 0));

        addressInfoLabel.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        addressInfoLabel.setForeground(new java.awt.Color(11, 56, 118));
        addressInfoLabel.setText("Address Information");

        provinceLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        provinceLabel.setForeground(new java.awt.Color(102, 102, 102));
        provinceLabel.setText("Province *");

        userProvince.setBackground(new java.awt.Color(255, 255, 255));
        userProvince.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        userProvince.setForeground(new java.awt.Color(0, 0, 0));

        cityLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        cityLabel.setForeground(new java.awt.Color(102, 102, 102));
        cityLabel.setText("City/Municipality *");

        userCity.setBackground(new java.awt.Color(255, 255, 255));
        userCity.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        userCity.setForeground(new java.awt.Color(0, 0, 0));

        barangayLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        barangayLabel.setForeground(new java.awt.Color(102, 102, 102));
        barangayLabel.setText("Barangay *");

        identificationLabel.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        identificationLabel.setForeground(new java.awt.Color(11, 56, 118));
        identificationLabel.setText("Identification");

        idTypeLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        idTypeLabel.setForeground(new java.awt.Color(102, 102, 102));
        idTypeLabel.setText("ID Type *");

        userBarangay.setBackground(new java.awt.Color(255, 255, 255));
        userBarangay.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        userBarangay.setForeground(new java.awt.Color(0, 0, 0));

        idNumberLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        idNumberLabel.setForeground(new java.awt.Color(102, 102, 102));
        idNumberLabel.setText("ID Number *");

        idType.setBackground(new java.awt.Color(255, 255, 255));
        idType.setForeground(new java.awt.Color(0, 0, 0));
        idType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select ID Type" }));

        idNumber.setBackground(new java.awt.Color(255, 255, 255));
        idNumber.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        idNumber.setForeground(new java.awt.Color(0, 0, 0));

        accountInfoLabel.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        accountInfoLabel.setForeground(new java.awt.Color(11, 56, 118));
        accountInfoLabel.setText("Account Information");

        userName.setBackground(new java.awt.Color(255, 255, 255));
        userName.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        userName.setForeground(new java.awt.Color(0, 0, 0));

        usernameLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        usernameLabel.setForeground(new java.awt.Color(102, 102, 102));
        usernameLabel.setText("Username *");

        passwordLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        passwordLabel.setForeground(new java.awt.Color(102, 102, 102));
        passwordLabel.setText("Password *");

        textLabel.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        textLabel.setForeground(new java.awt.Color(102, 102, 102));
        textLabel.setText("5 - 20 characters, letters, numbers and underscore only ");

        someTextLabel.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        someTextLabel.setForeground(new java.awt.Color(102, 102, 102));
        someTextLabel.setText("8+ characters, must contain letter, number and special characters (@$%*#?&)");

        confirmPasswordLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        confirmPasswordLabel.setForeground(new java.awt.Color(102, 102, 102));
        confirmPasswordLabel.setText("Confirm Password *");

        registerButton.setBackground(new java.awt.Color(11, 56, 118));
        registerButton.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        registerButton.setForeground(new java.awt.Color(255, 255, 255));
        registerButton.setText("Register");
        registerButton.setBorderPainted(false);
        registerButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerButtonActionPerformed(evt);
            }
        });

        backToLogInButton.setBackground(new java.awt.Color(255, 255, 255));
        backToLogInButton.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        backToLogInButton.setForeground(new java.awt.Color(11, 56, 118));
        backToLogInButton.setText("Back to Login");
        backToLogInButton.setBorderPainted(false);
        backToLogInButton.setContentAreaFilled(false);
        backToLogInButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backToLogInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backToLogInButtonActionPerformed(evt);
            }
        });

        userPassword.setBackground(new java.awt.Color(255, 255, 255));
        userPassword.setForeground(new java.awt.Color(0, 0, 0));

        userConfirmPassword.setBackground(new java.awt.Color(255, 255, 255));
        userConfirmPassword.setForeground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout userRegistrationPanelLayout = new javax.swing.GroupLayout(userRegistrationPanel);
        userRegistrationPanel.setLayout(userRegistrationPanelLayout);
        userRegistrationPanelLayout.setHorizontalGroup(
            userRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userRegistrationPanelLayout.createSequentialGroup()
                .addGroup(userRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, userRegistrationPanelLayout.createSequentialGroup()
                        .addGroup(userRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, userRegistrationPanelLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(personalInfoLabel))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, userRegistrationPanelLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(lastNameLabel)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, userRegistrationPanelLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(userRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(backToLogInButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(registerButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lastName, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(userRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(passwordLabel)
                                .addComponent(usernameLabel)
                                .addComponent(accountInfoLabel)
                                .addComponent(cellphoneNumberLabel)
                                .addComponent(firstName, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                                .addComponent(firstNameLabel)
                                .addComponent(cellphoneNumber)
                                .addComponent(addressInfoLabel)
                                .addComponent(provinceLabel)
                                .addComponent(userProvince, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                                .addComponent(cityLabel)
                                .addComponent(userCity, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                                .addComponent(barangayLabel)
                                .addComponent(identificationLabel)
                                .addComponent(idTypeLabel)
                                .addComponent(userBarangay, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                                .addComponent(idNumberLabel)
                                .addComponent(idType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(idNumber, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE))
                            .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textLabel)
                            .addComponent(someTextLabel)
                            .addComponent(confirmPasswordLabel)
                            .addComponent(userPassword)
                            .addComponent(userConfirmPassword))))
                .addGap(24, 24, 24))
            .addGroup(userRegistrationPanelLayout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addComponent(createNewAccountLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        userRegistrationPanelLayout.setVerticalGroup(
            userRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(userRegistrationPanelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(createNewAccountLabel)
                .addGap(29, 29, 29)
                .addComponent(personalInfoLabel)
                .addGap(12, 12, 12)
                .addComponent(lastNameLabel)
                .addGap(6, 6, 6)
                .addComponent(lastName, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(firstNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(firstName, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cellphoneNumberLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cellphoneNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(addressInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(provinceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userProvince, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cityLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userCity, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(barangayLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userBarangay, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(identificationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(idTypeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(idType, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(idNumberLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(idNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(accountInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(usernameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(passwordLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(someTextLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(confirmPasswordLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(registerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(backToLogInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(388, Short.MAX_VALUE))
        );

        userRegistrationScrollPane.setViewportView(userRegistrationPanel);

        getContentPane().add(userRegistrationScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 0, -1, 800));

        somePanel.setBackground(new java.awt.Color(204, 204, 204));

        javax.swing.GroupLayout somePanelLayout = new javax.swing.GroupLayout(somePanel);
        somePanel.setLayout(somePanelLayout);
        somePanelLayout.setHorizontalGroup(
            somePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1200, Short.MAX_VALUE)
        );
        somePanelLayout.setVerticalGroup(
            somePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );

        getContentPane().add(somePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1200, 800));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void backToLogInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backToLogInButtonActionPerformed
        // Return to login frame
        this.dispose();
        new LogInFrame().setVisible(true);
    }//GEN-LAST:event_backToLogInButtonActionPerformed

    private void registerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerButtonActionPerformed
        performRegistration();
    }//GEN-LAST:event_registerButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RegisterFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RegisterFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RegisterFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RegisterFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RegisterFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel accountInfoLabel;
    private javax.swing.JLabel addressInfoLabel;
    private javax.swing.JButton backToLogInButton;
    private javax.swing.JLabel barangayLabel;
    private javax.swing.JTextField cellphoneNumber;
    private javax.swing.JLabel cellphoneNumberLabel;
    private javax.swing.JLabel cityLabel;
    private javax.swing.JLabel confirmPasswordLabel;
    private javax.swing.JLabel createNewAccountLabel;
    private javax.swing.JTextField firstName;
    private javax.swing.JLabel firstNameLabel;
    private javax.swing.JTextField idNumber;
    private javax.swing.JLabel idNumberLabel;
    private javax.swing.JComboBox<String> idType;
    private javax.swing.JLabel idTypeLabel;
    private javax.swing.JLabel identificationLabel;
    private javax.swing.JTextField lastName;
    private javax.swing.JLabel lastNameLabel;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel personalInfoLabel;
    private javax.swing.JLabel provinceLabel;
    private javax.swing.JButton registerButton;
    private javax.swing.JPanel somePanel;
    private javax.swing.JLabel someTextLabel;
    private javax.swing.JLabel textLabel;
    private javax.swing.JTextField userBarangay;
    private javax.swing.JTextField userCity;
    private javax.swing.JPasswordField userConfirmPassword;
    private javax.swing.JTextField userName;
    private javax.swing.JPasswordField userPassword;
    private javax.swing.JTextField userProvince;
    private javax.swing.JPanel userRegistrationPanel;
    private javax.swing.JScrollPane userRegistrationScrollPane;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
