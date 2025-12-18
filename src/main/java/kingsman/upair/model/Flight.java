package kingsman.upair.model;

/**
 * Model class representing a Flight
 * Follows OOP principles with encapsulation
 */
public class Flight {
    private String airline;
    private String origin;
    private String destination;
    private String duration;
    private String aircraftModel;
    private String flightCode;
    private int seatCapacity;
    private double baseFare;
    
    // Default constructor
    public Flight() {
    }
    
    // Parameterized constructor
    public Flight(String airline, String origin, String destination, String duration,
                  String aircraftModel, String flightCode, int seatCapacity, double baseFare) {
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.duration = duration;
        this.aircraftModel = aircraftModel;
        this.flightCode = flightCode;
        this.seatCapacity = seatCapacity;
        this.baseFare = baseFare;
    }
    
    // Getters and Setters
    public String getAirline() {
        return airline;
    }
    
    public void setAirline(String airline) {
        this.airline = airline;
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
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getAircraftModel() {
        return aircraftModel;
    }
    
    public void setAircraftModel(String aircraftModel) {
        this.aircraftModel = aircraftModel;
    }
    
    public String getFlightCode() {
        return flightCode;
    }
    
    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }
    
    public int getSeatCapacity() {
        return seatCapacity;
    }
    
    public void setSeatCapacity(int seatCapacity) {
        this.seatCapacity = seatCapacity;
    }
    
    public double getBaseFare() {
        return baseFare;
    }
    
    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }
    
    /**
     * Validates if flight information is valid
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return airline != null && !airline.trim().isEmpty() &&
               origin != null && !origin.trim().isEmpty() &&
               destination != null && !destination.trim().isEmpty() &&
               duration != null && !duration.trim().isEmpty() &&
               aircraftModel != null && !aircraftModel.trim().isEmpty() &&
               flightCode != null && !flightCode.trim().isEmpty() &&
               seatCapacity >= 150 &&
               baseFare > 0 &&
               !origin.equals(destination); // Origin and destination must be different
    }
    
    @Override
    public String toString() {
        return "Flight{" +
                "airline='" + airline + '\'' +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", duration='" + duration + '\'' +
                ", aircraftModel='" + aircraftModel + '\'' +
                ", flightCode='" + flightCode + '\'' +
                ", seatCapacity=" + seatCapacity +
                ", baseFare=" + baseFare +
                '}';
    }
}

