package com.PredictiveMaintenanceLite.service;

import com.PredictiveMaintenanceLite.dto.AssetDto;
import com.PredictiveMaintenanceLite.entity.Asset;
import com.PredictiveMaintenanceLite.exception.BusinessException;
import com.PredictiveMaintenanceLite.exception.ResourceNotFoundException;
import com.PredictiveMaintenanceLite.repository.AssetRepository;
import com.PredictiveMaintenanceLite.repository.ReadingRepository;
import com.PredictiveMaintenanceLite.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class  AssetService {

    // Nested record for the avg-rms chart data — lives here because only this service uses it
    public record AvgRmsRow(Long assetId, String assetName, LocalDate date, Double averageRms) {}

    private final AssetRepository   assetRepository;
    private final TicketRepository  ticketRepository;
    private final ReadingRepository readingRepository;

    // Find assets with optional name and/or type filters
    public List<AssetDto> findAll(String name, String type) {
        String n = (name != null && !name.isBlank()) ? name.trim() : null;
        String t = (type != null && !type.isBlank()) ? type.trim() : null;

        List<Asset> assets;
        if (n != null && t != null) assets = assetRepository.findByNameContainingIgnoreCaseAndAssetTypeIgnoreCase(n, t);
        else if (n != null)         assets = assetRepository.findByNameContainingIgnoreCase(n);
        else if (t != null)         assets = assetRepository.findByAssetTypeIgnoreCase(t);
        else                        assets = assetRepository.findAll();

        // Convert each entity to a DTO before returning
        return assets.stream().map(AssetDto::from).toList();
    }

    public List<String> findAllAssetTypes() {
        return assetRepository.findAllDistinctTypes();
    }

    // Internal helper — returns entity (used by other services + update/delete)
    public Asset findEntityById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
    }

    public AssetDto findById(Long id) {
        return AssetDto.from(findEntityById(id));
    }

    @Transactional
    public AssetDto create(Asset asset) {
        if (assetRepository.existsByName(asset.getName())) {
            throw new BusinessException("An asset named '" + asset.getName() + "' already exists");
        }
        asset.setId(null); // prevent client from forcing a specific ID
        return AssetDto.from(assetRepository.save(asset));
    }

    @Transactional
    public AssetDto update(Long id, Asset incoming) {
        Asset asset = findEntityById(id);
        if (assetRepository.existsByNameAndIdNot(incoming.getName(), id)) {
            throw new BusinessException("An asset named '" + incoming.getName() + "' already exists");
        }
        asset.setName(incoming.getName());
        asset.setLocation(incoming.getLocation());
        asset.setAssetType(incoming.getAssetType());
        asset.setDescription(incoming.getDescription());
        return AssetDto.from(assetRepository.save(asset));
    }

    @Transactional
    public AssetDto setStatus(Long id, boolean active) {
        Asset asset = findEntityById(id);
        asset.setActive(active);
        return AssetDto.from(assetRepository.save(asset));
    }

    @Transactional
    public void delete(Long id) {
        if (!assetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asset", id);
        }
        // Delete order matters: tickets reference readings (NOT NULL), so tickets go first
        ticketRepository.deleteByAsset_Id(id);
        readingRepository.deleteBySensor_Asset_Id(id);
        assetRepository.deleteById(id); // cascade removes sensors and threshold
    }

    public List<AssetDto> findAssetsWithViolationsLast24Hours() {
        return assetRepository
                .findAssetsWithViolationsSince(LocalDateTime.now().minusHours(24))
                .stream().map(AssetDto::from).toList();
    }

    public List<AvgRmsRow> getAverageRmsPerAssetLast30Days() {
        return assetRepository.findAverageRmsPerAssetPerDay(LocalDateTime.now().minusDays(30));
    }
}
