package com.university.dormitory.repository;

import com.university.dormitory.entity.HousingApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HousingApplicationRepository extends JpaRepository<HousingApplication, Long> {
    List<HousingApplication> findByUserId(Long userId);
}
