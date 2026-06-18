package com.salon.controller;

import com.salon.model.Booking;
import com.salon.model.Service;
import com.salon.model.Staff;
import com.salon.model.User;
import com.salon.repository.ServiceRepository;
import com.salon.repository.StaffRepository;
import com.salon.service.BookingService;
import com.salon.service.EmailService;
import com.salon.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private EmailService emailService;

    @Autowired
    private TimeSlotService timeSlotService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Booking> bookings = bookingService.getUserBookings(user);

        // Calculate statistics
        int totalBookings = bookings.size();
        int confirmedCount = (int) bookings.stream()
                .filter(b -> "confirmed".equals(b.getStatus()))
                .count();
        int pendingCount = (int) bookings.stream()
                .filter(b -> "pending".equals(b.getStatus()))
                .count();
        int upcomingCount = (int) bookings.stream()
                .filter(b -> "confirmed".equals(b.getStatus()) || "pending".equals(b.getStatus()))
                .count();

        // Get recent bookings (last 5)
        List<Booking> recentBookings = bookings.stream()
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("bookings", recentBookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("upcomingCount", upcomingCount);

        return "customer/dashboard";
    }

    @GetMapping("/book")
    public String bookPage(Model model) {
        List<Service> services = serviceRepository.findByActiveTrue();
        List<Staff> staff = staffRepository.findByIsAvailableTrue();
        model.addAttribute("services", services);
        model.addAttribute("staff", staff);
        return "customer/book";
    }

    @GetMapping("/get-available-slots")
    @ResponseBody
    public List<String> getAvailableSlots(@RequestParam Long staffId,
                                          @RequestParam String date) {
        LocalDate bookingDate = LocalDate.parse(date);
        if (bookingDate.isBefore(LocalDate.now())) {
            return List.of();
        }
        List<LocalTime> availableSlots = timeSlotService.getAvailableTimeSlots(staffId, bookingDate);
        return availableSlots.stream()
                .map(time -> time.toString())
                .toList();
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

        System.out.println("========================================");
        System.out.println("📝 BOOKING REQUEST:");
        System.out.println("  User: " + user.getFullName());
        System.out.println("  Email: " + user.getEmail());
        System.out.println("  Service ID: " + serviceId);
        System.out.println("  Staff ID: " + staffId);
        System.out.println("  Date: " + date);
        System.out.println("  Time: " + time);
        System.out.println("========================================");

        LocalDate bookingDate = LocalDate.parse(date);
        LocalTime bookingTime = LocalTime.parse(time);

        if (bookingDate.isBefore(LocalDate.now())) {
            model.addAttribute("error", "Cannot book on past dates");
            return "customer/book";
        }

        if (!timeSlotService.isTimeSlotAvailable(staffId, bookingDate, bookingTime)) {
            model.addAttribute("error", "Time slot not available");
            model.addAttribute("services", serviceRepository.findByActiveTrue());
            model.addAttribute("staff", staffRepository.findByIsAvailableTrue());
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
        System.out.println("✅ Booking saved with ID: " + savedBooking.getId());

        try {
            System.out.println("📧 Attempting to send email to: " + user.getEmail());
            emailService.sendBookingConfirmation(savedBooking, user);
            System.out.println("✅ Email sent successfully!");
        } catch (Exception e) {
            System.err.println("❌ Email failed: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/customer/dashboard";
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Booking booking = bookingService.cancelBooking(id);
        if (booking != null) {
            try {
                System.out.println("📧 Attempting to send cancellation email to: " + user.getEmail());
                emailService.sendBookingCancellation(booking, user);
                System.out.println("✅ Cancellation email sent to: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Cancellation email failed: " + e.getMessage());
            }
        }
        return "redirect:/customer/dashboard";
    }

    @GetMapping("/test-booking-email")
    @ResponseBody
    public String testBookingEmail(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "❌ Please login first";
        }

        try {
            Booking testBooking = new Booking();
            testBooking.setId(888L);
            testBooking.setDate(LocalDate.now());
            testBooking.setTime(LocalTime.now());
            testBooking.setStatus("confirmed");
            testBooking.setUser(user);

            emailService.sendBookingConfirmation(testBooking, user);
            return "✅ Booking email sent to: " + user.getEmail();
        } catch (Exception e) {
            return "❌ Email failed: " + e.getMessage();
        }
    }
}