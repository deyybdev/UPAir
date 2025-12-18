/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package kingsman.upair;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.Timer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Calendar;
import java.util.Date;
import java.awt.GridLayout;
import java.awt.Color;
import kingsman.upair.model.Flight;
import kingsman.upair.model.Schedule;
import kingsman.upair.model.Booking;
import kingsman.upair.model.FlightOffer;
import kingsman.upair.service.FlightStatusService;
import kingsman.upair.service.ScheduleService;
import kingsman.upair.service.PriceCalculationService;
import kingsman.upair.repository.BookingRepository;
import kingsman.upair.repository.FlightRepository;

/**
 * Passenger Frame for passenger operations
 * 
 * @author admin
 */
public class PassengerFrame extends javax.swing.JFrame {
    
    private String currentUsername = ""; // Will be set from login
    private Timer statusUpdateTimer; // Timer for real-time status updates
    private String selectedFlightCode = ""; // Currently selected flight for booking
    private Schedule selectedSchedule = null; // Currently selected schedule
    private List<String> selectedSeats = new ArrayList<>(); // Selected seats
    private Map<String, String> seatToPassengerName = new HashMap<>(); // Seat to passenger name mapping
    private boolean isRoundTrip = false; // Trip type flag

    /**
     * Creates new form PassengerFrame
     */
    public PassengerFrame() {
        initComponents();
        initializePassengerComponents();
    }
    
    /**
     * Creates new form PassengerFrame with username
     */
    public PassengerFrame(String username) {
        initComponents();
        this.currentUsername = username;
        this.username.setText(username);
        initializePassengerComponents();
        // Center frame on screen
        setLocationRelativeTo(null);
    }
    
