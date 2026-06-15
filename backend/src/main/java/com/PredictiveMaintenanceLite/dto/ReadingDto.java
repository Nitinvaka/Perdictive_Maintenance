package com.PredictiveMaintenanceLite.dto;

import com.PredictiveMaintenanceLite.entity.Reading;
import java.time.LocalDateTime;

public record ReadingDto(
        Long id,
        Double rms,
        Double temperature,
        LocalDateTime timestamp,
        boolean processed,
        Long sensorId,    // flat — from reading.getSensor()
        String sensorName,
        Long assetId,     // flat — from reading.getSensor().getAsset()
        String assetName
) {
    public static ReadingDto from(Reading r) {
        // Navigate the chain safely with null checks
        Long   sId   = r.getSensor() != null ? r.getSensor().getId()   : null;
        String sName = r.getSensor() != null ? r.getSensor().getName() : null;
        Long   aId   = (r.getSensor() != null && r.getSensor().getAsset() != null)
                       ? r.getSensor().getAsset().getId()   : null;
        String aName = (r.getSensor() != null && r.getSensor().getAsset() != null)
                       ? r.getSensor().getAsset().getName() : null;

        return new ReadingDto(
                r.getId(), r.getRms(), r.getTemperature(),
                r.getTimestamp(), r.isProcessed(),
                sId, sName, aId, aName
        );
    }
}
