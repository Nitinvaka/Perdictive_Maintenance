package com.cognizant.PredictiveMaintenanceLite.controller;

import com.cognizant.PredictiveMaintenanceLite.dto.ReadingDto;
import com.cognizant.PredictiveMaintenanceLite.service.ReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final ReadingService readingService;

    // Request body records
    public record PublishRequest(String deviceId, Double rms, Double temp, String ts) {}
    public record PublishBySensorRequest(Long sensorId, Double rms, Double temp, String ts) {}

    // Publish by device serial number (IoT device sends its serial)
    @PostMapping("/publish")
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingDto publish(@RequestBody PublishRequest req) {
        LocalDateTime ts = req.ts() != null ? LocalDateTime.parse(req.ts()) : LocalDateTime.now();
        return readingService.processIoTPayload(req.deviceId(), req.rms(), req.temp(), ts);
    }

    // Publish by sensor ID (Simulator page picks a sensor from dropdown)
    @PostMapping("/publish-by-sensor")
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingDto publishBySensor(@RequestBody PublishBySensorRequest req) {
        LocalDateTime ts = req.ts() != null ? LocalDateTime.parse(req.ts()) : LocalDateTime.now();
        return readingService.processIoTPayloadBySensorId(req.sensorId(), req.rms(), req.temp(), ts);
    }
}
