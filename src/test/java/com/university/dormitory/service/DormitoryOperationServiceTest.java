package com.university.dormitory.service;

import com.university.dormitory.dto.ResidenceContractResponseDto;
import com.university.dormitory.dto.operation.*;
import com.university.dormitory.entity.*;
import com.university.dormitory.entity.HousingApplication.ApplicationStatus;
import com.university.dormitory.entity.ResidenceContract.ContractStatus;
import com.university.dormitory.entity.RoomTransferRequest.TransferStatus;
import com.university.dormitory.exception.BusinessRuleException;
import com.university.dormitory.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DormitoryOperationServiceTest {

    @Mock
    private HousingApplicationRepository applicationRepository;

    @Mock
    private ResidenceContractRepository contractRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomTransferRequestRepository transferRepository;

    @InjectMocks
    private DormitoryOperationService operationService;

    private User testUser;
    private Room testRoom1;
    private Room testRoom2;
    private HousingApplication testApp;
    private ResidenceContract testContract;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("Тестов Тест Тестович")
                .email("test@university.ru")
                .role("STUDENT")
                .build();

        testRoom1 = Room.builder()
                .id(10L)
                .roomNumber("101")
                .capacity(2)
                .isAvailable(true)
                .build();

        testRoom2 = Room.builder()
                .id(20L)
                .roomNumber("102")
                .capacity(1)
                .isAvailable(true)
                .build();

        testApp = HousingApplication.builder()
                .id(100L)
                .user(testUser)
                .submissionDate(LocalDate.now().minusDays(5))
                .status(ApplicationStatus.PENDING)
                .build();

        testContract = ResidenceContract.builder()
                .id(200L)
                .application(testApp)
                .room(testRoom1)
                .checkInDate(LocalDate.now().minusDays(3))
                .checkOutDate(LocalDate.now().plusDays(100))
                .status(ContractStatus.ACTIVE)
                .build();
    }

    // =========================================================================
    // Бизнес-операция 1: Заселение студента (Check-in)
    // =========================================================================

    @Test
    @DisplayName("Операция 1 (успех): Заселение в свободную комнату")
    void checkIn_Success() {
        CheckInRequestDto dto = CheckInRequestDto.builder()
                .applicationId(100L)
                .roomId(10L)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusMonths(10))
                .build();

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(testApp));
        when(contractRepository.existsByApplicationUserIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(false);
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom1));
        when(contractRepository.countByRoomIdAndStatus(10L, ContractStatus.ACTIVE)).thenReturn(0L);
        when(contractRepository.save(any(ResidenceContract.class))).thenAnswer(invocation -> {
            ResidenceContract saved = invocation.getArgument(0);
            saved.setId(999L);
            return saved;
        });

        ResidenceContractResponseDto result = operationService.checkIn(dto);

        assertNotNull(result);
        assertEquals(999L, result.getId());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(ApplicationStatus.APPROVED, testApp.getStatus());
        verify(applicationRepository).save(testApp);
        verify(contractRepository).save(any(ResidenceContract.class));
    }

    @Test
    @DisplayName("Операция 1 (ошибка): Попытка заселения при наличии активного договора")
    void checkIn_FailsWhenActiveContractExists() {
        CheckInRequestDto dto = CheckInRequestDto.builder()
                .applicationId(100L)
                .roomId(10L)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusMonths(10))
                .build();

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(testApp));
        when(contractRepository.existsByApplicationUserIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> operationService.checkIn(dto));
        assertTrue(ex.getMessage().contains("уже имеет активный договор"));
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("Операция 1 (ошибка): Попытка заселения в переполненную комнату")
    void checkIn_FailsWhenRoomIsFull() {
        CheckInRequestDto dto = CheckInRequestDto.builder()
                .applicationId(100L)
                .roomId(10L)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusMonths(10))
                .build();

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(testApp));
        when(contractRepository.existsByApplicationUserIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(false);
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom1));
        when(contractRepository.countByRoomIdAndStatus(10L, ContractStatus.ACTIVE)).thenReturn(2L); // capacity is 2

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> operationService.checkIn(dto));
        assertTrue(ex.getMessage().contains("нет свободных мест"));
    }

    // =========================================================================
    // Бизнес-операция 2: Переселение студента (Approve Transfer)
    // =========================================================================

    @Test
    @DisplayName("Операция 2 (успех): Одобрение переселения в целевую комнату")
    void approveTransfer_Success() {
        RoomTransferRequest request = RoomTransferRequest.builder()
                .id(50L)
                .contract(testContract)
                .targetRoom(testRoom2)
                .submissionDate(LocalDate.now().minusDays(1))
                .status(TransferStatus.PENDING)
                .build();

        when(transferRepository.findById(50L)).thenReturn(Optional.of(request));
        when(contractRepository.countByRoomIdAndStatus(20L, ContractStatus.ACTIVE)).thenReturn(0L);

        TransferApprovalResponseDto result = operationService.approveTransfer(50L);

        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        assertEquals(testRoom2, testContract.getRoom());
        assertTrue(testRoom1.getIsAvailable());
        assertFalse(testRoom2.getIsAvailable()); // capacity 1 filled
        verify(contractRepository).save(testContract);
        verify(transferRepository).save(request);
    }

    @Test
    @DisplayName("Операция 2 (ошибка): Переселение в ту же самую комнату")
    void approveTransfer_FailsSameRoom() {
        RoomTransferRequest request = RoomTransferRequest.builder()
                .id(50L)
                .contract(testContract)
                .targetRoom(testRoom1) // Same room
                .submissionDate(LocalDate.now().minusDays(1))
                .status(TransferStatus.PENDING)
                .build();

        when(transferRepository.findById(50L)).thenReturn(Optional.of(request));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> operationService.approveTransfer(50L));
        assertTrue(ex.getMessage().contains("совпадает с текущей комнатой"));
    }

    // =========================================================================
    // Бизнес-операция 3: Выселение / Расторжение договора (Terminate Contract)
    // =========================================================================

    @Test
    @DisplayName("Операция 3 (успех): Досрочное расторжение договора и освобождение комнаты")
    void terminateContract_Success() {
        RoomTransferRequest pendingTransfer = RoomTransferRequest.builder()
                .id(77L)
                .contract(testContract)
                .targetRoom(testRoom2)
                .status(TransferStatus.PENDING)
                .build();

        when(contractRepository.findById(200L)).thenReturn(Optional.of(testContract));
        when(transferRepository.findAllByContractIdAndStatus(200L, TransferStatus.PENDING))
                .thenReturn(List.of(pendingTransfer));

        TerminateContractRequestDto dto = TerminateContractRequestDto.builder()
                .reason("По собственному желанию")
                .terminationDate(LocalDate.now())
                .build();

        TerminateContractResponseDto result = operationService.terminateContract(200L, dto);

        assertNotNull(result);
        assertEquals("TERMINATED", result.getContractStatus());
        assertEquals(1, result.getRejectedTransferRequestsCount());
        assertEquals(TransferStatus.REJECTED, pendingTransfer.getStatus());
        assertTrue(testRoom1.getIsAvailable());
        verify(contractRepository).save(testContract);
    }

    // =========================================================================
    // Бизнес-операция 4: Пакетное закрытие истекших договоров (Batch Expirations)
    // =========================================================================

    @Test
    @DisplayName("Операция 4 (успех): Пакетное закрытие истекших договоров")
    void processExpirations_Success() {
        ResidenceContract expiredContract = ResidenceContract.builder()
                .id(300L)
                .application(testApp)
                .room(testRoom2)
                .checkInDate(LocalDate.now().minusMonths(10))
                .checkOutDate(LocalDate.now().minusDays(1))
                .status(ContractStatus.ACTIVE)
                .build();

        when(contractRepository.findAllByStatusAndCheckOutDateLessThanEqual(eq(ContractStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(List.of(expiredContract));
        when(roomRepository.findById(20L)).thenReturn(Optional.of(testRoom2));
        when(contractRepository.countByRoomIdAndStatus(20L, ContractStatus.ACTIVE)).thenReturn(0L);

        BatchExpirationResponseDto result = operationService.processExpirations(LocalDate.now());

        assertNotNull(result);
        assertEquals(1, result.getExpiredContractsCount());
        assertEquals(1, result.getFreedRoomsCount());
        assertEquals(ContractStatus.EXPIRED, expiredContract.getStatus());
        assertTrue(testRoom2.getIsAvailable());
    }

    // =========================================================================
    // Бизнес-операция 5: Вывод комнаты на ремонт / техобслуживание (Maintenance)
    // =========================================================================

    @Test
    @DisplayName("Операция 5 (отклонение): Попытка закрыть комнату с жильцами при политике REJECT_IF_OCCUPIED")
    void setRoomMaintenance_RejectsWhenOccupied() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom1));
        when(contractRepository.findAllByRoomIdAndStatus(10L, ContractStatus.ACTIVE))
                .thenReturn(List.of(testContract));

        RoomMaintenanceRequestDto dto = RoomMaintenanceRequestDto.builder()
                .reason("Капремонт")
                .policy(RoomMaintenanceRequestDto.MaintenancePolicy.REJECT_IF_OCCUPIED)
                .build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> operationService.setRoomMaintenance(10L, dto));
        assertTrue(ex.getMessage().contains("проживают 1 чел"));
    }

    @Test
    @DisplayName("Операция 5 (успех): Принудительный вывод на ремонт с завершением договоров")
    void setRoomMaintenance_ForceTerminatesContracts() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom1));
        when(contractRepository.findAllByRoomIdAndStatus(10L, ContractStatus.ACTIVE))
                .thenReturn(List.of(testContract));

        RoomMaintenanceRequestDto dto = RoomMaintenanceRequestDto.builder()
                .reason("Аварийное затопление")
                .policy(RoomMaintenanceRequestDto.MaintenancePolicy.FORCE_TERMINATE_CONTRACTS)
                .build();

        RoomMaintenanceResponseDto result = operationService.setRoomMaintenance(10L, dto);

        assertNotNull(result);
        assertFalse(testRoom1.getIsAvailable());
        assertEquals(1, result.getTerminatedContractsCount());
        assertEquals(ContractStatus.TERMINATED, testContract.getStatus());
        verify(contractRepository).save(testContract);
        verify(roomRepository).save(testRoom1);
    }
}
