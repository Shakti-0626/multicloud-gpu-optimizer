package com.multicloud.optimizer.controller;

import com.multicloud.optimizer.dto.JobRequest;
import com.multicloud.optimizer.dto.RecommendationResponse;
import com.multicloud.optimizer.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommend")
@Tag(name = "Recommendation", description = "Instant multi-cloud GPU placement recommendation without persistence")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    @Operation(summary = "Get ranked GPU placements and baseline comparisons for an AI job")
    public ResponseEntity<RecommendationResponse> getRecommendation(@Valid @RequestBody JobRequest request) {
        RecommendationResponse response = recommendationService.recommendWithoutSaving(request);
        return ResponseEntity.ok(response);
    }
}
