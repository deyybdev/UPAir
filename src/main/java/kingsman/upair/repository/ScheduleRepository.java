package kingsman.upair.repository;

import kingsman.upair.model.Schedule;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Repository class for managing schedule data persistence
 * Follows Repository Pattern and Single Responsibility Principle
 * Uses data structures for efficient data management
 */
public class ScheduleRepository {
    
    private static final String SCHEDULE_FILE = "SCHflight_data.txt";
    private static final String DELIMITER = "|";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    // In-memory cache using Map for O(1) lookup performance
    private static final Map<String, Schedule> scheduleCache = new HashMap<>();
    private static boolean cacheLoaded = false;
    
    /**
     * Loads all schedules from file into memory cache
     */
    private static void loadCache() {
        if (cacheLoaded) {
            return;
        }
        
        File file = new File(SCHEDULE_FILE);
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
                if (parts.length >= 7) {
                    Schedule schedule = new Schedule();
                    schedule.setScheduleId(parts[0].trim());
                    schedule.setAirline(parts[1].trim());
                    schedule.setFlightCode(parts[2].trim());
                    schedule.setOrigin(parts[3].trim());
                    schedule.setDestination(parts[4].trim());
                    schedule.setDepartureDate(LocalDate.parse(parts[5].trim(), DATE_FORMATTER));
                    schedule.setDepartureTime(LocalTime.parse(parts[6].trim(), TIME_FORMATTER));
                    scheduleCache.put(schedule.getScheduleId(), schedule);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading schedules: " + e.getMessage());
        }
    }
    
    /**
     * Generates a unique schedule ID
     * Format: SCH-YYYYMMDD-HHMMSS-XXXX (where XXXX is random)
     * @return Unique schedule ID
     */
    public static String generateScheduleId() {
        loadCache();
        String baseId;
        Random random = new Random();
        
        do {
            LocalDate now = LocalDate.now();
            LocalTime time = LocalTime.now();
            int randomNum = random.nextInt(10000);
            baseId = String.format("SCH-%s-%s-%04d",
                now.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                time.format(DateTimeFormatter.ofPattern("HHmmss")),
                randomNum);
        } while (scheduleCache.containsKey(baseId)); // Ensure uniqueness
        
        return baseId;
    }
    
