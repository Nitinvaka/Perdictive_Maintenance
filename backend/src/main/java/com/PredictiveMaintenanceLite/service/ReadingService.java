package com.PredictiveMaintenanceLite.service;

import com.PredictiveMaintenanceLite.dto.ReadingDto;
import com.PredictiveMaintenanceLite.entity.Reading;
import com.PredictiveMaintenanceLite.entity.Sensor;
import com.PredictiveMaintenanceLite.exception.ResourceNotFoundException;
import com.PredictiveMaintenanceLite.repository.ReadingRepository;
import com.PredictiveMaintenanceLite.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final ReadingRepository  readingRepository;
    private final SensorRepository   sensorRepository;
    private final MaintenanceService maintenanceService;

    // Picks the right repository method based on which filter is present
    public Page<ReadingDto> findReadings(Long sensorId, Long assetId, String assetName,
                                         LocalDateTime from, LocalDateTime to, Pageable pageable) {
        LocalDateTime f    = from != null ? from : LocalDateTime.now().minusDays(7);
        LocalDateTime t    = to   != null ? to   : LocalDateTime.now();
        String        name = (assetName != null && !assetName.isBlank()) ? assetName.trim() : null;

        Page<Reading> page;
        if (sensorId != null) page = readingRepository.findBySensor_IdAndTimestampBetween(sensorId, f, t, pageable);
        else if (assetId != null) page = readingRepository.findBySensor_Asset_IdAndTimestampBetween(assetId, f, t, pageable);
        else if (name    != null) page = readingRepository.findBySensor_Asset_NameContainingIgnoreCaseAndTimestampBetween(name, f, t, pageable);
        else                      page = readingRepository.findByTimestampBetween(f, t, pageable);

        // Convert each entity to a DTO
        return page.map(ReadingDto::from);
    }

    public ReadingDto findById(Long id) {
        return ReadingDto.from(
                readingRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Reading", id))
        );
    }

    @Transactional
    public ReadingDto create(Long sensorId, Reading incoming) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", sensorId));

        incoming.setId(null);
        incoming.setSensor(sensor);
        if (incoming.getTimestamp() == null) incoming.setTimestamp(LocalDateTime.now());

        Reading saved = readingRepository.save(incoming);
        maintenanceService.evaluateThresholds(saved); // check thresholds + raise ticket if needed
        return ReadingDto.from(saved);
    }

    // IoT simulator publishes by device serial number
    @Transactional
    public ReadingDto processIoTPayload(String deviceId, Double rms, Double temp, LocalDateTime ts) {
        Sensor sensor = sensorRepository.findBySerialNumber(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("No sensor with serial number '" + deviceId + "'"));
        return saveAndEvaluate(sensor, rms, temp, ts);
    }

    // Simulator page publishes by sensor ID directly
    @Transactional
    public ReadingDto processIoTPayloadBySensorId(Long sensorId, Double rms, Double temp, LocalDateTime ts) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", sensorId));
        return saveAndEvaluate(sensor, rms, temp, ts);
    }

    // Shared helper: save the reading then evaluate thresholds
    private ReadingDto saveAndEvaluate(Sensor sensor, Double rms, Double temp, LocalDateTime ts) {
        Reading reading = new Reading();
        reading.setSensor(sensor);
        reading.setRms(rms);
        reading.setTemperature(temp);
        reading.setTimestamp(ts != null ? ts : LocalDateTime.now());

        Reading saved = readingRepository.save(reading);
        maintenanceService.evaluateThresholds(saved);
        return ReadingDto.from(saved);
    }
}
