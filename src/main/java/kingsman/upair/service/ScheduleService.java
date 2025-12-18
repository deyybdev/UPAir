package kingsman.upair.service;

import kingsman.upair.model.Schedule;
import kingsman.upair.repository.ScheduleRepository;

import java.util.List;

/**
 * Service class for handling schedule management business logic
 * Follows Service Layer Pattern
 */
public class ScheduleService {
    
    /**
     * Saves a new schedule
     * @param schedule The schedule to save
     * @return ServiceResult containing success status and message
     */
    public static ServiceResult saveSchedule(Schedule schedule) {
        if (schedule == null || !schedule.isValid()) {
            return new ServiceResult(false, "Invalid schedule information!");
        }
        
        boolean saved = ScheduleRepository.saveSchedule(schedule);
        if (saved) {
            return new ServiceResult(true, "Schedule added successfully!");
        } else {
            return new ServiceResult(false, "Failed to save schedule! Schedule ID may already exist.");
        }
    }
    
    /**
     * Updates an existing schedule
     * @param schedule The schedule to update
     * @return ServiceResult containing success status and message
     */
    public static ServiceResult updateSchedule(Schedule schedule) {
        if (schedule == null || !schedule.isValid()) {
            return new ServiceResult(false, "Invalid schedule information!");
        }
        
        boolean updated = ScheduleRepository.updateSchedule(schedule);
        if (updated) {
            return new ServiceResult(true, "Schedule updated successfully!");
        } else {
            return new ServiceResult(false, "Failed to update schedule! Schedule may not exist.");
        }
    }
    
    /**
     * Deletes a schedule
     * @param scheduleId The schedule ID to delete
     * @return ServiceResult containing success status and message
     */
    public static ServiceResult deleteSchedule(String scheduleId) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            return new ServiceResult(false, "Schedule ID is required!");
        }
        
        boolean deleted = ScheduleRepository.deleteSchedule(scheduleId);
        if (deleted) {
            return new ServiceResult(true, "Schedule deleted successfully!");
        } else {
            return new ServiceResult(false, "Failed to delete schedule! Schedule may not exist.");
        }
    }
    
    /**
     * Gets all schedules
     * @return List of all schedules
     */
    public static List<Schedule> getAllSchedules() {
        return ScheduleRepository.getAllSchedules();
    }
    
    /**
     * Gets schedules by airline
     * @param airline The airline name
     * @return List of schedules
     */
    public static List<Schedule> getSchedulesByAirline(String airline) {
        return ScheduleRepository.getSchedulesByAirline(airline);
    }
    
    /**
     * Gets a schedule by ID
     * @param scheduleId The schedule ID
     * @return Schedule object if found, null otherwise
     */
    public static Schedule getScheduleById(String scheduleId) {
        return ScheduleRepository.getScheduleById(scheduleId);
    }
    
    /**
     * Gets flight codes for a specific airline
     * @param airline The airline name
     * @return List of flight codes
     */
    public static List<String> getFlightCodesByAirline(String airline) {
        return ScheduleRepository.getFlightCodesByAirline(airline);
    }
    
    /**
     * Gets flight details by flight code
     * @param flightCode The flight code
     * @return Flight object if found, null otherwise
     */
    public static kingsman.upair.model.Flight getFlightByCode(String flightCode) {
        return ScheduleRepository.getFlightByCode(flightCode);
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

