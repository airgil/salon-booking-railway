package com.salon.repository;

import com.salon.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // Find available staff - CORRECTED
    List<Staff> findByIsAvailableTrue();

    // Find staff by availability
    List<Staff> findByIsAvailable(Boolean isAvailable);
}