package com.cognizant.PredictiveMaintenanceLite.service;

import com.cognizant.PredictiveMaintenanceLite.dto.TicketDto;
import com.cognizant.PredictiveMaintenanceLite.entity.Ticket;
import com.cognizant.PredictiveMaintenanceLite.enums.TicketStatus;
import com.cognizant.PredictiveMaintenanceLite.exception.BusinessException;
import com.cognizant.PredictiveMaintenanceLite.exception.ResourceNotFoundException;
import com.cognizant.PredictiveMaintenanceLite.repository.AssetRepository;
import com.cognizant.PredictiveMaintenanceLite.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AssetRepository  assetRepository;

    // State machine: defines which status transitions are legal
    // OPEN → IN_PROGRESS or CLOSED  |  IN_PROGRESS → CLOSED  |  CLOSED → nothing
    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED = Map.of(
            TicketStatus.OPEN,        Set.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
            TicketStatus.IN_PROGRESS, Set.of(TicketStatus.CLOSED),
            TicketStatus.CLOSED,      Set.of()
    );

    public Page<TicketDto> findAll(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(TicketDto::from);
    }

    public Page<TicketDto> findByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable).map(TicketDto::from);
    }

    // Internal — returns entity so updateStatus can operate on it
    public Ticket findEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
    }

    public List<TicketDto> findByAssetId(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", assetId);
        }
        return ticketRepository.findByAsset_IdOrderByCreatedAtDesc(assetId)
                .stream().map(TicketDto::from).toList();
    }

    public List<TicketDto> findByAssetName(String assetName) {
        return ticketRepository.findByAsset_NameContainingIgnoreCaseOrderByCreatedAtDesc(assetName)
                .stream().map(TicketDto::from).toList();
    }

    public List<TicketDto> findAllActive() {
        return ticketRepository.findByStatusInOrderByCreatedAtDesc(
                List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS))
                .stream().map(TicketDto::from).toList();
    }

    public long countOpen() {
        return ticketRepository.countByStatus(TicketStatus.OPEN);
    }

    @Transactional
    public TicketDto updateStatus(Long id, TicketStatus newStatus) {
        Ticket ticket  = findEntityById(id);
        TicketStatus current = ticket.getStatus();

        Set<TicketStatus> allowed = ALLOWED.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BusinessException(
                    "Cannot move ticket " + id + " from " + current + " to " + newStatus +
                    ". Allowed: " + (allowed.isEmpty() ? "none (terminal state)" : allowed));
        }

        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.CLOSED) ticket.setClosedAt(LocalDateTime.now());
        return TicketDto.from(ticketRepository.save(ticket));
    }
}
