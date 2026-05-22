package com.cognizant.PredictiveMaintenanceLite.controller;

import com.cognizant.PredictiveMaintenanceLite.dto.ReadingDto;
import com.cognizant.PredictiveMaintenanceLite.entity.Reading;
import com.cognizant.PredictiveMaintenanceLite.service.ReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/readings")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;

    // Request body for POST
    public record ReadingRequest(Long sensorId, Double rms, Double temperature, String timestamp) {}

    @GetMapping
    public Page<ReadingDto> findReadings(
            @RequestParam(required = false) Long sensorId,
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        return readingService.findReadings(sensorId, assetId, assetName, from, to, pageable);
    }

    @GetMapping("/{id}")
    public ReadingDto findById(@PathVariable Long id) {
        return readingService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingDto create(@RequestBody ReadingRequest req) {
        Reading r = new Reading();
        r.setRms(req.rms());
        r.setTemperature(req.temperature());
        if (req.timestamp() != null) r.setTimestamp(LocalDateTime.parse(req.timestamp()));
        return readingService.create(req.sensorId(), r);
    }
}
