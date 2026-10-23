package com.university.dormitory.repository;

import com.university.dormitory.entity.HousingApplication;
import com.university.dormitory.entity.HousingApplication.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HousingApplicationRepository extends JpaRepository<HousingApplication, Long> {
    List<HousingApplication> findByUserId(Long userId);
    boolean existsByUserIdAndStatus(Long userId, ApplicationStatus status);
}
