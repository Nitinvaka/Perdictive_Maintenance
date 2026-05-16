package com.cognizant.PredictiveMaintenanceLite.service;

import com.cognizant.PredictiveMaintenanceLite.entity.Reading;
import com.cognizant.PredictiveMaintenanceLite.entity.Threshold;
import com.cognizant.PredictiveMaintenanceLite.entity.Ticket;
import com.cognizant.PredictiveMaintenanceLite.repository.ReadingRepository;
import com.cognizant.PredictiveMaintenanceLite.repository.ThresholdRepository;
import com.cognizant.PredictiveMaintenanceLite.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final ThresholdRepository thresholdRepository;
    private final TicketRepository ticketRepository;
    private final ReadingRepository readingRepository;

    @Transactional
    public void evaluateThresholds(Reading reading) {
        Long assetId = reading.getSensor().getAsset().getId();
        Optional<Threshold> opt = thresholdRepository.findByAsset_Id(assetId);

        // No threshold set for this asset → just mark processed and return
        if (opt.isEmpty()) {
            reading.setProcessed(true);
            readingRepository.save(reading);
            return;
        }

        Threshold threshold = opt.get();
        List<String> breaches = new ArrayList<>();

        if (reading.getRms() > threshold.getRmsMax()) {
            breaches.add("RMS " + reading.getRms() + " exceeded limit " + threshold.getRmsMax());
        }
        if (reading.getTemperature() > threshold.getTempMax()) {
            breaches.add("Temperature " + reading.getTemperature() + "°C exceeded limit " + threshold.getTempMax() + "°C");
        }

        // Found at least one breach AND no ticket already exists for this reading
        if (!breaches.isEmpty() && !ticketRepository.existsByReading_Id(reading.getId())) {
            String description = "[" + reading.getSensor().getAsset().getName() + " | " +
                    reading.getSensor().getName() + "] " +
                    String.join("; ", breaches);

            Ticket ticket = new Ticket();
            ticket.setAsset(reading.getSensor().getAsset());
            ticket.setSensor(reading.getSensor());
            ticket.setReading(reading);
            ticket.setDescription(description);

            ticketRepository.save(ticket);
        }

        reading.setProcessed(true);
        readingRepository.save(reading);
    }
}