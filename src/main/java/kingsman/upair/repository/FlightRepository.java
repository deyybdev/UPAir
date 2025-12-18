package kingsman.upair.repository;

import kingsman.upair.model.Flight;
import kingsman.upair.model.FlightOffer;

import java.io.*;
import java.util.*;

/**
 * Repository class for managing flight data persistence
 * Follows Repository Pattern and Single Responsibility Principle
 * Uses data structures for efficient data management
 */
public class FlightRepository {
    
    private static final String FLIGHT_DATA_FILE = "FDflight_data.txt";
    private static final String FLIGHT_OFFER_FILE = "FOflight_data.txt";
    private static final String DELIMITER = "|";
    
    // In-memory cache using Map for O(1) lookup performance
    private static final Map<String, Flight> flightCache = new HashMap<>();
    private static final Map<String, FlightOffer> offerCache = new HashMap<>();
    private static boolean cacheLoaded = false;
    
    /**
     * Loads all flight data from files into memory cache
     */
    private static void loadCache() {
        if (cacheLoaded) {
            return;
        }
        
        loadFlights();
        loadOffers();
        cacheLoaded = true;
    }
    
    /**
     * Loads flights from file into cache
     */
    private static void loadFlights() {
        File file = new File(FLIGHT_DATA_FILE);
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
                if (parts.length >= 8) {
                    Flight flight = new Flight(
                        parts[0].trim(), // airline
                        parts[1].trim(), // origin
                        parts[2].trim(), // destination
                        parts[3].trim(), // duration
                        parts[4].trim(), // aircraftModel
                        parts[5].trim(), // flightCode
                        Integer.parseInt(parts[6].trim()), // seatCapacity
                        Double.parseDouble(parts[7].trim()) // baseFare
                    );
                    flightCache.put(flight.getFlightCode(), flight);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading flights: " + e.getMessage());
        }
    }
    
