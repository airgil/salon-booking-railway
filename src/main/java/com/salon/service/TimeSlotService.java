package com.salon.service;

import com.salon.model.Booking;
import com.salon.model.Staff;
import com.salon.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeSlotService {

    @Autowired
    private BookingRepository bookingRepository;

    // Working hours
    private static final LocalTime START_TIME = LocalTime.of(9, 0);  // 9:00 AM
    private static final LocalTime END_TIME = LocalTime.of(19, 0);   // 7:00 PM
    private static final int SLOT_DURATION = 30; // 30 minutes per slot

    public List<LocalTime> generateTimeSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = START_TIME;

        while (current.isBefore(END_TIME)) {
            slots.add(current);
            current = current.plusMinutes(SLOT_DURATION);
        }
        return slots;
    }

    public List<LocalTime> getAvailableTimeSlots(Long staffId, LocalDate date) {
        // Get all possible time slots
        List<LocalTime> allSlots = generateTimeSlots();

        // Get booked times for this staff on this date
        List<LocalTime> bookedTimes = bookingRepository.findBookedTimesByStaffAndDate(staffId, date);

        // Remove booked times from available slots
        List<LocalTime> availableSlots = new ArrayList<>(allSlots);
        availableSlots.removeAll(bookedTimes);

        return availableSlots;
    }

    public List<LocalTime> getBookedTimeSlots(Long staffId, LocalDate date) {
        return bookingRepository.findBookedTimesByStaffAndDate(staffId, date);
    }

    public boolean isTimeSlotAvailable(Long staffId, LocalDate date, LocalTime time) {
        List<LocalTime> bookedTimes = bookingRepository.findBookedTimesByStaffAndDate(staffId, date);
        return !bookedTimes.contains(time);
    }
}