package com.cognizant.PredictiveMaintenanceLite.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Maps to the "thresholds" table.
 * ThresholdDto.from(threshold) flattens assetId/assetName for the frontend —
 * so no @JsonIgnore on asset and no @Transient getters needed here.
 */
@Entity
@Table(name = "thresholds")
@Getter
@Setter
public class Threshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique=true enforces the 1:1 relationship at DB level
    @OneToOne
    @JoinColumn(name = "asset_id", nullable = false, unique = true)
    private Asset asset;

    @Column(name = "rms_max", nullable = false)
    private Double rmsMax;

    @Column(name = "temp_max", nullable = false)
    private Double tempMax;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Same method runs on both INSERT and UPDATE — refreshes updatedAt every save
    @PrePersist
    @PreUpdate
    protected void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }
}
