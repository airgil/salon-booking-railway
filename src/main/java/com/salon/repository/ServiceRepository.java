package com.salon.repository;

import com.salon.model.SalonService;  // ← CHANGED
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<SalonService, Long> {  // ← CHANGED
    List<SalonService> findByActiveTrue();  // ← CHANGED
    List<SalonService> findByActive(Boolean active);  // ← CHANGED
}