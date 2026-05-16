package com.cognizant.PredictiveMaintenanceLite.service;

import com.cognizant.PredictiveMaintenanceLite.dto.ThresholdDto;
import com.cognizant.PredictiveMaintenanceLite.entity.Asset;
import com.cognizant.PredictiveMaintenanceLite.entity.Threshold;
import com.cognizant.PredictiveMaintenanceLite.exception.BusinessException;
import com.cognizant.PredictiveMaintenanceLite.exception.ResourceNotFoundException;
import com.cognizant.PredictiveMaintenanceLite.repository.AssetRepository;
import com.cognizant.PredictiveMaintenanceLite.repository.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThresholdService {

    private final ThresholdRepository thresholdRepository;
    private final AssetRepository     assetRepository;

    // Search by asset id (numeric) or asset name (text)
    public List<ThresholdDto> findAll(String search) {
        List<Threshold> list;
        if (search == null || search.isBlank()) {
            list = thresholdRepository.findAll();
        } else {
            String trimmed = search.trim();
            try {
                Long id = Long.parseLong(trimmed);
                list = thresholdRepository.findByAsset_Id(id).map(List::of).orElseGet(List::of);
            } catch (NumberFormatException ignored) {
                list = thresholdRepository.findByAsset_NameContainingIgnoreCase(trimmed);
            }
        }
        return list.stream().map(ThresholdDto::from).toList();
    }

    public ThresholdDto findById(Long id) {
        return ThresholdDto.from(
                thresholdRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Threshold", id))
        );
    }

    public ThresholdDto findByAssetId(Long assetId) {
        return ThresholdDto.from(
                thresholdRepository.findByAsset_Id(assetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Threshold for asset", assetId))
        );
    }

    @Transactional
    public ThresholdDto create(Long assetId, Double rmsMax, Double tempMax) {
        if (thresholdRepository.existsByAsset_Id(assetId)) {
            throw new BusinessException("Asset " + assetId + " already has a threshold. Use PUT to update it.");
        }
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId));

        Threshold t = new Threshold();
        t.setAsset(asset);
        t.setRmsMax(rmsMax);
        t.setTempMax(tempMax);
        return ThresholdDto.from(thresholdRepository.save(t));
    }

    @Transactional
    public ThresholdDto update(Long id, Double rmsMax, Double tempMax) {
        Threshold t = thresholdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Threshold", id));
        t.setRmsMax(rmsMax);
        t.setTempMax(tempMax);
        return ThresholdDto.from(thresholdRepository.save(t));
    }

    @Transactional
    public void delete(Long id) {
        if (!thresholdRepository.existsById(id)) {
            throw new ResourceNotFoundException("Threshold", id);
        }
        thresholdRepository.deleteById(id);
    }
}
