package kingsman.upair.model;

/**
 * Model class representing a Flight Offer (additional details for flights)
 * Follows OOP principles with encapsulation
 */
public class FlightOffer {
    private String flightCode;
    private String cabinClass;
    private String seatType;
    private String foodAndBeverages;
    private String entertainment;
    private String amenity;
    private String moreDetails;
    
    // Default constructor
    public FlightOffer() {
    }
    
    // Parameterized constructor
    public FlightOffer(String flightCode, String cabinClass, String seatType,
                      String foodAndBeverages, String entertainment, String amenity, String moreDetails) {
        this.flightCode = flightCode;
        this.cabinClass = cabinClass;
        this.seatType = seatType;
        this.foodAndBeverages = foodAndBeverages;
        this.entertainment = entertainment;
        this.amenity = amenity;
        this.moreDetails = moreDetails;
    }
    
    // Getters and Setters
    public String getFlightCode() {
        return flightCode;
    }
    
    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }
    
    public String getCabinClass() {
        return cabinClass;
    }
    
    public void setCabinClass(String cabinClass) {
        this.cabinClass = cabinClass;
    }
    
    public String getSeatType() {
        return seatType;
    }
    
    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }
    
    public String getFoodAndBeverages() {
        return foodAndBeverages;
    }
    
    public void setFoodAndBeverages(String foodAndBeverages) {
        this.foodAndBeverages = foodAndBeverages;
    }
    
    public String getEntertainment() {
        return entertainment;
    }
    
    public void setEntertainment(String entertainment) {
        this.entertainment = entertainment;
    }
    
    public String getAmenity() {
        return amenity;
    }
    
    public void setAmenity(String amenity) {
        this.amenity = amenity;
    }
    
    public String getMoreDetails() {
        return moreDetails;
    }
    
    public void setMoreDetails(String moreDetails) {
        this.moreDetails = moreDetails;
    }
    
    /**
     * Validates if flight offer information is valid
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return flightCode != null && !flightCode.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "FlightOffer{" +
                "flightCode='" + flightCode + '\'' +
                ", cabinClass='" + cabinClass + '\'' +
                ", seatType='" + seatType + '\'' +
                ", foodAndBeverages='" + foodAndBeverages + '\'' +
                ", entertainment='" + entertainment + '\'' +
                ", amenity='" + amenity + '\'' +
                ", moreDetails='" + moreDetails + '\'' +
                '}';
    }
}