    /**
     * Initializes passenger components
     */
    private void initializePassengerComponents() {
        // Initialize airport dropdowns
        initializeAirportDropdowns();
        
        // Initialize trip type radio buttons
        initializeTripType();
        
        // Initialize spinners
        adultCounter.setModel(new javax.swing.SpinnerNumberModel(1, 1, 10, 1));
        minorCounter.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        
        // Initialize payment method combo box
        initializePaymentMethod();
        
        // Set default to One Way and initialize dropdowns
        oneWayType.setSelected(true);
        updateOriginDestinationDropdowns();
        
        // Add listeners
        oneWayType.addActionListener(e -> handleTripTypeChange());
        roundTripType.addActionListener(e -> handleTripTypeChange());
        originToBook.addActionListener(e -> {
            if (roundTripType.isSelected()) {
                updateDestinationDropdown();
            }
        });
        
        // Add table selection listener for booking - show price and seats
        bookFlightResultTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = bookFlightResultTable.getSelectedRow();
                if (selectedRow >= 0) {
                    handleFlightSelectionForBooking();
                }
            }
        });
        
        // Add listeners for passenger counters to update price
        adultCounter.addChangeListener(e -> updatePriceIfFlightSelected());
        minorCounter.addChangeListener(e -> updatePriceIfFlightSelected());
        
        // Start real-time status updates
        startStatusUpdateTimer();
        
        // Load initial flight overview
        populateFlightOverview();
    }
    
    /**
     * Initializes airport dropdowns
     */
    private void initializeAirportDropdowns() {
        String[] airports = {
            "Daraga(DRP)",
            "Manila(MNL)",
            "Cebu(CEB)"
        };
        
        // Flight Overview dropdowns
        originComboBox.removeAllItems();
        destinationComboBox.removeAllItems();
        for (String airport : airports) {
            originComboBox.addItem(airport);
            destinationComboBox.addItem(airport);
        }
        
        // Book Flight dropdowns
        originToBook.removeAllItems();
        destinationToBook.removeAllItems();
        for (String airport : airports) {
            originToBook.addItem(airport);
            destinationToBook.addItem(airport);
        }
    }
    
    /**
     * Updates destination dropdown to exclude selected origin (for round trip)
     */
    private void updateDestinationDropdown() {
        if (oneWayType.isSelected()) {
            // One way already handled in updateOriginDestinationDropdowns
            return;
        }
        
        String selectedOrigin = (String) originToBook.getSelectedItem();
        if (selectedOrigin == null) return;
        
        destinationToBook.removeAllItems();
        String[] airports = {"Daraga(DRP)", "Manila(MNL)", "Cebu(CEB)"};
        
        for (String airport : airports) {
            if (!airport.equals(selectedOrigin)) {
                destinationToBook.addItem(airport);
            }
        }
    }
    
    /**
     * Initializes trip type selection
     */
    private void initializeTripType() {
        // Create button group for radio buttons
        javax.swing.ButtonGroup tripTypeGroup = new javax.swing.ButtonGroup();
        tripTypeGroup.add(oneWayType);
        tripTypeGroup.add(roundTripType);
        oneWayType.setSelected(true);
    }
    
    /**
     * Initializes payment method combo box
     */
    private void initializePaymentMethod() {
        String[] paymentMethods = {"Online", "Cash"};
        paymentMethod.setModel(new javax.swing.DefaultComboBoxModel<>(paymentMethods));
        paymentMethod.setSelectedIndex(0); // Default to Online
        paymentMethod.setFont(new java.awt.Font("Segoe UI", 0, 14));
    }
    
    /**
     * Handles trip type change
     * One Way: Only DRP in origin, MNL and CEB in destination
     * Round Trip: All airports available, but considers reverse route as well
     */
    private void handleTripTypeChange() {
        isRoundTrip = roundTripType.isSelected();
        updateOriginDestinationDropdowns();
    }
    
    /**
     * Updates origin and destination dropdowns based on trip type
     */
    private void updateOriginDestinationDropdowns() {
        originToBook.removeAllItems();
        destinationToBook.removeAllItems();
        
        if (oneWayType.isSelected()) {
            // One Way: Only DRP in origin, MNL and CEB in destination
            originToBook.addItem("Daraga(DRP)");
            destinationToBook.addItem("Manila(MNL)");
            destinationToBook.addItem("Cebu(CEB)");
        } else {
            // Round Trip: All airports available
            originToBook.addItem("Daraga(DRP)");
            originToBook.addItem("Manila(MNL)");
            originToBook.addItem("Cebu(CEB)");
            destinationToBook.addItem("Daraga(DRP)");
            destinationToBook.addItem("Manila(MNL)");
            destinationToBook.addItem("Cebu(CEB)");
        }
    }
    
    /**
     * Starts timer for real-time status updates
     */
    private void startStatusUpdateTimer() {
        statusUpdateTimer = new Timer(60000, e -> { // Update every minute
            populateFlightOverview();
        });
        statusUpdateTimer.start();
    }
    
    /**
     * Populates flight overview table with real-time status
     */
    private void populateFlightOverview() {
        DefaultTableModel model = (DefaultTableModel) flightOverviewTable.getModel();
        model.setRowCount(0);
        
        List<FlightStatusService.ScheduleWithStatus> schedules = FlightStatusService.getSchedulesWithStatus();
        
        for (FlightStatusService.ScheduleWithStatus sws : schedules) {
            Schedule schedule = sws.getSchedule();
            String status = sws.getStatus();
            
            model.addRow(new Object[]{
                schedule.getAirline(),
                schedule.getFlightCode(),
                schedule.getOrigin(),
                schedule.getDestination(),
                schedule.getDepartureDate().toString(),
                schedule.getDepartureTime().toString(),
                status
            });
        }
        
        // Show message if no flights - check column count first
        if (model.getRowCount() == 0) {
            int colCount = model.getColumnCount();
            Object[] emptyRow = new Object[colCount];
            for (int i = 0; i < colCount - 1; i++) {
                emptyRow[i] = "";
            }
            emptyRow[colCount - 1] = "No available flights";
            model.addRow(emptyRow);
            flightOverviewTable.setEnabled(false);
        } else {
            flightOverviewTable.setEnabled(true);
        }
    }
    
    /**
     * Populates available flights for booking (only Scheduled status)
     */
    private void populateAvailableFlightsForBooking() {
        DefaultTableModel model = (DefaultTableModel) bookFlightResultTable.getModel();
        model.setRowCount(0);
        
        // Get only scheduled flights
        List<FlightStatusService.ScheduleWithStatus> schedules = FlightStatusService.getScheduledFlightsOnly();
        
        // Filter by origin, destination, and date if provided
        String origin = (String) originToBook.getSelectedItem();
        String destination = (String) destinationToBook.getSelectedItem();
        Date selectedDate = bookflightDateChooser.getDate();
        LocalDate filterDate = null;
        
        if (selectedDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(selectedDate);
            filterDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        }
        
        for (FlightStatusService.ScheduleWithStatus sws : schedules) {
            Schedule schedule = sws.getSchedule();
            boolean matches = false;
            
            if (roundTripType.isSelected() && origin != null && destination != null) {
                // Round trip: match either direction (DRP->MNL or MNL->DRP)
                boolean forwardMatch = schedule.getOrigin().equals(origin) && schedule.getDestination().equals(destination);
                boolean reverseMatch = schedule.getOrigin().equals(destination) && schedule.getDestination().equals(origin);
                matches = forwardMatch || reverseMatch;
            } else if (oneWayType.isSelected()) {
                // One way: exact match
                matches = (origin == null || schedule.getOrigin().equals(origin)) &&
                         (destination == null || schedule.getDestination().equals(destination));
            }
            
            if (filterDate != null && !schedule.getDepartureDate().equals(filterDate)) {
                matches = false;
            }
            
            if (matches) {
                model.addRow(new Object[]{
                    schedule.getAirline(),
                    schedule.getFlightCode(),
                    schedule.getOrigin(),
                    schedule.getDestination(),
                    schedule.getDepartureDate().toString(),
                    schedule.getDepartureTime().toString()
                });
            }
        }
        
        // Show message if no flights - check column count first
        if (model.getRowCount() == 0) {
            int colCount = model.getColumnCount();
            Object[] emptyRow = new Object[colCount];
            for (int i = 0; i < colCount - 1; i++) {
                emptyRow[i] = "";
            }
            emptyRow[colCount - 1] = "No available flights";
            model.addRow(emptyRow);
            bookFlightResultTable.setEnabled(false);
        } else {
            bookFlightResultTable.setEnabled(true);
        }
        
        // Clear previous selection
        selectedSchedule = null;
        totalPrice.setText("PHP 0.00");
        clearSeatPanels();
    }
    
    /**
     * Generates seat buttons in the specific seat panels
     * Business Class: 2 panels (businessClassSeats1, businessClassSeats2) - 2 columns x 10 rows = 20 seats each (40 total)
     * Economy Class: 4 panels (economyClassSeats1-4) - 3 columns x 12 rows = 36 seats each (144 total)
     * Total: 184 seats
     */
    private void generateSeatButtons() {
        // Clear all seat panels
        businessClassSeats1.removeAll();
        businessClassSeats2.removeAll();
        economyClassSeats1.removeAll();
        economyClassSeats2.removeAll();
        economyClassSeats3.removeAll();
        economyClassSeats4.removeAll();
        
        // Get reserved seats for this flight
        List<String> reservedSeats = new ArrayList<>();
        if (selectedSchedule != null) {
            reservedSeats = BookingRepository.getReservedSeatsForFlight(
                selectedSchedule.getFlightCode(), 
                selectedSchedule.getDepartureDate()
            );
        }
        
        // Business Class Seats - Panel 1 (A1-A20)
        generateSeatButtonsForPanel(businessClassSeats1, 1, 20, reservedSeats, "A");
        
        // Business Class Seats - Panel 2 (B1-B20)
        generateSeatButtonsForPanel(businessClassSeats2, 1, 20, reservedSeats, "B");
        
        // Economy Class Seats - Panel 1 (C1-C36)
        generateSeatButtonsForPanel(economyClassSeats1, 1, 36, reservedSeats, "C");
        
        // Economy Class Seats - Panel 2 (D1-D36)
        generateSeatButtonsForPanel(economyClassSeats2, 1, 36, reservedSeats, "D");
        
        // Economy Class Seats - Panel 3 (E1-E36)
        generateSeatButtonsForPanel(economyClassSeats3, 1, 36, reservedSeats, "E");
        
        // Economy Class Seats - Panel 4 (F1-F36)
        generateSeatButtonsForPanel(economyClassSeats4, 1, 36, reservedSeats, "F");
        
        // Revalidate and repaint all panels
        businessClassSeats1.revalidate();
        businessClassSeats1.repaint();
        businessClassSeats2.revalidate();
        businessClassSeats2.repaint();
        economyClassSeats1.revalidate();
        economyClassSeats1.repaint();
        economyClassSeats2.revalidate();
        economyClassSeats2.repaint();
        economyClassSeats3.revalidate();
        economyClassSeats3.repaint();
        economyClassSeats4.revalidate();
        economyClassSeats4.repaint();
    }
    
    /**
     * Generates seat buttons for a specific panel
     * @param panel The panel to add buttons to
     * @param startSeat Starting seat number
     * @param endSeat Ending seat number
     * @param reservedSeats List of reserved seats
     * @param seatLetter The letter prefix for seats (A, B, C, D, E, F)
     */
    private void generateSeatButtonsForPanel(javax.swing.JPanel panel, int startSeat, int endSeat, 
                                             List<String> reservedSeats, String seatLetter) {
        for (int seatNum = startSeat; seatNum <= endSeat; seatNum++) {
            String seatLabel = seatLetter + seatNum;
            
            javax.swing.JButton seatButton = new javax.swing.JButton(seatLabel);
            seatButton.setFont(new java.awt.Font("Arial", 1, 10));
            seatButton.setFocusPainted(false);
            
            // Check if seat is reserved
            if (reservedSeats.contains(seatLabel)) {
                seatButton.setBackground(Color.RED);
                seatButton.setForeground(Color.WHITE);
                seatButton.setEnabled(false);
                seatButton.setToolTipText("Reserved");
            } else if (selectedSeats.contains(seatLabel)) {
                // Selected seat - disabled with blue background
                seatButton.setBackground(Color.BLUE);
                seatButton.setForeground(Color.WHITE);
                seatButton.setEnabled(false);
                seatButton.setToolTipText("Selected: " + seatToPassengerName.get(seatLabel));
            } else {
                // Available seat - green text on white/light background
                seatButton.setBackground(Color.WHITE);
                seatButton.setForeground(new Color(0, 128, 0)); // Dark green for readability
                seatButton.setEnabled(true);
                seatButton.setToolTipText("Available");
            }
            
            seatButton.addActionListener(e -> handleSeatSelection(seatButton, seatLabel));
            panel.add(seatButton);
        }
    }
    
    /**
     * Handles seat selection
     */
    private void handleSeatSelection(javax.swing.JButton seatButton, String seatLabel) {
        int totalPassengers = (Integer) adultCounter.getValue() + (Integer) minorCounter.getValue();
        
        if (selectedSeats.size() >= totalPassengers) {
            JOptionPane.showMessageDialog(this, 
                "You have already selected seats for all passengers!", 
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Select seat
        selectedSeats.add(seatLabel);
        
        // Get passenger name
        String passengerName = JOptionPane.showInputDialog(this, 
            "Enter passenger name for seat " + seatLabel + ":", 
            "Passenger Name", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (passengerName != null && !passengerName.trim().isEmpty()) {
            seatToPassengerName.put(seatLabel, passengerName.trim());
            // Update button appearance - disabled with blue background
            seatButton.setBackground(Color.BLUE);
            seatButton.setForeground(Color.WHITE);
            seatButton.setEnabled(false);
            seatButton.setToolTipText("Selected: " + passengerName.trim());
        } else {
            selectedSeats.remove(seatLabel);
            JOptionPane.showMessageDialog(this, 
                "Passenger name is required!", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        userPanel = new javax.swing.JPanel();
        userSidePanel = new javax.swing.JPanel();
        airlineComparisonButton = new javax.swing.JButton();
        userDashboardButton = new javax.swing.JButton();
        flightOverviewButton = new javax.swing.JButton();
        bookFlightButton = new javax.swing.JButton();
        bookingStatusButton = new javax.swing.JButton();
        userTopPanel = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        logOutButton = new javax.swing.JButton();
        passengerID = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        userProfileButton = new javax.swing.JButton();
        userTabbedPanel = new javax.swing.JTabbedPane();
        userDashboardPanel = new javax.swing.JPanel();
        dashboardPanel = new javax.swing.JPanel();
        welcomeLabel = new javax.swing.JLabel();
        activeFlightsPanel = new javax.swing.JPanel();
        activeFlightsLabel = new javax.swing.JLabel();
        activeFlights = new javax.swing.JLabel();
        totalBookingsPanel = new javax.swing.JPanel();
        totalBookingsLabel = new javax.swing.JLabel();
        totalBookings = new javax.swing.JLabel();
        vouchersAvailablePanel = new javax.swing.JPanel();
        vouchersAvailableLabel = new javax.swing.JLabel();
        vouchersAvailable = new javax.swing.JLabel();
        myDashboardPanel = new javax.swing.JPanel();
        lastDepartedPanel = new javax.swing.JPanel();
        photoLabel = new javax.swing.JPanel();
        planeLabel = new javax.swing.JLabel();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        status = new javax.swing.JLabel();
        routePanel = new javax.swing.JPanel();
        routeLabel = new javax.swing.JLabel();
        route = new javax.swing.JLabel();
        airlinePanel = new javax.swing.JPanel();
        airlineLabel = new javax.swing.JLabel();
        airline = new javax.swing.JLabel();
        departureDatePanel = new javax.swing.JPanel();
        departureDateLabel = new javax.swing.JLabel();
        departureDate = new javax.swing.JLabel();
        departureTimePanel = new javax.swing.JPanel();
        departureTimeLabel = new javax.swing.JLabel();
        departureTime = new javax.swing.JLabel();
        lastDepartedLabel = new javax.swing.JLabel();
        myRecentBookingsPanel = new javax.swing.JPanel();
        myRecentBookingLabel = new javax.swing.JLabel();
        recentBookingScrollPane = new javax.swing.JScrollPane();
        recentBookingTable = new javax.swing.JTable();
        viewAllBookingsButton = new javax.swing.JButton();
        advertisementPanel = new javax.swing.JPanel();
        promotionalPanel = new javax.swing.JPanel();
        textLabel = new javax.swing.JLabel();
        alsoTextLabel = new javax.swing.JLabel();
        promoPanel = new javax.swing.JPanel();
        promotional1Label = new javax.swing.JLabel();
        promotional2Label = new javax.swing.JLabel();
        promotional3Label = new javax.swing.JLabel();
        promotional4Label = new javax.swing.JLabel();
        viewTermsLabel = new javax.swing.JButton();
        redeemButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        flightOverviewPanel = new javax.swing.JPanel();
        flightOverviewHeaderPanel = new javax.swing.JPanel();
        flightOverviewHeaderLabel = new javax.swing.JLabel();
        refreshButton1 = new javax.swing.JButton();
        availableFlightsPanel = new javax.swing.JPanel();
        selectDateLabel = new javax.swing.JLabel();
        originLabel = new javax.swing.JLabel();
        destinationLabel = new javax.swing.JLabel();
        dateChooser = new com.toedter.calendar.JDateChooser();
        destinationComboBox = new javax.swing.JComboBox<>();
        originComboBox = new javax.swing.JComboBox<>();
        searchFlightButton = new javax.swing.JButton();
        availableFlightsScrollPane = new javax.swing.JScrollPane();
        flightOverviewTable = new javax.swing.JTable();
        bookFlightPanel = new javax.swing.JPanel();
        bookFlightHeaderPanel = new javax.swing.JPanel();
        bookFlightHeaderLabel = new javax.swing.JLabel();
        refreshButton2 = new javax.swing.JButton();
        availableFlightsPanel1 = new javax.swing.JPanel();
        totalPrice = new javax.swing.JLabel();
        bookflightDateChooser = new com.toedter.calendar.JDateChooser();
        availableFlightsScrollPane1 = new javax.swing.JScrollPane();
        bookFlightResultTable = new javax.swing.JTable();
        dateLabelBookFlight = new javax.swing.JLabel();
        roundTripType = new javax.swing.JRadioButton();
        oneWayType = new javax.swing.JRadioButton();
        destinationLabelBookFlight = new javax.swing.JLabel();
        destinationToBook = new javax.swing.JComboBox<>();
        originToBook = new javax.swing.JComboBox<>();
        originToLabel = new javax.swing.JLabel();
        adultLabel = new javax.swing.JLabel();
        minorCounter = new javax.swing.JSpinner();
        adultCounter = new javax.swing.JSpinner();
        minorCounterLabel = new javax.swing.JLabel();
        totalPriceLabel = new javax.swing.JLabel();
        bookButton = new javax.swing.JButton();
        searchBookFlightButton = new javax.swing.JButton();
        totalPriceLabel1 = new javax.swing.JLabel();
        paymentMethod = new javax.swing.JComboBox<>();
        seatsSelector = new javax.swing.JScrollPane();
        seatsPanell = new javax.swing.JPanel();
        businessClassSeats2 = new javax.swing.JPanel();
        economyClassSeats2 = new javax.swing.JPanel();
        businessClassSeats1 = new javax.swing.JPanel();
        economyClassSeats1 = new javax.swing.JPanel();
        economyClassSeats3 = new javax.swing.JPanel();
        economyClassSeats4 = new javax.swing.JPanel();
        bookingStatusPanel = new javax.swing.JPanel();
        bookingStatusHeaderPanel = new javax.swing.JPanel();
        bookingStatusHeaderLabel = new javax.swing.JLabel();
        refreshButton3 = new javax.swing.JButton();
        availableFlightsPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        bookingStatusRecord = new javax.swing.JPanel();
        statusFlight = new javax.swing.JLabel();
        selectDateLabel12 = new javax.swing.JLabel();
        bookingID = new javax.swing.JLabel();
        paymentStatus = new javax.swing.JLabel();
        selectDateLabel15 = new javax.swing.JLabel();
        selectDateLabel16 = new javax.swing.JLabel();
        selectDateLabel17 = new javax.swing.JLabel();
        selectDateLabel18 = new javax.swing.JLabel();
        selectDateLabel19 = new javax.swing.JLabel();
        totalPriceBooked = new javax.swing.JLabel();
        paymentType = new javax.swing.JLabel();
        seatBooked = new javax.swing.JLabel();
        flightCode = new javax.swing.JLabel();
        routeDetails = new javax.swing.JLabel();
        selectDateLabel25 = new javax.swing.JLabel();
        selectDateLabel26 = new javax.swing.JLabel();
        flightStatusBooked = new javax.swing.JLabel();
        airlineType = new javax.swing.JLabel();
        timeBooked = new javax.swing.JLabel();
        flightTicketType = new javax.swing.JLabel();
        selectDateLabel31 = new javax.swing.JLabel();
        selectDateLabel32 = new javax.swing.JLabel();
        cancelBookingButton = new javax.swing.JButton();
        downloadTicketButton = new javax.swing.JButton();
        selectDateLabel33 = new javax.swing.JLabel();
        selectDateLabel34 = new javax.swing.JLabel();
        selectDateLabel35 = new javax.swing.JLabel();
        departureDateTime = new javax.swing.JLabel();
        passengerType = new javax.swing.JLabel();
        nextButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        airlineComparisonPanel = new javax.swing.JPanel();
        airlineComparisonHeaderPanel = new javax.swing.JPanel();
        airlineComparisonHeaderLabel = new javax.swing.JLabel();
        refreshButton4 = new javax.swing.JButton();
        availableFlightsPanel3 = new javax.swing.JPanel();
        resultCompareLabel = new javax.swing.JPanel();
        resultShow = new javax.swing.JLabel();
        result1Panel = new javax.swing.JPanel();
        flightType = new javax.swing.JLabel();
        airlineName = new javax.swing.JLabel();
        selectDateLabel127 = new javax.swing.JLabel();
        flightDuration = new javax.swing.JLabel();
        flightDate = new javax.swing.JLabel();
        airlineRating = new javax.swing.JLabel();
        price1 = new javax.swing.JLabel();
        bookInThisAirlineButton = new javax.swing.JButton();
        viewDetailsButton = new javax.swing.JButton();
        result2Panel = new javax.swing.JPanel();
        flightType1 = new javax.swing.JLabel();
        airlineName1 = new javax.swing.JLabel();
        selectDateLabel128 = new javax.swing.JLabel();
        flightDuration1 = new javax.swing.JLabel();
        flightDate1 = new javax.swing.JLabel();
        airlineRating1 = new javax.swing.JLabel();
        price2 = new javax.swing.JLabel();
        bookInThisAirlineButton1 = new javax.swing.JButton();
        viewDetailsButton1 = new javax.swing.JButton();
        result3Panel = new javax.swing.JPanel();
        flightType2 = new javax.swing.JLabel();
        airlineName2 = new javax.swing.JLabel();
        selectDateLabel129 = new javax.swing.JLabel();
        flightDuration2 = new javax.swing.JLabel();
        flightDate2 = new javax.swing.JLabel();
        airlineRating2 = new javax.swing.JLabel();
        price3 = new javax.swing.JLabel();
        bookInThisAirlineButton2 = new javax.swing.JButton();
        viewDetailsButton2 = new javax.swing.JButton();
        dateLabelCompareLabel = new javax.swing.JLabel();
        selectOrderLabel = new javax.swing.JLabel();
        order = new javax.swing.JComboBox<>();
        sort = new javax.swing.JComboBox<>();
        selectToLabel = new javax.swing.JLabel();
        dateChooserCompare = new com.toedter.calendar.JDateChooser();
        selectSortLabel = new javax.swing.JLabel();
        from = new javax.swing.JComboBox<>();
        to = new javax.swing.JComboBox<>();
        applyFiltersButton = new javax.swing.JButton();
        fromLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        userPanel.setBackground(new java.awt.Color(255, 255, 255));
        userPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        userSidePanel.setBackground(new java.awt.Color(11, 56, 118));
        userSidePanel.setPreferredSize(new java.awt.Dimension(250, 760));
        userSidePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        airlineComparisonButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        airlineComparisonButton.setForeground(new java.awt.Color(255, 255, 255));
        airlineComparisonButton.setText("AIRLINE COMPARISON");
        airlineComparisonButton.setBorderPainted(false);
        airlineComparisonButton.setContentAreaFilled(false);
        airlineComparisonButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        airlineComparisonButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        airlineComparisonButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        userSidePanel.add(airlineComparisonButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 190, 250, 40));

        userDashboardButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        userDashboardButton.setForeground(new java.awt.Color(255, 255, 255));
        userDashboardButton.setText("DASHBOARD");
        userDashboardButton.setBorderPainted(false);
        userDashboardButton.setContentAreaFilled(false);
        userDashboardButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        userDashboardButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        userDashboardButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        userDashboardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userDashboardButtonActionPerformed(evt);
            }
        });
        userSidePanel.add(userDashboardButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 250, 40));

        flightOverviewButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        flightOverviewButton.setForeground(new java.awt.Color(255, 255, 255));
        flightOverviewButton.setText("FLIGHT OVERVIEW");
        flightOverviewButton.setBorderPainted(false);
        flightOverviewButton.setContentAreaFilled(false);
        flightOverviewButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        flightOverviewButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        flightOverviewButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        userSidePanel.add(flightOverviewButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 250, 40));

        bookFlightButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        bookFlightButton.setForeground(new java.awt.Color(255, 255, 255));
        bookFlightButton.setText("BOOK FLIGHT");
        bookFlightButton.setBorderPainted(false);
        bookFlightButton.setContentAreaFilled(false);
        bookFlightButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bookFlightButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        bookFlightButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        userSidePanel.add(bookFlightButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 250, 40));

        bookingStatusButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        bookingStatusButton.setForeground(new java.awt.Color(255, 255, 255));
        bookingStatusButton.setText("BOOKING STATUS");
        bookingStatusButton.setBorderPainted(false);
        bookingStatusButton.setContentAreaFilled(false);
        bookingStatusButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bookingStatusButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        bookingStatusButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        userSidePanel.add(bookingStatusButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 150, 250, 40));

        userPanel.add(userSidePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 250, 740));

        userTopPanel.setBackground(new java.awt.Color(5, 20, 42));
        userTopPanel.setForeground(new java.awt.Color(255, 255, 255));
        userTopPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        headerLabel.setFont(new java.awt.Font("Arial Black", 1, 20)); // NOI18N
        headerLabel.setForeground(new java.awt.Color(255, 255, 255));
        userTopPanel.add(headerLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 250, 60));

        logOutButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        logOutButton.setForeground(new java.awt.Color(255, 255, 255));
        logOutButton.setBorderPainted(false);
        logOutButton.setContentAreaFilled(false);
        logOutButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logOutButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        logOutButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        logOutButton.setIconTextGap(0);
        userTopPanel.add(logOutButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 0, 50, 60));

        passengerID.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        passengerID.setForeground(new java.awt.Color(255, 255, 255));
        passengerID.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        passengerID.setText("Passenger ID: PASS360789");
        userTopPanel.add(passengerID, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 20, 170, 40));

        username.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        username.setForeground(new java.awt.Color(255, 255, 255));
        username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        username.setText("user847887593");
        userTopPanel.add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 0, 210, 40));

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
        userTopPanel.add(userProfileButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 0, -1, 60));

        userPanel.add(userTopPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1200, 60));

        userDashboardPanel.setBackground(new java.awt.Color(204, 204, 204));
        userDashboardPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        dashboardPanel.setBackground(new java.awt.Color(204, 204, 204));
        dashboardPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        welcomeLabel.setFont(new java.awt.Font("Arial", 1, 30)); // NOI18N
        welcomeLabel.setForeground(new java.awt.Color(60, 63, 65));
        welcomeLabel.setText("Welcome Aboard Passenger!");
        dashboardPanel.add(welcomeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 430, 40));

        activeFlightsPanel.setBackground(new java.awt.Color(11, 56, 118));
        activeFlightsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        activeFlightsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        activeFlightsLabel.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        activeFlightsLabel.setForeground(new java.awt.Color(255, 255, 255));
        activeFlightsLabel.setText("UPCOMING FLIGHTS");
        activeFlightsPanel.add(activeFlightsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 190, -1));

        activeFlights.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 36)); // NOI18N
        activeFlights.setForeground(new java.awt.Color(255, 255, 255));
        activeFlights.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        activeFlights.setText("1");
        activeFlightsPanel.add(activeFlights, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 260, 40));

        dashboardPanel.add(activeFlightsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 280, 100));

        totalBookingsPanel.setBackground(new java.awt.Color(11, 56, 118));
        totalBookingsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        totalBookingsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        totalBookingsLabel.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        totalBookingsLabel.setForeground(new java.awt.Color(255, 255, 255));
        totalBookingsLabel.setText("TOTAL BOOKINGS");
        totalBookingsPanel.add(totalBookingsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 190, -1));

        totalBookings.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 36)); // NOI18N
        totalBookings.setForeground(new java.awt.Color(255, 255, 255));
        totalBookings.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        totalBookings.setText("3");
        totalBookingsPanel.add(totalBookings, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 260, 40));

        dashboardPanel.add(totalBookingsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 70, 280, 100));

        vouchersAvailablePanel.setBackground(new java.awt.Color(11, 56, 118));
        vouchersAvailablePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        vouchersAvailablePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        vouchersAvailableLabel.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        vouchersAvailableLabel.setForeground(new java.awt.Color(255, 255, 255));
        vouchersAvailableLabel.setText("VOUCHER AVAILABLE");
        vouchersAvailablePanel.add(vouchersAvailableLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 190, -1));

        vouchersAvailable.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 36)); // NOI18N
        vouchersAvailable.setForeground(new java.awt.Color(255, 255, 255));
        vouchersAvailable.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        vouchersAvailable.setText("3");
        vouchersAvailablePanel.add(vouchersAvailable, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 290, 40));

        dashboardPanel.add(vouchersAvailablePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 70, 310, 100));

        myDashboardPanel.setBackground(new java.awt.Color(255, 255, 255));
        myDashboardPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "MY DASHBOARD", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        myDashboardPanel.setForeground(new java.awt.Color(255, 255, 255));
        myDashboardPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lastDepartedPanel.setBackground(new java.awt.Color(255, 255, 255));
        lastDepartedPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(11, 57, 118), 2));
        lastDepartedPanel.setForeground(new java.awt.Color(255, 255, 255));
        lastDepartedPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        photoLabel.setBackground(new java.awt.Color(204, 204, 204));
        photoLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        javax.swing.GroupLayout photoLabelLayout = new javax.swing.GroupLayout(photoLabel);
        photoLabel.setLayout(photoLabelLayout);
        photoLabelLayout.setHorizontalGroup(
            photoLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(photoLabelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(planeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        photoLabelLayout.setVerticalGroup(
            photoLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(photoLabelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(planeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        lastDepartedPanel.add(photoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 400, 200));

        statusPanel.setBackground(new java.awt.Color(11, 56, 118));
        statusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(153, 153, 153))); // NOI18N
        statusPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        statusLabel.setBackground(new java.awt.Color(255, 255, 255));
        statusLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        statusLabel.setForeground(new java.awt.Color(255, 255, 255));
        statusLabel.setText("STATUS:");
        statusPanel.add(statusLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, 90, 40));

        status.setBackground(new java.awt.Color(255, 255, 255));
        status.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        status.setForeground(new java.awt.Color(255, 255, 255));
        status.setText("DEPARTED / EN ROUTE");
        statusPanel.add(status, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 0, 230, 40));

        lastDepartedPanel.add(statusPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 460, 400, 40));

        routePanel.setBackground(new java.awt.Color(255, 255, 255));
        routePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(153, 153, 153))); // NOI18N
        routePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        routeLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        routeLabel.setForeground(new java.awt.Color(60, 63, 65));
        routeLabel.setText("Route:");
        routePanel.add(routeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 70, 40));

        route.setBackground(new java.awt.Color(0, 0, 0));
        route.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        route.setText("DRP > MNL");
        routePanel.add(route, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 0, 230, 40));

        lastDepartedPanel.add(routePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 400, 40));

        airlinePanel.setBackground(new java.awt.Color(255, 255, 255));
        airlinePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(153, 153, 153))); // NOI18N
        airlinePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        airlineLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        airlineLabel.setForeground(new java.awt.Color(60, 63, 65));
        airlineLabel.setText("Airline");
        airlinePanel.add(airlineLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 70, 40));

        airline.setBackground(new java.awt.Color(0, 0, 0));
        airline.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        airline.setText("Philippine Airlines");
        airlinePanel.add(airline, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 0, 230, 40));

        lastDepartedPanel.add(airlinePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 310, 400, 40));

        departureDatePanel.setBackground(new java.awt.Color(255, 255, 255));
        departureDatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(153, 153, 153))); // NOI18N
        departureDatePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        departureDateLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        departureDateLabel.setForeground(new java.awt.Color(60, 63, 65));
        departureDateLabel.setText("Departed Date:");
        departureDatePanel.add(departureDateLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 150, 40));

        departureDate.setBackground(new java.awt.Color(0, 0, 0));
        departureDate.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        departureDate.setText("12/27/2025");
        departureDatePanel.add(departureDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 0, 230, 40));

        lastDepartedPanel.add(departureDatePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 360, 400, 40));

        departureTimePanel.setBackground(new java.awt.Color(255, 255, 255));
        departureTimePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(153, 153, 153))); // NOI18N
        departureTimePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        departureTimeLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        departureTimeLabel.setForeground(new java.awt.Color(60, 63, 65));
        departureTimeLabel.setText("Departed Time:");
        departureTimePanel.add(departureTimeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 150, 40));

        departureTime.setBackground(new java.awt.Color(0, 0, 0));
        departureTime.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        departureTime.setText("16:38 UTC");
        departureTimePanel.add(departureTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 0, 230, 40));

        lastDepartedPanel.add(departureTimePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 410, 400, 40));

        lastDepartedLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        lastDepartedLabel.setForeground(new java.awt.Color(60, 63, 65));
        lastDepartedLabel.setText("LAST DEPARTED FLIGHT");
        lastDepartedPanel.add(lastDepartedLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 30));

        myDashboardPanel.add(lastDepartedPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 440, 510));

        myRecentBookingsPanel.setBackground(new java.awt.Color(255, 255, 255));
        myRecentBookingsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(11, 57, 118), 2));
        myRecentBookingsPanel.setForeground(new java.awt.Color(255, 255, 255));
        myRecentBookingsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        myRecentBookingLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        myRecentBookingLabel.setForeground(new java.awt.Color(60, 63, 65));
        myRecentBookingLabel.setText("MY RECENT BOOKINGS");
        myRecentBookingsPanel.add(myRecentBookingLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 30));

        recentBookingTable.setForeground(new java.awt.Color(255, 255, 255));
        recentBookingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Flight No.", "Route", "Date", "Status"
            }
        ));
        recentBookingScrollPane.setViewportView(recentBookingTable);

        myRecentBookingsPanel.add(recentBookingScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 420, 160));

        viewAllBookingsButton.setBackground(new java.awt.Color(11, 56, 118));
        viewAllBookingsButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        viewAllBookingsButton.setForeground(new java.awt.Color(255, 255, 255));
        viewAllBookingsButton.setText("View All Bookings");
        viewAllBookingsButton.setBorder(null);
        viewAllBookingsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewAllBookingsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        viewAllBookingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewAllBookingsButtonActionPerformed(evt);
            }
        });
        myRecentBookingsPanel.add(viewAllBookingsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 210, 170, 30));

        myDashboardPanel.add(myRecentBookingsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 30, 440, 250));

        advertisementPanel.setBackground(new java.awt.Color(255, 255, 255));
        advertisementPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(11, 57, 118), 2));
        advertisementPanel.setForeground(new java.awt.Color(255, 255, 255));
        advertisementPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        promotionalPanel.setBackground(new java.awt.Color(11, 56, 118));
        promotionalPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        textLabel.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        textLabel.setForeground(new java.awt.Color(255, 255, 255));
        textLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textLabel.setText("Exclusive offers and travel updates for you   ");
        promotionalPanel.add(textLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 420, 40));

        alsoTextLabel.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        alsoTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        alsoTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        alsoTextLabel.setText("PROMOTIONS & ANNOUNCEMENTS");
        promotionalPanel.add(alsoTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 420, 40));

        advertisementPanel.add(promotionalPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 420, 60));

        promoPanel.setBackground(new java.awt.Color(255, 255, 255));
        promoPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        promoPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        promotional1Label.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        promotional1Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        promotional1Label.setText("Use Voucher Code: UP500  ");
        promoPanel.add(promotional1Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 420, 30));

        promotional2Label.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        promotional2Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        promotional2Label.setText("✈ LIMITED-TIME FLIGHT DEAL  ");
        promoPanel.add(promotional2Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 420, 20));

        promotional3Label.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        promotional3Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        promotional3Label.setText("Valid until: Dec 31, 2025");
        promoPanel.add(promotional3Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 420, 20));

        promotional4Label.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        promotional4Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        promotional4Label.setText("Get ₱500 OFF on your next booking");
        promoPanel.add(promotional4Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 420, 20));

        viewTermsLabel.setBackground(new java.awt.Color(11, 56, 118));
        viewTermsLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        viewTermsLabel.setForeground(new java.awt.Color(255, 255, 255));
        viewTermsLabel.setText("View Terms");
        viewTermsLabel.setBorder(null);
        viewTermsLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewTermsLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        viewTermsLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewTermsLabelActionPerformed(evt);
            }
        });
        promoPanel.add(viewTermsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 130, 170, 30));

        redeemButton.setBackground(new java.awt.Color(11, 56, 118));
        redeemButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        redeemButton.setForeground(new java.awt.Color(255, 255, 255));
        redeemButton.setText("Redeem Voucher");
        redeemButton.setBorder(null);
        redeemButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        redeemButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        redeemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redeemButtonActionPerformed(evt);
            }
        });
        promoPanel.add(redeemButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 170, 30));

        advertisementPanel.add(promoPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 420, 170));

        myDashboardPanel.add(advertisementPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 290, 440, 250));

        dashboardPanel.add(myDashboardPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, 910, 550));

        refreshButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        refreshButton.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refreshButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        dashboardPanel.add(refreshButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 10, -1, 60));

        userDashboardPanel.add(dashboardPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 950, -1));

        userTabbedPanel.addTab("tab5", userDashboardPanel);

        flightOverviewPanel.setBackground(new java.awt.Color(204, 204, 204));
        flightOverviewPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        flightOverviewHeaderPanel.setBackground(new java.awt.Color(204, 204, 204));
        flightOverviewHeaderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        flightOverviewHeaderPanel.setForeground(new java.awt.Color(255, 255, 255));
        flightOverviewHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        flightOverviewHeaderLabel.setFont(new java.awt.Font("Bahnschrift", 1, 36)); // NOI18N
        flightOverviewHeaderLabel.setForeground(new java.awt.Color(60, 63, 65));
        flightOverviewHeaderLabel.setText("FLIGHT OVERVIEW");
        flightOverviewHeaderLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        flightOverviewHeaderPanel.add(flightOverviewHeaderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 510, 60));

        refreshButton1.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        refreshButton1.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton1.setBorderPainted(false);
        refreshButton1.setContentAreaFilled(false);
        refreshButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refreshButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        refreshButton1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        refreshButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButton1ActionPerformed(evt);
            }
        });
        flightOverviewHeaderPanel.add(refreshButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 0, -1, 50));

        flightOverviewPanel.add(flightOverviewHeaderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 910, 50));

        availableFlightsPanel.setBackground(new java.awt.Color(255, 255, 255));
        availableFlightsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Available Flights", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        availableFlightsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        selectDateLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        selectDateLabel.setText("Select Date:");
        availableFlightsPanel.add(selectDateLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 90, 20));

        originLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        originLabel.setText("Origin");
        availableFlightsPanel.add(originLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 40, -1, -1));

        destinationLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        destinationLabel.setText("Destination");
        availableFlightsPanel.add(destinationLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 40, -1, -1));

        dateChooser.setBackground(new java.awt.Color(255, 255, 255));
        availableFlightsPanel.add(dateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 200, 40));

        destinationComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        availableFlightsPanel.add(destinationComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 60, 200, 40));

        originComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        availableFlightsPanel.add(originComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 60, 200, 40));

        searchFlightButton.setBackground(new java.awt.Color(5, 20, 42));
        searchFlightButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        searchFlightButton.setForeground(new java.awt.Color(255, 255, 255));
        searchFlightButton.setText("Search Flight");
        searchFlightButton.setBorder(null);
        searchFlightButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        searchFlightButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        searchFlightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFlightButtonActionPerformed(evt);
            }
        });
        availableFlightsPanel.add(searchFlightButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 60, 180, 40));

        flightOverviewTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Airline", "Flight Code", "Origin", "Destination", "Time", "Status"
            }
        ));
        availableFlightsScrollPane.setViewportView(flightOverviewTable);

        availableFlightsPanel.add(availableFlightsScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 860, 520));

        flightOverviewPanel.add(availableFlightsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 910, 650));

        userTabbedPanel.addTab("tab1", flightOverviewPanel);

        bookFlightPanel.setBackground(new java.awt.Color(153, 153, 153));
        bookFlightPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bookFlightHeaderPanel.setBackground(new java.awt.Color(204, 204, 204));
        bookFlightHeaderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        bookFlightHeaderPanel.setForeground(new java.awt.Color(255, 255, 255));
        bookFlightHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bookFlightHeaderLabel.setFont(new java.awt.Font("Bahnschrift", 1, 36)); // NOI18N
        bookFlightHeaderLabel.setForeground(new java.awt.Color(60, 63, 65));
        bookFlightHeaderLabel.setText("BOOK FLIGHT");
        bookFlightHeaderLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bookFlightHeaderPanel.add(bookFlightHeaderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 510, 60));

        refreshButton2.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        refreshButton2.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton2.setBorderPainted(false);
        refreshButton2.setContentAreaFilled(false);
        refreshButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refreshButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        refreshButton2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        refreshButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButton2ActionPerformed(evt);
            }
        });
        bookFlightHeaderPanel.add(refreshButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 0, -1, 50));

        bookFlightPanel.add(bookFlightHeaderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 910, 50));

        availableFlightsPanel1.setBackground(new java.awt.Color(204, 204, 204));
        availableFlightsPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Book Flight", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        availableFlightsPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        totalPrice.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        totalPrice.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        totalPrice.setText("PHP 00.00");
        totalPrice.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        availableFlightsPanel1.add(totalPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 180, 150, 40));

        bookflightDateChooser.setBackground(new java.awt.Color(255, 255, 255));
        availableFlightsPanel1.add(bookflightDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 100, 180, 40));

        bookFlightResultTable.setModel(new javax.swing.table.DefaultTableModel(
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
        availableFlightsScrollPane1.setViewportView(bookFlightResultTable);

        availableFlightsPanel1.add(availableFlightsScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, 550, 350));

        dateLabelBookFlight.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        dateLabelBookFlight.setText("Date");
        availableFlightsPanel1.add(dateLabelBookFlight, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 80, 40, 20));

        roundTripType.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        roundTripType.setText("Round Trip");
        availableFlightsPanel1.add(roundTripType, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 40, -1, -1));

        oneWayType.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        oneWayType.setText("One Way");
        availableFlightsPanel1.add(oneWayType, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, -1));

        destinationLabelBookFlight.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        destinationLabelBookFlight.setText("Destination");
        availableFlightsPanel1.add(destinationLabelBookFlight, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 80, 80, 20));

        destinationToBook.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        availableFlightsPanel1.add(destinationToBook, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 100, 160, 40));

        originToBook.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        availableFlightsPanel1.add(originToBook, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 160, 40));

        originToLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        originToLabel.setText("Origin");
        availableFlightsPanel1.add(originToLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, 80, 20));

        adultLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        adultLabel.setText("Adult");
        availableFlightsPanel1.add(adultLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 160, 50, 20));
        availableFlightsPanel1.add(minorCounter, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 180, 120, 40));
        availableFlightsPanel1.add(adultCounter, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, 120, 40));

        minorCounterLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        minorCounterLabel.setText("Minor");
        availableFlightsPanel1.add(minorCounterLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 160, 50, 20));

        totalPriceLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        totalPriceLabel.setText("Payment");
        availableFlightsPanel1.add(totalPriceLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 160, 60, 20));

        bookButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        bookButton.setForeground(new java.awt.Color(5, 20, 42));
        bookButton.setText("Book");
        bookButton.setBorder(null);
        bookButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bookButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bookButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookButtonActionPerformed(evt);
            }
        });
        availableFlightsPanel1.add(bookButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 610, 100, 30));

        searchBookFlightButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        searchBookFlightButton.setForeground(new java.awt.Color(5, 20, 42));
        searchBookFlightButton.setText("Search");
        searchBookFlightButton.setBorder(null);
        searchBookFlightButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        searchBookFlightButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        searchBookFlightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBookFlightButtonActionPerformed(evt);
            }
        });
        availableFlightsPanel1.add(searchBookFlightButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 610, 100, 30));

        totalPriceLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        totalPriceLabel1.setText("Total Price:");
        availableFlightsPanel1.add(totalPriceLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 160, 90, 20));

        paymentMethod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        availableFlightsPanel1.add(paymentMethod, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 180, 90, 40));

        bookFlightPanel.add(availableFlightsPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 590, 650));

        seatsPanell.setBackground(new java.awt.Color(204, 204, 204));
        seatsPanell.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        businessClassSeats2.setBackground(new java.awt.Color(255, 255, 255));
        businessClassSeats2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        businessClassSeats2.setLayout(new java.awt.GridLayout(10, 2, 3, 3));
        seatsPanell.add(businessClassSeats2, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 50, 130, 610));

        economyClassSeats2.setBackground(new java.awt.Color(255, 255, 255));
        economyClassSeats2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        economyClassSeats2.setLayout(new java.awt.GridLayout(12, 3, 3, 3));
        seatsPanell.add(economyClassSeats2, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 710, 140, 610));

        businessClassSeats1.setBackground(new java.awt.Color(255, 255, 255));
        businessClassSeats1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        businessClassSeats1.setLayout(new java.awt.GridLayout(10, 2, 3, 3));
        seatsPanell.add(businessClassSeats1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 130, 610));

        economyClassSeats1.setBackground(new java.awt.Color(255, 255, 255));
        economyClassSeats1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        economyClassSeats1.setLayout(new java.awt.GridLayout(12, 3, 3, 3));
        seatsPanell.add(economyClassSeats1, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 1350, 140, 610));

        economyClassSeats3.setBackground(new java.awt.Color(255, 255, 255));
        economyClassSeats3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        economyClassSeats3.setLayout(new java.awt.GridLayout(12, 3, 3, 3));
        seatsPanell.add(economyClassSeats3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 710, 140, 610));

        economyClassSeats4.setBackground(new java.awt.Color(255, 255, 255));
        economyClassSeats4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        economyClassSeats4.setLayout(new java.awt.GridLayout(12, 3, 3, 3));
        seatsPanell.add(economyClassSeats4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 1350, 140, 610));

        seatsSelector.setViewportView(seatsPanell);

        bookFlightPanel.add(seatsSelector, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 80, 320, 650));

        userTabbedPanel.addTab("tab2", bookFlightPanel);

        bookingStatusPanel.setBackground(new java.awt.Color(204, 204, 204));
        bookingStatusPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bookingStatusHeaderPanel.setBackground(new java.awt.Color(204, 204, 204));
        bookingStatusHeaderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        bookingStatusHeaderPanel.setForeground(new java.awt.Color(255, 255, 255));
        bookingStatusHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bookingStatusHeaderLabel.setFont(new java.awt.Font("Bahnschrift", 1, 36)); // NOI18N
        bookingStatusHeaderLabel.setForeground(new java.awt.Color(60, 63, 65));
        bookingStatusHeaderLabel.setText("BOOKING STATUS");
        bookingStatusHeaderLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bookingStatusHeaderPanel.add(bookingStatusHeaderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 510, 60));

        refreshButton3.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        refreshButton3.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton3.setBorderPainted(false);
        refreshButton3.setContentAreaFilled(false);
        refreshButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refreshButton3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        refreshButton3.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        refreshButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButton3ActionPerformed(evt);
            }
        });
        bookingStatusHeaderPanel.add(refreshButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 0, -1, 50));

        bookingStatusPanel.add(bookingStatusHeaderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 910, 50));

        availableFlightsPanel2.setBackground(new java.awt.Color(204, 204, 204));
        availableFlightsPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "My Bookings", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        availableFlightsPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bookingStatusRecord.setBackground(new java.awt.Color(255, 255, 255));
        bookingStatusRecord.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        bookingStatusRecord.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        statusFlight.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        statusFlight.setText("Upcoming Flight");
        bookingStatusRecord.add(statusFlight, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 0, 150, 30));

        selectDateLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel12.setText("Status:");
        bookingStatusRecord.add(selectDateLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 110, 60, 30));

        bookingID.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bookingID.setText("BK000002");
        bookingStatusRecord.add(bookingID, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 0, 120, 30));

        paymentStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        paymentStatus.setText("Paid");
        bookingStatusRecord.add(paymentStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 110, 70, 30));

        selectDateLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel15.setText("Route:");
        bookingStatusRecord.add(selectDateLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 90, 30));

        selectDateLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel16.setText("Departure:");
        bookingStatusRecord.add(selectDateLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, 90, 30));

        selectDateLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel17.setText("Passengers:");
        bookingStatusRecord.add(selectDateLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 90, 90, 30));

        selectDateLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel18.setText("Payment:");
        bookingStatusRecord.add(selectDateLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 90, 30));

        selectDateLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel19.setText("Flight Code:");
        bookingStatusRecord.add(selectDateLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, 90, 30));

        totalPriceBooked.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        totalPriceBooked.setText("PHP 3,091.00 ");
        bookingStatusRecord.add(totalPriceBooked, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 130, 90, 30));

        paymentType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        paymentType.setText("Online");
        bookingStatusRecord.add(paymentType, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 110, 90, 30));

        seatBooked.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        seatBooked.setText("A1");
        bookingStatusRecord.add(seatBooked, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 90, 90, 30));

        flightCode.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        flightCode.setText("JK123");
        bookingStatusRecord.add(flightCode, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 30, 90, 30));

        routeDetails.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        routeDetails.setText("MNL > DRP");
        bookingStatusRecord.add(routeDetails, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 50, 90, 30));

        selectDateLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel25.setText("Airline:");
        bookingStatusRecord.add(selectDateLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 30, 90, 30));

        selectDateLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel26.setText("Type:");
        bookingStatusRecord.add(selectDateLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 50, 90, 30));

        flightStatusBooked.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        flightStatusBooked.setText("Pending");
        bookingStatusRecord.add(flightStatusBooked, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 120, 100, 30));

        airlineType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        airlineType.setText("Philippine Airlines");
        bookingStatusRecord.add(airlineType, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 30, 180, 30));

        timeBooked.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        timeBooked.setText("Dec 10, 2025, 13:23");
        bookingStatusRecord.add(timeBooked, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 70, 170, 30));

        flightTicketType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        flightTicketType.setText("One Way");
        bookingStatusRecord.add(flightTicketType, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 50, 90, 30));

        selectDateLabel31.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel31.setText("Time Booked:");
        bookingStatusRecord.add(selectDateLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 70, 100, 30));

        selectDateLabel32.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        selectDateLabel32.setText("Status:");
        bookingStatusRecord.add(selectDateLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 120, 100, 30));

        cancelBookingButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        cancelBookingButton.setForeground(new java.awt.Color(255, 0, 0));
        cancelBookingButton.setText("CANCEL BOOKING");
        cancelBookingButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        cancelBookingButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cancelBookingButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cancelBookingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBookingButtonActionPerformed(evt);
            }
        });
        bookingStatusRecord.add(cancelBookingButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 180, 140, 30));

        downloadTicketButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        downloadTicketButton.setForeground(new java.awt.Color(5, 20, 42));
        downloadTicketButton.setText("DOWNLOAD TICKET");
        downloadTicketButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        downloadTicketButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        downloadTicketButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        downloadTicketButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadTicketButtonActionPerformed(evt);
            }
        });
        bookingStatusRecord.add(downloadTicketButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 180, 130, 30));

        selectDateLabel33.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        selectDateLabel33.setText("Booking ID:");
        bookingStatusRecord.add(selectDateLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 110, 30));

        selectDateLabel34.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel34.setText("Seat:");
        bookingStatusRecord.add(selectDateLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, 90, 30));

        selectDateLabel35.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectDateLabel35.setText("Total Ticket Price:");
        bookingStatusRecord.add(selectDateLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, 140, 30));

        departureDateTime.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        departureDateTime.setText("Dec 17, 2025, 14:30");
        bookingStatusRecord.add(departureDateTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 70, 140, 30));

        passengerType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        passengerType.setText("1 Adult");
        bookingStatusRecord.add(passengerType, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 90, 140, 30));

        jPanel1.add(bookingStatusRecord, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 830, 220));

        availableFlightsPanel2.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 870, 570));

        nextButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        nextButton.setForeground(new java.awt.Color(5, 20, 42));
        nextButton.setText("Next");
        nextButton.setBorder(null);
        nextButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        nextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        availableFlightsPanel2.add(nextButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 610, 100, 30));

        previousButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        previousButton.setForeground(new java.awt.Color(5, 20, 42));
        previousButton.setText("Previous");
        previousButton.setBorder(null);
        previousButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        previousButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });
        availableFlightsPanel2.add(previousButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 610, 100, 30));

        bookingStatusPanel.add(availableFlightsPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 910, 650));

        userTabbedPanel.addTab("tab3", bookingStatusPanel);

        airlineComparisonPanel.setBackground(new java.awt.Color(204, 204, 204));
        airlineComparisonPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        airlineComparisonHeaderPanel.setBackground(new java.awt.Color(204, 204, 204));
        airlineComparisonHeaderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        airlineComparisonHeaderPanel.setForeground(new java.awt.Color(255, 255, 255));
        airlineComparisonHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        airlineComparisonHeaderLabel.setFont(new java.awt.Font("Bahnschrift", 1, 36)); // NOI18N
        airlineComparisonHeaderLabel.setForeground(new java.awt.Color(60, 63, 65));
        airlineComparisonHeaderLabel.setText("AIRLINE COMPARISON");
        airlineComparisonHeaderLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        airlineComparisonHeaderPanel.add(airlineComparisonHeaderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 510, 60));

        refreshButton4.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        refreshButton4.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton4.setBorderPainted(false);
        refreshButton4.setContentAreaFilled(false);
        refreshButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refreshButton4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        refreshButton4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        refreshButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButton4ActionPerformed(evt);
            }
        });
        airlineComparisonHeaderPanel.add(refreshButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 0, -1, 50));

        airlineComparisonPanel.add(airlineComparisonHeaderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 910, 50));

        availableFlightsPanel3.setBackground(new java.awt.Color(204, 204, 204));
        availableFlightsPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Compare Prices", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 15))); // NOI18N
        availableFlightsPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        resultCompareLabel.setBackground(new java.awt.Color(255, 255, 255));
        resultCompareLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        resultCompareLabel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        resultShow.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        resultShow.setText("Showing result:");
        resultCompareLabel.add(resultShow, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, 120, -1));

        result1Panel.setBackground(new java.awt.Color(255, 255, 255));
        result1Panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        result1Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        flightType.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        flightType.setText("One-way");
        result1Panel.add(flightType, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 70, 140, -1));

        airlineName.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        airlineName.setText("AIR ASIA");
        result1Panel.add(airlineName, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 140, -1));

        selectDateLabel127.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        selectDateLabel127.setText("MNL > CEB");
        result1Panel.add(selectDateLabel127, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 140, -1));

        flightDuration.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        flightDuration.setText("1h: 23m");
        result1Panel.add(flightDuration, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 70, 140, -1));

        flightDate.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        flightDate.setText("Dec 19, 2025 ");
        result1Panel.add(flightDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 140, -1));

        airlineRating.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        airlineRating.setText("Ratings: 4.4");
        result1Panel.add(airlineRating, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 40, 140, -1));

        price1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        price1.setText("PHP 2,090.00");
        result1Panel.add(price1, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, 140, -1));

        bookInThisAirlineButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bookInThisAirlineButton.setForeground(new java.awt.Color(5, 20, 42));
        bookInThisAirlineButton.setText("BOOK FLIGHT");
        bookInThisAirlineButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        bookInThisAirlineButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bookInThisAirlineButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bookInThisAirlineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookInThisAirlineButtonActionPerformed(evt);
            }
        });
        result1Panel.add(bookInThisAirlineButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 60, 130, 30));

        viewDetailsButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        viewDetailsButton.setForeground(new java.awt.Color(5, 20, 42));
        viewDetailsButton.setText("VIEW DETAILS");
        viewDetailsButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        viewDetailsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewDetailsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        viewDetailsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDetailsButtonActionPerformed(evt);
            }
        });
        result1Panel.add(viewDetailsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 20, 130, 30));

        resultCompareLabel.add(result1Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 830, 110));

        result2Panel.setBackground(new java.awt.Color(255, 255, 255));
        result2Panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        result2Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        flightType1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        flightType1.setText("One-way");
        result2Panel.add(flightType1, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 70, 140, -1));

        airlineName1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        airlineName1.setText("CEBU PACIFIC");
        result2Panel.add(airlineName1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 140, -1));

        selectDateLabel128.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        selectDateLabel128.setText("MNL > CEB");
        result2Panel.add(selectDateLabel128, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 140, -1));

        flightDuration1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        flightDuration1.setText("1h: 20m");
        result2Panel.add(flightDuration1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 70, 140, -1));

        flightDate1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        flightDate1.setText("Dec 19, 2025 ");
        result2Panel.add(flightDate1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 140, -1));

        airlineRating1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        airlineRating1.setText("Ratings: 4.5");
        result2Panel.add(airlineRating1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 40, 140, -1));

        price2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        price2.setText("PHP 2,190.00");
        result2Panel.add(price2, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, 140, -1));

        bookInThisAirlineButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bookInThisAirlineButton1.setForeground(new java.awt.Color(5, 20, 42));
        bookInThisAirlineButton1.setText("BOOK FLIGHT");
        bookInThisAirlineButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        bookInThisAirlineButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bookInThisAirlineButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bookInThisAirlineButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookInThisAirlineButton1ActionPerformed(evt);
            }
        });
        result2Panel.add(bookInThisAirlineButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 60, 130, 30));

        viewDetailsButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        viewDetailsButton1.setForeground(new java.awt.Color(5, 20, 42));
        viewDetailsButton1.setText("VIEW DETAILS");
        viewDetailsButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        viewDetailsButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewDetailsButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        viewDetailsButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDetailsButton1ActionPerformed(evt);
            }
        });
        result2Panel.add(viewDetailsButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 20, 130, 30));

        resultCompareLabel.add(result2Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 830, 110));

        result3Panel.setBackground(new java.awt.Color(255, 255, 255));
        result3Panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        result3Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        flightType2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        flightType2.setText("One-way");
        result3Panel.add(flightType2, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 70, 140, -1));

        airlineName2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        airlineName2.setText("PHILIPPINE AIRLINES");
        result3Panel.add(airlineName2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 210, -1));

        selectDateLabel129.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        selectDateLabel129.setText("MNL > CEB");
        result3Panel.add(selectDateLabel129, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 140, -1));

        flightDuration2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        flightDuration2.setText("1h: 10m");
        result3Panel.add(flightDuration2, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 70, 140, -1));

        flightDate2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        flightDate2.setText("Dec 19, 2025 ");
        result3Panel.add(flightDate2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 140, -1));

        airlineRating2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        airlineRating2.setText("Ratings: 4.8");
        result3Panel.add(airlineRating2, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 40, 140, -1));

        price3.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        price3.setText("PHP 2,250.00");
        result3Panel.add(price3, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, 140, -1));

        bookInThisAirlineButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bookInThisAirlineButton2.setForeground(new java.awt.Color(5, 20, 42));
        bookInThisAirlineButton2.setText("BOOK FLIGHT");
        bookInThisAirlineButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        bookInThisAirlineButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bookInThisAirlineButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bookInThisAirlineButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookInThisAirlineButton2ActionPerformed(evt);
            }
        });
        result3Panel.add(bookInThisAirlineButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 60, 130, 30));

        viewDetailsButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        viewDetailsButton2.setForeground(new java.awt.Color(5, 20, 42));
        viewDetailsButton2.setText("VIEW DETAILS");
        viewDetailsButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        viewDetailsButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewDetailsButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        viewDetailsButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDetailsButton2ActionPerformed(evt);
            }
        });
        result3Panel.add(viewDetailsButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 20, 130, 30));

        resultCompareLabel.add(result3Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 830, 110));

        availableFlightsPanel3.add(resultCompareLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 870, 460));

        dateLabelCompareLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        dateLabelCompareLabel.setText("Date:");
        availableFlightsPanel3.add(dateLabelCompareLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 30, 40, 20));

        selectOrderLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectOrderLabel.setText("Order:");
        availableFlightsPanel3.add(selectOrderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 90, 50, 20));

        order.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        availableFlightsPanel3.add(order, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 110, 170, 30));

        sort.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        availableFlightsPanel3.add(sort, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 110, 170, 30));

        selectToLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectToLabel.setText("To:");
        availableFlightsPanel3.add(selectToLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 30, 30, 20));
        availableFlightsPanel3.add(dateChooserCompare, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 50, 170, 30));

        selectSortLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        selectSortLabel.setText("Sort:");
        availableFlightsPanel3.add(selectSortLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 90, 40, 20));

        from.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        availableFlightsPanel3.add(from, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 50, 170, 30));

        to.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        availableFlightsPanel3.add(to, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 50, 170, 30));

        applyFiltersButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        applyFiltersButton.setForeground(new java.awt.Color(5, 20, 42));
        applyFiltersButton.setText("APPLY");
        applyFiltersButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        applyFiltersButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        applyFiltersButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        applyFiltersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyFiltersButtonActionPerformed(evt);
            }
        });
        availableFlightsPanel3.add(applyFiltersButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 100, 170, 40));

        fromLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        fromLabel.setText("From:");
        availableFlightsPanel3.add(fromLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 30, 40, 20));

        airlineComparisonPanel.add(availableFlightsPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 910, 650));

        userTabbedPanel.addTab("tab4", airlineComparisonPanel);

        userPanel.add(userTabbedPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 21, 950, 780));

        getContentPane().add(userPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1200, 800));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void viewTermsLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewTermsLabelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_viewTermsLabelActionPerformed

    private void viewAllBookingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewAllBookingsButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_viewAllBookingsButtonActionPerformed

    private void redeemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redeemButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_redeemButtonActionPerformed

    private void userProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userProfileButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_userProfileButtonActionPerformed

    private void userDashboardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userDashboardButtonActionPerformed
        // Dashboard panel is at index 0
        userTabbedPanel.setSelectedIndex(0);
        // Refresh dashboard data if needed
    }//GEN-LAST:event_userDashboardButtonActionPerformed

    private void refreshButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButton1ActionPerformed
        populateFlightOverview();
    }//GEN-LAST:event_refreshButton1ActionPerformed

    private void searchFlightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFlightButtonActionPerformed
        // Validate all filters are filled
        String origin = (String) originComboBox.getSelectedItem();
        String destination = (String) destinationComboBox.getSelectedItem();
        Date selectedDate = dateChooser.getDate();
        
        if (origin == null || origin.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an origin!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (destination == null || destination.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a destination!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a date!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        LocalDate filterDate = null;
        if (selectedDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(selectedDate);
            filterDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        }
        
        // Filter schedules
        DefaultTableModel model = (DefaultTableModel) flightOverviewTable.getModel();
        model.setRowCount(0);
        
        List<FlightStatusService.ScheduleWithStatus> filtered = FlightStatusService.getFilteredSchedules(
            filterDate, 
            origin, 
            destination
        );
        
        for (FlightStatusService.ScheduleWithStatus sws : filtered) {
            Schedule schedule = sws.getSchedule();
            model.addRow(new Object[]{
                schedule.getAirline(),
                schedule.getFlightCode(),
                schedule.getOrigin(),
                schedule.getDestination(),
                schedule.getDepartureDate().toString(),
                schedule.getDepartureTime().toString(),
                sws.getStatus()
            });
        }
        
        // Show message if no flights
        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"", "", "", "", "", "", "No available flights"});
            flightOverviewTable.setEnabled(false);
        } else {
            flightOverviewTable.setEnabled(true);
        }
    }//GEN-LAST:event_searchFlightButtonActionPerformed

    private void refreshButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButton2ActionPerformed
        populateAvailableFlightsForBooking();
        clearBookingForm();
    }//GEN-LAST:event_refreshButton2ActionPerformed

    private void bookButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookButtonActionPerformed
        if (selectedSchedule == null) {
            JOptionPane.showMessageDialog(this, "Please select a flight first!", 
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int totalPassengers = (Integer) adultCounter.getValue() + (Integer) minorCounter.getValue();
        if (selectedSeats.size() != totalPassengers) {
            JOptionPane.showMessageDialog(this, 
                "Please select seats for all passengers! (" + totalPassengers + " seats needed)", 
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create booking
        Booking booking = new Booking();
        booking.setBookingId(BookingRepository.generateBookingId());
        booking.setPassengerUsername(currentUsername);
        booking.setTripType(isRoundTrip ? "Round Trip" : "One Way");
        booking.setFlightCode(selectedSchedule.getFlightCode());
        booking.setOrigin(selectedSchedule.getOrigin());
        booking.setDestination(selectedSchedule.getDestination());
        booking.setDepartureDate(selectedSchedule.getDepartureDate());
        booking.setDepartureTime(selectedSchedule.getDepartureTime());
        booking.setNumberOfAdults((Integer) adultCounter.getValue());
        booking.setNumberOfMinors((Integer) minorCounter.getValue());
        booking.setReservedSeats(new ArrayList<>(selectedSeats));
        booking.setPassengerNames(new ArrayList<>(seatToPassengerName.values()));
        booking.setTotalPrice(Double.parseDouble(totalPrice.getText().replace("PHP ", "").replace(",", "")));
        
        // Get payment method
        String paymentMethodValue = (String) paymentMethod.getSelectedItem();
        if (paymentMethodValue != null && paymentMethodValue.equalsIgnoreCase("Cash")) {
            booking.setStatus("Pay at the counter");
        } else {
            booking.setStatus("Confirmed"); // Online payments are automatically confirmed
        }
        booking.setPaymentType(paymentMethodValue != null ? paymentMethodValue : "Online");
        
        // For round trip, we need to find return flight
        if (isRoundTrip) {
            // Find return flight (destination to origin)
            List<FlightStatusService.ScheduleWithStatus> returnFlights = 
                FlightStatusService.getScheduledFlightsOnly();
            
            Schedule returnSchedule = null;
            for (FlightStatusService.ScheduleWithStatus sws : returnFlights) {
                Schedule s = sws.getSchedule();
                if (s.getOrigin().equals(selectedSchedule.getDestination()) &&
                    s.getDestination().equals(selectedSchedule.getOrigin()) &&
                    s.getDepartureDate().isAfter(selectedSchedule.getDepartureDate())) {
                    returnSchedule = s;
                    break;
                }
            }
            
            if (returnSchedule == null) {
                JOptionPane.showMessageDialog(this, 
                    "No return flight found for the selected dates!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            booking.setReturnFlightCode(returnSchedule.getFlightCode());
            booking.setReturnDate(returnSchedule.getDepartureDate());
            booking.setReturnTime(returnSchedule.getDepartureTime());
        }
        
        // Save booking
        boolean saved = BookingRepository.saveBooking(booking);
        if (saved) {
            // Generate receipt
            String receiptPath = generateReceipt(booking);
            
            // Show receipt panel
            showReceiptPanel(receiptPath, booking);
            
            JOptionPane.showMessageDialog(this, 
                "Booking confirmed! Booking ID: " + booking.getBookingId() + "\nReceipt generated!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            clearBookingForm();
            populateAvailableFlightsForBooking();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to create booking! Please try again.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_bookButtonActionPerformed

    private void flightOverviewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flightOverviewButtonActionPerformed
        // Flight Overview panel is at index 1
        userTabbedPanel.setSelectedIndex(1);
        populateFlightOverview();
    }//GEN-LAST:event_flightOverviewButtonActionPerformed

    private void bookFlightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookFlightButtonActionPerformed
        // Book Flight panel is at index 2
        userTabbedPanel.setSelectedIndex(2);
        populateAvailableFlightsForBooking();
    }//GEN-LAST:event_bookFlightButtonActionPerformed

    private void searchBookFlightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBookFlightButtonActionPerformed
        // Validate form inputs
        String origin = (String) originToBook.getSelectedItem();
        String destination = (String) destinationToBook.getSelectedItem();
        Date selectedDate = bookflightDateChooser.getDate();
        
        if (origin == null || origin.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an origin!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (destination == null || destination.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a destination!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a departure date!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Search for available flights
        populateAvailableFlightsForBooking();
    }//GEN-LAST:event_searchBookFlightButtonActionPerformed

    private void bookingStatusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookingStatusButtonActionPerformed
        // Booking Status panel is at index 3
        userTabbedPanel.setSelectedIndex(3);
        // TODO: Load booking status data when this panel is implemented
    }//GEN-LAST:event_bookingStatusButtonActionPerformed

    private void airlineComparisonButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_airlineComparisonButtonActionPerformed
        // Airline Comparison panel is at index 4
        userTabbedPanel.setSelectedIndex(4);
        // TODO: Load airline comparison data when this panel is implemented
    }//GEN-LAST:event_airlineComparisonButtonActionPerformed
    
    /**
     * Clears seat panels
     */
    private void clearSeatPanels() {
        businessClassSeats1.removeAll();
        businessClassSeats2.removeAll();
        economyClassSeats1.removeAll();
        economyClassSeats2.removeAll();
        economyClassSeats3.removeAll();
        economyClassSeats4.removeAll();
        businessClassSeats1.revalidate();
        businessClassSeats1.repaint();
        businessClassSeats2.revalidate();
        businessClassSeats2.repaint();
        economyClassSeats1.revalidate();
        economyClassSeats1.repaint();
        economyClassSeats2.revalidate();
        economyClassSeats2.repaint();
        economyClassSeats3.revalidate();
        economyClassSeats3.repaint();
        economyClassSeats4.revalidate();
        economyClassSeats4.repaint();
    }
    
    /**
     * Handles flight selection in booking table - shows price and seats
     */
    private void handleFlightSelectionForBooking() {
        int selectedRow = bookFlightResultTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        
        // Check if it's the "No available flights" row
        DefaultTableModel model = (DefaultTableModel) bookFlightResultTable.getModel();
        if (selectedRow < model.getRowCount()) {
            Object lastColumnValue = model.getValueAt(selectedRow, model.getColumnCount() - 1);
            if (lastColumnValue != null && lastColumnValue.toString().equals("No available flights")) {
                return;
            }
        }
        
        String flightCode = (String) bookFlightResultTable.getValueAt(selectedRow, 1);
        String airline = (String) bookFlightResultTable.getValueAt(selectedRow, 0);
        LocalDate date = LocalDate.parse((String) bookFlightResultTable.getValueAt(selectedRow, 4));
        
        // Get schedule
        List<Schedule> schedules = ScheduleService.getAllSchedules();
        Schedule schedule = null;
        for (Schedule s : schedules) {
            if (s.getFlightCode().equals(flightCode) && s.getDepartureDate().equals(date)) {
                schedule = s;
                selectedSchedule = s;
                break;
            }
        }
        
        if (schedule == null) {
            JOptionPane.showMessageDialog(this, "Schedule not found!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get flight details
        Flight flight = ScheduleService.getFlightByCode(flightCode);
        if (flight == null) {
            JOptionPane.showMessageDialog(this, "Flight details not found!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Calculate and display price
        updatePriceForSelectedFlight(flight, schedule.getDepartureDate());
        
        // Generate seat buttons
        generateSeatButtons();
    }
    
    /**
     * Updates price when passenger counters change
     */
    private void updatePriceIfFlightSelected() {
        if (selectedSchedule == null) {
            return;
        }
        
        Flight flight = ScheduleService.getFlightByCode(selectedSchedule.getFlightCode());
        if (flight != null) {
            updatePriceForSelectedFlight(flight, selectedSchedule.getDepartureDate());
        }
    }
    
    /**
     * Updates price for selected flight
     */
    private void updatePriceForSelectedFlight(Flight flight, LocalDate date) {
        // Get flight offer
        FlightOffer offer = FlightRepository.getOfferByFlightCode(flight.getFlightCode());
        
        // Get passenger counts
        int adults = (Integer) adultCounter.getValue();
        int minors = (Integer) minorCounter.getValue();
        
        // Determine seat preference (default to Standard)
        String seatPreference = "Standard";
        
        // Calculate price
        double totalPriceValue = PriceCalculationService.calculatePrice(
            flight, adults, minors, date, seatPreference, null, offer
        );
        
        // Display price
        totalPrice.setText(String.format("PHP %,.2f", totalPriceValue));
    }
    
    /**
     * Clears booking form
     */
    private void clearBookingForm() {
        selectedSchedule = null;
        selectedSeats.clear();
        seatToPassengerName.clear();
        totalPrice.setText("PHP 0.00");
        // Clear seat panels
        clearSeatPanels();
    }
    
    /**
     * Generates a receipt in txt format
     * @param booking The booking to generate receipt for
     * @return Path to the generated receipt file
     */
    private String generateReceipt(Booking booking) {
        try {
            String receiptFileName = "BOOKRECEIPT_" + booking.getPassengerUsername() + ".txt";
            java.io.File receiptFile = new java.io.File(receiptFileName);
            
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(receiptFile))) {
                // Get passenger info
                kingsman.upair.model.Passenger passenger = 
                    kingsman.upair.repository.PassengerRepository.getPassengerByUsername(booking.getPassengerUsername());
                Flight flight = FlightRepository.getFlightByCode(booking.getFlightCode());
                FlightOffer offer = FlightRepository.getOfferByFlightCode(booking.getFlightCode());
                
                // Header
                writer.println("=".repeat(60));
                writer.println("           UPAIR AIRLINE BOOKING RECEIPT");
                writer.println("=".repeat(60));
                writer.println();
                
                // Booking Information
                writer.println("BOOKING INFORMATION");
                writer.println("-".repeat(60));
                writer.printf("%-20s: %s%n", "Booking ID", booking.getBookingId());
                writer.printf("%-20s: %s%n", "Booking Date", java.time.LocalDate.now().toString());
                writer.printf("%-20s: %s%n", "Booking Time", java.time.LocalTime.now().toString().substring(0, 8));
                writer.printf("%-20s: %s%n", "Status", booking.getStatus());
                writer.println();
                
                // Passenger Information
                writer.println("PASSENGER INFORMATION");
                writer.println("-".repeat(60));
                if (passenger != null) {
                    writer.printf("%-20s: %s%n", "Name", passenger.getFirstName() + " " + passenger.getLastName());
                    writer.printf("%-20s: %s%n", "Username", booking.getPassengerUsername());
                    writer.printf("%-20s: %s%n", "Contact", passenger.getCellphoneNumber());
                } else {
                    writer.printf("%-20s: %s%n", "Username", booking.getPassengerUsername());
                }
                writer.println();
                
                // Flight Information
                writer.println("FLIGHT INFORMATION");
                writer.println("-".repeat(60));
                writer.printf("%-20s: %s%n", "Trip Type", booking.getTripType());
                if (flight != null) {
                    writer.printf("%-20s: %s%n", "Airline", flight.getAirline());
                }
                writer.printf("%-20s: %s%n", "Flight Code", booking.getFlightCode());
                writer.printf("%-20s: %s%n", "Route", booking.getOrigin() + " → " + booking.getDestination());
                writer.printf("%-20s: %s%n", "Departure Date", booking.getDepartureDate().toString());
                writer.printf("%-20s: %s%n", "Departure Time", booking.getDepartureTime().toString());
                
                if (booking.getReturnDate() != null && booking.getReturnTime() != null) {
                    writer.printf("%-20s: %s%n", "Return Date", booking.getReturnDate().toString());
                    writer.printf("%-20s: %s%n", "Return Time", booking.getReturnTime().toString());
                    writer.printf("%-20s: %s%n", "Return Flight", booking.getReturnFlightCode());
                }
                writer.println();
                
                // Passenger Details
                writer.println("PASSENGER DETAILS");
                writer.println("-".repeat(60));
                writer.printf("%-20s: %d Adult(s), %d Minor(s)%n", 
                    "Passengers", booking.getNumberOfAdults(), booking.getNumberOfMinors());
                writer.println();
                writer.println("Seat Assignments:");
                for (int i = 0; i < booking.getReservedSeats().size(); i++) {
                    String seat = booking.getReservedSeats().get(i);
                    String name = i < booking.getPassengerNames().size() ? 
                        booking.getPassengerNames().get(i) : "N/A";
                    writer.printf("  %-10s: %s%n", seat, name);
                }
                writer.println();
                
                // Flight Details
                if (offer != null) {
                    writer.println("FLIGHT AMENITIES");
                    writer.println("-".repeat(60));
                    writer.printf("%-20s: %s%n", "Cabin Class", offer.getCabinClass());
                    writer.printf("%-20s: %s%n", "Seat Type", offer.getSeatType());
                    writer.printf("%-20s: %s%n", "Food & Beverages", offer.getFoodAndBeverages());
                    writer.printf("%-20s: %s%n", "Entertainment", offer.getEntertainment());
                    writer.printf("%-20s: %s%n", "Amenities", offer.getAmenity());
                    if (offer.getMoreDetails() != null && !offer.getMoreDetails().trim().isEmpty()) {
                        writer.printf("%-20s: %s%n", "Additional Info", offer.getMoreDetails());
                    }
                    writer.println();
                }
                
                // Pricing Details
                writer.println("PRICING DETAILS");
                writer.println("-".repeat(60));
                if (flight != null) {
                    double baseFare = flight.getBaseFare();
                    writer.printf("%-20s: PHP %,.2f%n", "Base Fare (per person)", baseFare);
                }
                writer.printf("%-20s: %d%n", "Adults", booking.getNumberOfAdults());
                writer.printf("%-20s: %d%n", "Minors", booking.getNumberOfMinors());
                if (booking.getVoucherCode() != null && !booking.getVoucherCode().trim().isEmpty()) {
                    writer.printf("%-20s: %s (10%% discount applied)%n", "Voucher Code", booking.getVoucherCode());
                }
                writer.println();
                writer.println("-".repeat(60));
                writer.printf("%-20s: PHP %,.2f%n", "TOTAL AMOUNT", booking.getTotalPrice());
                writer.println();
                
                // Payment Information
                writer.println("PAYMENT INFORMATION");
                writer.println("-".repeat(60));
                writer.printf("%-20s: %s%n", "Payment Method", booking.getPaymentType());
                writer.printf("%-20s: %s%n", "Payment Status", "Paid");
                writer.println();
                
                // Footer
                writer.println("=".repeat(60));
                writer.println("Thank you for choosing UPAir!");
                writer.println("For inquiries, please contact our customer service.");
                writer.println("=".repeat(60));
            }
            
            return receiptFile.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Error generating receipt: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Shows receipt panel with the generated receipt
     * @param receiptPath Path to the receipt file
     * @param booking The booking object
     */
    private void showReceiptPanel(String receiptPath, Booking booking) {
        if (receiptPath == null) {
            return;
        }
        
        // Create receipt panel
        javax.swing.JPanel receiptPanel = new javax.swing.JPanel();
        receiptPanel.setLayout(new java.awt.BorderLayout());
        receiptPanel.setBackground(Color.WHITE);
        
        // Create text area for receipt
        javax.swing.JTextArea receiptTextArea = new javax.swing.JTextArea();
        receiptTextArea.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 12));
        receiptTextArea.setEditable(false);
        receiptTextArea.setBackground(Color.WHITE);
        
        // Read receipt file
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(receiptPath));
            String line;
            StringBuilder receiptContent = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                receiptContent.append(line).append("\n");
            }
            reader.close();
            receiptTextArea.setText(receiptContent.toString());
        } catch (Exception e) {
            receiptTextArea.setText("Error loading receipt: " + e.getMessage());
        }
        
        // Create scroll pane
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(receiptTextArea);
        scrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Booking Receipt - " + booking.getBookingId()));
        
        // Create button panel
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        javax.swing.JButton closeButton = new javax.swing.JButton("Close");
        javax.swing.JButton printButton = new javax.swing.JButton("Print");
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        // Add components
        receiptPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
        receiptPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
        
        // Create frame
        javax.swing.JFrame receiptFrame = new javax.swing.JFrame("Booking Receipt - " + booking.getBookingId());
        receiptFrame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        receiptFrame.getContentPane().add(receiptPanel);
        receiptFrame.setSize(700, 600);
        receiptFrame.setLocationRelativeTo(this);
        
        // Button actions
        closeButton.addActionListener(e -> receiptFrame.dispose());
        printButton.addActionListener(e -> {
            try {
                receiptTextArea.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(receiptFrame, 
                    "Error printing receipt: " + ex.getMessage(), 
                    "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        receiptFrame.setVisible(true);
    }

    private void refreshButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_refreshButton3ActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nextButtonActionPerformed

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_previousButtonActionPerformed

    private void cancelBookingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBookingButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cancelBookingButtonActionPerformed

    private void downloadTicketButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadTicketButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_downloadTicketButtonActionPerformed

    private void refreshButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_refreshButton4ActionPerformed

    private void applyFiltersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyFiltersButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_applyFiltersButtonActionPerformed

    private void bookInThisAirlineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookInThisAirlineButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bookInThisAirlineButtonActionPerformed

    private void viewDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDetailsButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_viewDetailsButtonActionPerformed

    private void bookInThisAirlineButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookInThisAirlineButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bookInThisAirlineButton1ActionPerformed

    private void viewDetailsButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDetailsButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_viewDetailsButton1ActionPerformed

    private void bookInThisAirlineButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookInThisAirlineButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bookInThisAirlineButton2ActionPerformed

    private void viewDetailsButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDetailsButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_viewDetailsButton2ActionPerformed

    private void viewPriceButtonBooking1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewPriceButtonBooking1ActionPerformed
        // This is the search button - same functionality as searchBookFlightButton
        searchBookFlightButtonActionPerformed(evt);
    }//GEN-LAST:event_viewPriceButtonBooking1ActionPerformed

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
            java.util.logging.Logger.getLogger(PassengerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PassengerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PassengerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PassengerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PassengerFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activeFlights;
    private javax.swing.JLabel activeFlightsLabel;
    private javax.swing.JPanel activeFlightsPanel;
    private javax.swing.JSpinner adultCounter;
    private javax.swing.JLabel adultLabel;
    private javax.swing.JPanel advertisementPanel;
    private javax.swing.JLabel airline;
    private javax.swing.JButton airlineComparisonButton;
    private javax.swing.JLabel airlineComparisonHeaderLabel;
    private javax.swing.JPanel airlineComparisonHeaderPanel;
    private javax.swing.JPanel airlineComparisonPanel;
    private javax.swing.JLabel airlineLabel;
    private javax.swing.JLabel airlineName;
    private javax.swing.JLabel airlineName1;
    private javax.swing.JLabel airlineName2;
    private javax.swing.JPanel airlinePanel;
    private javax.swing.JLabel airlineRating;
    private javax.swing.JLabel airlineRating1;
    private javax.swing.JLabel airlineRating2;
    private javax.swing.JLabel airlineType;
    private javax.swing.JLabel alsoTextLabel;
    private javax.swing.JButton applyFiltersButton;
    private javax.swing.JPanel availableFlightsPanel;
    private javax.swing.JPanel availableFlightsPanel1;
    private javax.swing.JPanel availableFlightsPanel2;
    private javax.swing.JPanel availableFlightsPanel3;
    private javax.swing.JScrollPane availableFlightsScrollPane;
    private javax.swing.JScrollPane availableFlightsScrollPane1;
    private javax.swing.JButton bookButton;
    private javax.swing.JButton bookFlightButton;
    private javax.swing.JLabel bookFlightHeaderLabel;
    private javax.swing.JPanel bookFlightHeaderPanel;
    private javax.swing.JPanel bookFlightPanel;
    private javax.swing.JTable bookFlightResultTable;
    private javax.swing.JButton bookInThisAirlineButton;
    private javax.swing.JButton bookInThisAirlineButton1;
    private javax.swing.JButton bookInThisAirlineButton2;
    private com.toedter.calendar.JDateChooser bookflightDateChooser;
    private javax.swing.JLabel bookingID;
    private javax.swing.JButton bookingStatusButton;
    private javax.swing.JLabel bookingStatusHeaderLabel;
    private javax.swing.JPanel bookingStatusHeaderPanel;
    private javax.swing.JPanel bookingStatusPanel;
    private javax.swing.JPanel bookingStatusRecord;
    private javax.swing.JPanel businessClassSeats1;
    private javax.swing.JPanel businessClassSeats2;
    private javax.swing.JButton cancelBookingButton;
    private javax.swing.JPanel dashboardPanel;
    private com.toedter.calendar.JDateChooser dateChooser;
    private com.toedter.calendar.JDateChooser dateChooserCompare;
    private javax.swing.JLabel dateLabelBookFlight;
    private javax.swing.JLabel dateLabelCompareLabel;
    private javax.swing.JLabel departureDate;
    private javax.swing.JLabel departureDateLabel;
    private javax.swing.JPanel departureDatePanel;
    private javax.swing.JLabel departureDateTime;
    private javax.swing.JLabel departureTime;
    private javax.swing.JLabel departureTimeLabel;
    private javax.swing.JPanel departureTimePanel;
    private javax.swing.JComboBox<String> destinationComboBox;
    private javax.swing.JLabel destinationLabel;
    private javax.swing.JLabel destinationLabelBookFlight;
    private javax.swing.JComboBox<String> destinationToBook;
    private javax.swing.JButton downloadTicketButton;
    private javax.swing.JPanel economyClassSeats1;
    private javax.swing.JPanel economyClassSeats2;
    private javax.swing.JPanel economyClassSeats3;
    private javax.swing.JPanel economyClassSeats4;
    private javax.swing.JLabel flightCode;
    private javax.swing.JLabel flightDate;
    private javax.swing.JLabel flightDate1;
    private javax.swing.JLabel flightDate2;
    private javax.swing.JLabel flightDuration;
    private javax.swing.JLabel flightDuration1;
    private javax.swing.JLabel flightDuration2;
    private javax.swing.JButton flightOverviewButton;
    private javax.swing.JLabel flightOverviewHeaderLabel;
    private javax.swing.JPanel flightOverviewHeaderPanel;
    private javax.swing.JPanel flightOverviewPanel;
    private javax.swing.JTable flightOverviewTable;
    private javax.swing.JLabel flightStatusBooked;
    private javax.swing.JLabel flightTicketType;
    private javax.swing.JLabel flightType;
    private javax.swing.JLabel flightType1;
    private javax.swing.JLabel flightType2;
    private javax.swing.JComboBox<String> from;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lastDepartedLabel;
    private javax.swing.JPanel lastDepartedPanel;
    private javax.swing.JButton logOutButton;
    private javax.swing.JSpinner minorCounter;
    private javax.swing.JLabel minorCounterLabel;
    private javax.swing.JPanel myDashboardPanel;
    private javax.swing.JLabel myRecentBookingLabel;
    private javax.swing.JPanel myRecentBookingsPanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JRadioButton oneWayType;
    private javax.swing.JComboBox<String> order;
    private javax.swing.JComboBox<String> originComboBox;
    private javax.swing.JLabel originLabel;
    private javax.swing.JComboBox<String> originToBook;
    private javax.swing.JLabel originToLabel;
    private javax.swing.JLabel passengerID;
    private javax.swing.JLabel passengerType;
    private javax.swing.JComboBox<String> paymentMethod;
    private javax.swing.JLabel paymentStatus;
    private javax.swing.JLabel paymentType;
    private javax.swing.JPanel photoLabel;
    private javax.swing.JLabel planeLabel;
    private javax.swing.JButton previousButton;
    private javax.swing.JLabel price1;
    private javax.swing.JLabel price2;
    private javax.swing.JLabel price3;
    private javax.swing.JPanel promoPanel;
    private javax.swing.JLabel promotional1Label;
    private javax.swing.JLabel promotional2Label;
    private javax.swing.JLabel promotional3Label;
    private javax.swing.JLabel promotional4Label;
    private javax.swing.JPanel promotionalPanel;
    private javax.swing.JScrollPane recentBookingScrollPane;
    private javax.swing.JTable recentBookingTable;
    private javax.swing.JButton redeemButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton refreshButton1;
    private javax.swing.JButton refreshButton2;
    private javax.swing.JButton refreshButton3;
    private javax.swing.JButton refreshButton4;
    private javax.swing.JPanel result1Panel;
    private javax.swing.JPanel result2Panel;
    private javax.swing.JPanel result3Panel;
    private javax.swing.JPanel resultCompareLabel;
    private javax.swing.JLabel resultShow;
    private javax.swing.JRadioButton roundTripType;
    private javax.swing.JLabel route;
    private javax.swing.JLabel routeDetails;
    private javax.swing.JLabel routeLabel;
    private javax.swing.JPanel routePanel;
    private javax.swing.JButton searchBookFlightButton;
    private javax.swing.JButton searchFlightButton;
    private javax.swing.JLabel seatBooked;
    private javax.swing.JPanel seatsPanell;
    private javax.swing.JScrollPane seatsSelector;
    private javax.swing.JLabel selectDateLabel;
    private javax.swing.JLabel selectDateLabel12;
    private javax.swing.JLabel selectDateLabel127;
    private javax.swing.JLabel selectDateLabel128;
    private javax.swing.JLabel selectDateLabel129;
    private javax.swing.JLabel selectDateLabel15;
    private javax.swing.JLabel selectDateLabel16;
    private javax.swing.JLabel selectDateLabel17;
    private javax.swing.JLabel selectDateLabel18;
    private javax.swing.JLabel selectDateLabel19;
    private javax.swing.JLabel selectDateLabel25;
    private javax.swing.JLabel selectDateLabel26;
    private javax.swing.JLabel selectDateLabel31;
    private javax.swing.JLabel selectDateLabel32;
    private javax.swing.JLabel selectDateLabel33;
    private javax.swing.JLabel selectDateLabel34;
    private javax.swing.JLabel selectDateLabel35;
    private javax.swing.JLabel selectOrderLabel;
    private javax.swing.JLabel selectSortLabel;
    private javax.swing.JLabel selectToLabel;
    private javax.swing.JComboBox<String> sort;
    private javax.swing.JLabel status;
    private javax.swing.JLabel statusFlight;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JLabel textLabel;
    private javax.swing.JLabel timeBooked;
    private javax.swing.JComboBox<String> to;
    private javax.swing.JLabel totalBookings;
    private javax.swing.JLabel totalBookingsLabel;
    private javax.swing.JPanel totalBookingsPanel;
    private javax.swing.JLabel totalPrice;
    private javax.swing.JLabel totalPriceBooked;
    private javax.swing.JLabel totalPriceLabel;
    private javax.swing.JLabel totalPriceLabel1;
    private javax.swing.JButton userDashboardButton;
    private javax.swing.JPanel userDashboardPanel;
    private javax.swing.JPanel userPanel;
    private javax.swing.JButton userProfileButton;
    private javax.swing.JPanel userSidePanel;
    private javax.swing.JTabbedPane userTabbedPanel;
    private javax.swing.JPanel userTopPanel;
    private javax.swing.JLabel username;
    private javax.swing.JButton viewAllBookingsButton;
    private javax.swing.JButton viewDetailsButton;
    private javax.swing.JButton viewDetailsButton1;
    private javax.swing.JButton viewDetailsButton2;
    private javax.swing.JButton viewTermsLabel;
    private javax.swing.JLabel vouchersAvailable;
    private javax.swing.JLabel vouchersAvailableLabel;
    private javax.swing.JPanel vouchersAvailablePanel;
    private javax.swing.JLabel welcomeLabel;
    // End of variables declaration//GEN-END:variables
}
