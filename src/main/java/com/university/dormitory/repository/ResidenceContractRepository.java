package com.university.dormitory.repository;

import com.university.dormitory.entity.ResidenceContract;
import com.university.dormitory.entity.ResidenceContract.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResidenceContractRepository extends JpaRepository<ResidenceContract, Long> {

    long countByRoomIdAndStatus(Long roomId, ContractStatus status);

    List<ResidenceContract> findAllByRoomIdAndStatus(Long roomId, ContractStatus status);

    boolean existsByApplicationUserIdAndStatus(Long userId, ContractStatus status);

    List<ResidenceContract> findAllByStatusAndCheckOutDateLessThanEqual(ContractStatus status, LocalDate date);
}
