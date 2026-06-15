package com.PredictiveMaintenanceLite.dto;

import com.PredictiveMaintenanceLite.entity.Threshold;
import java.time.LocalDateTime;

public record ThresholdDto(
        Long id,
        Double rmsMax,
        Double tempMax,
        LocalDateTime updatedAt,
        Long assetId,    // flat — from threshold.getAsset()
        String assetName
) {
    public static ThresholdDto from(Threshold t) {
        return new ThresholdDto(
                t.getId(), t.getRmsMax(), t.getTempMax(), t.getUpdatedAt(),
                t.getAsset() != null ? t.getAsset().getId()   : null,
                t.getAsset() != null ? t.getAsset().getName() : null
        );
    }
}
