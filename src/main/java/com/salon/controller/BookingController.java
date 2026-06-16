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
    private ServiceRepository serviceRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EmailService emailService;

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

        System.out.println("========================================");
        System.out.println("📝 BOOKING REQUEST:");
        System.out.println("  User: " + user.getFullName() + " (" + user.getEmail() + ")");
        System.out.println("  Service ID: " + serviceId);
        System.out.println("  Staff ID: " + staffId);
        System.out.println("  Date: " + date);
        System.out.println("  Time: " + time);
        System.out.println("========================================");

        try {
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setService(serviceRepository.findById(serviceId).orElse(null));
            booking.setStaff(staffRepository.findById(staffId).orElse(null));
            booking.setDate(LocalDate.parse(date));
            booking.setTime(LocalTime.parse(time));
            booking.setStatus("confirmed");

            Booking savedBooking = bookingRepository.save(booking);
            System.out.println("✅ Booking saved with ID: " + savedBooking.getId());

            // Send email confirmation
            System.out.println("📧 Attempting to send email to: " + user.getEmail());
            try {
                emailService.sendBookingConfirmation(savedBooking, user);
                System.out.println("✅ Email send attempt completed");
            } catch (Exception e) {
                System.err.println("❌ Email failed: " + e.getMessage());
                e.printStackTrace();
            }

            return "redirect:/my-bookings?success=true";

        } catch (Exception e) {
            System.err.println("❌ Booking failed: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/book?error=true";
        }
    }

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

    @PostMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        bookingService.cancelBooking(id);
        return "redirect:/my-bookings";
    }

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

            System.out.println("📧 Calling emailService.sendBookingConfirmation...");
            emailService.sendBookingConfirmation(testBooking, user);
            System.out.println("✅ Test email complete!");

            return "✅ Test email sent to: " + user.getEmail() + ". Check your inbox and spam folder!";
        } catch (Exception e) {
            System.err.println("❌ Test email failed: " + e.getMessage());
            e.printStackTrace();
            return "❌ Email failed: " + e.getMessage();
        }
    }
}