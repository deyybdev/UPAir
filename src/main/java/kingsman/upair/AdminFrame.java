package kingsman.upair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Calendar;
import kingsman.upair.model.Flight;
import kingsman.upair.model.FlightOffer;
import kingsman.upair.model.Schedule;
import kingsman.upair.model.Booking;
import kingsman.upair.model.Passenger;
import kingsman.upair.service.FlightService;
import kingsman.upair.service.ScheduleService;
import kingsman.upair.repository.ScheduleRepository;
import kingsman.upair.repository.BookingRepository;
import kingsman.upair.repository.PassengerRepository;
        
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author admin
 */
public class AdminFrame extends javax.swing.JFrame {
    
    private String selectedAirlineName = "";
    private boolean isEditMode = false;
    private String editingFlightCode = "";
    
    // Schedule management variables
    private boolean isScheduleEditMode = false;
    private String editingScheduleId = "";
    private javax.swing.JComboBox<String> flightCodeScheduleCombo; // Dropdown for flight codes

    /**
     * Creates new form AdminFrame
     */
    public AdminFrame() {
        initComponents();
        setLocationRelativeTo(null);
        startDateTime();
        initializeFlightComponents();
    }
    
    /**
     * Initializes flight management components
     */
    private void initializeFlightComponents() {
        // Initialize origin and destination combo boxes
        initializeAirports();
        
        // Initialize aircraft model combo box
        initializeAircraftModels();
        
        // Initialize flight offer combo boxes
        initializeFlightOffers();
        
        // Add listener to origin combo box to filter destination
        origin.addActionListener(e -> updateDestinationComboBox());
        
        // Initialize schedule components
        initializeScheduleComponents();
    }
    
    /**
     * Initializes schedule management components
     */
    private void initializeScheduleComponents() {
        // Initialize airline dropdown for schedule
        initializeScheduleAirlines();
        
        // Create flight code combo box (since flightCodeSchedule is a text field, we'll create a combo)
        // Position it where the text field is
        flightCodeScheduleCombo = new javax.swing.JComboBox<>();
        addFlightSchedulePanel.add(flightCodeScheduleCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 130, 160, 30));
        flightCodeSchedule.setVisible(false); // Hide the text field
        
