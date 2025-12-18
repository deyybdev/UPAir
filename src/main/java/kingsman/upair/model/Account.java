package kingsman.upair.model;

/**
 * Model class representing a user's account information
 * Follows OOP principles with encapsulation
 */
public class Account {
    private String username;
    private String password; // Plain text password (will be encrypted when stored)
    private String encryptedPassword; // Encrypted password for storage
    
    // Default constructor
    public Account() {
    }
    
    // Parameterized constructor
    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getters and Setters (Encapsulation)
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEncryptedPassword() {
        return encryptedPassword;
    }
    
    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
    
    /**
     * Validates if account information is valid
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", hasPassword=" + (password != null && !password.isEmpty()) +
                ", hasEncryptedPassword=" + (encryptedPassword != null && !encryptedPassword.isEmpty()) +
                '}';
    }
}

