package com.PredictiveMaintenanceLite.controller;

import com.PredictiveMaintenanceLite.dto.ThresholdDto;
import com.PredictiveMaintenanceLite.service.ThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/thresholds")
@RequiredArgsConstructor
public class ThresholdController {

    private final ThresholdService thresholdService;

    // DTO‑style request validation
    public record CreateRequest(Long assetId, Double rmsMax, Double tempMax) {}

    public record UpdateRequest(Double rmsMax, Double tempMax) {}

    @GetMapping
    public List<ThresholdDto> findAll(@RequestParam(required = false) String search) {
        return thresholdService.findAll(search);
    }

    @GetMapping("/{id}")
    public ThresholdDto findById(@PathVariable Long id) {
        return thresholdService.findById(id);
    }

    @GetMapping("/by-asset/{assetId}")
    public ThresholdDto findByAsset(@PathVariable Long assetId) {
        return thresholdService.findByAssetId(assetId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThresholdDto create(@RequestBody CreateRequest req) {
        return thresholdService.create(req.assetId(), req.rmsMax(), req.tempMax());
    }

    @PutMapping("/{id}")
    public ThresholdDto update(@PathVariable Long id, @RequestBody UpdateRequest req) {
        return thresholdService.update(id, req.rmsMax(), req.tempMax());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        thresholdService.delete(id);
    }
}
