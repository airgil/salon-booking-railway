package com.salon.controller;

import com.salon.model.Booking;
import com.salon.model.Service;
import com.salon.model.Staff;
import com.salon.model.User;
import com.salon.repository.ServiceRepository;
import com.salon.repository.StaffRepository;
import com.salon.service.BookingService;
import com.salon.service.EmailService;  // ← ADD THIS IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private EmailService emailService;  // ← ADD THIS

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Booking> bookings = bookingService.getUserBookings(user);
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        return "customer/dashboard";
    }

    @GetMapping("/book")
    public String bookPage(Model model) {
        List<Service> services = serviceRepository.findByActiveTrue();
        List<Staff> staff = staffRepository.findByAvailableTrue();
        model.addAttribute("services", services);
        model.addAttribute("staff", staff);
        return "customer/book";
    }

    @PostMapping("/book")
    public String createBooking(@RequestParam Long serviceId,
                                @RequestParam Long staffId,
                                @RequestParam String date,
                                @RequestParam String time,
                                HttpSession session,
                                Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        LocalDate bookingDate = LocalDate.parse(date);
        LocalTime bookingTime = LocalTime.parse(time);

        if (bookingDate.isBefore(LocalDate.now())) {
            model.addAttribute("error", "Cannot book on past dates");
            return "customer/book";
        }

        if (!bookingService.isTimeSlotAvailable(staffId, bookingDate, bookingTime)) {
            model.addAttribute("error", "Time slot not available");
            model.addAttribute("services", serviceRepository.findByActiveTrue());
            model.addAttribute("staff", staffRepository.findByAvailableTrue());
            return "customer/book";
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setService(serviceRepository.findById(serviceId).orElse(null));
        booking.setStaff(staffRepository.findById(staffId).orElse(null));
        booking.setDate(bookingDate);
        booking.setTime(bookingTime);
        booking.setStatus("confirmed");

        Booking savedBooking = bookingService.createBooking(booking);

        // Send email confirmation
        try {
            emailService.sendBookingConfirmation(savedBooking, user);
            System.out.println("✅ Email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Email failed: " + e.getMessage());
        }

        return "redirect:/customer/dashboard";
    }

    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return "redirect:/customer/dashboard";
    }

    // Test email endpoint
    @GetMapping("/test-email")
    @ResponseBody
    public String testEmail(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "❌ Please login first at /login";
        }

        System.out.println("========================================");
        System.out.println("📧 TEST EMAIL REQUEST");
        System.out.println("  User: " + user.getFullName());
        System.out.println("  Email: " + user.getEmail());
        System.out.println("========================================");

        try {
            Booking testBooking = new Booking();
            testBooking.setId(999L);
            testBooking.setDate(LocalDate.now());
            testBooking.setTime(LocalTime.now());
            testBooking.setStatus("test");
            testBooking.setUser(user);

            emailService.sendBookingConfirmation(testBooking, user);
            return "✅ Test email sent to: " + user.getEmail() + ". Check your inbox and spam folder!";

        } catch (Exception e) {
            System.err.println("❌ Test email error: " + e.getMessage());
            e.printStackTrace();
            return "❌ Email failed: " + e.getMessage();
        }
    }
}