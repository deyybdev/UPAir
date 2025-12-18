package kingsman.upair.service;

import kingsman.upair.model.Account;
import kingsman.upair.repository.PassengerRepository;
import kingsman.upair.utils.PasswordEncryption;

/**
 * Service class for passenger login validation
 */
public class PassengerLoginService {
    
    /**
     * Validates passenger credentials
     * @param username The username
     * @param password The password
     * @return true if valid, false otherwise
     */
    public static boolean validatePassenger(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }
        
        Account account = PassengerRepository.getAccountByUsername(username);
        if (account == null) {
            return false;
        }
        
        // Decrypt and compare password
        String decryptedPassword = PasswordEncryption.decrypt(account.getEncryptedPassword());
        return decryptedPassword.equals(password);
    }
}

