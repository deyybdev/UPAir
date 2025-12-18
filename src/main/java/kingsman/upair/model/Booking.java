package kingsman.upair.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Model class representing a Passenger Booking
 * Follows OOP principles with encapsulation
 */
public class Booking {
    private String bookingId;
    private String passengerUsername;
    private String tripType; // "One Way" or "Round Trip"
    private String flightCode;
    private String returnFlightCode; // For round trip
    private String origin;
    private String destination;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private LocalDate returnDate; // For round trip
    private LocalTime returnTime; // For round trip
    private int numberOfAdults;
    private int numberOfMinors;
    private List<String> reservedSeats; // List of seat numbers (e.g., "A1", "B5")
    private List<String> passengerNames; // Names corresponding to seats
    private double totalPrice;
    private String status; // "Pending", "Confirmed", "Cancelled"
    private String voucherCode; // Optional voucher
    private String seatPreference; // "Standard", "Front", "Business"
    private String paymentType; // "Cash", "Credit Card", "Debit Card", "PayPal", etc.
    
    // Default constructor
    public Booking() {
    }
    
    // Parameterized constructor
    public Booking(String bookingId, String passengerUsername, String tripType, 
                   String flightCode, String origin, String destination,
                   LocalDate departureDate, LocalTime departureTime,
                   int numberOfAdults, int numberOfMinors,
                   List<String> reservedSeats, List<String> passengerNames,
                   double totalPrice, String status) {
        this.bookingId = bookingId;
        this.passengerUsername = passengerUsername;
        this.tripType = tripType;
        this.flightCode = flightCode;
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.numberOfAdults = numberOfAdults;
        this.numberOfMinors = numberOfMinors;
        this.reservedSeats = reservedSeats;
        this.passengerNames = passengerNames;
        this.totalPrice = totalPrice;
        this.status = status;
    }
    
    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getPassengerUsername() {
        return passengerUsername;
    }
    
    public void setPassengerUsername(String passengerUsername) {
        this.passengerUsername = passengerUsername;
    }
    
    public String getTripType() {
        return tripType;
    }
    
    public void setTripType(String tripType) {
        this.tripType = tripType;
    }
    
    public String getFlightCode() {
        return flightCode;
    }
    
    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }
    
    public String getReturnFlightCode() {
        return returnFlightCode;
    }
    
    public void setReturnFlightCode(String returnFlightCode) {
        this.returnFlightCode = returnFlightCode;
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
    
    public LocalDate getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    
    public LocalTime getReturnTime() {
        return returnTime;
    }
    
    public void setReturnTime(LocalTime returnTime) {
        this.returnTime = returnTime;
    }
    
    public int getNumberOfAdults() {
        return numberOfAdults;
    }
    
    public void setNumberOfAdults(int numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }
    
    public int getNumberOfMinors() {
        return numberOfMinors;
    }
    
    public void setNumberOfMinors(int numberOfMinors) {
        this.numberOfMinors = numberOfMinors;
    }
    
    public List<String> getReservedSeats() {
        return reservedSeats;
    }
    
    public void setReservedSeats(List<String> reservedSeats) {
        this.reservedSeats = reservedSeats;
    }
    
    public List<String> getPassengerNames() {
        return passengerNames;
    }
    
    public void setPassengerNames(List<String> passengerNames) {
        this.passengerNames = passengerNames;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getVoucherCode() {
        return voucherCode;
    }
    
    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
    
    public String getSeatPreference() {
        return seatPreference;
    }
    
    public void setSeatPreference(String seatPreference) {
        this.seatPreference = seatPreference;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    /**
     * Validates if booking information is valid
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return bookingId != null && !bookingId.trim().isEmpty() &&
               passengerUsername != null && !passengerUsername.trim().isEmpty() &&
               tripType != null && !tripType.trim().isEmpty() &&
               flightCode != null && !flightCode.trim().isEmpty() &&
               origin != null && !origin.trim().isEmpty() &&
               destination != null && !destination.trim().isEmpty() &&
               departureDate != null &&
               departureTime != null &&
               numberOfAdults >= 0 &&
               numberOfMinors >= 0 &&
               (numberOfAdults + numberOfMinors) > 0 &&
               reservedSeats != null && !reservedSeats.isEmpty() &&
               passengerNames != null && !passengerNames.isEmpty() &&
               reservedSeats.size() == passengerNames.size() &&
               totalPrice > 0;
    }
    
    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", passengerUsername='" + passengerUsername + '\'' +
                ", tripType='" + tripType + '\'' +
                ", flightCode='" + flightCode + '\'' +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", departureDate=" + departureDate +
                ", departureTime=" + departureTime +
                ", numberOfAdults=" + numberOfAdults +
                ", numberOfMinors=" + numberOfMinors +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                '}';
    }
}

