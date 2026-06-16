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

    public boolean createBooking(Booking booking) {
        booking.setStatus("pending");
        bookingRepository.save(booking);
        return true;
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

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public boolean cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null && "pending".equals(booking.getStatus())) {
            booking.setStatus("cancelled");
            bookingRepository.save(booking);
            return true;
        }
        return false;
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

    public List<Booking> getTodayBookings() {
        return bookingRepository.findByDate(LocalDate.now());
    }
}