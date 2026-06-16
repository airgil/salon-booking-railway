package com.salon.controller;

import com.salon.model.Booking;
import com.salon.model.Service;
import com.salon.model.Staff;
import com.salon.model.User;
import com.salon.repository.ServiceRepository;
import com.salon.repository.StaffRepository;
import com.salon.service.BookingService;
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
        List<Staff> staff = staffRepository.findByIsAvailableTrue();
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
            model.addAttribute("staff", staffRepository.findByIsAvailableTrue());
            return "customer/book";
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setService(serviceRepository.findById(serviceId).orElse(null));
        booking.setStaff(staffRepository.findById(staffId).orElse(null));
        booking.setDate(bookingDate);
        booking.setTime(bookingTime);

        bookingService.createBooking(booking);
        return "redirect:/customer/dashboard";
    }

    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return "redirect:/customer/dashboard";
    }




}