        // Initialize time spinners
        hourTimeSchedule.setModel(new javax.swing.SpinnerNumberModel(0, 0, 23, 1));
        minuteScheduleLabel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        
        // Add listeners
        airline.addActionListener(e -> updateScheduleFlightCodes());
        flightCodeScheduleCombo.addActionListener(e -> updateScheduleOriginDestination());
    }
    
    /**
     * Initializes airline dropdown for schedule panel
     */
    private void initializeScheduleAirlines() {
        String[] airlines = {
            "AirAsia",
            "Cebu Pacific Go",
            "Philippine Airlines"
        };
        
        airline.removeAllItems();
        for (String airlineName : airlines) {
            airline.addItem(airlineName);
        }
    }
    
    /**
     * Updates flight code dropdown based on selected airline
     */
    private void updateScheduleFlightCodes() {
        String selectedAirline = (String) airline.getSelectedItem();
        if (selectedAirline == null) {
            return;
        }
        
        flightCodeScheduleCombo.removeAllItems();
        List<String> flightCodes = ScheduleService.getFlightCodesByAirline(selectedAirline);
        
        for (String code : flightCodes) {
            flightCodeScheduleCombo.addItem(code);
        }
        
        // Clear origin and destination when airline changes
        originSchedule.removeAllItems();
        destinationSchedule.removeAllItems();
    }
    
    /**
     * Updates origin and destination dropdowns based on selected flight code
     */
    private void updateScheduleOriginDestination() {
        String selectedFlightCode = (String) flightCodeScheduleCombo.getSelectedItem();
        if (selectedFlightCode == null || selectedFlightCode.isEmpty()) {
            return;
        }
        
        kingsman.upair.model.Flight flight = ScheduleService.getFlightByCode(selectedFlightCode);
        if (flight != null) {
            // Update origin
            originSchedule.removeAllItems();
            originSchedule.addItem(flight.getOrigin());
            
            // Update destination
            destinationSchedule.removeAllItems();
            destinationSchedule.addItem(flight.getDestination());
        }
    }
    
    /**
     * Populates scheduled flights table
     */
    private void populateScheduledFlightsTable() {
        DefaultTableModel model = (DefaultTableModel) scheduledFlightsTable.getModel();
        model.setRowCount(0); // Clear existing rows
        
        List<Schedule> schedules = ScheduleService.getAllSchedules();
        for (Schedule schedule : schedules) {
            model.addRow(new Object[]{
                schedule.getScheduleId(),
                schedule.getAirline(),
                schedule.getFlightCode(),
                schedule.getOrigin(),
                schedule.getDestination(),
                schedule.getDepartureDate().toString(),
                schedule.getDepartureTime().toString()
            });
        }
    }
    
    /**
     * Clears schedule form
     */
    private void clearScheduleForm() {
        airline.setSelectedIndex(0);
        flightCodeScheduleCombo.removeAllItems();
        originSchedule.removeAllItems();
        destinationSchedule.removeAllItems();
        jDateChooser2.setDate(null);
        hourTimeSchedule.setValue(0);
        minuteScheduleLabel.setValue(0);
        isScheduleEditMode = false;
        editingScheduleId = "";
    }

    /**
     * Determines if a booking should be shown in Manage Bookings table
     * Cash payments OR online bookings with cancel request
     */
    private boolean shouldIncludeInManageBookings(Booking booking) {
        String paymentType = booking.getPaymentType() != null ? booking.getPaymentType() : "";
        String status = booking.getStatus() != null ? booking.getStatus() : "";
        
        // Cash payments (to be paid at counter)
        if (paymentType.equalsIgnoreCase("Cash")) {
            // Exclude already cancelled bookings
            return !status.equalsIgnoreCase("Cancelled");
        }
        
        // Online bookings with cancel request
        if (status.equalsIgnoreCase("Cancel Book")) {
            return true;
        }
        
        return false;
    }

    /**
     * Populates Manage Bookings table with pending cash payments and cancel requests
     */
    private void populateManageBookingsTable() {
        DefaultTableModel model = (DefaultTableModel) manageBookingsTable.getModel();
        model.setRowCount(0);
        
        List<Booking> bookings = BookingRepository.getAllBookings();
        for (Booking booking : bookings) {
            if (!shouldIncludeInManageBookings(booking)) {
                continue;
            }
            
            Passenger passenger = PassengerRepository.getPassengerByUsername(booking.getPassengerUsername());
            String name;
            if (passenger != null) {
                name = passenger.getFirstName() + " " + passenger.getLastName();
            } else {
                name = booking.getPassengerUsername();
            }
            
            String routeText = booking.getOrigin() + " \u2192 " + booking.getDestination();
            String tripType = booking.getTripType();
            String flightDateText = booking.getDepartureDate() != null ? booking.getDepartureDate().toString() : "";
            int totalPassengers = booking.getNumberOfAdults() + booking.getNumberOfMinors();
            
            String displayStatus;
            if (booking.getPaymentType() != null && booking.getPaymentType().equalsIgnoreCase("Cash")) {
                displayStatus = "Pay at the counter";
            } else if (booking.getStatus() != null && booking.getStatus().equalsIgnoreCase("Cancel Book")) {
                displayStatus = "Cancel book";
            } else {
                displayStatus = booking.getStatus() != null ? booking.getStatus() : "";
            }
            
            model.addRow(new Object[]{
                booking.getBookingId(),
                name,
                routeText,
                tripType,
                flightDateText,
                displayStatus,
                totalPassengers
            });
        }
    }

    /**
     * Populates Approved Bookings History table
     */
    private void populateApprovedBookingsTable() {
        DefaultTableModel model = (DefaultTableModel) approvedBookingsTable.getModel();
        model.setRowCount(0);
        
        List<Booking> bookings = BookingRepository.getAllBookings();
        for (Booking booking : bookings) {
            String status = booking.getStatus() != null ? booking.getStatus() : "";
            
            // Approved bookings: Confirmed or Cancelled
            if (!status.equalsIgnoreCase("Confirmed") && !status.equalsIgnoreCase("Cancelled")) {
                continue;
            }
            
            Passenger passenger = PassengerRepository.getPassengerByUsername(booking.getPassengerUsername());
            String name;
            if (passenger != null) {
                name = passenger.getFirstName() + " " + passenger.getLastName();
            } else {
                name = booking.getPassengerUsername();
            }
            
            String routeText = booking.getOrigin() + " \u2192 " + booking.getDestination();
            String tripType = booking.getTripType();
            String flightDateText = booking.getDepartureDate() != null ? booking.getDepartureDate().toString() : "";
            int totalPassengers = booking.getNumberOfAdults() + booking.getNumberOfMinors();
            String payment = booking.getPaymentType() != null ? booking.getPaymentType() : "";
            
            model.addRow(new Object[]{
                booking.getBookingId(),
                name,
                routeText,
                tripType,
                flightDateText,
                payment,
                totalPassengers
            });
        }
    }
    
    /**
     * Validates schedule form
     */
    private boolean validateScheduleForm() {
        if (airline.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select an airline!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (flightCodeScheduleCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a flight code!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (originSchedule.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Origin is required!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (destinationSchedule.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Destination is required!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (jDateChooser2.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Please select a departure date!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Initializes airport locations
     */
    private void initializeAirports() {
        String[] airports = {
            "Daraga(DRP)",
            "Manila(MNL)",
            "Cebu(CEB)"
        };
        
        origin.removeAllItems();
        destination.removeAllItems();
        
        for (String airport : airports) {
            origin.addItem(airport);
            destination.addItem(airport);
        }
    }
    
    /**
     * Updates destination combo box to exclude selected origin
     */
    private void updateDestinationComboBox() {
        String selectedOrigin = (String) origin.getSelectedItem();
        if (selectedOrigin == null) return;
        
        destination.removeAllItems();
        String[] airports = {"Daraga(DRP)", "Manila(MNL)", "Cebu(CEB)"};
        
        for (String airport : airports) {
            if (!airport.equals(selectedOrigin)) {
                destination.addItem(airport);
            }
        }
    }
    
    /**
     * Initializes aircraft model combo box
     */
    private void initializeAircraftModels() {
        String[] models = {
            "Airbus A320",
            "Airbus A321",
            "ATR 72-600"
        };
        
        aircraftModel.removeAllItems();
        for (String model : models) {
            aircraftModel.addItem(model);
        }
    }
    
    /**
     * Initializes flight offer combo boxes
     */
    private void initializeFlightOffers() {
        // Cabin Class options
        String[] cabinClasses = {
            "Economy",
            "Premium Economy",
            "Business",
            "First Class"
        };
        cabinClass.removeAllItems();
        for (String cc : cabinClasses) {
            cabinClass.addItem(cc);
        }
        
        // Seat Type options
        String[] seatTypes = {
            "Standard",
            "Extra Legroom",
            "Window",
            "Aisle",
            "Middle",
            "Reclining",
            "Flat Bed"
        };
        seatType.removeAllItems();
        for (String st : seatTypes) {
            seatType.addItem(st);
        }
        
        // Food and Beverages options
        String[] foodOptions = {
            "None",
            "Snacks Only",
            "Light Meal",
            "Full Meal",
            "Premium Meal",
            "Special Dietary (Vegetarian)",
            "Special Dietary (Halal)",
            "Special Dietary (Kosher)"
        };
        foodAndBeverages.removeAllItems();
        for (String food : foodOptions) {
            foodAndBeverages.addItem(food);
        }
        
        // Entertainment options
        String[] entertainmentOptions = {
            "None",
            "In-Flight Magazine",
            "Audio Entertainment",
            "Video Entertainment",
            "Wi-Fi Available",
            "Power Outlets",
            "USB Charging"
        };
        entertainment.removeAllItems();
        for (String ent : entertainmentOptions) {
            entertainment.addItem(ent);
        }
        
        // Amenity options
        String[] amenities = {
            "None",
            "Blanket & Pillow",
            "Headphones",
            "Eye Mask",
            "Toiletries Kit",
            "Priority Boarding",
            "Lounge Access",
            "Baggage Allowance"
        };
        amenity.removeAllItems();
        for (String am : amenities) {
            amenity.addItem(am);
        }
    }
    
    /**
     * Populates flight table with flights for selected airline
     */
    private void populateFlightTable() {
        DefaultTableModel model = (DefaultTableModel) flightTable.getModel();
        model.setRowCount(0); // Clear existing rows
        
        List<Flight> flights = FlightService.getFlightsByAirline(selectedAirlineName);
        for (Flight flight : flights) {
            model.addRow(new Object[]{
                flight.getAircraftModel(),
                flight.getFlightCode(),
                flight.getSeatCapacity(),
                String.format("PHP %.2f", flight.getBaseFare())
            });
        }
    }
    
    /**
     * Clears flight form
     */
    private void clearFlightForm() {
        origin.setSelectedIndex(0);
        destination.setSelectedIndex(0);
        duration.setText("");
        aircraftModel.setSelectedIndex(0);
        flightCode.setText("");
        seatCapacity.setText("");
        baseFare.setText("");
        isEditMode = false;
        editingFlightCode = "";
    }
    
    /**
     * Validates flight form
     */
    private boolean validateFlightForm() {
        if (origin.getSelectedItem() == null || destination.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select origin and destination!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (duration.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Duration is required!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            duration.requestFocus();
            return false;
        }
        
        if (aircraftModel.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select aircraft model!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (flightCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Flight code is required!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            flightCode.requestFocus();
            return false;
        }
        
        try {
            int capacity = Integer.parseInt(seatCapacity.getText().trim());
            if (capacity < 150) {
                JOptionPane.showMessageDialog(this, "Seat capacity must be at least 150!", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                seatCapacity.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Seat capacity must be a valid number!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            seatCapacity.requestFocus();
            return false;
        }
        
        try {
            double fare = Double.parseDouble(baseFare.getText().trim());
            if (fare <= 0) {
                JOptionPane.showMessageDialog(this, "Base fare must be greater than 0!", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                baseFare.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Base fare must be a valid number!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            baseFare.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton3 = new javax.swing.JButton();
        topPanel = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        logOutButton = new javax.swing.JButton();
        userProfileButton = new javax.swing.JButton();
        username = new javax.swing.JLabel();
        sidePanel = new javax.swing.JPanel();
        dashboardButton = new javax.swing.JButton();
        manageFlightsButton = new javax.swing.JButton();
        manageSchedulesButton = new javax.swing.JButton();
        manageBookingsButton = new javax.swing.JButton();
        manageSeatsButton = new javax.swing.JButton();
        tabPanel = new javax.swing.JTabbedPane();
        dashboardPanel = new javax.swing.JPanel();
        welcomeLabel = new javax.swing.JLabel();
        datetimePanel = new javax.swing.JPanel();
        datetimeLabel = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        activeFlightsPanel = new javax.swing.JPanel();
        activeFlightsLabel = new javax.swing.JLabel();
        activeFlights = new javax.swing.JLabel();
        passengersTodayPanel = new javax.swing.JPanel();
        passengersTodayLabel = new javax.swing.JLabel();
        passengersToday = new javax.swing.JLabel();
        totalRevenuePanel = new javax.swing.JPanel();
        totalRevenueLabel = new javax.swing.JLabel();
        totalRevenue = new javax.swing.JLabel();
        recentFlightsPanel = new javax.swing.JPanel();
        recentFlightsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        manageFlightsPanel = new javax.swing.JPanel();
        airlineSelectorPanel = new javax.swing.JPanel();
        airlineSelectorLabel = new javax.swing.JLabel();
        airAsiaButton = new javax.swing.JButton();
        cebGoButton = new javax.swing.JButton();
        palButton = new javax.swing.JButton();
        flightMaker = new javax.swing.JPanel();
        manageFlightsHeaderPanel = new javax.swing.JPanel();
        manageFlightsHeaderLabel = new javax.swing.JLabel();
        selectedAirlinePanel = new javax.swing.JPanel();
        selectedAirline = new javax.swing.JLabel();
        selectedAirlineLabel = new javax.swing.JLabel();
        addFlightDetailsPanel = new javax.swing.JPanel();
        originLabel = new javax.swing.JLabel();
        destinationLabel = new javax.swing.JLabel();
        durationLabel = new javax.swing.JLabel();
        aircraftModelLabel = new javax.swing.JLabel();
        flightCodeLabel = new javax.swing.JLabel();
        seatCapacityLabel = new javax.swing.JLabel();
        baseFareLabel = new javax.swing.JLabel();
        origin = new javax.swing.JComboBox<>();
        destination = new javax.swing.JComboBox<>();
        duration = new javax.swing.JTextField();
        aircraftModel = new javax.swing.JComboBox<>();
        flightCode = new javax.swing.JTextField();
        seatCapacity = new javax.swing.JTextField();
        baseFare = new javax.swing.JTextField();
        tableScrollPane = new javax.swing.JScrollPane();
        flightTable = new javax.swing.JTable();
        addFlightButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        addFlightOfferPanel = new javax.swing.JPanel();
        cabinClassLabel = new javax.swing.JLabel();
        moreDetailsLabel = new javax.swing.JLabel();
        seatTypeLabel = new javax.swing.JLabel();
        foodAndBeveragesLabel = new javax.swing.JLabel();
        entertainmentLabel = new javax.swing.JLabel();
        amenityLabel = new javax.swing.JLabel();
        cabinClass = new javax.swing.JComboBox<>();
        entertainment = new javax.swing.JComboBox<>();
        seatType = new javax.swing.JComboBox<>();
        amenity = new javax.swing.JComboBox<>();
        foodAndBeverages = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        moreDetails = new javax.swing.JTextArea();
        addOfferButton = new javax.swing.JButton();
        manageSchedulePanel = new javax.swing.JPanel();
        manageScheduleHeaderPanel = new javax.swing.JPanel();
        manageScheduleHeaderLabel = new javax.swing.JLabel();
        scheduledFlightsPanel = new javax.swing.JPanel();
        scheduledFlightsScrollPane = new javax.swing.JScrollPane();
        scheduledFlightsTable = new javax.swing.JTable();
        deleteSelectedButton = new javax.swing.JButton();
        editSelectedButton = new javax.swing.JButton();
        addFlightSchedulePanel = new javax.swing.JPanel();
        timeScheduleLabel = new javax.swing.JLabel();
        airlineLabel = new javax.swing.JLabel();
        flightCodeScheduleLabel = new javax.swing.JLabel();
        originScheduleLabel = new javax.swing.JLabel();
        destinationScheduleLabel = new javax.swing.JLabel();
        airline = new javax.swing.JComboBox<>();
        originSchedule = new javax.swing.JComboBox<>();
        detailsLabel = new javax.swing.JLabel();
        departureLabel = new javax.swing.JLabel();
        dateScheduleLabel = new javax.swing.JLabel();
        destinationSchedule = new javax.swing.JComboBox<>();
        hourTimeSchedule = new javax.swing.JSpinner();
        minuteScheduleLabel = new javax.swing.JSpinner();
        addScheduleButton = new javax.swing.JButton();
        flightCodeSchedule = new javax.swing.JTextField();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        manageBookingsPanel = new javax.swing.JPanel();
        bookingsMainPanel = new javax.swing.JPanel();
        manageBookingsHeaderPanel = new javax.swing.JPanel();
        manageBookingsHeaderLabel = new javax.swing.JLabel();
        bookingsPanel = new javax.swing.JPanel();
        viewSelectedButton = new javax.swing.JButton();
        manageBookingsScrollPane = new javax.swing.JScrollPane();
        manageBookingsTable = new javax.swing.JTable();
        approveHistoryButton = new javax.swing.JButton();
        approveSelectedButton = new javax.swing.JButton();
        inputBookingID = new javax.swing.JTextField();
        searchBookingID = new javax.swing.JButton();
        approvedBookingsHistory = new javax.swing.JPanel();
        approveBookingsHistoryLabel = new javax.swing.JLabel();
        approvedBookingsScrollPane = new javax.swing.JScrollPane();
        approvedBookingsTable = new javax.swing.JTable();
        backToBookingsButton = new javax.swing.JButton();
        personalBookingDetails = new javax.swing.JPanel();
        profilePhotoPanel = new javax.swing.JPanel();
        profilePhotoLabel = new javax.swing.JLabel();
        userDetailsPanel = new javax.swing.JPanel();
        flightDateLabel = new javax.swing.JLabel();
        userRequest = new javax.swing.JLabel();
        routeLabel = new javax.swing.JLabel();
        categoryLabel = new javax.swing.JLabel();
        otherInfoLabel = new javax.swing.JLabel();
        paymentLabel = new javax.swing.JLabel();
        noOfPassengerLabel = new javax.swing.JLabel();
        passengersBookedLabel = new javax.swing.JLabel();
        seatsLabel = new javax.swing.JLabel();
        userAddressLabel = new javax.swing.JLabel();
        userAddress = new javax.swing.JLabel();
        route = new javax.swing.JLabel();
        category = new javax.swing.JLabel();
        flightDate = new javax.swing.JLabel();
        paymentMode = new javax.swing.JLabel();
        noOfPassenger = new javax.swing.JLabel();
        passengersName = new javax.swing.JLabel();
        seats = new javax.swing.JLabel();
        bookingID = new javax.swing.JLabel();
        userName = new javax.swing.JLabel();
        backToButton = new javax.swing.JButton();
        manageSeatsPanel = new javax.swing.JPanel();
        manageSeatsHeaderPanel = new javax.swing.JPanel();
        manageSeatsHeaderLabel = new javax.swing.JLabel();
        manageSeats = new javax.swing.JPanel();
        manageSeatsScrollPane = new javax.swing.JScrollPane();
        seatsTable = new javax.swing.JTable();
        viewSelectedButtonSeats = new javax.swing.JButton();
        dateLabelSeats = new javax.swing.JLabel();
        scheduleID = new javax.swing.JTextField();
        searchScheduleLabel = new javax.swing.JLabel();
        originLabelSeats = new javax.swing.JLabel();
        destinationLabelSeats = new javax.swing.JLabel();
        destinationComboBox = new javax.swing.JComboBox<>();
        originComboBox = new javax.swing.JComboBox<>();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        seatsScrollPane = new javax.swing.JScrollPane();
        seatsPanell = new javax.swing.JPanel();
        abSectionSeats = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        B1 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        B4 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        B6 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        B11 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        A17 = new javax.swing.JButton();
        jButton39 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        A19 = new javax.swing.JButton();
        B19 = new javax.swing.JButton();
        jButton44 = new javax.swing.JButton();
        jButton45 = new javax.swing.JButton();
        abSectionSeats1 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        B2 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();
        jButton42 = new javax.swing.JButton();
        B5 = new javax.swing.JButton();
        jButton43 = new javax.swing.JButton();
        jButton46 = new javax.swing.JButton();
        jButton47 = new javax.swing.JButton();
        B7 = new javax.swing.JButton();
        jButton48 = new javax.swing.JButton();
        jButton49 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        jButton51 = new javax.swing.JButton();
        jButton52 = new javax.swing.JButton();
        jButton53 = new javax.swing.JButton();
        jButton54 = new javax.swing.JButton();
        jButton55 = new javax.swing.JButton();
        jButton56 = new javax.swing.JButton();
        B12 = new javax.swing.JButton();
        jButton57 = new javax.swing.JButton();
        jButton58 = new javax.swing.JButton();
        jButton59 = new javax.swing.JButton();
        jButton60 = new javax.swing.JButton();
        jButton61 = new javax.swing.JButton();
        jButton62 = new javax.swing.JButton();
        jButton63 = new javax.swing.JButton();
        jButton64 = new javax.swing.JButton();
        jButton65 = new javax.swing.JButton();
        jButton66 = new javax.swing.JButton();
        A18 = new javax.swing.JButton();
        jButton67 = new javax.swing.JButton();
        jButton68 = new javax.swing.JButton();
        jButton69 = new javax.swing.JButton();
        A20 = new javax.swing.JButton();
        B20 = new javax.swing.JButton();
        jButton70 = new javax.swing.JButton();
        jButton71 = new javax.swing.JButton();

        jButton3.setText("jButton3");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topPanel.setBackground(new java.awt.Color(5, 20, 42));
        topPanel.setForeground(new java.awt.Color(255, 255, 255));
        topPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        headerLabel.setFont(new java.awt.Font("Arial Black", 1, 20)); // NOI18N
        headerLabel.setForeground(new java.awt.Color(255, 255, 255));
        headerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        topPanel.add(headerLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 250, 60));

        logOutButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        logOutButton.setForeground(new java.awt.Color(255, 255, 255));
        logOutButton.setBorderPainted(false);
        logOutButton.setContentAreaFilled(false);
        logOutButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logOutButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        logOutButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        logOutButton.setIconTextGap(0);
        topPanel.add(logOutButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 0, 50, 60));

        userProfileButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        userProfileButton.setForeground(new java.awt.Color(255, 255, 255));
        userProfileButton.setBorderPainted(false);
        userProfileButton.setContentAreaFilled(false);
        userProfileButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        userProfileButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        userProfileButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        userProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userProfileButtonActionPerformed(evt);
            }
        });
        topPanel.add(userProfileButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 0, -1, 60));

        username.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        username.setForeground(new java.awt.Color(255, 255, 255));
        username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        username.setText("ADMINISTRATOR");
        topPanel.add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 0, 160, 60));

        getContentPane().add(topPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1200, 60));

        sidePanel.setBackground(new java.awt.Color(11, 56, 118));
        sidePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        dashboardButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 15)); // NOI18N
        dashboardButton.setForeground(new java.awt.Color(255, 255, 255));
        dashboardButton.setText("DASHBOARD");
        dashboardButton.setBorderPainted(false);
        dashboardButton.setContentAreaFilled(false);
        dashboardButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        dashboardButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        dashboardButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        dashboardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dashboardButtonActionPerformed(evt);
            }
        });
        sidePanel.add(dashboardButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 250, 40));

        manageFlightsButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 15)); // NOI18N
        manageFlightsButton.setForeground(new java.awt.Color(255, 255, 255));
        manageFlightsButton.setText("MANAGE FLIGHTS");
        manageFlightsButton.setBorderPainted(false);
        manageFlightsButton.setContentAreaFilled(false);
        manageFlightsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        manageFlightsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        manageFlightsButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        manageFlightsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageFlightsButtonActionPerformed(evt);
            }
        });
        sidePanel.add(manageFlightsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 250, 40));

        manageSchedulesButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        manageSchedulesButton.setForeground(new java.awt.Color(255, 255, 255));
        manageSchedulesButton.setText("MANAGE SCHEDULES");
        manageSchedulesButton.setBorderPainted(false);
        manageSchedulesButton.setContentAreaFilled(false);
        manageSchedulesButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        manageSchedulesButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        manageSchedulesButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        manageSchedulesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageSchedulesButtonActionPerformed(evt);
            }
        });
        sidePanel.add(manageSchedulesButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, -1, 40));

        manageBookingsButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 15)); // NOI18N
        manageBookingsButton.setForeground(new java.awt.Color(255, 255, 255));
        manageBookingsButton.setText("MANAGE BOOKINGS");
        manageBookingsButton.setBorderPainted(false);
        manageBookingsButton.setContentAreaFilled(false);
        manageBookingsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        manageBookingsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        manageBookingsButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        manageBookingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageBookingsButtonActionPerformed(evt);
            }
        });
        sidePanel.add(manageBookingsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 150, 250, 40));

        manageSeatsButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 15)); // NOI18N
        manageSeatsButton.setForeground(new java.awt.Color(255, 255, 255));
        manageSeatsButton.setText("MANAGE SEATS");
        manageSeatsButton.setBorderPainted(false);
        manageSeatsButton.setContentAreaFilled(false);
        manageSeatsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        manageSeatsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        manageSeatsButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        manageSeatsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageSeatsButtonActionPerformed(evt);
            }
        });
        sidePanel.add(manageSeatsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 190, 250, 40));

        getContentPane().add(sidePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 250, 740));

        dashboardPanel.setBackground(new java.awt.Color(204, 204, 204));
        dashboardPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        welcomeLabel.setFont(new java.awt.Font("Arial", 1, 30)); // NOI18N
        welcomeLabel.setForeground(new java.awt.Color(60, 63, 65));
        welcomeLabel.setText("Hello Administrator!");
        dashboardPanel.add(welcomeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 300, 40));

        datetimePanel.setBackground(new java.awt.Color(5, 20, 42));
        datetimePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        datetimePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        datetimeLabel.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        datetimeLabel.setForeground(new java.awt.Color(255, 255, 255));
        datetimeLabel.setText("SYSTEM DATE AND TIME");
        datetimePanel.add(datetimeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 190, -1));

        timeLabel.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        timeLabel.setForeground(new java.awt.Color(255, 255, 255));
        timeLabel.setText("--:--:-- --");
        datetimePanel.add(timeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 200, 30));

        dateLabel.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        dateLabel.setForeground(new java.awt.Color(255, 255, 255));
        dateLabel.setText("------------");
        datetimePanel.add(dateLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 200, 30));

        dashboardPanel.add(datetimePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 240, 100));

        activeFlightsPanel.setBackground(new java.awt.Color(11, 56, 118));
        activeFlightsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        activeFlightsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        activeFlightsLabel.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        activeFlightsLabel.setForeground(new java.awt.Color(255, 255, 255));
        activeFlightsLabel.setText("ACTIVE FLIGHTS");
        activeFlightsPanel.add(activeFlightsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 190, -1));

        activeFlights.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 36)); // NOI18N
        activeFlights.setForeground(new java.awt.Color(255, 255, 255));
        activeFlights.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        activeFlights.setText("0");
        activeFlightsPanel.add(activeFlights, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 200, 40));

        dashboardPanel.add(activeFlightsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 70, 220, 100));

        passengersTodayPanel.setBackground(new java.awt.Color(11, 56, 118));
        passengersTodayPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        passengersTodayPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        passengersTodayLabel.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        passengersTodayLabel.setForeground(new java.awt.Color(255, 255, 255));
        passengersTodayLabel.setText("PASSENGERS TODAY");
        passengersTodayPanel.add(passengersTodayLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 190, -1));

        passengersToday.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 36)); // NOI18N
        passengersToday.setForeground(new java.awt.Color(255, 255, 255));
        passengersToday.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        passengersToday.setText("0");
        passengersTodayPanel.add(passengersToday, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 200, 40));

        dashboardPanel.add(passengersTodayPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 70, 220, 100));

        totalRevenuePanel.setBackground(new java.awt.Color(11, 56, 118));
        totalRevenuePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        totalRevenuePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        totalRevenueLabel.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        totalRevenueLabel.setForeground(new java.awt.Color(255, 255, 255));
        totalRevenueLabel.setText("TOTAL REVENUE");
        totalRevenuePanel.add(totalRevenueLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 190, -1));

        totalRevenue.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 24)); // NOI18N
        totalRevenue.setForeground(new java.awt.Color(255, 255, 255));
        totalRevenue.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        totalRevenue.setText("PHP 0.00");
        totalRevenuePanel.add(totalRevenue, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 200, 40));

        dashboardPanel.add(totalRevenuePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 70, 220, 100));

        recentFlightsPanel.setBackground(new java.awt.Color(255, 255, 255));
        recentFlightsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(11, 57, 118), 2));
        recentFlightsPanel.setForeground(new java.awt.Color(255, 255, 255));
        recentFlightsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        recentFlightsLabel.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        recentFlightsLabel.setForeground(new java.awt.Color(60, 63, 65));
        recentFlightsLabel.setText("RECENT FLIGHTS");
        recentFlightsPanel.add(recentFlightsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 2, -1, 50));

        jTable1.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        jTable1.setForeground(new java.awt.Color(51, 51, 51));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Flight Number", "Aircraft Model", "Departure  Time", "Destination", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        recentFlightsPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 910, 510));

        dashboardPanel.add(recentFlightsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 930, 570));

        tabPanel.addTab("tab1", dashboardPanel);

        manageFlightsPanel.setBackground(new java.awt.Color(204, 204, 204));
        manageFlightsPanel.setLayout(new java.awt.CardLayout());

        airlineSelectorPanel.setBackground(new java.awt.Color(204, 204, 204));
        airlineSelectorPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        airlineSelectorLabel.setFont(new java.awt.Font("Arial", 1, 30)); // NOI18N
        airlineSelectorLabel.setForeground(new java.awt.Color(60, 63, 65));
        airlineSelectorLabel.setText("Select Airline");
        airlineSelectorPanel.add(airlineSelectorLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 40, 200, 40));

        airAsiaButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 15)); // NOI18N
        airAsiaButton.setForeground(new java.awt.Color(5, 20, 42));
        airAsiaButton.setText("AIR ASIA");
        airAsiaButton.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(5, 20, 42), 2, true));
        airAsiaButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        airAsiaButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        airAsiaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                airAsiaButtonActionPerformed(evt);
            }
        });
        airlineSelectorPanel.add(airAsiaButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 110, 270, 160));

        cebGoButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 15)); // NOI18N
        cebGoButton.setForeground(new java.awt.Color(5, 20, 42));
        cebGoButton.setText("CEBU PACIFIC GO");
        cebGoButton.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(5, 20, 42), 2, true));
        cebGoButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cebGoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cebGoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cebGoButtonActionPerformed(evt);
            }
        });
        airlineSelectorPanel.add(cebGoButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 110, 270, 160));

        palButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 15)); // NOI18N
        palButton.setForeground(new java.awt.Color(5, 20, 42));
        palButton.setText("PHILIPPINE AIRLINES");
        palButton.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(5, 20, 42), 2, true));
        palButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        palButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        palButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                palButtonActionPerformed(evt);
            }
        });
        airlineSelectorPanel.add(palButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 110, 270, 160));

        manageFlightsPanel.add(airlineSelectorPanel, "card3");

        flightMaker.setBackground(new java.awt.Color(255, 255, 255));
        flightMaker.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        manageFlightsHeaderPanel.setBackground(new java.awt.Color(204, 204, 204));
        manageFlightsHeaderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        manageFlightsHeaderPanel.setForeground(new java.awt.Color(255, 255, 255));
        manageFlightsHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        manageFlightsHeaderLabel.setFont(new java.awt.Font("Bahnschrift", 1, 55)); // NOI18N
        manageFlightsHeaderLabel.setForeground(new java.awt.Color(60, 63, 65));
        manageFlightsHeaderLabel.setText("MANAGE FLIGHTS");
        manageFlightsHeaderPanel.add(manageFlightsHeaderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 510, 60));

        flightMaker.add(manageFlightsHeaderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 910, 70));

        selectedAirlinePanel.setBackground(new java.awt.Color(255, 255, 255));
        selectedAirlinePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        selectedAirlinePanel.setForeground(new java.awt.Color(255, 255, 255));
        selectedAirlinePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        selectedAirline.setFont(new java.awt.Font("Arial Black", 1, 24)); // NOI18N
        selectedAirline.setForeground(new java.awt.Color(60, 63, 65));
        selectedAirline.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        selectedAirline.setText("null");
        selectedAirlinePanel.add(selectedAirline, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 10, 240, 30));

        selectedAirlineLabel.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        selectedAirlineLabel.setForeground(new java.awt.Color(60, 63, 65));
        selectedAirlineLabel.setText("Selected Airline:");
        selectedAirlinePanel.add(selectedAirlineLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 240, 30));

        flightMaker.add(selectedAirlinePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 910, 50));

        addFlightDetailsPanel.setBackground(new java.awt.Color(204, 204, 204));
        addFlightDetailsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Add Flight Details", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        addFlightDetailsPanel.setForeground(new java.awt.Color(255, 255, 255));
        addFlightDetailsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        originLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        originLabel.setText("Origin");
        addFlightDetailsPanel.add(originLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        destinationLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        destinationLabel.setText("Destination");
        addFlightDetailsPanel.add(destinationLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 30, -1, -1));

        durationLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        durationLabel.setText("Duration");
        addFlightDetailsPanel.add(durationLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 30, -1, -1));

        aircraftModelLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        aircraftModelLabel.setText("Aircraft Model");
        addFlightDetailsPanel.add(aircraftModelLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));

        flightCodeLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        flightCodeLabel.setText("Flight Code");
        addFlightDetailsPanel.add(flightCodeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 100, -1, -1));

        seatCapacityLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        seatCapacityLabel.setText("Seat Capacity");
        addFlightDetailsPanel.add(seatCapacityLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 100, -1, -1));

        baseFareLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        baseFareLabel.setText("Base Fare");
        addFlightDetailsPanel.add(baseFareLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 100, -1, -1));

        origin.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightDetailsPanel.add(origin, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 190, 40));

        destination.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightDetailsPanel.add(destination, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 50, 190, 40));
        addFlightDetailsPanel.add(duration, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 50, 150, 40));

        aircraftModel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightDetailsPanel.add(aircraftModel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 150, 40));
        addFlightDetailsPanel.add(flightCode, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 120, 150, 40));
        addFlightDetailsPanel.add(seatCapacity, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 120, 110, 40));
        addFlightDetailsPanel.add(baseFare, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 120, 110, 40));

        flightTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Aircraft Model", "Flight Code", "Seat Capacity", "Base Fare (in PHP)"
            }
        ));
        tableScrollPane.setViewportView(flightTable);

        addFlightDetailsPanel.add(tableScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, 550, 350));

        addFlightButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        addFlightButton.setForeground(new java.awt.Color(5, 20, 42));
        addFlightButton.setText("Add Flight");
        addFlightButton.setBorder(null);
        addFlightButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addFlightButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addFlightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFlightButtonActionPerformed(evt);
            }
        });
        addFlightDetailsPanel.add(addFlightButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 540, 110, 30));

        deleteButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        deleteButton.setForeground(new java.awt.Color(5, 20, 42));
        deleteButton.setText("Delete Selected");
        deleteButton.setBorder(null);
        deleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        addFlightDetailsPanel.add(deleteButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 540, 110, 30));

        editButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        editButton.setForeground(new java.awt.Color(5, 20, 42));
        editButton.setText("Edit Selected");
        editButton.setBorder(null);
        editButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        editButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        addFlightDetailsPanel.add(editButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 540, 110, 30));

        backButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        backButton.setForeground(new java.awt.Color(5, 20, 42));
        backButton.setText("Back");
        backButton.setBorder(null);
        backButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        addFlightDetailsPanel.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 540, 90, 30));

        flightMaker.add(addFlightDetailsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 580, 580));

        addFlightOfferPanel.setBackground(new java.awt.Color(204, 204, 204));
        addFlightOfferPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Add Flight Offer Details", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        addFlightOfferPanel.setForeground(new java.awt.Color(255, 255, 255));
        addFlightOfferPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cabinClassLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        cabinClassLabel.setText("Cabin Class");
        addFlightOfferPanel.add(cabinClassLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, -1, -1));

        moreDetailsLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        moreDetailsLabel.setText("Add More Details");
        addFlightOfferPanel.add(moreDetailsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, -1, -1));

        seatTypeLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        seatTypeLabel.setText("Seat Type");
        addFlightOfferPanel.add(seatTypeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 50, -1, -1));

        foodAndBeveragesLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        foodAndBeveragesLabel.setText("Food and Beverages");
        addFlightOfferPanel.add(foodAndBeveragesLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, -1, -1));

        entertainmentLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        entertainmentLabel.setText("Entertainment");
        addFlightOfferPanel.add(entertainmentLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 130, -1, -1));

        amenityLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        amenityLabel.setText("Amenity");
        addFlightOfferPanel.add(amenityLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 210, -1, -1));

        cabinClass.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightOfferPanel.add(cabinClass, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 70, 140, 40));

        entertainment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightOfferPanel.add(entertainment, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 150, 140, 40));

        seatType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightOfferPanel.add(seatType, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 140, 40));

        amenity.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightOfferPanel.add(amenity, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 230, 140, 40));

        foodAndBeverages.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightOfferPanel.add(foodAndBeverages, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 140, 40));

        moreDetails.setColumns(20);
        moreDetails.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        moreDetails.setRows(5);
        moreDetails.setText("Enter text");
        jScrollPane3.setViewportView(moreDetails);

        addFlightOfferPanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 320, 300, 210));

        addOfferButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        addOfferButton.setForeground(new java.awt.Color(5, 20, 42));
        addOfferButton.setText("Add Offer");
        addOfferButton.setBorder(null);
        addOfferButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addOfferButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addOfferButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addOfferButtonActionPerformed(evt);
            }
        });
        addFlightOfferPanel.add(addOfferButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 540, 110, 30));

        flightMaker.add(addFlightOfferPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 160, 320, 580));

        manageFlightsPanel.add(flightMaker, "card2");

        tabPanel.addTab("tab2", manageFlightsPanel);

        manageSchedulePanel.setBackground(new java.awt.Color(255, 255, 255));
        manageSchedulePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        manageScheduleHeaderPanel.setBackground(new java.awt.Color(204, 204, 204));
        manageScheduleHeaderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        manageScheduleHeaderPanel.setForeground(new java.awt.Color(255, 255, 255));
        manageScheduleHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        manageScheduleHeaderLabel.setFont(new java.awt.Font("Bahnschrift", 1, 55)); // NOI18N
        manageScheduleHeaderLabel.setForeground(new java.awt.Color(60, 63, 65));
        manageScheduleHeaderLabel.setText("MANAGE SCHEDULES");
        manageScheduleHeaderPanel.add(manageScheduleHeaderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 580, 60));

        manageSchedulePanel.add(manageScheduleHeaderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 910, 70));

        scheduledFlightsPanel.setBackground(new java.awt.Color(204, 204, 204));
        scheduledFlightsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Scheduled Flights", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        scheduledFlightsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        scheduledFlightsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Airline", "Flight Code", "Origin", "Destination", "Date", "Departure Time"
            }
        ));
        scheduledFlightsScrollPane.setViewportView(scheduledFlightsTable);

        scheduledFlightsPanel.add(scheduledFlightsScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 580, 510));

        deleteSelectedButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        deleteSelectedButton.setForeground(new java.awt.Color(5, 20, 42));
        deleteSelectedButton.setText("Delete Selected");
        deleteSelectedButton.setBorder(null);
        deleteSelectedButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteSelectedButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedButtonActionPerformed(evt);
            }
        });
        scheduledFlightsPanel.add(deleteSelectedButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 550, 130, 30));

        editSelectedButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        editSelectedButton.setForeground(new java.awt.Color(5, 20, 42));
        editSelectedButton.setText("Edit Selected");
        editSelectedButton.setBorder(null);
        editSelectedButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        editSelectedButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSelectedButtonActionPerformed(evt);
            }
        });
        scheduledFlightsPanel.add(editSelectedButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 550, 130, 30));

        manageSchedulePanel.add(scheduledFlightsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 600, 590));

        addFlightSchedulePanel.setBackground(new java.awt.Color(204, 204, 204));
        addFlightSchedulePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Add Flight Schedule", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        addFlightSchedulePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        timeScheduleLabel.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        timeScheduleLabel.setText("Time");
        addFlightSchedulePanel.add(timeScheduleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, -1, -1));

        airlineLabel.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        airlineLabel.setText("Airline:");
        addFlightSchedulePanel.add(airlineLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        flightCodeScheduleLabel.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        flightCodeScheduleLabel.setText("Flight Code:");
        addFlightSchedulePanel.add(flightCodeScheduleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        originScheduleLabel.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        originScheduleLabel.setText("Origin:");
        addFlightSchedulePanel.add(originScheduleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        destinationScheduleLabel.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        destinationScheduleLabel.setText("Destination:");
        addFlightSchedulePanel.add(destinationScheduleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));

        airline.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightSchedulePanel.add(airline, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 90, 160, 30));

        originSchedule.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightSchedulePanel.add(originSchedule, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 170, 160, 30));

        detailsLabel.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        detailsLabel.setText("Details");
        addFlightSchedulePanel.add(detailsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 50, -1, -1));

        departureLabel.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        departureLabel.setText("Departure");
        addFlightSchedulePanel.add(departureLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 260, -1, -1));

        dateScheduleLabel.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        dateScheduleLabel.setText("Date ");
        addFlightSchedulePanel.add(dateScheduleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, -1, -1));

        destinationSchedule.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addFlightSchedulePanel.add(destinationSchedule, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 210, 160, 30));

        hourTimeSchedule.setToolTipText("");
        addFlightSchedulePanel.add(hourTimeSchedule, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 340, 70, -1));
        addFlightSchedulePanel.add(minuteScheduleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 340, 70, -1));

        addScheduleButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        addScheduleButton.setForeground(new java.awt.Color(5, 20, 42));
        addScheduleButton.setText("Add Schedule");
        addScheduleButton.setBorder(null);
        addScheduleButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addScheduleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addScheduleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addScheduleButtonActionPerformed(evt);
            }
        });
        addFlightSchedulePanel.add(addScheduleButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 400, 140, 30));
        addFlightSchedulePanel.add(flightCodeSchedule, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 130, 160, 30));
        addFlightSchedulePanel.add(jDateChooser2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 300, 160, 30));

        manageSchedulePanel.add(addFlightSchedulePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 110, 300, 450));

        tabPanel.addTab("tab3", manageSchedulePanel);

        manageBookingsPanel.setBackground(new java.awt.Color(255, 255, 255));
        manageBookingsPanel.setLayout(new java.awt.CardLayout());

        bookingsMainPanel.setBackground(new java.awt.Color(153, 153, 153));
        bookingsMainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        manageBookingsHeaderPanel.setBackground(new java.awt.Color(204, 204, 204));
        manageBookingsHeaderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        manageBookingsHeaderPanel.setForeground(new java.awt.Color(255, 255, 255));
        manageBookingsHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        manageBookingsHeaderLabel.setFont(new java.awt.Font("Bahnschrift", 1, 55)); // NOI18N
        manageBookingsHeaderLabel.setForeground(new java.awt.Color(60, 63, 65));
        manageBookingsHeaderLabel.setText("MANAGE BOOKINGS");
        manageBookingsHeaderPanel.add(manageBookingsHeaderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 580, 60));

        bookingsMainPanel.add(manageBookingsHeaderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 910, 70));

        bookingsPanel.setBackground(new java.awt.Color(204, 204, 204));
        bookingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Bookings", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        bookingsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        viewSelectedButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        viewSelectedButton.setForeground(new java.awt.Color(5, 20, 42));
        viewSelectedButton.setText("View Selected");
        viewSelectedButton.setBorder(null);
        viewSelectedButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewSelectedButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        viewSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewSelectedButtonActionPerformed(evt);
            }
        });
        bookingsPanel.add(viewSelectedButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 610, 140, 30));

        manageBookingsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Booking ID", "Name", "Route", "Trip Type", "Flight Date", "Status", "No. of Passenger"
            }
        ));
        manageBookingsScrollPane.setViewportView(manageBookingsTable);

        bookingsPanel.add(manageBookingsScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 890, 550));

        approveHistoryButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        approveHistoryButton.setForeground(new java.awt.Color(5, 20, 42));
        approveHistoryButton.setText("Approved History");
        approveHistoryButton.setBorder(null);
        approveHistoryButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        approveHistoryButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        approveHistoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                approveHistoryButtonActionPerformed(evt);
            }
        });
        bookingsPanel.add(approveHistoryButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 610, 140, 30));

        approveSelectedButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        approveSelectedButton.setForeground(new java.awt.Color(5, 20, 42));
        approveSelectedButton.setText("Approve Selected");
        approveSelectedButton.setBorder(null);
        approveSelectedButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        approveSelectedButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        approveSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                approveSelectedButtonActionPerformed(evt);
            }
        });
        bookingsPanel.add(approveSelectedButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 610, 140, 30));

        inputBookingID.setText("Enter Booking ID");
        bookingsPanel.add(inputBookingID, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 210, 40));

        searchBookingID.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        searchBookingID.setForeground(new java.awt.Color(5, 20, 42));
        searchBookingID.setText("Search");
        searchBookingID.setBorder(null);
        searchBookingID.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        searchBookingID.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        searchBookingID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBookingIDActionPerformed(evt);
            }
        });
        bookingsPanel.add(searchBookingID, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 10, 170, 40));

        bookingsMainPanel.add(bookingsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 910, 650));

        manageBookingsPanel.add(bookingsMainPanel, "card2");

        approvedBookingsHistory.setBackground(new java.awt.Color(204, 204, 204));
        approvedBookingsHistory.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        approveBookingsHistoryLabel.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        approveBookingsHistoryLabel.setForeground(new java.awt.Color(60, 63, 65));
        approveBookingsHistoryLabel.setText("Approved Bookings History");
        approvedBookingsHistory.add(approveBookingsHistoryLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 20, -1, 40));

        approvedBookingsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Booking ID", "Name", "Route", "Category", "Flight Date", "Payment", "No. of Passenger"
            }
        ));
        approvedBookingsScrollPane.setViewportView(approvedBookingsTable);

        approvedBookingsHistory.add(approvedBookingsScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 890, 620));

        backToBookingsButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        backToBookingsButton.setForeground(new java.awt.Color(5, 20, 42));
        backToBookingsButton.setText("Back To Bookings");
        backToBookingsButton.setBorder(null);
        backToBookingsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backToBookingsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        backToBookingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backToBookingsButtonActionPerformed(evt);
            }
        });
        approvedBookingsHistory.add(backToBookingsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 690, 390, 40));

        manageBookingsPanel.add(approvedBookingsHistory, "card3");

        personalBookingDetails.setBackground(new java.awt.Color(204, 204, 204));
        personalBookingDetails.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        profilePhotoPanel.setBackground(new java.awt.Color(153, 153, 153));
        profilePhotoPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        profilePhotoPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        profilePhotoPanel.add(profilePhotoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 110, 120));

        personalBookingDetails.add(profilePhotoPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 120, 120));

        userDetailsPanel.setBackground(new java.awt.Color(204, 204, 204));
        userDetailsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Personal and Booking Details", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        userDetailsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        flightDateLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        flightDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        flightDateLabel.setText("FLIGHT DATE:");
        userDetailsPanel.add(flightDateLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 200, -1, -1));

        userRequest.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        userRequest.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        userRequest.setText("No special requests");
        userDetailsPanel.add(userRequest, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 400, -1, -1));

        routeLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        routeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        routeLabel.setText("ROUTE:");
        userDetailsPanel.add(routeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 120, -1, -1));

        categoryLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        categoryLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        categoryLabel.setText("TRIP TYPE");
        userDetailsPanel.add(categoryLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 160, -1, -1));

        otherInfoLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        otherInfoLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        otherInfoLabel.setText("OTHER INFO:");
        userDetailsPanel.add(otherInfoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 400, -1, 20));

        paymentLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        paymentLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        paymentLabel.setText("PAYMENT:");
        userDetailsPanel.add(paymentLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 240, 90, 20));

        noOfPassengerLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        noOfPassengerLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        noOfPassengerLabel.setText("NO. OF PASSENGER:");
        userDetailsPanel.add(noOfPassengerLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 280, -1, 20));

        passengersBookedLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        passengersBookedLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        passengersBookedLabel.setText("PASSENGERS BOOKED:");
        userDetailsPanel.add(passengersBookedLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 320, -1, 20));

        seatsLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        seatsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        seatsLabel.setText("SEATS:");
        userDetailsPanel.add(seatsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 360, -1, 20));

        userAddressLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        userAddressLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        userAddressLabel.setText("ADDRESS:");
        userDetailsPanel.add(userAddressLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 80, -1, -1));

        userAddress.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        userAddress.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        userAddress.setText("Address, City, Country");
        userDetailsPanel.add(userAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 80, -1, -1));

        route.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        route.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        route.setText("MNL - CRK");
        userDetailsPanel.add(route, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 120, -1, -1));

        category.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        category.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        category.setText("One Way");
        userDetailsPanel.add(category, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 160, -1, -1));

        flightDate.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        flightDate.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        flightDate.setText("2025-12-25");
        userDetailsPanel.add(flightDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 200, -1, -1));

        paymentMode.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        paymentMode.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        paymentMode.setText("Online");
        userDetailsPanel.add(paymentMode, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 240, -1, -1));

        noOfPassenger.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        noOfPassenger.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        noOfPassenger.setText("3");
        userDetailsPanel.add(noOfPassenger, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 280, -1, -1));

        passengersName.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        passengersName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        passengersName.setText("Passenger1, Passenger2");
        userDetailsPanel.add(passengersName, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 320, -1, -1));

        seats.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        seats.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        seats.setText("A1, A2, A3");
        userDetailsPanel.add(seats, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 360, -1, -1));

        personalBookingDetails.add(userDetailsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 910, 520));

        bookingID.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        bookingID.setForeground(new java.awt.Color(60, 63, 65));
        bookingID.setText("Booking ID: BK003");
        personalBookingDetails.add(bookingID, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 90, 240, 30));

        userName.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        userName.setForeground(new java.awt.Color(60, 63, 65));
        userName.setText("John Doe");
        personalBookingDetails.add(userName, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 50, 240, 30));

        backToButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        backToButton.setForeground(new java.awt.Color(5, 20, 42));
        backToButton.setText("Back To Bookings");
        backToButton.setBorder(null);
        backToButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backToButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        backToButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backToButtonActionPerformed(evt);
            }
        });
        personalBookingDetails.add(backToButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 690, 390, 40));

        manageBookingsPanel.add(personalBookingDetails, "card4");

        tabPanel.addTab("tab4", manageBookingsPanel);

        manageSeatsPanel.setBackground(new java.awt.Color(255, 255, 255));
        manageSeatsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        manageSeatsHeaderPanel.setBackground(new java.awt.Color(204, 204, 204));
        manageSeatsHeaderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        manageSeatsHeaderPanel.setForeground(new java.awt.Color(255, 255, 255));
        manageSeatsHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        manageSeatsHeaderLabel.setFont(new java.awt.Font("Bahnschrift", 1, 55)); // NOI18N
        manageSeatsHeaderLabel.setForeground(new java.awt.Color(60, 63, 65));
        manageSeatsHeaderLabel.setText("MANAGE SEATS");
        manageSeatsHeaderPanel.add(manageSeatsHeaderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 580, 60));

        manageSeatsPanel.add(manageSeatsHeaderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 910, 70));

        manageSeats.setBackground(new java.awt.Color(204, 204, 204));
        manageSeats.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Manage Seats", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        manageSeats.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        seatsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Airline", "Flight Code", "Origin", "Destination", "Date", "Departure Time"
            }
        ));
        manageSeatsScrollPane.setViewportView(seatsTable);

        manageSeats.add(manageSeatsScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 550, 410));

        viewSelectedButtonSeats.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        viewSelectedButtonSeats.setForeground(new java.awt.Color(5, 20, 42));
        viewSelectedButtonSeats.setText("View Selected");
        viewSelectedButtonSeats.setBorder(null);
        viewSelectedButtonSeats.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewSelectedButtonSeats.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        viewSelectedButtonSeats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewSelectedButtonSeatsActionPerformed(evt);
            }
        });
        manageSeats.add(viewSelectedButtonSeats, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 580, 130, 30));

        dateLabelSeats.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        dateLabelSeats.setText("Date");
        manageSeats.add(dateLabelSeats, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 90, -1, 20));
        manageSeats.add(scheduleID, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 50, 200, 30));

        searchScheduleLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        searchScheduleLabel.setText("Search Schedule ID:");
        manageSeats.add(searchScheduleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 50, -1, 30));

        originLabelSeats.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        originLabelSeats.setText("Origin");
        manageSeats.add(originLabelSeats, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, 20));

        destinationLabelSeats.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        destinationLabelSeats.setText("Destination");
        manageSeats.add(destinationLabelSeats, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 90, -1, 20));

        destinationComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        manageSeats.add(destinationComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 110, 170, 30));

        originComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        manageSeats.add(originComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 170, 30));
        manageSeats.add(jDateChooser1, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 110, 160, 30));

        manageSeatsPanel.add(manageSeats, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 570, 620));

        seatsPanell.setBackground(new java.awt.Color(204, 204, 204));
        seatsPanell.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        abSectionSeats.setBackground(new java.awt.Color(255, 255, 255));
        abSectionSeats.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        abSectionSeats.setLayout(new java.awt.GridLayout(20, 2, 3, 3));

        jButton6.setBackground(new java.awt.Color(51, 255, 51));
        jButton6.setText("C1");
        jButton6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton6);

        B1.setBackground(new java.awt.Color(51, 255, 51));
        B1.setText("D1");
        B1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(B1);

        jButton8.setBackground(new java.awt.Color(51, 255, 51));
        jButton8.setText("C2");
        jButton8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton8);

        jButton9.setBackground(new java.awt.Color(51, 255, 51));
        jButton9.setText("D2");
        jButton9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        abSectionSeats.add(jButton9);

        jButton10.setBackground(new java.awt.Color(51, 255, 51));
        jButton10.setText("C3");
        jButton10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton10);

        jButton11.setBackground(new java.awt.Color(51, 255, 51));
        jButton11.setText("D3");
        jButton11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton11);

        jButton12.setBackground(new java.awt.Color(51, 255, 51));
        jButton12.setText("C4");
        jButton12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton12);

        B4.setBackground(new java.awt.Color(51, 255, 51));
        B4.setText("D4");
        B4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(B4);

        jButton14.setBackground(new java.awt.Color(51, 255, 51));
        jButton14.setText("C5");
        jButton14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton14);

        jButton15.setBackground(new java.awt.Color(51, 255, 51));
        jButton15.setText("D5");
        jButton15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton15);

        jButton16.setBackground(new java.awt.Color(51, 255, 51));
        jButton16.setText("C6");
        jButton16.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton16);

        B6.setBackground(new java.awt.Color(51, 255, 51));
        B6.setText("D6");
        B6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(B6);

        jButton18.setBackground(new java.awt.Color(51, 255, 51));
        jButton18.setText("C7");
        jButton18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton18);

        jButton19.setBackground(new java.awt.Color(51, 255, 51));
        jButton19.setText("D7");
        jButton19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton19);

        jButton20.setBackground(new java.awt.Color(51, 255, 51));
        jButton20.setText("C8");
        jButton20.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton20);

        jButton21.setBackground(new java.awt.Color(51, 255, 51));
        jButton21.setText("D8");
        jButton21.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton21);

        jButton22.setBackground(new java.awt.Color(51, 255, 51));
        jButton22.setText("C9");
        jButton22.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton22);

        jButton23.setBackground(new java.awt.Color(51, 255, 51));
        jButton23.setText("D9");
        jButton23.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton23);

        jButton24.setBackground(new java.awt.Color(51, 255, 51));
        jButton24.setText("C10");
        jButton24.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton24);

        jButton25.setBackground(new java.awt.Color(51, 255, 51));
        jButton25.setText("D10");
        jButton25.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton25);

        jButton26.setBackground(new java.awt.Color(51, 255, 51));
        jButton26.setText("C11");
        jButton26.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton26);

        B11.setBackground(new java.awt.Color(51, 255, 51));
        B11.setText("D11");
        B11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(B11);

        jButton28.setBackground(new java.awt.Color(51, 255, 51));
        jButton28.setText("C12");
        jButton28.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton28);

        jButton29.setBackground(new java.awt.Color(51, 255, 51));
        jButton29.setText("D12");
        jButton29.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton29);

        jButton30.setBackground(new java.awt.Color(51, 255, 51));
        jButton30.setText("C13");
        jButton30.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton30);

        jButton31.setBackground(new java.awt.Color(51, 255, 51));
        jButton31.setText("D13");
        jButton31.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton31);

        jButton32.setBackground(new java.awt.Color(51, 255, 51));
        jButton32.setText("C14");
        jButton32.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton32);

        jButton33.setBackground(new java.awt.Color(51, 255, 51));
        jButton33.setText("D14");
        jButton33.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton33);

        jButton34.setBackground(new java.awt.Color(51, 255, 51));
        jButton34.setText("C15");
        jButton34.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton34);

        jButton35.setBackground(new java.awt.Color(51, 255, 51));
        jButton35.setText("D15");
        jButton35.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton35);

        jButton36.setBackground(new java.awt.Color(51, 255, 51));
        jButton36.setText("C16");
        jButton36.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton36);

        jButton37.setBackground(new java.awt.Color(51, 255, 51));
        jButton37.setText("D16");
        jButton37.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton37);

        A17.setBackground(new java.awt.Color(51, 255, 51));
        A17.setText("C17");
        A17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(A17);

        jButton39.setBackground(new java.awt.Color(51, 255, 51));
        jButton39.setText("D17");
        jButton39.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton39);

        jButton40.setBackground(new java.awt.Color(51, 255, 51));
        jButton40.setText("C18");
        jButton40.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton40);

        jButton41.setBackground(new java.awt.Color(51, 255, 51));
        jButton41.setText("D18");
        jButton41.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton41);

        A19.setBackground(new java.awt.Color(51, 255, 51));
        A19.setText("C19");
        A19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(A19);

        B19.setBackground(new java.awt.Color(51, 255, 51));
        B19.setText("D19");
        B19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(B19);

        jButton44.setBackground(new java.awt.Color(51, 255, 51));
        jButton44.setText("C20");
        jButton44.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton44);

        jButton45.setBackground(new java.awt.Color(51, 255, 51));
        jButton45.setText("D20");
        jButton45.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats.add(jButton45);

        seatsPanell.add(abSectionSeats, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 10, 140, 610));

        abSectionSeats1.setBackground(new java.awt.Color(255, 255, 255));
        abSectionSeats1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        abSectionSeats1.setLayout(new java.awt.GridLayout(20, 2, 3, 3));

        jButton7.setBackground(new java.awt.Color(51, 255, 51));
        jButton7.setText("A1");
        jButton7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton7);

        B2.setBackground(new java.awt.Color(51, 255, 51));
        B2.setText("B1");
        B2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(B2);

        jButton13.setBackground(new java.awt.Color(51, 255, 51));
        jButton13.setText("A2");
        jButton13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton13);

        jButton17.setBackground(new java.awt.Color(51, 255, 51));
        jButton17.setText("B2");
        jButton17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });
        abSectionSeats1.add(jButton17);

        jButton27.setBackground(new java.awt.Color(51, 255, 51));
        jButton27.setText("A3");
        jButton27.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton27);

        jButton38.setBackground(new java.awt.Color(51, 255, 51));
        jButton38.setText("B3");
        jButton38.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton38);

        jButton42.setBackground(new java.awt.Color(51, 255, 51));
        jButton42.setText("A4");
        jButton42.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton42);

        B5.setBackground(new java.awt.Color(51, 255, 51));
        B5.setText("B4");
        B5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(B5);

        jButton43.setBackground(new java.awt.Color(51, 255, 51));
        jButton43.setText("A5");
        jButton43.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton43);

        jButton46.setBackground(new java.awt.Color(51, 255, 51));
        jButton46.setText("B5");
        jButton46.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton46);

        jButton47.setBackground(new java.awt.Color(51, 255, 51));
        jButton47.setText("A6");
        jButton47.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton47);

        B7.setBackground(new java.awt.Color(51, 255, 51));
        B7.setText("B6");
        B7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(B7);

        jButton48.setBackground(new java.awt.Color(51, 255, 51));
        jButton48.setText("A7");
        jButton48.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton48);

        jButton49.setBackground(new java.awt.Color(51, 255, 51));
        jButton49.setText("B7");
        jButton49.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton49);

        jButton50.setBackground(new java.awt.Color(51, 255, 51));
        jButton50.setText("A8");
        jButton50.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton50);

        jButton51.setBackground(new java.awt.Color(51, 255, 51));
        jButton51.setText("B8");
        jButton51.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton51);

        jButton52.setBackground(new java.awt.Color(51, 255, 51));
        jButton52.setText("A9");
        jButton52.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton52);

        jButton53.setBackground(new java.awt.Color(51, 255, 51));
        jButton53.setText("B9");
        jButton53.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton53);

        jButton54.setBackground(new java.awt.Color(51, 255, 51));
        jButton54.setText("A10");
        jButton54.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton54);

        jButton55.setBackground(new java.awt.Color(51, 255, 51));
        jButton55.setText("B10");
        jButton55.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton55);

        jButton56.setBackground(new java.awt.Color(51, 255, 51));
        jButton56.setText("A11");
        jButton56.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton56);

        B12.setBackground(new java.awt.Color(51, 255, 51));
        B12.setText("B11");
        B12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(B12);

        jButton57.setBackground(new java.awt.Color(51, 255, 51));
        jButton57.setText("A12");
        jButton57.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton57);

        jButton58.setBackground(new java.awt.Color(51, 255, 51));
        jButton58.setText("B12");
        jButton58.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton58);

        jButton59.setBackground(new java.awt.Color(51, 255, 51));
        jButton59.setText("A13");
        jButton59.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton59);

        jButton60.setBackground(new java.awt.Color(51, 255, 51));
        jButton60.setText("B13");
        jButton60.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton60);

        jButton61.setBackground(new java.awt.Color(51, 255, 51));
        jButton61.setText("A14");
        jButton61.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton61);

        jButton62.setBackground(new java.awt.Color(51, 255, 51));
        jButton62.setText("B14");
        jButton62.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton62);

        jButton63.setBackground(new java.awt.Color(51, 255, 51));
        jButton63.setText("A15");
        jButton63.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton63);

        jButton64.setBackground(new java.awt.Color(51, 255, 51));
        jButton64.setText("B15");
        jButton64.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton64);

        jButton65.setBackground(new java.awt.Color(51, 255, 51));
        jButton65.setText("A16");
        jButton65.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton65);

        jButton66.setBackground(new java.awt.Color(51, 255, 51));
        jButton66.setText("B16");
        jButton66.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton66);

        A18.setBackground(new java.awt.Color(51, 255, 51));
        A18.setText("A17");
        A18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(A18);

        jButton67.setBackground(new java.awt.Color(51, 255, 51));
        jButton67.setText("B17");
        jButton67.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton67);

        jButton68.setBackground(new java.awt.Color(51, 255, 51));
        jButton68.setText("A18");
        jButton68.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton68);

        jButton69.setBackground(new java.awt.Color(51, 255, 51));
        jButton69.setText("B18");
        jButton69.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton69);

        A20.setBackground(new java.awt.Color(51, 255, 51));
        A20.setText("A19");
        A20.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(A20);

        B20.setBackground(new java.awt.Color(51, 255, 51));
        B20.setText("B19");
        B20.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(B20);

        jButton70.setBackground(new java.awt.Color(51, 255, 51));
        jButton70.setText("A20");
        jButton70.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton70);

        jButton71.setBackground(new java.awt.Color(51, 255, 51));
        jButton71.setText("B20");
        jButton71.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        abSectionSeats1.add(jButton71);

        seatsPanell.add(abSectionSeats1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 140, 610));

        seatsScrollPane.setViewportView(seatsPanell);

        manageSeatsPanel.add(seatsScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 110, 330, 620));

        tabPanel.addTab("tab5", manageSeatsPanel);

        getContentPane().add(tabPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 21, 950, 780));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dashboardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardButtonActionPerformed
        tabPanel.setSelectedIndex(0);
    }//GEN-LAST:event_dashboardButtonActionPerformed

    private void manageFlightsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageFlightsButtonActionPerformed
        tabPanel.setSelectedIndex(1);
    }//GEN-LAST:event_manageFlightsButtonActionPerformed

    private void manageSchedulesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageSchedulesButtonActionPerformed
        tabPanel.setSelectedIndex(2);
        populateScheduledFlightsTable();
        clearScheduleForm();
    }//GEN-LAST:event_manageSchedulesButtonActionPerformed

    private void manageSeatsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageSeatsButtonActionPerformed
        tabPanel.setSelectedIndex(4);
    }//GEN-LAST:event_manageSeatsButtonActionPerformed

    private void manageBookingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageBookingsButtonActionPerformed
        tabPanel.setSelectedIndex(3);
        populateManageBookingsTable();
    }//GEN-LAST:event_manageBookingsButtonActionPerformed

    private void airAsiaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_airAsiaButtonActionPerformed
        selectedAirlineName = "AirAsia";
        selectedAirline.setText("AirAsia");
        ((java.awt.CardLayout) manageFlightsPanel.getLayout()).show(manageFlightsPanel, "card2");
        clearFlightForm();
        populateFlightTable();
    }//GEN-LAST:event_airAsiaButtonActionPerformed

    private void cebGoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cebGoButtonActionPerformed
        selectedAirlineName = "Cebu Pacific Go";
        selectedAirline.setText("Cebu Pacific Go");
        ((java.awt.CardLayout) manageFlightsPanel.getLayout()).show(manageFlightsPanel, "card2");
        clearFlightForm();
        populateFlightTable();
    }//GEN-LAST:event_cebGoButtonActionPerformed

    private void palButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_palButtonActionPerformed
        selectedAirlineName = "Philippine Airlines";
        selectedAirline.setText("Philippine Airlines");
        ((java.awt.CardLayout) manageFlightsPanel.getLayout()).show(manageFlightsPanel, "card2");
        clearFlightForm();
        populateFlightTable();
    }//GEN-LAST:event_palButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        ((java.awt.CardLayout) manageFlightsPanel.getLayout()).show(manageFlightsPanel, "card3");
        clearFlightForm();
        selectedAirlineName = "";
    }//GEN-LAST:event_backButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a flight to delete!", 
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String flightCodeToDelete = (String) flightTable.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete flight " + flightCodeToDelete + "?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            FlightService.ServiceResult result = FlightService.deleteFlight(flightCodeToDelete);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                populateFlightTable();
                clearFlightForm();
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a flight to edit!", 
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String flightCodeToEdit = (String) flightTable.getValueAt(selectedRow, 1);
        Flight flight = FlightService.getFlightByCode(flightCodeToEdit);
        
        if (flight != null) {
            isEditMode = true;
            editingFlightCode = flightCodeToEdit;
            
            // Populate form with flight data
            origin.setSelectedItem(flight.getOrigin());
            updateDestinationComboBox();
            destination.setSelectedItem(flight.getDestination());
            duration.setText(flight.getDuration());
            aircraftModel.setSelectedItem(flight.getAircraftModel());
            flightCode.setText(flight.getFlightCode());
            seatCapacity.setText(String.valueOf(flight.getSeatCapacity()));
            baseFare.setText(String.valueOf(flight.getBaseFare()));
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void addFlightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFlightButtonActionPerformed
        if (!validateFlightForm()) {
            return;
        }
        
        if (selectedAirlineName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an airline first!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Flight flight = new Flight();
            flight.setAirline(selectedAirlineName);
            flight.setOrigin((String) origin.getSelectedItem());
            flight.setDestination((String) destination.getSelectedItem());
            flight.setDuration(duration.getText().trim());
            flight.setAircraftModel((String) aircraftModel.getSelectedItem());
            flight.setFlightCode(flightCode.getText().trim());
            flight.setSeatCapacity(Integer.parseInt(seatCapacity.getText().trim()));
            flight.setBaseFare(Double.parseDouble(baseFare.getText().trim()));
            
            FlightService.ServiceResult result;
            if (isEditMode) {
                result = FlightService.updateFlight(flight);
            } else {
                result = FlightService.saveFlight(flight);
            }
            
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                populateFlightTable();
                clearFlightForm();
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format!", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_addFlightButtonActionPerformed

    private void addOfferButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addOfferButtonActionPerformed
        if (flightCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a flight code first!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String flightCodeValue = flightCode.getText().trim();
        
        // Check if flight exists
        Flight flight = FlightService.getFlightByCode(flightCodeValue);
        if (flight == null) {
            JOptionPane.showMessageDialog(this, "Flight code does not exist! Please add the flight first.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        FlightOffer offer = new FlightOffer();
        offer.setFlightCode(flightCodeValue);
        offer.setCabinClass((String) cabinClass.getSelectedItem());
        offer.setSeatType((String) seatType.getSelectedItem());
        offer.setFoodAndBeverages((String) foodAndBeverages.getSelectedItem());
        offer.setEntertainment((String) entertainment.getSelectedItem());
        offer.setAmenity((String) amenity.getSelectedItem());
        offer.setMoreDetails(moreDetails.getText().trim());
        
        FlightService.ServiceResult result = FlightService.saveFlightOffer(offer);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            // Clear offer form
            cabinClass.setSelectedIndex(0);
            seatType.setSelectedIndex(0);
            foodAndBeverages.setSelectedIndex(0);
            entertainment.setSelectedIndex(0);
            amenity.setSelectedIndex(0);
            moreDetails.setText("");
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_addOfferButtonActionPerformed

    private void editSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSelectedButtonActionPerformed
        int selectedRow = scheduledFlightsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to edit!", 
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String scheduleId = (String) scheduledFlightsTable.getValueAt(selectedRow, 0);
        Schedule schedule = ScheduleService.getScheduleById(scheduleId);
        
        if (schedule != null) {
            isScheduleEditMode = true;
            editingScheduleId = scheduleId;
            
            // Populate form with schedule data
            airline.setSelectedItem(schedule.getAirline());
            updateScheduleFlightCodes();
            flightCodeScheduleCombo.setSelectedItem(schedule.getFlightCode());
            updateScheduleOriginDestination();
            originSchedule.setSelectedItem(schedule.getOrigin());
            destinationSchedule.setSelectedItem(schedule.getDestination());
            
            // Set date
            LocalDate localDate = schedule.getDepartureDate();
            Calendar cal = Calendar.getInstance();
            cal.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
            jDateChooser2.setDate(cal.getTime());
            
            // Set time
            LocalTime localTime = schedule.getDepartureTime();
            hourTimeSchedule.setValue(localTime.getHour());
            minuteScheduleLabel.setValue(localTime.getMinute());
        }
    }//GEN-LAST:event_editSelectedButtonActionPerformed

    private void addScheduleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addScheduleButtonActionPerformed
        if (!validateScheduleForm()) {
            return;
        }
        
        try {
            Schedule schedule = new Schedule();
            
            // Generate ID if not in edit mode
            if (!isScheduleEditMode) {
                schedule.setScheduleId(ScheduleRepository.generateScheduleId());
            } else {
                schedule.setScheduleId(editingScheduleId);
            }
            
            schedule.setAirline((String) airline.getSelectedItem());
            schedule.setFlightCode((String) flightCodeScheduleCombo.getSelectedItem());
            schedule.setOrigin((String) originSchedule.getSelectedItem());
            schedule.setDestination((String) destinationSchedule.getSelectedItem());
            
            // Convert date
            Date date = jDateChooser2.getDate();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            LocalDate localDate = LocalDate.of(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            );
            schedule.setDepartureDate(localDate);
            
            // Convert time
            int hour = (Integer) hourTimeSchedule.getValue();
            int minute = (Integer) minuteScheduleLabel.getValue();
            LocalTime localTime = LocalTime.of(hour, minute);
            schedule.setDepartureTime(localTime);
            
            ScheduleService.ServiceResult result;
            if (isScheduleEditMode) {
                result = ScheduleService.updateSchedule(schedule);
            } else {
                result = ScheduleService.saveSchedule(schedule);
            }
            
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                populateScheduledFlightsTable();
                clearScheduleForm();
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_addScheduleButtonActionPerformed

    private void deleteSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedButtonActionPerformed
        int selectedRow = scheduledFlightsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to delete!", 
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String scheduleId = (String) scheduledFlightsTable.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete schedule " + scheduleId + "?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            ScheduleService.ServiceResult result = ScheduleService.deleteSchedule(scheduleId);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                populateScheduledFlightsTable();
                clearScheduleForm();
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_deleteSelectedButtonActionPerformed

    private void viewSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewSelectedButtonActionPerformed
        int selectedRow = manageBookingsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking first!",
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) manageBookingsTable.getModel();
        String bookingId = (String) model.getValueAt(selectedRow, 0);
        if (bookingId == null || bookingId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid booking selected!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Booking booking = BookingRepository.getBookingById(bookingId);
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Booking not found!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get passenger details
        Passenger passenger = PassengerRepository.getPassengerByUsername(booking.getPassengerUsername());
        String fullName;
        String addressText = "";
        if (passenger != null) {
            fullName = passenger.getFirstName() + " " + passenger.getLastName();
            addressText = passenger.getBarangay() + ", " + passenger.getCity() + ", " + passenger.getProvince();
        } else {
            fullName = booking.getPassengerUsername();
        }
        
        // Populate labels in personal booking details panel
        userName.setText(fullName);
        bookingID.setText("Booking ID: " + booking.getBookingId());
        userAddress.setText(addressText);
        route.setText(booking.getOrigin() + " \u2192 " + booking.getDestination());
        category.setText(booking.getTripType());
        flightDate.setText(booking.getDepartureDate() != null ? booking.getDepartureDate().toString() : "");
        paymentMode.setText(booking.getPaymentType() != null ? booking.getPaymentType() : "");
        int totalPassengers = booking.getNumberOfAdults() + booking.getNumberOfMinors();
        noOfPassenger.setText(String.valueOf(totalPassengers));
        
        // Passenger names and seats
        List<String> passengerNames = booking.getPassengerNames();
        List<String> seatsList = booking.getReservedSeats();
        if (passengerNames != null && !passengerNames.isEmpty()) {
            passengersName.setText(String.join(", ", passengerNames));
        } else {
            passengersName.setText("N/A");
        }
        if (seatsList != null && !seatsList.isEmpty()) {
            seats.setText(String.join(", ", seatsList));
        } else {
            seats.setText("N/A");
        }
        
        // Request info based on status
        String status = booking.getStatus() != null ? booking.getStatus() : "";
        if (status.equalsIgnoreCase("Cancel Book")) {
            userRequest.setText("Request to cancel booking");
        } else if (status.equalsIgnoreCase("Pay at the counter")) {
            userRequest.setText("Pay at the counter upon check-in");
        } else {
            userRequest.setText("No special requests");
        }
        
        // Show personal booking details panel
        java.awt.CardLayout cl = (java.awt.CardLayout) manageBookingsPanel.getLayout();
        cl.show(manageBookingsPanel, "card4");
    }//GEN-LAST:event_viewSelectedButtonActionPerformed

    private void approveHistoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_approveHistoryButtonActionPerformed
        // Show Approved Bookings History panel
        populateApprovedBookingsTable();
        java.awt.CardLayout cl = (java.awt.CardLayout) manageBookingsPanel.getLayout();
        cl.show(manageBookingsPanel, "card3");
    }//GEN-LAST:event_approveHistoryButtonActionPerformed

    private void approveSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_approveSelectedButtonActionPerformed
        int selectedRow = manageBookingsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking first!",
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) manageBookingsTable.getModel();
        String bookingId = (String) model.getValueAt(selectedRow, 0);
        String statusLabel = (String) model.getValueAt(selectedRow, 5);
        
        Booking booking = BookingRepository.getBookingById(bookingId);
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Booking not found!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if ("Pay at the counter".equalsIgnoreCase(statusLabel)) {
            // Approving cash payment - confirm booking
            booking.setStatus("Confirmed");
        } else if ("Cancel book".equalsIgnoreCase(statusLabel)) {
            // Approving cancellation request
            booking.setStatus("Cancelled");
        } else {
            JOptionPane.showMessageDialog(this, "This booking is already processed.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        boolean updated = BookingRepository.updateBooking(booking);
        if (updated) {
            JOptionPane.showMessageDialog(this, "Booking updated successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            populateManageBookingsTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update booking!",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_approveSelectedButtonActionPerformed

    private void backToBookingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backToBookingsButtonActionPerformed
        // Back from Approved History to main bookings panel
        populateManageBookingsTable();
        java.awt.CardLayout cl = (java.awt.CardLayout) manageBookingsPanel.getLayout();
        cl.show(manageBookingsPanel, "card2");
    }//GEN-LAST:event_backToBookingsButtonActionPerformed

    private void backToButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backToButtonActionPerformed
        // Back from personal booking details to main bookings panel
        java.awt.CardLayout cl = (java.awt.CardLayout) manageBookingsPanel.getLayout();
        cl.show(manageBookingsPanel, "card2");
    }//GEN-LAST:event_backToButtonActionPerformed

    private void viewSelectedButtonSeatsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewSelectedButtonSeatsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_viewSelectedButtonSeatsActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton17ActionPerformed

    private void userProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userProfileButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_userProfileButtonActionPerformed

    private void searchBookingIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBookingIDActionPerformed
        String bookingId = inputBookingID.getText();
        if (bookingId == null || bookingId.trim().isEmpty() ||
            bookingId.equalsIgnoreCase("Enter Booking ID")) {
            JOptionPane.showMessageDialog(this, "Please enter a Booking ID!",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        bookingId = bookingId.trim();
        DefaultTableModel model = (DefaultTableModel) manageBookingsTable.getModel();
        model.setRowCount(0);
        
        Booking booking = BookingRepository.getBookingById(bookingId);
        if (booking != null && shouldIncludeInManageBookings(booking)) {
            Passenger passenger = PassengerRepository.getPassengerByUsername(booking.getPassengerUsername());
            String name;
            if (passenger != null) {
                name = passenger.getFirstName() + " " + passenger.getLastName();
            } else {
                name = booking.getPassengerUsername();
            }
            
            String routeText = booking.getOrigin() + " \u2192 " + booking.getDestination();
            String tripType = booking.getTripType();
            String flightDateText = booking.getDepartureDate() != null ? booking.getDepartureDate().toString() : "";
            int totalPassengers = booking.getNumberOfAdults() + booking.getNumberOfMinors();
            
            String displayStatus;
            if (booking.getPaymentType() != null && booking.getPaymentType().equalsIgnoreCase("Cash")) {
                displayStatus = "Pay at the counter";
            } else if (booking.getStatus() != null && booking.getStatus().equalsIgnoreCase("Cancel Book")) {
                displayStatus = "Cancel book";
            } else {
                displayStatus = booking.getStatus() != null ? booking.getStatus() : "";
            }
            
            model.addRow(new Object[]{
                booking.getBookingId(),
                name,
                routeText,
                tripType,
                flightDateText,
                displayStatus,
                totalPassengers
            });
        } else {
            JOptionPane.showMessageDialog(this, "No booking found for ID: " + bookingId,
                "No Results", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_searchBookingIDActionPerformed
    
    
    private void startDateTime() {

        DateTimeFormatter timeFormat =
                DateTimeFormatter.ofPattern("hh:mm:ss a");

        DateTimeFormatter dateFormat =
                DateTimeFormatter.ofPattern("MMMM dd yyyy");

        Timer timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();

            timeLabel.setText(now.format(timeFormat));
            dateLabel.setText(now.format(dateFormat).toUpperCase());
        });

        timer.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton A17;
    private javax.swing.JButton A18;
    private javax.swing.JButton A19;
    private javax.swing.JButton A20;
    private javax.swing.JButton B1;
    private javax.swing.JButton B11;
    private javax.swing.JButton B12;
    private javax.swing.JButton B19;
    private javax.swing.JButton B2;
    private javax.swing.JButton B20;
    private javax.swing.JButton B4;
    private javax.swing.JButton B5;
    private javax.swing.JButton B6;
    private javax.swing.JButton B7;
    private javax.swing.JPanel abSectionSeats;
    private javax.swing.JPanel abSectionSeats1;
    private javax.swing.JLabel activeFlights;
    private javax.swing.JLabel activeFlightsLabel;
    private javax.swing.JPanel activeFlightsPanel;
    private javax.swing.JButton addFlightButton;
    private javax.swing.JPanel addFlightDetailsPanel;
    private javax.swing.JPanel addFlightOfferPanel;
    private javax.swing.JPanel addFlightSchedulePanel;
    private javax.swing.JButton addOfferButton;
    private javax.swing.JButton addScheduleButton;
    private javax.swing.JButton airAsiaButton;
    private javax.swing.JComboBox<String> aircraftModel;
    private javax.swing.JLabel aircraftModelLabel;
    private javax.swing.JComboBox<String> airline;
    private javax.swing.JLabel airlineLabel;
    private javax.swing.JLabel airlineSelectorLabel;
    private javax.swing.JPanel airlineSelectorPanel;
    private javax.swing.JComboBox<String> amenity;
    private javax.swing.JLabel amenityLabel;
    private javax.swing.JLabel approveBookingsHistoryLabel;
    private javax.swing.JButton approveHistoryButton;
    private javax.swing.JButton approveSelectedButton;
    private javax.swing.JPanel approvedBookingsHistory;
    private javax.swing.JScrollPane approvedBookingsScrollPane;
    private javax.swing.JTable approvedBookingsTable;
    private javax.swing.JButton backButton;
    private javax.swing.JButton backToBookingsButton;
    private javax.swing.JButton backToButton;
    private javax.swing.JTextField baseFare;
    private javax.swing.JLabel baseFareLabel;
    private javax.swing.JLabel bookingID;
    private javax.swing.JPanel bookingsMainPanel;
    private javax.swing.JPanel bookingsPanel;
    private javax.swing.JComboBox<String> cabinClass;
    private javax.swing.JLabel cabinClassLabel;
    private javax.swing.JLabel category;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JButton cebGoButton;
    private javax.swing.JButton dashboardButton;
    private javax.swing.JPanel dashboardPanel;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JLabel dateLabelSeats;
    private javax.swing.JLabel dateScheduleLabel;
    private javax.swing.JLabel datetimeLabel;
    private javax.swing.JPanel datetimePanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton deleteSelectedButton;
    private javax.swing.JLabel departureLabel;
    private javax.swing.JComboBox<String> destination;
    private javax.swing.JComboBox<String> destinationComboBox;
    private javax.swing.JLabel destinationLabel;
    private javax.swing.JLabel destinationLabelSeats;
    private javax.swing.JComboBox<String> destinationSchedule;
    private javax.swing.JLabel destinationScheduleLabel;
    private javax.swing.JLabel detailsLabel;
    private javax.swing.JTextField duration;
    private javax.swing.JLabel durationLabel;
    private javax.swing.JButton editButton;
    private javax.swing.JButton editSelectedButton;
    private javax.swing.JComboBox<String> entertainment;
    private javax.swing.JLabel entertainmentLabel;
    private javax.swing.JTextField flightCode;
    private javax.swing.JLabel flightCodeLabel;
    private javax.swing.JTextField flightCodeSchedule;
    private javax.swing.JLabel flightCodeScheduleLabel;
    private javax.swing.JLabel flightDate;
    private javax.swing.JLabel flightDateLabel;
    private javax.swing.JPanel flightMaker;
    private javax.swing.JTable flightTable;
    private javax.swing.JComboBox<String> foodAndBeverages;
    private javax.swing.JLabel foodAndBeveragesLabel;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JSpinner hourTimeSchedule;
    private javax.swing.JTextField inputBookingID;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton44;
    private javax.swing.JButton jButton45;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton48;
    private javax.swing.JButton jButton49;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton51;
    private javax.swing.JButton jButton52;
    private javax.swing.JButton jButton53;
    private javax.swing.JButton jButton54;
    private javax.swing.JButton jButton55;
    private javax.swing.JButton jButton56;
    private javax.swing.JButton jButton57;
    private javax.swing.JButton jButton58;
    private javax.swing.JButton jButton59;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton60;
    private javax.swing.JButton jButton61;
    private javax.swing.JButton jButton62;
    private javax.swing.JButton jButton63;
    private javax.swing.JButton jButton64;
    private javax.swing.JButton jButton65;
    private javax.swing.JButton jButton66;
    private javax.swing.JButton jButton67;
    private javax.swing.JButton jButton68;
    private javax.swing.JButton jButton69;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton70;
    private javax.swing.JButton jButton71;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton logOutButton;
    private javax.swing.JButton manageBookingsButton;
    private javax.swing.JLabel manageBookingsHeaderLabel;
    private javax.swing.JPanel manageBookingsHeaderPanel;
    private javax.swing.JPanel manageBookingsPanel;
    private javax.swing.JScrollPane manageBookingsScrollPane;
    private javax.swing.JTable manageBookingsTable;
    private javax.swing.JButton manageFlightsButton;
    private javax.swing.JLabel manageFlightsHeaderLabel;
    private javax.swing.JPanel manageFlightsHeaderPanel;
    private javax.swing.JPanel manageFlightsPanel;
    private javax.swing.JLabel manageScheduleHeaderLabel;
    private javax.swing.JPanel manageScheduleHeaderPanel;
    private javax.swing.JPanel manageSchedulePanel;
    private javax.swing.JButton manageSchedulesButton;
    private javax.swing.JPanel manageSeats;
    private javax.swing.JButton manageSeatsButton;
    private javax.swing.JLabel manageSeatsHeaderLabel;
    private javax.swing.JPanel manageSeatsHeaderPanel;
    private javax.swing.JPanel manageSeatsPanel;
    private javax.swing.JScrollPane manageSeatsScrollPane;
    private javax.swing.JSpinner minuteScheduleLabel;
    private javax.swing.JTextArea moreDetails;
    private javax.swing.JLabel moreDetailsLabel;
    private javax.swing.JLabel noOfPassenger;
    private javax.swing.JLabel noOfPassengerLabel;
    private javax.swing.JComboBox<String> origin;
    private javax.swing.JComboBox<String> originComboBox;
    private javax.swing.JLabel originLabel;
    private javax.swing.JLabel originLabelSeats;
    private javax.swing.JComboBox<String> originSchedule;
    private javax.swing.JLabel originScheduleLabel;
    private javax.swing.JLabel otherInfoLabel;
    private javax.swing.JButton palButton;
    private javax.swing.JLabel passengersBookedLabel;
    private javax.swing.JLabel passengersName;
    private javax.swing.JLabel passengersToday;
    private javax.swing.JLabel passengersTodayLabel;
    private javax.swing.JPanel passengersTodayPanel;
    private javax.swing.JLabel paymentLabel;
    private javax.swing.JLabel paymentMode;
    private javax.swing.JPanel personalBookingDetails;
    private javax.swing.JLabel profilePhotoLabel;
    private javax.swing.JPanel profilePhotoPanel;
    private javax.swing.JLabel recentFlightsLabel;
    private javax.swing.JPanel recentFlightsPanel;
    private javax.swing.JLabel route;
    private javax.swing.JLabel routeLabel;
    private javax.swing.JTextField scheduleID;
    private javax.swing.JPanel scheduledFlightsPanel;
    private javax.swing.JScrollPane scheduledFlightsScrollPane;
    private javax.swing.JTable scheduledFlightsTable;
    private javax.swing.JButton searchBookingID;
    private javax.swing.JLabel searchScheduleLabel;
    private javax.swing.JTextField seatCapacity;
    private javax.swing.JLabel seatCapacityLabel;
    private javax.swing.JComboBox<String> seatType;
    private javax.swing.JLabel seatTypeLabel;
    private javax.swing.JLabel seats;
    private javax.swing.JLabel seatsLabel;
    private javax.swing.JPanel seatsPanell;
    private javax.swing.JScrollPane seatsScrollPane;
    private javax.swing.JTable seatsTable;
    private javax.swing.JLabel selectedAirline;
    private javax.swing.JLabel selectedAirlineLabel;
    private javax.swing.JPanel selectedAirlinePanel;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JTabbedPane tabPanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JLabel timeScheduleLabel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JLabel totalRevenue;
    private javax.swing.JLabel totalRevenueLabel;
    private javax.swing.JPanel totalRevenuePanel;
    private javax.swing.JLabel userAddress;
    private javax.swing.JLabel userAddressLabel;
    private javax.swing.JPanel userDetailsPanel;
    private javax.swing.JLabel userName;
    private javax.swing.JButton userProfileButton;
    private javax.swing.JLabel userRequest;
    private javax.swing.JLabel username;
    private javax.swing.JButton viewSelectedButton;
    private javax.swing.JButton viewSelectedButtonSeats;
    private javax.swing.JLabel welcomeLabel;
    // End of variables declaration//GEN-END:variables
}
