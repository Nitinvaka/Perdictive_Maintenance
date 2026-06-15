package com.PredictiveMaintenanceLite.repository;

import com.PredictiveMaintenanceLite.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {

    List<Sensor> findByAsset_Id(Long assetId);

    List<Sensor> findByAsset_NameContainingIgnoreCase(String namePart);

    Optional<Sensor> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    boolean existsByNameAndAsset_Id(String name, Long assetId);

    boolean existsByNameAndAsset_IdAndIdNot(String name, Long assetId, Long id);
}
