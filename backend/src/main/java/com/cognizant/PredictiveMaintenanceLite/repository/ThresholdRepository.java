package com.cognizant.PredictiveMaintenanceLite.repository;

import com.cognizant.PredictiveMaintenanceLite.entity.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThresholdRepository extends JpaRepository<Threshold, Long> {

    Optional<Threshold> findByAsset_Id(Long assetId);

    boolean existsByAsset_Id(Long assetId);

    List<Threshold> findByAsset_NameContainingIgnoreCase(String namePart);
}
