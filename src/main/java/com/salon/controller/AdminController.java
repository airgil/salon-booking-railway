package com.salon.controller;

import com.salon.model.Booking;
import com.salon.model.Service;
import com.salon.model.Staff;
import com.salon.repository.ServiceRepository;
import com.salon.repository.StaffRepository;
import com.salon.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private StaffRepository staffRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Booking> bookings = bookingService.getAllBookings();
        int totalBookings = bookings.size();
        int pendingCount = (int) bookings.stream().filter(b -> "pending".equals(b.getStatus())).count();
        int todayCount = bookingService.getTodayBookings().size();

        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("recentBookings", bookings.stream().limit(5).toArray());
        return "admin/dashboard";
    }

    @GetMapping("/bookings")
    public String bookings(Model model) {
        List<Booking> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }

    @PostMapping("/booking/update")
    public String updateBookingStatus(@RequestParam Long id, @RequestParam String status) {
        bookingService.updateStatus(id, status);
        return "redirect:/admin/bookings";
    }

    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("services", serviceRepository.findAll());
        return "admin/services";
    }

    @PostMapping("/service/add")
    public String addService(@ModelAttribute Service service) {
        service.setActive(true);
        serviceRepository.save(service);
        return "redirect:/admin/services";
    }

    @GetMapping("/service/delete/{id}")
    public String deleteService(@PathVariable Long id) {
        serviceRepository.deleteById(id);
        return "redirect:/admin/services";
    }

    @GetMapping("/staff")
    public String staff(Model model) {
        model.addAttribute("staff", staffRepository.findAll());
        return "admin/staff";
    }

    @PostMapping("/staff/add")
    public String addStaff(@ModelAttribute Staff staff) {
        // Use either setAvailable or setIsAvailable based on your Staff entity
        staff.setIsAvailable(true);  // ← Use this if your entity has setIsAvailable
        // OR
        // staff.setAvailable(true);  // ← Use this if you added the setAvailable method
        staffRepository.save(staff);
        return "redirect:/admin/staff";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        // Get all bookings
        List<Booking> bookings = bookingService.getAllBookings();

        // Calculate statistics
        int totalBookings = bookings.size();
        int completedCount = (int) bookings.stream()
                .filter(b -> "completed".equals(b.getStatus()))
                .count();
        int pendingCount = (int) bookings.stream()
                .filter(b -> "pending".equals(b.getStatus()))
                .count();
        int cancelledCount = (int) bookings.stream()
                .filter(b -> "cancelled".equals(b.getStatus()))
                .count();

        // Add to model
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("cancelledCount", cancelledCount);

        return "admin/reports";
    }
}