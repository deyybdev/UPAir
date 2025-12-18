package kingsman.upair.repository;

import kingsman.upair.model.Admin;
import kingsman.upair.utils.PasswordEncryption;

import java.io.*;
import java.util.*;

/**
 * Repository class for managing admin data persistence
 * Follows Repository Pattern and Single Responsibility Principle
 */
public class AdminRepository {
    
    private static final String ADMIN_FILE = "admin_data.txt";
    private static final String PIN_FILE = "administration_pin.txt";
    private static final String DELIMITER = "|";
    
    /**
     * Initializes admin data file with default credentials if it doesn't exist
     */
    public static void initializeAdminData() {
        File file = new File(ADMIN_FILE);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Default admin credentials
                String encryptedPassword = PasswordEncryption.encrypt("admin@123");
                writer.println(String.join(DELIMITER, "admin", encryptedPassword));
            } catch (IOException e) {
                System.err.println("Error initializing admin data: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initializes administration PIN file with 8 pins if it doesn't exist
     */
    public static void initializeAdminPins() {
        File file = new File(PIN_FILE);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Generate 8 random 6-character pins
                Random random = new Random();
                for (int i = 0; i < 8; i++) {
                    String pin = generatePin(random);
                    writer.println(pin);
                }
            } catch (IOException e) {
                System.err.println("Error initializing admin pins: " + e.getMessage());
            }
        }
    }
    
    /**
     * Generates a random 6-character PIN
     */
    private static String generatePin(Random random) {
        StringBuilder pin = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 6; i++) {
            pin.append(chars.charAt(random.nextInt(chars.length())));
        }
        return pin.toString();
    }
    
    /**
     * Validates admin credentials
     * @param username The username to check
     * @param password The password to check
     * @return true if credentials are valid, false otherwise
     */
    public static boolean validateAdmin(String username, String password) {
        File file = new File(ADMIN_FILE);
        if (!file.exists()) {
            initializeAdminData();
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\" + DELIMITER);
                if (parts.length >= 2) {
                    String fileUsername = parts[0].trim();
                    String encryptedPassword = parts[1].trim();
                    String decryptedPassword = PasswordEncryption.decrypt(encryptedPassword);
                    
                    if (fileUsername.equalsIgnoreCase(username) && decryptedPassword.equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error validating admin: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Validates and consumes an administration PIN
     * If PIN is valid, it is removed from the file (like Google backup codes)
     * @param pin The PIN to validate
     * @return true if PIN is valid and consumed, false otherwise
     */
    public static boolean validateAndConsumePin(String pin) {
        File file = new File(PIN_FILE);
        if (!file.exists()) {
            initializeAdminPins();
            return false; // New file created, no pins available yet
        }
        
        List<String> pins = new ArrayList<>();
        boolean pinFound = false;
        
        // Read all pins
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String filePin = line.trim();
                if (filePin.equalsIgnoreCase(pin)) {
                    pinFound = true;
                    // Don't add this pin to the list (effectively removing it)
                } else {
                    pins.add(filePin);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading pins: " + e.getMessage());
            return false;
        }
        
        // If PIN was found, write back the remaining pins
        if (pinFound) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (String remainingPin : pins) {
                    writer.println(remainingPin);
                }
            } catch (IOException e) {
                System.err.println("Error updating pins: " + e.getMessage());
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets the number of remaining PINs
     * @return Number of remaining PINs
     */
    public static int getRemainingPinCount() {
        File file = new File(PIN_FILE);
        if (!file.exists()) {
            return 0;
        }
        
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            System.err.println("Error counting pins: " + e.getMessage());
        }
        
        return count;
    }
}

