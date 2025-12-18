package kingsman.upair.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Model class representing a Flight Schedule
 * Follows OOP principles with encapsulation
 */
public class Schedule {
    private String scheduleId; // Unique flight ID
    private String airline;
    private String flightCode;
    private String origin;
    private String destination;
    private LocalDate departureDate;
    private LocalTime departureTime;
    
    // Default constructor
    public Schedule() {
    }
    
    // Parameterized constructor
    public Schedule(String scheduleId, String airline, String flightCode, 
                   String origin, String destination, LocalDate departureDate, LocalTime departureTime) {
        this.scheduleId = scheduleId;
        this.airline = airline;
        this.flightCode = flightCode;
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
    }
    
    // Getters and Setters
    public String getScheduleId() {
        return scheduleId;
    }
    
    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }
    
    public String getAirline() {
        return airline;
    }
    
    public void setAirline(String airline) {
        this.airline = airline;
    }
    
    public String getFlightCode() {
        return flightCode;
    }
    
    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }
    
    public String getOrigin() {
        return origin;
    }
    
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public LocalDate getDepartureDate() {
        return departureDate;
    }
    
    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }
    
    public LocalTime getDepartureTime() {
        return departureTime;
    }
    
    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }
    
    /**
     * Validates if schedule information is valid
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return scheduleId != null && !scheduleId.trim().isEmpty() &&
               airline != null && !airline.trim().isEmpty() &&
               flightCode != null && !flightCode.trim().isEmpty() &&
               origin != null && !origin.trim().isEmpty() &&
               destination != null && !destination.trim().isEmpty() &&
               departureDate != null &&
               departureTime != null;
    }
    
    @Override
    public String toString() {
        return "Schedule{" +
                "scheduleId='" + scheduleId + '\'' +
                ", airline='" + airline + '\'' +
                ", flightCode='" + flightCode + '\'' +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", departureDate=" + departureDate +
                ", departureTime=" + departureTime +
                '}';
    }
}

