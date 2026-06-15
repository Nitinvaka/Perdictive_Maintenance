package com.PredictiveMaintenanceLite.controller;

import com.PredictiveMaintenanceLite.dto.SensorDto;
import com.PredictiveMaintenanceLite.entity.Sensor;
import com.PredictiveMaintenanceLite.enums.SensorType;
import com.PredictiveMaintenanceLite.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    // Request body for create/update
    public record SensorRequest(Long assetId, String name, String serialNumber, String sensorType) {}

    // Request body for PATCH /status
    public record StatusRequest(Boolean active) {}

    @GetMapping
    public List<SensorDto> findAll(@RequestParam(required = false) String search) {
        return sensorService.findAll(search);
    }

    @GetMapping("/{id}")
    public SensorDto findById(@PathVariable Long id) {
        return sensorService.findById(id);
    }

    @GetMapping("/by-asset/{assetId}")
    public List<SensorDto> findByAsset(@PathVariable Long assetId) {
        return sensorService.findByAssetId(assetId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorDto create(@RequestBody SensorRequest req) {
        return sensorService.create(req.assetId(), toSensor(req));
    }

    @PutMapping("/{id}")
    public SensorDto update(@PathVariable Long id, @RequestBody SensorRequest req) {
        return sensorService.update(id, req.assetId(), toSensor(req));
    }

    @PatchMapping("/{id}/status")
    public SensorDto setStatus(@PathVariable Long id, @RequestBody StatusRequest body) {
        boolean active = body.active() != null ? body.active() : true;
        return sensorService.setStatus(id, active);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sensorService.delete(id);
    }

    // Converts the request record into a Sensor entity for the service
    private Sensor toSensor(SensorRequest req) {
        Sensor s = new Sensor();
        s.setName(req.name());
        s.setSerialNumber(req.serialNumber());
        if (req.sensorType() != null) s.setSensorType(SensorType.valueOf(req.sensorType()));
        return s;
    }
}
