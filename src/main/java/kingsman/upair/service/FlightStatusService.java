package kingsman.upair.service;

import kingsman.upair.model.Schedule;
import kingsman.upair.repository.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Service class for managing flight status in real-time
 * Handles status updates: Scheduled, Departed, On Air, Arrived
 */
public class FlightStatusService {
    
    /**
     * Gets the current status of a schedule based on real-time
     * @param schedule The schedule to check
     * @return Status string: "Scheduled", "Departed", "On Air", or "Arrived"
     */
    public static String getFlightStatus(Schedule schedule) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledDateTime = LocalDateTime.of(schedule.getDepartureDate(), schedule.getDepartureTime());
        
        // Get flight duration from flight data
        kingsman.upair.model.Flight flight = ScheduleService.getFlightByCode(schedule.getFlightCode());
        if (flight == null) {
            return "Scheduled";
        }
        
        // Parse duration (format: "2h 30m" or "2h30m" or "2.5h")
        int durationMinutes = parseDurationToMinutes(flight.getDuration());
        
        // Calculate arrival time
        LocalDateTime arrivalDateTime = scheduledDateTime.plusMinutes(durationMinutes);
        
        // Check if flight should be deleted (Arrived flights from MNL/CEB to DRP after duration + 2 minutes)
        if (shouldDeleteFlight(schedule, now, arrivalDateTime)) {
            return "DELETED"; // Special status to indicate deletion
        }
        
        // Status logic
        if (now.isBefore(scheduledDateTime)) {
            return "Scheduled";
        } else if (now.isAfter(scheduledDateTime) && now.isBefore(scheduledDateTime.plusMinutes(2))) {
            return "Departed";
        } else if (now.isAfter(scheduledDateTime.plusMinutes(2)) && now.isBefore(arrivalDateTime)) {
            return "On Air";
        } else if (now.isAfter(arrivalDateTime)) {
            // Check if it's a flight to Daraga that should be deleted
            if (schedule.getDestination().contains("Daraga") || schedule.getDestination().contains("DRP")) {
                if (now.isAfter(arrivalDateTime.plusMinutes(2))) {
                    return "DELETED";
                }
            }
            return "Arrived";
        }
        
        return "Scheduled";
    }
    
    /**
     * Checks if a flight should be deleted
     * Rules: Flights from MNL/CEB to DRP should be deleted after duration + 2 minutes
     */
    private static boolean shouldDeleteFlight(Schedule schedule, LocalDateTime now, LocalDateTime arrivalDateTime) {
        String origin = schedule.getOrigin();
        String destination = schedule.getDestination();
        
        // Check if origin is Manila or Cebu and destination is Daraga
        boolean isFromManilaOrCebu = origin.contains("Manila") || origin.contains("MNL") ||
                                     origin.contains("Cebu") || origin.contains("CEB");
        boolean isToDaraga = destination.contains("Daraga") || destination.contains("DRP");
        
        if (isFromManilaOrCebu && isToDaraga) {
            // Delete after arrival time + 2 minutes
            return now.isAfter(arrivalDateTime.plusMinutes(2));
        }
        
        return false;
    }
    
    /**
     * Parses duration string to minutes
     * Handles formats like "2h 30m", "2h30m", "2.5h", "150m"
     */
    private static int parseDurationToMinutes(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            return 0;
        }
        
        duration = duration.trim().toLowerCase();
        int totalMinutes = 0;
        
        // Check for hours
        if (duration.contains("h")) {
            String[] parts = duration.split("h");
            try {
                double hours = Double.parseDouble(parts[0].trim());
                totalMinutes += (int)(hours * 60);
                duration = parts.length > 1 ? parts[1].trim() : "";
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        // Check for minutes
        if (duration.contains("m")) {
            String minutesStr = duration.replaceAll("[^0-9]", "");
            try {
                totalMinutes += Integer.parseInt(minutesStr);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        // If no h or m found, assume it's in minutes
        if (totalMinutes == 0) {
            try {
                totalMinutes = Integer.parseInt(duration.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                totalMinutes = 120; // Default 2 hours
            }
        }
        
        return totalMinutes;
    }
    
    /**
     * Gets all schedules with their current status
     * Filters out deleted flights and removes them from file
     */
    public static List<ScheduleWithStatus> getSchedulesWithStatus() {
        List<Schedule> allSchedules = ScheduleService.getAllSchedules();
        List<ScheduleWithStatus> result = new ArrayList<>();
        List<String> schedulesToDelete = new ArrayList<>();
        
        for (Schedule schedule : allSchedules) {
            String status = getFlightStatus(schedule);
            if (status.equals("DELETED")) {
                schedulesToDelete.add(schedule.getScheduleId());
            } else {
                result.add(new ScheduleWithStatus(schedule, status));
            }
        }
        
        // Delete flights that should be removed
        for (String scheduleId : schedulesToDelete) {
            ScheduleRepository.deleteSchedule(scheduleId);
        }
        
        return result;
    }
    
    /**
     * Gets schedules filtered by criteria
     */
    public static List<ScheduleWithStatus> getFilteredSchedules(LocalDate date, String origin, String destination) {
        List<ScheduleWithStatus> allSchedules = getSchedulesWithStatus();
        List<ScheduleWithStatus> filtered = new ArrayList<>();
        
        for (ScheduleWithStatus sws : allSchedules) {
            Schedule s = sws.getSchedule();
            boolean matches = true;
            
            if (date != null && !s.getDepartureDate().equals(date)) {
                matches = false;
            }
            
            if (origin != null && !origin.trim().isEmpty() && 
                !s.getOrigin().toLowerCase().contains(origin.toLowerCase())) {
                matches = false;
            }
            
            if (destination != null && !destination.trim().isEmpty() &&
                !s.getDestination().toLowerCase().contains(destination.toLowerCase())) {
                matches = false;
            }
            
            if (matches) {
                filtered.add(sws);
            }
        }
        
        return filtered;
    }
    
    /**
     * Gets only scheduled flights (not departed, on air, or arrived)
     */
    public static List<ScheduleWithStatus> getScheduledFlightsOnly() {
        List<ScheduleWithStatus> all = getSchedulesWithStatus();
        List<ScheduleWithStatus> scheduled = new ArrayList<>();
        
        for (ScheduleWithStatus sws : all) {
            if (sws.getStatus().equals("Scheduled")) {
                scheduled.add(sws);
            }
        }
        
        return scheduled;
    }
    
    /**
     * Inner class to hold schedule with its status
     */
    public static class ScheduleWithStatus {
        private final Schedule schedule;
        private final String status;
        
        public ScheduleWithStatus(Schedule schedule, String status) {
            this.schedule = schedule;
            this.status = status;
        }
        
        public Schedule getSchedule() {
            return schedule;
        }
        
        public String getStatus() {
            return status;
        }
    }
}

