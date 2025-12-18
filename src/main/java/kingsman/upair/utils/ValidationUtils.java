package kingsman.upair.utils;

/**
 * Utility class for validation operations
 * Follows Single Responsibility Principle
 */
public class ValidationUtils {
    
    // Special characters allowed in password
    private static final String SPECIAL_CHARS = "@$%*#?&";
    
    // Minimum and maximum username length
    private static final int MIN_USERNAME_LENGTH = 5;
    private static final int MAX_USERNAME_LENGTH = 20;
    
    // Minimum password length
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    /**
     * Validates username format
     * Rules: 5-20 characters, letters, numbers, and underscore only
     * 
     * @param username The username to validate
     * @return ValidationResult containing isValid flag and error message
     */
    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "Username is required!");
        }
        
        String trimmedUsername = username.trim();
        
        // Check length: 5-20 characters
        if (trimmedUsername.length() < MIN_USERNAME_LENGTH || trimmedUsername.length() > MAX_USERNAME_LENGTH) {
            return new ValidationResult(false, 
                String.format("Username must be %d-%d characters long!", 
                    MIN_USERNAME_LENGTH, MAX_USERNAME_LENGTH));
        }
        
        // Check if contains only letters, numbers, and underscore
        if (!trimmedUsername.matches("^[a-zA-Z0-9_]+$")) {
            return new ValidationResult(false, 
                "Username must contain only letters, numbers, and underscore!");
        }
        
        return new ValidationResult(true, "Username is valid");
    }
    
    /**
     * Validates password format
     * Rules: 8+ characters, must contain letter, number, and special character
     * 
     * @param password The password to validate
     * @return ValidationResult containing isValid flag and error message
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return new ValidationResult(false, 
                String.format("Password must be at least %d characters long!", MIN_PASSWORD_LENGTH));
        }
        
        boolean hasLetter = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;
        
        // Check each character
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            } else if (SPECIAL_CHARS.indexOf(c) != -1) {
                hasSpecial = true;
            }
        }
        
        // Build error message if validation fails
        if (!hasLetter || !hasNumber || !hasSpecial) {
            StringBuilder errorMsg = new StringBuilder("Password must contain:");
            if (!hasLetter) errorMsg.append(" at least one letter,");
            if (!hasNumber) errorMsg.append(" at least one number,");
            if (!hasSpecial) errorMsg.append(" at least one special character (" + SPECIAL_CHARS + "),");
            errorMsg.setLength(errorMsg.length() - 1); // Remove last comma
            return new ValidationResult(false, errorMsg.toString());
        }
        
        return new ValidationResult(true, "Password is valid");
    }
    
    /**
     * Validates if two passwords match
     * 
     * @param password The original password
     * @param confirmPassword The confirmation password
     * @return ValidationResult containing isValid flag and error message
     */
    public static ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return new ValidationResult(false, "Both password fields are required!");
        }
        
        if (!password.equals(confirmPassword)) {
            return new ValidationResult(false, "Passwords do not match!");
        }
        
        return new ValidationResult(true, "Passwords match");
    }
    
    /**
     * Inner class to represent validation result
     * Encapsulates validation outcome
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String message;
        
        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

