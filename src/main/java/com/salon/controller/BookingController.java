package com.salon.controller;

import com.salon.model.Booking;
import com.salon.model.User;
import com.salon.repository.BookingRepository;
import com.salon.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class BookingController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping("/booking/create")
    public String createBooking(@RequestParam Long serviceId,
                                @RequestParam Long staffId,
                                @RequestParam String date,
                                @RequestParam String time,
                                HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setService(serviceRepository.findById(serviceId).orElse(null));
        booking.setStaff(staffRepository.findById(staffId).orElse(null));
        booking.setDate(LocalDate.parse(date));  // Using setDate, not setBookingDate
        booking.setTime(LocalTime.parse(time));  // Using setTime, not setBookingTime
        booking.setStatus("confirmed");

        Booking savedBooking = bookingRepository.save(booking);

        // Send confirmation email
        emailService.sendBookingConfirmation(savedBooking, user);

        return "redirect:/my-bookings";
    }

    @PostMapping("/booking/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking != null && booking.getUser().getId().equals(user.getId())) {
            booking.setStatus("cancelled");
            bookingRepository.save(booking);

            // Send cancellation email
            emailService.sendBookingCancellation(booking, user);
        }

        return "redirect:/my-bookings";
    }
}