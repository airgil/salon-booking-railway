package com.salon.repository;

import com.salon.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // Method if your field is "available"
    List<Staff> findByAvailableTrue();

    // Method if your field is "isAvailable"
    List<Staff> findByIsAvailableTrue();

    // Find by availability with parameter
    List<Staff> findByAvailable(Boolean available);
    List<Staff> findByIsAvailable(Boolean isAvailable);
}