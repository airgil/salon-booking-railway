package com.salon.repository;

import com.salon.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find bookings by user ID
    List<Booking> findByUserId(Long userId);

    // Find bookings by user ID ordered by date (newest first)
    List<Booking> findByUserIdOrderByDateDesc(Long userId);

    // Find bookings by date (for reminders)
    List<Booking> findByDate(LocalDate date);

    // Find bookings by staff ID and date (for availability checking)
    List<Booking> findByStaffIdAndDate(Long staffId, LocalDate date);

    // Find bookings by staff, date, and time
    List<Booking> findByStaffIdAndDateAndTime(Long staffId, LocalDate date, LocalTime time);

    // Find bookings by status
    List<Booking> findByStatus(String status);

    // Find bookings by user and status
    List<Booking> findByUserIdAndStatus(Long userId, String status);

    // Count bookings by date
    long countByDate(LocalDate date);

    // Find confirmed bookings for a specific date (for reminders)
    @Query("SELECT b FROM Booking b WHERE b.date = :date AND b.status = 'confirmed'")
    List<Booking> findConfirmedBookingsByDate(@Param("date") LocalDate date);

    // Find active bookings for a staff on a specific date (not cancelled)
    @Query("SELECT b FROM Booking b WHERE b.staff.id = :staffId AND b.date = :date AND b.status != 'cancelled'")
    List<Booking> findActiveBookingsByStaffAndDate(@Param("staffId") Long staffId, @Param("date") LocalDate date);

    // Find upcoming bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.date >= :today AND b.status != 'cancelled' ORDER BY b.date ASC, b.time ASC")
    List<Booking> findUpcomingBookingsByUser(@Param("userId") Long userId, @Param("today") LocalDate today);

    // Find past bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.date < :today ORDER BY b.date DESC, b.time DESC")
    List<Booking> findPastBookingsByUser(@Param("userId") Long userId, @Param("today") LocalDate today);
}