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
        List<Booking> allBookings = bookingService.getAllBookings();
        List<Booking> filteredBookings = allBookings;

        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            filteredBookings = filteredBookings.stream()
                    .filter(b -> status.equals(b.getStatus()))
                    .collect(Collectors.toList());
        }

        if (dateFrom != null && !dateFrom.isEmpty()) {
            LocalDate fromDate = LocalDate.parse(dateFrom);
            filteredBookings = filteredBookings.stream()
                    .filter(b -> b.getDate() != null && !b.getDate().isBefore(fromDate))
                    .collect(Collectors.toList());
        }

        if (dateTo != null && !dateTo.isEmpty()) {
            LocalDate toDate = LocalDate.parse(dateTo);
            filteredBookings = filteredBookings.stream()
                    .filter(b -> b.getDate() != null && !b.getDate().isAfter(toDate))
                    .collect(Collectors.toList());
        }

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

    @GetMapping("/service/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            serviceRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Service deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete service: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    @GetMapping("/staff")
    public String staff(Model model) {
        List<Staff> staffList = staffRepository.findAll();
        long availableCount = staffList.stream()
                .filter(s -> s.getIsAvailable() != null && s.getIsAvailable())
                .count();

        model.addAttribute("staff", staffList);
        model.addAttribute("availableCount", availableCount);
        return "admin/staff";
    }

    @PostMapping("/staff/add")
    public String addStaff(@ModelAttribute Staff staff, RedirectAttributes redirectAttributes) {
        try {
            staff.setIsAvailable(true);
            staffRepository.save(staff);
            redirectAttributes.addFlashAttribute("success", "Staff member added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add staff: " + e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/staff/toggle/{id}")
    public String toggleStaffAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Staff staff = staffRepository.findById(id).orElse(null);
            if (staff != null) {
                boolean currentStatus = staff.getIsAvailable();
                staff.setIsAvailable(!currentStatus);
                staffRepository.save(staff);
                redirectAttributes.addFlashAttribute("success",
                        "Staff availability updated! " + staff.getStaffName() +
                                " is now " + (staff.getIsAvailable() ? "Available" : "Unavailable"));
            } else {
                redirectAttributes.addFlashAttribute("error", "Staff not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to toggle availability: " + e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @GetMapping("/staff/delete/{id}")
    public String deleteStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Staff staff = staffRepository.findById(id).orElse(null);

        if (staff == null) {
            redirectAttributes.addFlashAttribute("error", "Staff member not found!");
            return "redirect:/admin/staff";
        }

        // Check if staff has bookings
        List<Booking> bookings = bookingService.getBookingsByStaff(staff);

        if (!bookings.isEmpty()) {
            redirectAttributes.addFlashAttribute("info",
                    "Staff member '" + staff.getStaffName() +
                            "' has " + bookings.size() + " existing bookings. Please reassign them first.");
            return "redirect:/admin/staff/reassign/" + id;
        }

        try {
            staffRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Staff member deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete staff: " + e.getMessage());
        }

        return "redirect:/admin/staff";
    }

    @GetMapping("/staff/reassign/{id}")
    public String showReassignForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Staff staffToDelete = staffRepository.findById(id).orElse(null);

        if (staffToDelete == null) {
            redirectAttributes.addFlashAttribute("error", "Staff member not found!");
            return "redirect:/admin/staff";
        }

        // Get all other staff members (excluding the one to delete)
        List<Staff> availableStaff = staffRepository.findAll().stream()
                .filter(s -> !s.getId().equals(id))
                .filter(s -> s.getIsAvailable() != null && s.getIsAvailable())
                .collect(Collectors.toList());

        if (availableStaff.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "No other available staff members found. Please add another staff member first.");
            return "redirect:/admin/staff";
        }

        int bookingsCount = bookingService.getBookingsByStaff(staffToDelete).size();

        model.addAttribute("staffToDelete", staffToDelete);
        model.addAttribute("availableStaff", availableStaff);
        model.addAttribute("bookingsCount", bookingsCount);

        return "admin/staff-reassign";
    }

    @PostMapping("/staff/reassign")
    public String reassignStaff(@RequestParam Long oldStaffId,
                                @RequestParam Long newStaffId,
                                RedirectAttributes redirectAttributes) {
        try {
            Staff oldStaff = staffRepository.findById(oldStaffId).orElse(null);
            Staff newStaff = staffRepository.findById(newStaffId).orElse(null);

            if (oldStaff == null || newStaff == null) {
                redirectAttributes.addFlashAttribute("error", "Staff member not found!");
                return "redirect:/admin/staff";
            }

            // Reassign bookings
            int count = bookingService.reassignBookings(oldStaffId, newStaffId);

            redirectAttributes.addFlashAttribute("success",
                    "Successfully reassigned " + count + " bookings from " +
                            oldStaff.getStaffName() + " to " + newStaff.getStaffName() +
                            " and deleted " + oldStaff.getStaffName());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reassign: " + e.getMessage());
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