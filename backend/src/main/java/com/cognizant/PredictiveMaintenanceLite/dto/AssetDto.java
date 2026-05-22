package com.cognizant.PredictiveMaintenanceLite.dto;

import com.cognizant.PredictiveMaintenanceLite.entity.Asset;
import java.time.LocalDateTime;

public record AssetDto(
        Long id,
        String name,
        String location,
        String assetType,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // Converts a DB entity into this DTO — called in the service layer
    public static AssetDto from(Asset a) {
        return new AssetDto(
                a.getId(), a.getName(), a.getLocation(),
                a.getAssetType(), a.getDescription(), a.isActive(),
                a.getCreatedAt(), a.getUpdatedAt()
        );
    }
}
