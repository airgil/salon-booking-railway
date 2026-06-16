package com.salon.service;

import com.salon.model.Booking;
import com.salon.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@EnableScheduling
public class ReminderService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmailService emailService;

    // Runs every day at 9 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Use the correct method - matches your entity field 'date'
        List<Booking> tomorrowBookings = bookingRepository.findConfirmedBookingsByDate(tomorrow);

        for (Booking booking : tomorrowBookings) {
            if (booking.getUser() != null) {
                emailService.sendReminder(booking, booking.getUser());
            }
        }

        System.out.println("Sent " + tomorrowBookings.size() + " reminders for " + tomorrow);
    }
}