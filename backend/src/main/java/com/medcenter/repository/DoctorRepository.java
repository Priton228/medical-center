package com.medcenter.repository;

import com.medcenter.domain.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUserUsername(String username);
    List<Doctor> findBySpecializationIgnoreCase(String specialization);
    Page<Doctor> findAllByAvailableTrue(Pageable pageable);
}
