package com.salon.repository;

import com.salon.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // Find available staff
    List<Staff> findByIsAvailableTrue();

    // Alternative method name if using different field name
    List<Staff> findByAvailableTrue();

    // Find staff by availability
    List<Staff> findByIsAvailable(Boolean isAvailable);
}