package com.cognizant.PredictiveMaintenanceLite.controller;

import com.cognizant.PredictiveMaintenanceLite.dto.TicketDto;
import com.cognizant.PredictiveMaintenanceLite.enums.TicketStatus;
import com.cognizant.PredictiveMaintenanceLite.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // Request body for PATCH /status
    public record StatusRequest(String status) {}

    // Response for GET /count/open
    public record OpenCount(long openTickets) {}

    @GetMapping
    public Page<TicketDto> findAll(
            @RequestParam(required = false) TicketStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        // One endpoint handles both filtered and unfiltered with a ternary
        return status != null
                ? ticketService.findByStatus(status, pageable)
                : ticketService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public TicketDto findById(@PathVariable Long id) {
        // findEntityById throws 404 if not found — TicketDto.from wraps the result
        return TicketDto.from(ticketService.findEntityById(id));
    }

    @GetMapping("/by-asset/{assetId}")
    public List<TicketDto> findByAsset(@PathVariable Long assetId) {
        return ticketService.findByAssetId(assetId);
    }

    @GetMapping("/by-asset-name")
    public List<TicketDto> findByAssetName(@RequestParam String name) {
        return ticketService.findByAssetName(name);
    }

    @GetMapping("/active")
    public List<TicketDto> findActive() {
        return ticketService.findAllActive();
    }

    @GetMapping("/count/open")
    public OpenCount countOpen() {
        return new OpenCount(ticketService.countOpen());
    }

    @PatchMapping("/{id}/status")
    public TicketDto updateStatus(@PathVariable Long id, @RequestBody StatusRequest body) {
        return ticketService.updateStatus(id, TicketStatus.valueOf(body.status()));
    }
}
