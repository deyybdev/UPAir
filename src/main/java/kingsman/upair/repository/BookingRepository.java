package kingsman.upair.repository;

import kingsman.upair.model.Booking;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Repository class for managing booking data persistence
 * Follows Repository Pattern and Single Responsibility Principle
 */
public class BookingRepository {
    
    private static final String BOOKING_FILE = "BKpassenger_data.txt";
    private static final String DELIMITER = "|";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    // In-memory cache
    private static final Map<String, Booking> bookingCache = new HashMap<>();
    private static boolean cacheLoaded = false;
    
    /**
     * Loads all bookings from file into memory cache
     */
    private static void loadCache() {
        if (cacheLoaded) {
            return;
        }
        
        File file = new File(BOOKING_FILE);
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
                if (parts.length >= 12) {
                    Booking booking = new Booking();
                    booking.setBookingId(parts[0].trim());
                    booking.setPassengerUsername(parts[1].trim());
                    booking.setTripType(parts[2].trim());
                    booking.setFlightCode(parts[3].trim());
                    booking.setReturnFlightCode(parts[4].trim().isEmpty() ? null : parts[4].trim());
                    booking.setOrigin(parts[5].trim());
                    booking.setDestination(parts[6].trim());
                    booking.setDepartureDate(LocalDate.parse(parts[7].trim(), DATE_FORMATTER));
                    booking.setDepartureTime(LocalTime.parse(parts[8].trim(), TIME_FORMATTER));
                    booking.setReturnDate(parts[9].trim().isEmpty() ? null : LocalDate.parse(parts[9].trim(), DATE_FORMATTER));
                    booking.setReturnTime(parts[10].trim().isEmpty() ? null : LocalTime.parse(parts[10].trim(), TIME_FORMATTER));
                    booking.setNumberOfAdults(Integer.parseInt(parts[11].trim()));
                    booking.setNumberOfMinors(Integer.parseInt(parts[12].trim()));
                    
                    // Parse seats and names (format: "A1:John Doe,B2:Jane Doe")
                    if (parts.length > 13 && !parts[13].trim().isEmpty()) {
                        List<String> seats = new ArrayList<>();
                        List<String> names = new ArrayList<>();
                        String[] seatNamePairs = parts[13].trim().split(",");
                        for (String pair : seatNamePairs) {
                            String[] seatName = pair.split(":");
                            if (seatName.length == 2) {
                                seats.add(seatName[0].trim());
                                names.add(seatName[1].trim());
                            }
                        }
                        booking.setReservedSeats(seats);
                        booking.setPassengerNames(names);
                    }
                    
                    booking.setTotalPrice(Double.parseDouble(parts[14].trim()));
                    booking.setStatus(parts[15].trim());
                    booking.setVoucherCode(parts.length > 16 && !parts[16].trim().isEmpty() ? parts[16].trim() : null);
                    booking.setSeatPreference(parts.length > 17 && !parts[17].trim().isEmpty() ? parts[17].trim() : null);
                    booking.setPaymentType(parts.length > 18 && !parts[18].trim().isEmpty() ? parts[18].trim() : null);
                    
                    bookingCache.put(booking.getBookingId(), booking);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
    }
    
    /**
     * Generates a unique booking ID
     * Format: BK-YYYYMMDD-HHMMSS-XXXX
     */
    public static String generateBookingId() {
        loadCache();
        String baseId;
        Random random = new Random();
        
        do {
            LocalDate now = LocalDate.now();
            LocalTime time = LocalTime.now();
            int randomNum = random.nextInt(10000);
            baseId = String.format("BK-%s-%s-%04d",
                now.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                time.format(DateTimeFormatter.ofPattern("HHmmss")),
                randomNum);
        } while (bookingCache.containsKey(baseId));
        
        return baseId;
    }
    
    /**
     * Saves a booking to file
     */
    public static boolean saveBooking(Booking booking) {
        if (booking == null || !booking.isValid()) {
            return false;
        }
        
        if (booking.getBookingId() == null || booking.getBookingId().trim().isEmpty()) {
            booking.setBookingId(generateBookingId());
        }
        
        loadCache();
        
        try {
            File file = new File(BOOKING_FILE);
            boolean fileExists = file.exists();
            
            // Format seats and names as "A1:John Doe,B2:Jane Doe"
            StringBuilder seatNamePairs = new StringBuilder();
            if (booking.getReservedSeats() != null && booking.getPassengerNames() != null) {
                for (int i = 0; i < booking.getReservedSeats().size(); i++) {
                    if (i > 0) seatNamePairs.append(",");
                    seatNamePairs.append(booking.getReservedSeats().get(i))
                                 .append(":")
                                 .append(booking.getPassengerNames().get(i));
                }
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                writer.println(String.join(DELIMITER,
                    booking.getBookingId(),
                    booking.getPassengerUsername(),
                    booking.getTripType(),
                    booking.getFlightCode(),
                    booking.getReturnFlightCode() != null ? booking.getReturnFlightCode() : "",
                    booking.getOrigin(),
                    booking.getDestination(),
                    booking.getDepartureDate().format(DATE_FORMATTER),
                    booking.getDepartureTime().format(TIME_FORMATTER),
                    booking.getReturnDate() != null ? booking.getReturnDate().format(DATE_FORMATTER) : "",
                    booking.getReturnTime() != null ? booking.getReturnTime().format(TIME_FORMATTER) : "",
                    String.valueOf(booking.getNumberOfAdults()),
                    String.valueOf(booking.getNumberOfMinors()),
                    seatNamePairs.toString(),
                    String.valueOf(booking.getTotalPrice()),
                    booking.getStatus(),
                    booking.getVoucherCode() != null ? booking.getVoucherCode() : "",
                    booking.getSeatPreference() != null ? booking.getSeatPreference() : "",
                    booking.getPaymentType() != null ? booking.getPaymentType() : ""
                ));
            }
            
            bookingCache.put(booking.getBookingId(), booking);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving booking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets all bookings
     */
    public static List<Booking> getAllBookings() {
        loadCache();
        return new ArrayList<>(bookingCache.values());
    }
    
    /**
     * Gets bookings by passenger username
     */
    public static List<Booking> getBookingsByUsername(String username) {
        loadCache();
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookingCache.values()) {
            if (booking.getPassengerUsername().equalsIgnoreCase(username)) {
                result.add(booking);
            }
        }
        return result;
    }
    
    /**
     * Gets a booking by booking ID
     */
    public static Booking getBookingById(String bookingId) {
        loadCache();
        return bookingCache.get(bookingId);
    }
    
    /**
     * Gets reserved seats for a flight code and date
     */
    public static List<String> getReservedSeatsForFlight(String flightCode, LocalDate date) {
        loadCache();
        List<String> reservedSeats = new ArrayList<>();
        for (Booking booking : bookingCache.values()) {
            if (booking.getFlightCode().equals(flightCode) &&
                booking.getDepartureDate().equals(date) &&
                !booking.getStatus().equals("Cancelled")) {
                if (booking.getReservedSeats() != null) {
                    reservedSeats.addAll(booking.getReservedSeats());
                }
            }
        }
        return reservedSeats;
    }
    
    /**
     * Updates an existing booking in the file and cache
     * @param updatedBooking The booking with updated information
     * @return true if update was successful, false otherwise
     */
    public static boolean updateBooking(Booking updatedBooking) {
        if (updatedBooking == null || updatedBooking.getBookingId() == null ||
            updatedBooking.getBookingId().trim().isEmpty()) {
            return false;
        }
        
        loadCache();
        if (!bookingCache.containsKey(updatedBooking.getBookingId())) {
            return false; // Booking doesn't exist
        }
        
        // Reload all bookings, update the one we need, and rewrite the file
        List<Booking> bookings = getAllBookings();
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).getBookingId().equals(updatedBooking.getBookingId())) {
                bookings.set(i, updatedBooking);
                break;
            }
        }
        
