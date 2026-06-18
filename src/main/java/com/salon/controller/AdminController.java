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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        // Get all bookings
        List<Booking> allBookings = bookingService.getAllBookings();

        // Start with all bookings
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
                    .filter(b -> b.getDate() != null && !b.getDate().isBefore(fromDate))
                    .collect(Collectors.toList());
        }

        // Apply date to filter
        if (dateTo != null && !dateTo.isEmpty()) {
            LocalDate toDate = LocalDate.parse(dateTo);
            filteredBookings = filteredBookings.stream()
                    .filter(b -> b.getDate() != null && !b.getDate().isAfter(toDate))
                    .collect(Collectors.toList());
        }

        // Calculate statistics from filtered bookings
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

        // Add attributes to model
        model.addAttribute("bookings", filteredBookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);

        // These are the critical values for the filter form
        model.addAttribute("selectedStatus", status != null ? status : "all");
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

/*    @GetMapping("/service/delete/{id}")
    public String deleteService(@PathVariable Long id) {
        serviceRepository.deleteById(id);
        return "redirect:/admin/services";
    }*/

    @GetMapping("/service/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        System.out.println("========================================");
        System.out.println("🗑️ Deleting service with ID: " + id);
        System.out.println("========================================");

        try {
            serviceRepository.deleteById(id);
            System.out.println("✅ Service deleted successfully");
            redirectAttributes.addFlashAttribute("success", "Service deleted successfully!");
        } catch (Exception e) {
            System.err.println("❌ Failed to delete service: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete service: " + e.getMessage());
        }

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

    // ADD THIS METHOD - Toggle Staff Availability
    @PostMapping("/staff/toggle/{id}")
    public String toggleStaffAvailability(@PathVariable Long id) {
        Staff staff = staffRepository.findById(id).orElse(null);
        if (staff != null) {
            staff.setIsAvailable(!staff.getIsAvailable());
            staffRepository.save(staff);
        }
        return "redirect:/admin/staff";
    }


    // ADD THIS METHOD - Delete Staff
    @GetMapping("/staff/delete/{id}")
    public String deleteStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        System.out.println("========================================");
        System.out.println("🗑️ Deleting staff with ID: " + id);
        System.out.println("========================================");

        try {
            Staff staff = staffRepository.findById(id).orElse(null);
            if (staff == null) {
                redirectAttributes.addFlashAttribute("error", "Staff member not found!");
                return "redirect:/admin/staff";
            }

            // Check if staff has bookings
            List<Booking> bookings = bookingService.getBookingsByStaff(staff);
            if (!bookings.isEmpty()) {
                System.out.println("⚠️ Staff has " + bookings.size() + " bookings. Cannot delete.");
                redirectAttributes.addFlashAttribute("error",
                        "Cannot delete staff member '" + staff.getStaffName() +
                                "' because they have " + bookings.size() + " existing bookings. " +
                                "Please reassign or cancel these bookings first.");
                return "redirect:/admin/staff";
            }

            staffRepository.deleteById(id);
            System.out.println("✅ Staff deleted successfully: " + staff.getStaffName());
            redirectAttributes.addFlashAttribute("success", "Staff member deleted successfully!");

        } catch (Exception e) {
            System.err.println("❌ Failed to delete staff: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete staff: " + e.getMessage());
        }

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