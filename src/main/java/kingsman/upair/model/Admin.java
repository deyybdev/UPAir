package kingsman.upair.model;

/**
 * Model class representing an Admin account
 * Follows OOP principles with encapsulation
 */
public class Admin {
    private String username;
    private String password;
    
    // Default constructor
    public Admin() {
    }
    
    // Parameterized constructor
    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getters and Setters
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
    
    /**
     * Validates if admin credentials are valid
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "username='" + username + '\'' +
                ", hasPassword=" + (password != null && !password.isEmpty()) +
                '}';
    }
}

