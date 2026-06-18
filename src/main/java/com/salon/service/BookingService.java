package com.salon.service;

import com.salon.model.Booking;
import com.salon.model.Staff;
import com.salon.model.User;
import com.salon.repository.BookingRepository;
import com.salon.repository.ServiceRepository;
import com.salon.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    public Booking createBooking(Booking booking) {
        booking.setStatus("pending");
        return bookingRepository.save(booking);  // ← Must return the saved booking
    }

    public boolean isTimeSlotAvailable(Long staffId, LocalDate date, LocalTime time) {
        Staff staff = staffRepository.findById(staffId).orElse(null);
        if (staff == null) return false;
        long count = bookingRepository.countByStaffAndDateAndTimeAndStatusNot(staff, date, time, "cancelled");
        return count == 0;
    }

    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByDateDesc(user);
    }

    public List<Booking> getBookingsByStaff(Staff staff) {
        return bookingRepository.findByStaff(staff);
    }



    // NEW: Get bookings with filters
    public List<Booking> getUserBookingsWithFilter(User user, String status, String dateRange) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        // Parse date range
        if (dateRange != null && !dateRange.isEmpty()) {
            LocalDate today = LocalDate.now();
            switch (dateRange) {
                case "today":
                    startDate = today;
                    endDate = today;
                    break;
                case "this-week":
                    startDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
                    endDate = startDate.plusDays(6);
                    break;
                case "this-month":
                    startDate = today.withDayOfMonth(1);
                    endDate = today.withDayOfMonth(today.lengthOfMonth());
                    break;
                case "last-month":
                    startDate = today.minusMonths(1).withDayOfMonth(1);
                    endDate = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth());
                    break;
                case "all":
                default:
                    startDate = null;
                    endDate = null;
                    break;
            }
        }

        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            if (startDate != null && endDate != null) {
                return bookingRepository.findByUserAndStatusAndDateBetween(user, status, startDate, endDate);
            } else if (startDate != null) {
                return bookingRepository.findByUserAndDateBetween(user, startDate, endDate);
            } else {
                return bookingRepository.findByUserAndStatus(user, status);
            }
        } else {
            if (startDate != null && endDate != null) {
                return bookingRepository.findByUserAndDateBetween(user, startDate, endDate);
            } else {
                return bookingRepository.findByUserOrderByDateDesc(user);
            }
        }
    }

    public List<String> getUserBookingStatuses(User user) {
        return bookingRepository.findDistinctStatusesByUser(user);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null && ("pending".equals(booking.getStatus()) || "confirmed".equals(booking.getStatus()))) {
            booking.setStatus("cancelled");
            return bookingRepository.save(booking);
        }
        return null;
    }

    public boolean updateStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            booking.setStatus(status);
            bookingRepository.save(booking);
            return true;
        }
        return false;
    }

    // Reassign bookings from one staff to another
    public int reassignBookings(Long oldStaffId, Long newStaffId) {
        Staff oldStaff = staffRepository.findById(oldStaffId).orElse(null);
        Staff newStaff = staffRepository.findById(newStaffId).orElse(null);

        if (oldStaff == null || newStaff == null) {
            return 0;
        }

        List<Booking> bookings = bookingRepository.findByStaff(oldStaff);
        int count = bookings.size();

        for (Booking booking : bookings) {
            booking.setStaff(newStaff);
            bookingRepository.save(booking);
        }

        // Delete the old staff after reassignment
        staffRepository.deleteById(oldStaffId);

        return count;
    }


    public List<Booking> getTodayBookings() {
        return bookingRepository.findByDate(LocalDate.now());
    }
}