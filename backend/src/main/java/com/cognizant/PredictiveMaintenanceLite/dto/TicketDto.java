package com.cognizant.PredictiveMaintenanceLite.dto;

import com.cognizant.PredictiveMaintenanceLite.entity.Ticket;
import com.cognizant.PredictiveMaintenanceLite.enums.TicketStatus;
import java.time.LocalDateTime;

public record TicketDto(
        Long id,
        TicketStatus status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        Long assetId,      // flat — from ticket.getAsset()
        String assetName,
        Long sensorId,     // flat — from ticket.getSensor()
        String sensorName,
        Long readingId     // flat — from ticket.getReading()
) {
    public static TicketDto from(Ticket t) {
        return new TicketDto(
                t.getId(), t.getStatus(), t.getDescription(),
                t.getCreatedAt(), t.getClosedAt(),
                t.getAsset()   != null ? t.getAsset().getId()    : null,
                t.getAsset()   != null ? t.getAsset().getName()  : null,
                t.getSensor()  != null ? t.getSensor().getId()   : null,
                t.getSensor()  != null ? t.getSensor().getName() : null,
                t.getReading() != null ? t.getReading().getId()  : null
        );
    }
}
