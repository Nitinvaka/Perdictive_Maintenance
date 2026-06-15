package com.PredictiveMaintenanceLite.entity;

import com.PredictiveMaintenanceLite.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Maps to the "tickets" table.
 * TicketDto.from(ticket) flattens asset + sensor + reading info for the frontend —
 * so all three @JsonIgnore annotations and all @Transient getters are removed.
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    // The specific reading that triggered this ticket — NOT NULL because
    // a ticket without a breach reading makes no sense
    @ManyToOne
    @JoinColumn(name = "reading_id", nullable = false)
    private Reading reading;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Set when the ticket is CLOSED — null while still open or in-progress
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null)    status    = TicketStatus.OPEN;
    }
}
