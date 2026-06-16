package com.salon.repository;

import com.salon.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // CORRECT - uses isAvailable field
    List<Staff> findByIsAvailableTrue();

    // Optional: find by availability parameter
    List<Staff> findByIsAvailable(Boolean isAvailable);
}