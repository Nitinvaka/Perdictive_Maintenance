package com.PredictiveMaintenanceLite.controller;

import com.PredictiveMaintenanceLite.dto.AssetDto;
import com.PredictiveMaintenanceLite.entity.Asset;
import com.PredictiveMaintenanceLite.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    public record StatusRequest(Boolean active) {}

    @GetMapping
    public List<AssetDto> findAll(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String type) {
        return assetService.findAll(name, type);
    }

    @GetMapping("/types")
    public List<String> getAllTypes() {
        return assetService.findAllAssetTypes();
    }

    @GetMapping("/{id}")
    public AssetDto findById(@PathVariable Long id) {
        return assetService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetDto create(@RequestBody Asset asset) {
        return assetService.create(asset);
    }

    @PutMapping("/{id}")
    public AssetDto update(@PathVariable Long id, @RequestBody Asset asset) {
        return assetService.update(id, asset);
    }

    @PatchMapping("/{id}/status")
    public AssetDto setStatus(@PathVariable Long id, @RequestBody StatusRequest body) {
        // Default to true if the client didn't send the field
        boolean active = body.active() != null ? body.active() : true;
        return assetService.setStatus(id, active);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assetService.delete(id);
    }

    @GetMapping("/violations")
    public List<AssetDto> getViolations() {
        return assetService.findAssetsWithViolationsLast24Hours();
    }

    @GetMapping("/avg-rms")
    public List<AssetService.AvgRmsRow> getAverageRms() {
        return assetService.getAverageRmsPerAssetLast30Days();
    }
}
