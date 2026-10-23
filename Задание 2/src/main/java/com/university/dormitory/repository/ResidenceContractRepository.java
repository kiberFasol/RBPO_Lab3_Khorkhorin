package com.university.dormitory.repository;

import com.university.dormitory.entity.ResidenceContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResidenceContractRepository extends JpaRepository<ResidenceContract, Long> {
    List<ResidenceContract> findByApplicationId(Long applicationId);
    List<ResidenceContract> findByRoomId(Long roomId);
}
