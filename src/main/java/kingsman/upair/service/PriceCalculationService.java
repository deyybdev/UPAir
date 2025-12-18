package kingsman.upair.service;

import kingsman.upair.model.Flight;
import kingsman.upair.model.FlightOffer;
import java.time.LocalDate;
import java.time.Month;

/**
 * Service class for calculating flight prices
 * Handles base fare, passenger count, season, vouchers, seat preferences, and flight offers
 */
public class PriceCalculationService {
    
    // Season multipliers (Philippine context)
    private static final double PEAK_SEASON_MULTIPLIER = 1.3; // December, January, April (Holy Week)
    private static final double REGULAR_SEASON_MULTIPLIER = 1.0;
    private static final double OFF_SEASON_MULTIPLIER = 0.9; // Low season months
    
    // Seat preference multipliers
    private static final double FRONT_SEAT_MULTIPLIER = 1.15; // 15% premium for front seats
    private static final double BUSINESS_SEAT_MULTIPLIER = 1.5; // 50% premium for business class
    
    // Passenger type multipliers
    private static final double ADULT_MULTIPLIER = 1.0;
    private static final double MINOR_MULTIPLIER = 0.75; // 25% discount for minors
    
    /**
     * Calculates total price for a flight booking
     * @param flight The flight
     * @param numberOfAdults Number of adult passengers
     * @param numberOfMinors Number of minor passengers
     * @param departureDate Departure date
     * @param seatPreference Seat preference ("Standard", "Front", "Business")
     * @param voucherCode Optional voucher code
     * @param flightOffer Optional flight offer details
     * @return Total calculated price
     */
    public static double calculatePrice(Flight flight, int numberOfAdults, int numberOfMinors,
                                       LocalDate departureDate, String seatPreference,
                                       String voucherCode, FlightOffer flightOffer) {
        if (flight == null) {
            return 0.0;
        }
        
        double basePrice = flight.getBaseFare();
        
        // Apply season multiplier
        double seasonMultiplier = getSeasonMultiplier(departureDate);
        basePrice *= seasonMultiplier;
        
        // Apply seat preference multiplier
        double seatMultiplier = getSeatMultiplier(seatPreference);
        basePrice *= seatMultiplier;
        
        // Calculate adult prices
        double adultTotal = basePrice * numberOfAdults * ADULT_MULTIPLIER;
        
        // Calculate minor prices (with discount)
        double minorTotal = basePrice * numberOfMinors * MINOR_MULTIPLIER;
        
        double subtotal = adultTotal + minorTotal;
        
        // Apply flight offer adjustments (if business class or premium)
        if (flightOffer != null) {
            subtotal = applyFlightOfferAdjustments(subtotal, flightOffer);
        }
        
        // Apply voucher discount
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            subtotal = applyVoucherDiscount(subtotal, voucherCode);
        }
        
        return Math.round(subtotal * 100.0) / 100.0; // Round to 2 decimal places
    }
    
    /**
     * Gets season multiplier based on date
     * Philippine context: Peak seasons are December, January, and April (Holy Week)
     */
    private static double getSeasonMultiplier(LocalDate date) {
        Month month = date.getMonth();
        
        // Peak season: December, January
        if (month == Month.DECEMBER || month == Month.JANUARY) {
            return PEAK_SEASON_MULTIPLIER;
        }
        
        // April (Holy Week) - approximate
        if (month == Month.APRIL) {
            return PEAK_SEASON_MULTIPLIER;
        }
        
        // Off season: February, March, May, June, July, August, September, October, November
        // (excluding peak months)
        return REGULAR_SEASON_MULTIPLIER;
    }
    
    /**
     * Gets seat preference multiplier
     */
    private static double getSeatMultiplier(String seatPreference) {
        if (seatPreference == null) {
            return REGULAR_SEASON_MULTIPLIER;
        }
        
        switch (seatPreference.toLowerCase()) {
            case "front":
                return FRONT_SEAT_MULTIPLIER;
            case "business":
                return BUSINESS_SEAT_MULTIPLIER;
            default:
                return REGULAR_SEASON_MULTIPLIER;
        }
    }
    
    /**
     * Applies flight offer adjustments
     * Business class and premium offers may affect pricing
     */
    private static double applyFlightOfferAdjustments(double price, FlightOffer offer) {
        if (offer == null) {
            return price;
        }
        
        String cabinClass = offer.getCabinClass();
        if (cabinClass != null) {
            switch (cabinClass.toLowerCase()) {
                case "business":
                    // Business class already handled by seat preference, but add small premium
                    return price * 1.1;
                case "first class":
                    return price * 1.2;
                default:
                    return price;
            }
        }
        
        return price;
    }
    
    /**
     * Applies voucher discount
     * Simple implementation - can be extended
     */
    private static double applyVoucherDiscount(double price, String voucherCode) {
        // Simple voucher system - 10% discount
        // Can be extended to check voucher database
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            return price * 0.9; // 10% discount
        }
        return price;
    }
}