        // Rewrite file with updated bookings
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKING_FILE))) {
            for (Booking booking : bookings) {
                // Format seats and names as "A1:John Doe,B2:Jane Doe"
                StringBuilder seatNamePairs = new StringBuilder();
                if (booking.getReservedSeats() != null && booking.getPassengerNames() != null) {
                    for (int i = 0; i < booking.getReservedSeats().size(); i++) {
                        if (i > 0) seatNamePairs.append(",");
                        seatNamePairs.append(booking.getReservedSeats().get(i))
                                     .append(":")
                                     .append(booking.getPassengerNames().get(i));
                    }
                }
                
                writer.println(String.join(DELIMITER,
                    booking.getBookingId(),
                    booking.getPassengerUsername(),
                    booking.getTripType(),
                    booking.getFlightCode(),
                    booking.getReturnFlightCode() != null ? booking.getReturnFlightCode() : "",
                    booking.getOrigin(),
                    booking.getDestination(),
                    booking.getDepartureDate().format(DATE_FORMATTER),
                    booking.getDepartureTime().format(TIME_FORMATTER),
                    booking.getReturnDate() != null ? booking.getReturnDate().format(DATE_FORMATTER) : "",
                    booking.getReturnTime() != null ? booking.getReturnTime().format(TIME_FORMATTER) : "",
                    String.valueOf(booking.getNumberOfAdults()),
                    String.valueOf(booking.getNumberOfMinors()),
                    seatNamePairs.toString(),
                    String.valueOf(booking.getTotalPrice()),
                    booking.getStatus(),
                    booking.getVoucherCode() != null ? booking.getVoucherCode() : "",
                    booking.getSeatPreference() != null ? booking.getSeatPreference() : "",
                    booking.getPaymentType() != null ? booking.getPaymentType() : ""
                ));
            }
        } catch (IOException e) {
            System.err.println("Error updating booking: " + e.getMessage());
            return false;
        }
        
        // Update cache
        bookingCache.put(updatedBooking.getBookingId(), updatedBooking);
        return true;
    }
    
    /**
     * Clears the cache
     */
    public static void clearCache() {
        bookingCache.clear();
        cacheLoaded = false;
    }
}

