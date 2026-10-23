package com.university.dormitory.repository;

import com.university.dormitory.entity.RoomTransferRequest;
import com.university.dormitory.entity.RoomTransferRequest.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTransferRequestRepository extends JpaRepository<RoomTransferRequest, Long> {

    List<RoomTransferRequest> findAllByContractId(Long contractId);

    List<RoomTransferRequest> findAllByContractIdAndStatus(Long contractId, TransferStatus status);

    boolean existsByContractIdAndStatus(Long contractId, TransferStatus status);
}
