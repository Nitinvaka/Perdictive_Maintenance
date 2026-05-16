package com.cognizant.PredictiveMaintenanceLite.repository;

import com.cognizant.PredictiveMaintenanceLite.entity.Reading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {

    Page<Reading> findBySensor_IdAndTimestampBetween(
            Long sensorId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Reading> findBySensor_Asset_IdAndTimestampBetween(
            Long assetId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Reading> findBySensor_Asset_NameContainingIgnoreCaseAndTimestampBetween(
            String namePart, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Reading> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    void deleteBySensor_Asset_Id(Long assetId);
}
