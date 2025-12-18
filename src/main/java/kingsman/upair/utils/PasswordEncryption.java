package kingsman.upair.utils;

/**
 * Utility class for encrypting and decrypting passwords
 * Uses XOR-based encryption for basic security
 * Follows Single Responsibility Principle
 */
public class PasswordEncryption {
    
    // Encryption key - in a real application, this should be more secure
    private static final String ENCRYPTION_KEY = "UPAir2024Key";
    
    /**
     * Encrypts a password using XOR cipher
     * @param password The plain text password to encrypt
     * @return The encrypted password in hex format
     */
    public static String encrypt(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        
        StringBuilder encrypted = new StringBuilder();
        int keyIndex = 0;
        
        for (int i = 0; i < password.length(); i++) {
            char originalChar = password.charAt(i);
            char keyChar = ENCRYPTION_KEY.charAt(keyIndex % ENCRYPTION_KEY.length());
            
            // XOR operation
            char encryptedChar = (char) (originalChar ^ keyChar);
            
            // Convert to hex for safe storage
            encrypted.append(String.format("%02X", (int) encryptedChar));
            
            keyIndex++;
        }
        
        return encrypted.toString();
    }
    
    /**
     * Decrypts an encrypted password
     * @param encryptedPassword The encrypted password in hex format
     * @return The decrypted plain text password
     */
    public static String decrypt(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            return encryptedPassword;
        }
        
        // Check if it's in hex format (encrypted)
        if (encryptedPassword.length() % 2 != 0) {
            return encryptedPassword; // Not encrypted, return as is
        }
        
        StringBuilder decrypted = new StringBuilder();
        int keyIndex = 0;
        
        // Convert hex string back to characters
        for (int i = 0; i < encryptedPassword.length(); i += 2) {
            try {
                int encryptedValue = Integer.parseInt(encryptedPassword.substring(i, i + 2), 16);
                char keyChar = ENCRYPTION_KEY.charAt(keyIndex % ENCRYPTION_KEY.length());
                
                // XOR operation (same as encryption)
                char decryptedChar = (char) (encryptedValue ^ keyChar);
                decrypted.append(decryptedChar);
                
                keyIndex++;
            } catch (NumberFormatException e) {
                // If parsing fails, return original
                return encryptedPassword;
            }
        }
        
        return decrypted.toString();
    }
}

