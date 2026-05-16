package com.cognizant.PredictiveMaintenanceLite.repository;

import com.cognizant.PredictiveMaintenanceLite.entity.Asset;
import com.cognizant.PredictiveMaintenanceLite.service.AssetService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);


    List<Asset> findByNameContainingIgnoreCase(String namePart);

    List<Asset> findByAssetTypeIgnoreCase(String type);

    List<Asset> findByNameContainingIgnoreCaseAndAssetTypeIgnoreCase(String namePart, String type);

    @Query("SELECT DISTINCT a.assetType FROM Asset a WHERE a.assetType IS NOT NULL ORDER BY a.assetType")
    List<String> findAllDistinctTypes();

    @Query("""
            SELECT DISTINCT a FROM Asset a
            JOIN a.threshold t
            JOIN a.sensors s
            JOIN s.readings r
            WHERE r.timestamp >= :since
              AND (r.rms > t.rmsMax OR r.temperature > t.tempMax)
            """)
    List<Asset> findAssetsWithViolationsSince(@Param("since") LocalDateTime since);

    @Query("""
        SELECT new com.cognizant.PredictiveMaintenanceLite.service.AssetService$AvgRmsRow(
            a.id, a.name, cast(r.timestamp as localdate), AVG(r.rms))
        FROM Asset a
        JOIN a.sensors s
        JOIN s.readings r
        WHERE r.timestamp >= :since
        GROUP BY a.id, a.name, cast(r.timestamp as localdate)
        ORDER BY a.name, cast(r.timestamp as localdate)
        """)
    List<AssetService.AvgRmsRow> findAverageRmsPerAssetPerDay(@Param("since") LocalDateTime since);
}
