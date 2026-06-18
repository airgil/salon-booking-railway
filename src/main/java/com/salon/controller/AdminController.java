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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    public String bookings(@RequestParam(required = false) String status,
                           @RequestParam(required = false) String dateFrom,
                           @RequestParam(required = false) String dateTo,
                           Model model) {
        List<Booking> allBookings = bookingService.getAllBookings();
        List<Booking> filteredBookings = allBookings;

        // Apply status filter
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            filteredBookings = filteredBookings.stream()
                    .filter(b -> status.equals(b.getStatus()))
                    .collect(Collectors.toList());
        }

        // Apply date from filter
        if (dateFrom != null && !dateFrom.isEmpty()) {
            LocalDate fromDate = LocalDate.parse(dateFrom);
            filteredBookings = filteredBookings.stream()
                    .filter(b -> b.getDate().isAfter(fromDate.minusDays(1)))
                    .collect(Collectors.toList());
        }

        // Apply date to filter
        if (dateTo != null && !dateTo.isEmpty()) {
            LocalDate toDate = LocalDate.parse(dateTo);
            filteredBookings = filteredBookings.stream()
                    .filter(b -> b.getDate().isBefore(toDate.plusDays(1)))
                    .collect(Collectors.toList());
        }

        // Calculate statistics from all bookings (not filtered)
        int totalBookings = allBookings.size();
        int pendingCount = (int) allBookings.stream()
                .filter(b -> "pending".equals(b.getStatus()))
                .count();
        int confirmedCount = (int) allBookings.stream()
                .filter(b -> "confirmed".equals(b.getStatus()))
                .count();
        int completedCount = (int) allBookings.stream()
                .filter(b -> "completed".equals(b.getStatus()))
                .count();
        int cancelledCount = (int) allBookings.stream()
                .filter(b -> "cancelled".equals(b.getStatus()))
                .count();

        model.addAttribute("bookings", filteredBookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDateFrom", dateFrom);
        model.addAttribute("selectedDateTo", dateTo);

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
        staff.setIsAvailable(true);
        staffRepository.save(staff);
        return "redirect:/admin/staff";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        List<Booking> bookings = bookingService.getAllBookings();

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

        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("cancelledCount", cancelledCount);

        return "admin/reports";
    }
}