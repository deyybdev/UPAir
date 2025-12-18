package kingsman.upair.service;

import kingsman.upair.model.Account;
import kingsman.upair.model.Passenger;
import kingsman.upair.repository.PassengerRepository;
import kingsman.upair.utils.ValidationUtils;

/**
 * Service class for handling registration business logic
 * Follows Service Layer Pattern and Single Responsibility Principle
 * Separates business logic from UI components
 */
public class RegistrationService {
    
    /**
     * Registers a new passenger
     * Handles validation, duplicate checking, and data persistence
     * 
     * @param passenger The passenger personal information
     * @param account The account information
     * @return RegistrationResult containing success status and message
     */
    public static RegistrationResult registerPassenger(Passenger passenger, Account account) {
        // Validate passenger data
        if (passenger == null || !passenger.isValid()) {
            return new RegistrationResult(false, "Invalid passenger information!");
        }
        
        // Validate account data
        if (account == null || !account.isValid()) {
            return new RegistrationResult(false, "Invalid account information!");
        }
        
        // Validate username format
        ValidationUtils.ValidationResult usernameValidation = ValidationUtils.validateUsername(account.getUsername());
        if (!usernameValidation.isValid()) {
            return new RegistrationResult(false, usernameValidation.getMessage());
        }
        
        // Validate password format
        ValidationUtils.ValidationResult passwordValidation = ValidationUtils.validatePassword(account.getPassword());
        if (!passwordValidation.isValid()) {
            return new RegistrationResult(false, passwordValidation.getMessage());
        }
        
        // Check for duplicate username
        if (PassengerRepository.usernameExists(account.getUsername())) {
            return new RegistrationResult(false, "Username already exists! Please choose a different username.");
        }
        
        // Ensure passenger username matches account username
        passenger.setUsername(account.getUsername());
        
        // Save passenger data
        boolean passengerSaved = PassengerRepository.savePassenger(passenger);
        if (!passengerSaved) {
            return new RegistrationResult(false, "Failed to save passenger information!");
        }
        
        // Save account data
        boolean accountSaved = PassengerRepository.saveAccount(account);
        if (!accountSaved) {
            return new RegistrationResult(false, "Failed to save account information!");
        }
        
        return new RegistrationResult(true, "Registration successful! You can now log in with your credentials.");
    }
    
    /**
     * Validates password confirmation
     * 
     * @param password The original password
     * @param confirmPassword The confirmation password
     * @return ValidationResult containing validation status and message
     */
    public static ValidationUtils.ValidationResult validatePasswordConfirmation(String password, String confirmPassword) {
        return ValidationUtils.validatePasswordMatch(password, confirmPassword);
    }
    
    /**
     * Inner class to represent registration result
     * Encapsulates registration outcome
     */
    public static class RegistrationResult {
        private final boolean success;
        private final String message;
        
        public RegistrationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

