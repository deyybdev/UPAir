package kingsman.upair.service;

import kingsman.upair.model.Flight;
import kingsman.upair.model.FlightOffer;
import kingsman.upair.repository.FlightRepository;

import java.util.List;

/**
 * Service class for handling flight management business logic
 * Follows Service Layer Pattern
 */
public class FlightService {
    
    /**
     * Saves a new flight
     * @param flight The flight to save
     * @return ServiceResult containing success status and message
     */
    public static ServiceResult saveFlight(Flight flight) {
        if (flight == null || !flight.isValid()) {
            return new ServiceResult(false, "Invalid flight information!");
        }
        
        // Check for duplicate flight code
        Flight existing = FlightRepository.getFlightByCode(flight.getFlightCode());
        if (existing != null) {
            return new ServiceResult(false, "Flight code already exists!");
        }
        
        boolean saved = FlightRepository.saveFlight(flight);
        if (saved) {
            return new ServiceResult(true, "Flight added successfully!");
        } else {
            return new ServiceResult(false, "Failed to save flight!");
        }
    }
    
    /**
     * Updates an existing flight
     * @param flight The flight to update
     * @return ServiceResult containing success status and message
     */
    public static ServiceResult updateFlight(Flight flight) {
        if (flight == null || !flight.isValid()) {
            return new ServiceResult(false, "Invalid flight information!");
        }
        
        boolean updated = FlightRepository.updateFlight(flight);
        if (updated) {
            return new ServiceResult(true, "Flight updated successfully!");
        } else {
            return new ServiceResult(false, "Failed to update flight! Flight may not exist.");
        }
    }
    
    /**
     * Deletes a flight
     * @param flightCode The flight code to delete
     * @return ServiceResult containing success status and message
     */
    public static ServiceResult deleteFlight(String flightCode) {
        if (flightCode == null || flightCode.trim().isEmpty()) {
            return new ServiceResult(false, "Flight code is required!");
        }
        
        boolean deleted = FlightRepository.deleteFlight(flightCode);
        if (deleted) {
            return new ServiceResult(true, "Flight deleted successfully!");
        } else {
            return new ServiceResult(false, "Failed to delete flight! Flight may not exist.");
        }
    }
    
    /**
     * Gets all flights for an airline
     * @param airline The airline name
     * @return List of flights
     */
    public static List<Flight> getFlightsByAirline(String airline) {
        return FlightRepository.getFlightsByAirline(airline);
    }
    
    /**
     * Gets a flight by flight code
     * @param flightCode The flight code
     * @return Flight object if found, null otherwise
     */
    public static Flight getFlightByCode(String flightCode) {
        return FlightRepository.getFlightByCode(flightCode);
    }
    
    /**
     * Saves or updates a flight offer
     * @param offer The flight offer to save
     * @return ServiceResult containing success status and message
     */
    public static ServiceResult saveFlightOffer(FlightOffer offer) {
        if (offer == null || !offer.isValid()) {
            return new ServiceResult(false, "Invalid flight offer information!");
        }
        
        boolean saved = FlightRepository.saveFlightOffer(offer);
        if (saved) {
            return new ServiceResult(true, "Flight offer saved successfully!");
        } else {
            return new ServiceResult(false, "Failed to save flight offer!");
        }
    }
    
    /**
     * Inner class to represent service result
     */
    public static class ServiceResult {
        private final boolean success;
        private final String message;
        
        public ServiceResult(boolean success, String message) {
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

