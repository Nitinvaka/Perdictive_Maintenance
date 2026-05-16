package com.cognizant.PredictiveMaintenanceLite.entity;

import com.cognizant.PredictiveMaintenanceLite.enums.SensorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps to the "sensors" table.
 * SensorDto.from(sensor) flattens assetId/assetName for the frontend —
 * so this entity no longer needs @JsonIgnore on asset or @Transient getters.
 */
@Entity
@Table(name = "sensors")
@Getter
@Setter
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning side — holds the asset_id foreign key column
    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private String name;

    // Auto-generated if blank on create — see SensorService.create()
    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false)
    private SensorType sensorType;

    private boolean active = true;

    @Column(name = "installed_at", updatable = false)
    private LocalDateTime installedAt;

    // Cascade: deleting a sensor deletes its readings too
    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reading> readings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (installedAt == null) installedAt = LocalDateTime.now();
    }
}
