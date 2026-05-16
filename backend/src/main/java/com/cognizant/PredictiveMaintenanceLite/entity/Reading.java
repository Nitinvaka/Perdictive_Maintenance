package com.cognizant.PredictiveMaintenanceLite.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Maps to the "readings" table.
 * ReadingDto.from(reading) flattens sensor + asset info for the frontend —
 * so no @JsonIgnore on sensor and no @Transient getters needed here.
 */
@Entity
@Table(name = "readings")
@Getter
@Setter
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning side — holds the sensor_id foreign key column
    @ManyToOne
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private Double rms;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Flipped to true once MaintenanceService has evaluated this reading
    // against the asset's threshold and raised a ticket if needed
    private boolean processed = false;
}
