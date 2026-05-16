package com.cognizant.PredictiveMaintenanceLite.dto;

import com.cognizant.PredictiveMaintenanceLite.entity.Sensor;
import com.cognizant.PredictiveMaintenanceLite.enums.SensorType;
import java.time.LocalDateTime;

public record SensorDto(
        Long id,
        String name,
        String serialNumber,
        SensorType sensorType,
        boolean active,
        LocalDateTime installedAt,
        Long assetId,      // flat — pulled from sensor.getAsset()
        String assetName   // flat — pulled from sensor.getAsset()
) {
    public static SensorDto from(Sensor s) {
        return new SensorDto(
                s.getId(), s.getName(), s.getSerialNumber(),
                s.getSensorType(), s.isActive(), s.getInstalledAt(),
                s.getAsset() != null ? s.getAsset().getId()   : null,
                s.getAsset() != null ? s.getAsset().getName() : null
        );
    }
}
