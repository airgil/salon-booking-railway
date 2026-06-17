package com.salon.repository;

import com.salon.model.Booking;
import com.salon.model.Staff;
import com.salon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Basic queries
    List<Booking> findByUser(User user);
    List<Booking> findByUserOrderByDateDesc(User user);
    List<Booking> findByDate(LocalDate date);
    List<Booking> findByStaff(Staff staff);
    List<Booking> findByStaffAndDate(Staff staff, LocalDate date);
    List<Booking> findByStaffAndDateAndTime(Staff staff, LocalDate date, LocalTime time);
    List<Booking> findByStatus(String status);

    // For reminders - get confirmed bookings for a specific date
    @Query("SELECT b FROM Booking b WHERE b.date = :date AND b.status = 'confirmed'")
    List<Booking> findConfirmedBookingsByDate(@Param("date") LocalDate date);

    // Find all bookings ordered by date and time
    @Query("SELECT b FROM Booking b ORDER BY b.date DESC, b.time DESC")
    List<Booking> findAllByOrderByDateDescTimeDesc();

    // Find user bookings ordered by date and time
    @Query("SELECT b FROM Booking b WHERE b.user = :user ORDER BY b.date DESC, b.time DESC")
    List<Booking> findByUserOrderByDateDescTimeDesc(@Param("user") User user);

    // Count bookings by staff, date, time, and status (not cancelled)
    long countByStaffAndDateAndTimeAndStatusNot(Staff staff, LocalDate date, LocalTime time, String status);

    // Alternative query using @Query
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.staff = :staff AND b.date = :date AND b.time = :time AND b.status != :status")
    long countActiveBookingsByStaffAndDateTime(
            @Param("staff") Staff staff,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("status") String status
    );

    // Get booked times for a specific staff on a specific date
    @Query("SELECT b.time FROM Booking b WHERE b.staff.id = :staffId AND b.date = :date AND b.status != 'cancelled'")
    List<LocalTime> findBookedTimesByStaffAndDate(
            @Param("staffId") Long staffId,
            @Param("date") LocalDate date
    );

    // Get all bookings for a staff on a specific date
    @Query("SELECT b FROM Booking b WHERE b.staff.id = :staffId AND b.date = :date AND b.status != 'cancelled'")
    List<Booking> findBookingsByStaffAndDate(
            @Param("staffId") Long staffId,
            @Param("date") LocalDate date
    );

}