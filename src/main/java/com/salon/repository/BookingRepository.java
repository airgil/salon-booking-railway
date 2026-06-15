package com.salon.repository;

import com.salon.model.Booking;
import com.salon.model.User;
import com.salon.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserOrderByDateDescTimeDesc(User user);
    List<Booking> findByStaffOrderByDateDescTimeDesc(Staff staff);
    List<Booking> findAllByOrderByDateDescTimeDesc();
    List<Booking> findByDate(LocalDate date);
    int countByStaffAndDateAndTimeAndStatusNot(Staff staff, LocalDate date, LocalTime time, String status);
}