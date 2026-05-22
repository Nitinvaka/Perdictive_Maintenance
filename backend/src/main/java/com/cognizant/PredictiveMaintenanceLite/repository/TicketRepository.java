package com.cognizant.PredictiveMaintenanceLite.repository;

import com.cognizant.PredictiveMaintenanceLite.entity.Ticket;
import com.cognizant.PredictiveMaintenanceLite.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    long countByStatus(TicketStatus status);

    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    List<Ticket> findByAsset_IdOrderByCreatedAtDesc(Long assetId);

    List<Ticket> findByAsset_NameContainingIgnoreCaseOrderByCreatedAtDesc(String namePart);

    List<Ticket> findByStatusInOrderByCreatedAtDesc(List<TicketStatus> statuses);

    boolean existsByReading_Id(Long readingId);

    void deleteByAsset_Id(Long assetId);

    void deleteBySensor_Id(Long sensorId);
}
