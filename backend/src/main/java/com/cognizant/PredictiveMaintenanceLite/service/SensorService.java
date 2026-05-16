package com.cognizant.PredictiveMaintenanceLite.service;

import com.cognizant.PredictiveMaintenanceLite.dto.SensorDto;
import com.cognizant.PredictiveMaintenanceLite.entity.Asset;
import com.cognizant.PredictiveMaintenanceLite.entity.Sensor;
import com.cognizant.PredictiveMaintenanceLite.exception.BusinessException;
import com.cognizant.PredictiveMaintenanceLite.exception.ResourceNotFoundException;
import com.cognizant.PredictiveMaintenanceLite.repository.AssetRepository;
import com.cognizant.PredictiveMaintenanceLite.repository.SensorRepository;
import com.cognizant.PredictiveMaintenanceLite.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository  sensorRepository;
    private final AssetRepository   assetRepository;
    private final TicketRepository  ticketRepository;

    public List<SensorDto> findAll(String search) {
        List<Sensor> sensors = (search == null || search.isBlank())
                ? sensorRepository.findAll()
                : sensorRepository.findByAsset_NameContainingIgnoreCase(search.trim());
        return sensors.stream().map(SensorDto::from).toList();
    }

    // Internal — returns entity so other services (ReadingService) can use it directly
    public Sensor findEntityById(Long id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
    }

    public SensorDto findById(Long id) {
        return SensorDto.from(findEntityById(id));
    }

    public List<SensorDto> findByAssetId(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", assetId);
        }
        return sensorRepository.findByAsset_Id(assetId)
                .stream().map(SensorDto::from).toList();
    }

    @Transactional
    public SensorDto create(Long assetId, Sensor incoming) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId));

        // Auto-generate a serial number if the client didn't provide one
        if (incoming.getSerialNumber() == null || incoming.getSerialNumber().isBlank()) {
            incoming.setSerialNumber(generateSerialNumber(asset, incoming.getName()));
        }
        if (sensorRepository.existsBySerialNumber(incoming.getSerialNumber())) {
            throw new BusinessException("Sensor with serial '" + incoming.getSerialNumber() + "' already exists");
        }
        if (sensorRepository.existsByNameAndAsset_Id(incoming.getName(), assetId)) {
            throw new BusinessException("Asset '" + asset.getName() + "' already has a sensor named '" + incoming.getName() + "'");
        }

        incoming.setId(null);
        incoming.setAsset(asset);
        return SensorDto.from(sensorRepository.save(incoming));
    }

    @Transactional
    public SensorDto update(Long id, Long assetId, Sensor incoming) {
        Sensor sensor = findEntityById(id);
        Asset  asset  = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId));

        // Keep the existing serial if the client didn't provide a new one
        if (incoming.getSerialNumber() == null || incoming.getSerialNumber().isBlank()) {
            incoming.setSerialNumber(sensor.getSerialNumber());
        }
        if (sensorRepository.existsBySerialNumberAndIdNot(incoming.getSerialNumber(), id)) {
            throw new BusinessException("Sensor with serial '" + incoming.getSerialNumber() + "' already exists");
        }
        if (sensorRepository.existsByNameAndAsset_IdAndIdNot(incoming.getName(), assetId, id)) {
            throw new BusinessException("Asset '" + asset.getName() + "' already has a sensor named '" + incoming.getName() + "'");
        }

        sensor.setAsset(asset);
        sensor.setName(incoming.getName());
        sensor.setSerialNumber(incoming.getSerialNumber());
        sensor.setSensorType(incoming.getSensorType());
        return SensorDto.from(sensorRepository.save(sensor));
    }

    @Transactional
    public SensorDto setStatus(Long id, boolean active) {
        Sensor sensor = findEntityById(id);
        sensor.setActive(active);
        return SensorDto.from(sensorRepository.save(sensor));
    }

    @Transactional
    public void delete(Long id) {
        if (!sensorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sensor", id);
        }
        // Tickets reference this sensor's readings — wipe tickets first
        ticketRepository.deleteBySensor_Id(id);
        sensorRepository.deleteById(id); // cascade removes readings
    }

    // Generates a unique serial: e.g. SN-PUMPA-VIB1-3F2A
    private String generateSerialNumber(Asset asset, String sensorName) {
        String assetTag = asset.getName().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (assetTag.length() > 8) assetTag = assetTag.substring(0, 8);

        String sensorTag = (sensorName != null ? sensorName : "S").toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (sensorTag.length() > 6) sensorTag = sensorTag.substring(0, 6);

        String suffix = Integer.toHexString((int) (System.nanoTime() & 0xFFFF)).toUpperCase();
        return "SN-" + assetTag + "-" + sensorTag + "-" + suffix;
    }
}