    /**
     * Loads flight offers from file into cache
     */
    private static void loadOffers() {
        File file = new File(FLIGHT_OFFER_FILE);
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
                    FlightOffer offer = new FlightOffer(
                        parts[0].trim(), // flightCode
                        parts[1].trim(), // cabinClass
                        parts[2].trim(), // seatType
                        parts[3].trim(), // foodAndBeverages
                        parts[4].trim(), // entertainment
                        parts[5].trim(), // amenity
                        parts[6].trim()  // moreDetails
                    );
                    offerCache.put(offer.getFlightCode(), offer);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading offers: " + e.getMessage());
        }
    }
    
    /**
     * Saves a flight to file
     * @param flight The flight to save
     * @return true if successful, false otherwise
     */
    public static boolean saveFlight(Flight flight) {
        if (flight == null || !flight.isValid()) {
            return false;
        }
        
        // Check for duplicate flight code
        loadCache();
        if (flightCache.containsKey(flight.getFlightCode())) {
            return false; // Duplicate flight code
        }
        
        try {
            File file = new File(FLIGHT_DATA_FILE);
            boolean fileExists = file.exists();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                writer.println(String.join(DELIMITER,
                    flight.getAirline(),
                    flight.getOrigin(),
                    flight.getDestination(),
                    flight.getDuration(),
                    flight.getAircraftModel(),
                    flight.getFlightCode(),
                    String.valueOf(flight.getSeatCapacity()),
                    String.valueOf(flight.getBaseFare())
                ));
            }
            
            // Update cache
            flightCache.put(flight.getFlightCode(), flight);
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving flight: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates an existing flight
     * @param flight The flight to update
     * @return true if successful, false otherwise
     */
    public static boolean updateFlight(Flight flight) {
        if (flight == null || !flight.isValid()) {
            return false;
        }
        
        loadCache();
        if (!flightCache.containsKey(flight.getFlightCode())) {
            return false; // Flight doesn't exist
        }
        
        // Reload all flights, update the one we need, and rewrite
        List<Flight> flights = getAllFlights();
        for (int i = 0; i < flights.size(); i++) {
            if (flights.get(i).getFlightCode().equals(flight.getFlightCode())) {
                flights.set(i, flight);
                break;
            }
        }
        
        // Rewrite file
        try (PrintWriter writer = new PrintWriter(new FileWriter(FLIGHT_DATA_FILE))) {
            for (Flight f : flights) {
                writer.println(String.join(DELIMITER,
                    f.getAirline(),
                    f.getOrigin(),
                    f.getDestination(),
                    f.getDuration(),
                    f.getAircraftModel(),
                    f.getFlightCode(),
                    String.valueOf(f.getSeatCapacity()),
                    String.valueOf(f.getBaseFare())
                ));
            }
        } catch (IOException e) {
            System.err.println("Error updating flight: " + e.getMessage());
            return false;
        }
        
        // Update cache
        flightCache.put(flight.getFlightCode(), flight);
        
        return true;
    }
    
    /**
     * Deletes a flight by flight code
     * @param flightCode The flight code to delete
     * @return true if successful, false otherwise
     */
    public static boolean deleteFlight(String flightCode) {
        if (flightCode == null || flightCode.trim().isEmpty()) {
            return false;
        }
        
        loadCache();
        if (!flightCache.containsKey(flightCode)) {
            return false; // Flight doesn't exist
        }
        
        // Reload all flights, remove the one we need, and rewrite
        List<Flight> flights = getAllFlights();
        flights.removeIf(f -> f.getFlightCode().equals(flightCode));
        
        // Rewrite file
        try (PrintWriter writer = new PrintWriter(new FileWriter(FLIGHT_DATA_FILE))) {
            for (Flight f : flights) {
                writer.println(String.join(DELIMITER,
                    f.getAirline(),
                    f.getOrigin(),
                    f.getDestination(),
                    f.getDuration(),
                    f.getAircraftModel(),
                    f.getFlightCode(),
                    String.valueOf(f.getSeatCapacity()),
                    String.valueOf(f.getBaseFare())
                ));
            }
        } catch (IOException e) {
            System.err.println("Error deleting flight: " + e.getMessage());
            return false;
        }
        
        // Update cache
        flightCache.remove(flightCode);
        
        return true;
    }
    
    /**
     * Gets all flights
     * @return List of all flights
     */
    public static List<Flight> getAllFlights() {
        loadCache();
        return new ArrayList<>(flightCache.values());
    }
    
    /**
     * Gets flights by airline
     * @param airline The airline name
     * @return List of flights for the airline
     */
    public static List<Flight> getFlightsByAirline(String airline) {
        loadCache();
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flightCache.values()) {
            if (flight.getAirline().equalsIgnoreCase(airline)) {
                result.add(flight);
            }
        }
        return result;
    }
    
    /**
     * Gets a flight by flight code
     * @param flightCode The flight code
     * @return Flight object if found, null otherwise
     */
    public static Flight getFlightByCode(String flightCode) {
        loadCache();
        return flightCache.get(flightCode);
    }
    
    /**
     * Saves a flight offer to file
     * @param offer The flight offer to save
     * @return true if successful, false otherwise
     */
    public static boolean saveFlightOffer(FlightOffer offer) {
        if (offer == null || !offer.isValid()) {
            return false;
        }
        
        loadCache();
        
        try {
            File file = new File(FLIGHT_OFFER_FILE);
            boolean fileExists = file.exists();
            
            // Check if offer already exists for this flight code
            boolean exists = offerCache.containsKey(offer.getFlightCode());
            
            if (exists) {
                // Update existing offer
                List<FlightOffer> offers = getAllOffers();
                for (int i = 0; i < offers.size(); i++) {
                    if (offers.get(i).getFlightCode().equals(offer.getFlightCode())) {
                        offers.set(i, offer);
                        break;
                    }
                }
                
                // Rewrite file
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    for (FlightOffer o : offers) {
                        writer.println(String.join(DELIMITER,
                            o.getFlightCode(),
                            o.getCabinClass() != null ? o.getCabinClass() : "",
                            o.getSeatType() != null ? o.getSeatType() : "",
                            o.getFoodAndBeverages() != null ? o.getFoodAndBeverages() : "",
                            o.getEntertainment() != null ? o.getEntertainment() : "",
                            o.getAmenity() != null ? o.getAmenity() : "",
                            o.getMoreDetails() != null ? o.getMoreDetails() : ""
                        ));
                    }
                }
            } else {
                // Add new offer
                try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                    writer.println(String.join(DELIMITER,
                        offer.getFlightCode(),
                        offer.getCabinClass() != null ? offer.getCabinClass() : "",
                        offer.getSeatType() != null ? offer.getSeatType() : "",
                        offer.getFoodAndBeverages() != null ? offer.getFoodAndBeverages() : "",
                        offer.getEntertainment() != null ? offer.getEntertainment() : "",
                        offer.getAmenity() != null ? offer.getAmenity() : "",
                        offer.getMoreDetails() != null ? offer.getMoreDetails() : ""
                    ));
                }
            }
            
            // Update cache
            offerCache.put(offer.getFlightCode(), offer);
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving flight offer: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets all flight offers
     * @return List of all flight offers
     */
    public static List<FlightOffer> getAllOffers() {
        loadCache();
        return new ArrayList<>(offerCache.values());
    }
    
    /**
     * Gets a flight offer by flight code
     * @param flightCode The flight code
     * @return FlightOffer object if found, null otherwise
     */
    public static FlightOffer getOfferByFlightCode(String flightCode) {
        loadCache();
        return offerCache.get(flightCode);
    }
    
    /**
     * Clears the cache
     */
    public static void clearCache() {
        flightCache.clear();
        offerCache.clear();
        cacheLoaded = false;
    }
}

