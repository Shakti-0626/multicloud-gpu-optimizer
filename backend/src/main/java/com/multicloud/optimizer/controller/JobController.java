package com.multicloud.optimizer.controller;

import com.multicloud.optimizer.domain.Job;
import com.multicloud.optimizer.dto.JobRequest;
import com.multicloud.optimizer.dto.RecommendationResponse;
import com.multicloud.optimizer.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "AI Job submission, placement persistence, and retrieval")
public class JobController {

    private final RecommendationService recommendationService;

    public JobController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    @Operation(summary = "Submit a job, persist recommendations, and return placement results")
    public ResponseEntity<RecommendationResponse> submitJob(@Valid @RequestBody JobRequest request) {
        RecommendationResponse response = recommendationService.submitJobAndRecommend(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch a previously submitted job with all evaluated placements")
    public ResponseEntity<Job> getJob(@PathVariable Long id) {
        Job job = recommendationService.getJobById(id);
        return ResponseEntity.ok(job);
    }
}
