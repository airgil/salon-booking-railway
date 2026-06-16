package com.salon.controller;

import com.salon.model.Booking;
import com.salon.model.Service;
import com.salon.model.Staff;
import com.salon.model.User;
import com.salon.repository.BookingRepository;
import com.salon.repository.ServiceRepository;
import com.salon.repository.StaffRepository;
import com.salon.service.BookingService;
import com.salon.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;  // ← This was missing

    @Autowired
    private StaffRepository staffRepository;      // ← Add this too

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EmailService emailService;

    // Show booking form
    @GetMapping("/book")
    public String showBookingForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Service> services = serviceRepository.findAll();
        List<Staff> staff = staffRepository.findAll();

        model.addAttribute("services", services);
        model.addAttribute("staff", staff);
        model.addAttribute("booking", new Booking());

        return "booking-form";
    }

    // Create booking
    @PostMapping("/book")
    public String createBooking(@RequestParam Long serviceId,
                                @RequestParam Long staffId,
                                @RequestParam String date,
                                @RequestParam String time,
                                HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setService(serviceRepository.findById(serviceId).orElse(null));
            booking.setStaff(staffRepository.findById(staffId).orElse(null));
            booking.setDate(LocalDate.parse(date));
            booking.setTime(LocalTime.parse(time));
            booking.setStatus("confirmed");

            Booking savedBooking = bookingRepository.save(booking);

            // Send email confirmation (optional - comment if not working)
            try {
                emailService.sendBookingConfirmation(savedBooking, user);
            } catch (Exception e) {
                System.out.println("Email not sent: " + e.getMessage());
            }

            return "redirect:/my-bookings?success=true";

        } catch (Exception e) {
            System.out.println("Booking failed: " + e.getMessage());
            return "redirect:/book?error=true";
        }
    }

    // View user bookings
    @GetMapping("/my-bookings")
    public String myBookings(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingRepository.findByUserOrderByDateDesc(user);
        model.addAttribute("bookings", bookings);

        return "my-bookings";
    }

    // Cancel booking
    @PostMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        bookingService.cancelBooking(id);
        return "redirect:/my-bookings";
    }

    // Admin - view all bookings
    @GetMapping("/admin/bookings")
    public String adminBookings(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingRepository.findAllByOrderByDateDescTimeDesc();
        model.addAttribute("bookings", bookings);

        return "admin/bookings";
    }
}