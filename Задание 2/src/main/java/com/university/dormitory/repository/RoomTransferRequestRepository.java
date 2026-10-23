package com.university.dormitory.repository;

import com.university.dormitory.entity.RoomTransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomTransferRequestRepository extends JpaRepository<RoomTransferRequest, Long> {
    List<RoomTransferRequest> findByContractId(Long contractId);
}
