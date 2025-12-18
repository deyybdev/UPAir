package kingsman.upair.repository;

import kingsman.upair.model.Account;
import kingsman.upair.model.Passenger;
import kingsman.upair.utils.PasswordEncryption;

import java.io.*;
import java.util.*;

/**
 * Repository class for managing passenger data persistence
 * Follows Repository Pattern and Single Responsibility Principle
 * Uses data structures (List, Map) for efficient data management
 */
public class PassengerRepository {
    
    private static final String PD_FILE = "PDpassenger_data.txt";
    private static final String AF_FILE = "AFpassenger_data.txt";
    private static final String DELIMITER = "|";
    
    // In-memory cache using Map for O(1) lookup performance
    private static final Map<String, Account> accountCache = new HashMap<>();
    private static final Map<String, Passenger> passengerCache = new HashMap<>();
    
    // Flag to track if cache is loaded
    private static boolean cacheLoaded = false;
    
    /**
     * Loads all data from files into memory cache
     * Improves performance by reducing file I/O operations
     */
    private static void loadCache() {
        if (cacheLoaded) {
            return;
        }
        
        loadAccounts();
        loadPassengers();
        cacheLoaded = true;
    }
    
    /**
     * Loads accounts from file into cache
     */
    private static void loadAccounts() {
        File file = new File(AF_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\" + DELIMITER);
                if (parts.length >= 2) {
                    Account account = new Account();
                    account.setUsername(parts[0].trim());
                    account.setEncryptedPassword(parts[1].trim());
                    accountCache.put(account.getUsername().toLowerCase(), account);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading accounts: " + e.getMessage());
        }
    }
    
    /**
     * Loads passengers from file into cache
     */
    private static void loadPassengers() {
        File file = new File(PD_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\" + DELIMITER);
                if (parts.length >= 9) {
                    Passenger passenger = new Passenger(
                        parts[0].trim(), // username
                        parts[1].trim(), // firstName
                        parts[2].trim(), // lastName
                        parts[3].trim(), // cellphoneNumber
                        parts[4].trim(), // province
                        parts[5].trim(), // city
                        parts[6].trim(), // barangay
                        parts[7].trim(), // idType
                        parts[8].trim()  // idNumber
                    );
                    passengerCache.put(passenger.getUsername().toLowerCase(), passenger);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading passengers: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a username already exists
     * Uses cache for O(1) lookup performance
     * 
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public static boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        loadCache();
        return accountCache.containsKey(username.trim().toLowerCase());
    }
    
    /**
     * Saves passenger personal details to file
     * Also updates the cache
     * 
     * @param passenger The passenger object to save
     * @return true if successful, false otherwise
     */
    public static boolean savePassenger(Passenger passenger) {
        if (passenger == null || !passenger.isValid()) {
            return false;
        }
        
        try {
            File file = new File(PD_FILE);
            boolean fileExists = file.exists();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                // Write data with delimiter
                writer.println(String.join(DELIMITER, 
                    passenger.getUsername(),
                    passenger.getFirstName(),
                    passenger.getLastName(),
                    passenger.getCellphoneNumber(),
                    passenger.getProvince(),
                    passenger.getCity(),
                    passenger.getBarangay(),
                    passenger.getIdType(),
                    passenger.getIdNumber()
                ));
            }
            
            // Update cache
            passengerCache.put(passenger.getUsername().toLowerCase(), passenger);
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving passenger: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves account information to file
     * Password is encrypted before saving
     * Also updates the cache
     * 
     * @param account The account object to save
     * @return true if successful, false otherwise
     */
    public static boolean saveAccount(Account account) {
        if (account == null || !account.isValid()) {
            return false;
        }
        
        try {
            // Encrypt password
            String encryptedPassword = PasswordEncryption.encrypt(account.getPassword());
            account.setEncryptedPassword(encryptedPassword);
            
            File file = new File(AF_FILE);
            boolean fileExists = file.exists();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                // Write data with delimiter
                writer.println(String.join(DELIMITER, 
                    account.getUsername(),
                    account.getEncryptedPassword()
                ));
            }
            
            // Update cache
            accountCache.put(account.getUsername().toLowerCase(), account);
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving account: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves a passenger by username
     * Uses cache for O(1) lookup
     * 
     * @param username The username to search for
     * @return Passenger object if found, null otherwise
     */
    public static Passenger getPassengerByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        loadCache();
        return passengerCache.get(username.trim().toLowerCase());
    }
    
    /**
     * Retrieves an account by username
     * Uses cache for O(1) lookup
     * 
     * @param username The username to search for
     * @return Account object if found, null otherwise
     */
    public static Account getAccountByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        loadCache();
        return accountCache.get(username.trim().toLowerCase());
    }
    
    /**
     * Gets all usernames (for admin/debugging purposes)
     * 
     * @return List of all usernames
     */
    public static List<String> getAllUsernames() {
        loadCache();
        return new ArrayList<>(accountCache.keySet());
    }
    
    /**
     * Clears the cache (useful for testing or when files are modified externally)
     */
    public static void clearCache() {
        accountCache.clear();
        passengerCache.clear();
        cacheLoaded = false;
    }
}

