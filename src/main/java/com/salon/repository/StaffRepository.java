package com.salon.repository;

import com.salon.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // CORRECT - matches your Staff entity field "isAvailable"
    List<Staff> findByIsAvailableTrue();

    // Also add this for flexibility
    List<Staff> findByIsAvailable(Boolean isAvailable);
}