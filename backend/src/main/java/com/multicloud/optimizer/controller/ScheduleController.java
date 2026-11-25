package com.multicloud.optimizer.controller;

import com.multicloud.optimizer.dto.BatchScheduleRequestDto;
import com.multicloud.optimizer.dto.BatchScheduleResponseDto;
import com.multicloud.optimizer.service.SchedulingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
@Tag(name = "Batch Scheduler", description = "Capacity-aware Multi-Job Optimization using OR-Tools CP-SAT ILP")
public class ScheduleController {

    private final SchedulingService schedulingService;

    public ScheduleController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @PostMapping
    @Operation(summary = "Optimize a batch of AI jobs under regional GPU capacity constraints")
    public ResponseEntity<BatchScheduleResponseDto> scheduleBatch(@Valid @RequestBody BatchScheduleRequestDto request) {
        BatchScheduleResponseDto response = schedulingService.scheduleBatch(request);
        return ResponseEntity.ok(response);
    }
}
