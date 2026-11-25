package com.multicloud.optimizer.controller;

import com.multicloud.optimizer.domain.enums.PricingModel;
import com.multicloud.optimizer.dto.PriceDto;
import com.multicloud.optimizer.repository.PriceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@Tag(name = "Pricing", description = "Query multi-cloud GPU instances and price catalog")
public class PriceController {

    private final PriceRepository priceRepository;

    public PriceController(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @GetMapping
    @Operation(summary = "List multi-cloud GPU prices with optional filters")
    public ResponseEntity<List<PriceDto>> getPrices(
            @Parameter(description = "Filter by cloud provider (AWS, GCP, AZURE)")
            @RequestParam(required = false) String provider,

            @Parameter(description = "Filter by GPU model (e.g. A100, H100, T4, L4, A10G)")
            @RequestParam(required = false) String gpuModel,

            @Parameter(description = "Filter by region (e.g. us-east-1, us-central1, eastus)")
            @RequestParam(required = false) String region,

            @Parameter(description = "Filter by pricing model (ON_DEMAND, SPOT)")
            @RequestParam(required = false) PricingModel pricingModel
    ) {
        List<PriceDto> list = priceRepository.findByFilters(provider, gpuModel, region, pricingModel)
                .stream()
                .map(PriceDto::fromEntity)
                .toList();

        return ResponseEntity.ok(list);
    }
}