    /**
     * Saves a schedule to file
     * @param schedule The schedule to save
     * @return true if successful, false otherwise
     */
    public static boolean saveSchedule(Schedule schedule) {
        if (schedule == null || !schedule.isValid()) {
            return false;
        }
        
        // Generate ID if not set
        if (schedule.getScheduleId() == null || schedule.getScheduleId().trim().isEmpty()) {
            schedule.setScheduleId(generateScheduleId());
        }
        
        loadCache();
        
        // Check for duplicate schedule ID
        if (scheduleCache.containsKey(schedule.getScheduleId())) {
            return false; // Duplicate ID
        }
        
        try {
            File file = new File(SCHEDULE_FILE);
            boolean fileExists = file.exists();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                writer.println(String.join(DELIMITER,
                    schedule.getScheduleId(),
                    schedule.getAirline(),
                    schedule.getFlightCode(),
                    schedule.getOrigin(),
                    schedule.getDestination(),
                    schedule.getDepartureDate().format(DATE_FORMATTER),
                    schedule.getDepartureTime().format(TIME_FORMATTER)
                ));
            }
            
            // Update cache
            scheduleCache.put(schedule.getScheduleId(), schedule);
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving schedule: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates an existing schedule
     * @param schedule The schedule to update
     * @return true if successful, false otherwise
     */
    public static boolean updateSchedule(Schedule schedule) {
        if (schedule == null || !schedule.isValid()) {
            return false;
        }
        
        loadCache();
        if (!scheduleCache.containsKey(schedule.getScheduleId())) {
            return false; // Schedule doesn't exist
        }
        
        // Reload all schedules, update the one we need, and rewrite
        List<Schedule> schedules = getAllSchedules();
        for (int i = 0; i < schedules.size(); i++) {
            if (schedules.get(i).getScheduleId().equals(schedule.getScheduleId())) {
                schedules.set(i, schedule);
                break;
            }
        }
        
        // Rewrite file
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCHEDULE_FILE))) {
            for (Schedule s : schedules) {
                writer.println(String.join(DELIMITER,
                    s.getScheduleId(),
                    s.getAirline(),
                    s.getFlightCode(),
                    s.getOrigin(),
                    s.getDestination(),
                    s.getDepartureDate().format(DATE_FORMATTER),
                    s.getDepartureTime().format(TIME_FORMATTER)
                ));
            }
        } catch (IOException e) {
            System.err.println("Error updating schedule: " + e.getMessage());
            return false;
        }
        
        // Update cache
        scheduleCache.put(schedule.getScheduleId(), schedule);
        
        return true;
    }
    
    /**
     * Deletes a schedule by schedule ID
     * @param scheduleId The schedule ID to delete
     * @return true if successful, false otherwise
     */
    public static boolean deleteSchedule(String scheduleId) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            return false;
        }
        
        loadCache();
        if (!scheduleCache.containsKey(scheduleId)) {
            return false; // Schedule doesn't exist
        }
        
        // Reload all schedules, remove the one we need, and rewrite
        List<Schedule> schedules = getAllSchedules();
        schedules.removeIf(s -> s.getScheduleId().equals(scheduleId));
        
        // Rewrite file
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCHEDULE_FILE))) {
            for (Schedule s : schedules) {
                writer.println(String.join(DELIMITER,
                    s.getScheduleId(),
                    s.getAirline(),
                    s.getFlightCode(),
                    s.getOrigin(),
                    s.getDestination(),
                    s.getDepartureDate().format(DATE_FORMATTER),
                    s.getDepartureTime().format(TIME_FORMATTER)
                ));
            }
        } catch (IOException e) {
            System.err.println("Error deleting schedule: " + e.getMessage());
            return false;
        }
        
        // Update cache
        scheduleCache.remove(scheduleId);
        
        return true;
    }
    
    /**
     * Gets all schedules
     * @return List of all schedules
     */
    public static List<Schedule> getAllSchedules() {
        loadCache();
        return new ArrayList<>(scheduleCache.values());
    }
    
    /**
     * Gets schedules by airline
     * @param airline The airline name
     * @return List of schedules for the airline
     */
    public static List<Schedule> getSchedulesByAirline(String airline) {
        loadCache();
        List<Schedule> result = new ArrayList<>();
        for (Schedule schedule : scheduleCache.values()) {
            if (schedule.getAirline().equalsIgnoreCase(airline)) {
                result.add(schedule);
            }
        }
        return result;
    }
    
    /**
     * Gets a schedule by schedule ID
     * @param scheduleId The schedule ID
     * @return Schedule object if found, null otherwise
     */
    public static Schedule getScheduleById(String scheduleId) {
        loadCache();
        return scheduleCache.get(scheduleId);
    }
    
    /**
     * Gets flight codes for a specific airline
     * @param airline The airline name
     * @return List of flight codes
     */
    public static List<String> getFlightCodesByAirline(String airline) {
        List<String> flightCodes = new ArrayList<>();
        List<kingsman.upair.model.Flight> flights = FlightRepository.getFlightsByAirline(airline);
        for (kingsman.upair.model.Flight flight : flights) {
            flightCodes.add(flight.getFlightCode());
        }
        return flightCodes;
    }
    
    /**
     * Gets flight details by flight code
     * @param flightCode The flight code
     * @return Flight object if found, null otherwise
     */
    public static kingsman.upair.model.Flight getFlightByCode(String flightCode) {
        return FlightRepository.getFlightByCode(flightCode);
    }
    
    /**
     * Clears the cache
     */
    public static void clearCache() {
        scheduleCache.clear();
        cacheLoaded = false;
    }
}

