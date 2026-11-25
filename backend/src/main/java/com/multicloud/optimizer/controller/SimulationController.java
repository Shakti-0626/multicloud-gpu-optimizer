package com.multicloud.optimizer.controller;

import com.multicloud.optimizer.dto.SimulationDto;
import com.multicloud.optimizer.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulations")
@Tag(name = "Simulator", description = "Run multi-scenario simulations and workload comparisons")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping
    @Operation(summary = "Execute simulation on a workload scenario (e.g. mixed, training-heavy, inference-burst, capacity-crunch, spot-volatile)")
    public ResponseEntity<SimulationDto> runSimulation(
            @RequestParam(defaultValue = "mixed") String scenario,
            @RequestParam(defaultValue = "0") int jobCount
    ) {
        SimulationDto result = simulationService.runSimulation(scenario, jobCount);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{scenario}")
    @Operation(summary = "Get simulation metrics and benchmark charts for a scenario")
    public ResponseEntity<SimulationDto> getSimulationResult(@PathVariable String scenario) {
        SimulationDto result = simulationService.getCachedOrRun(scenario);
        return ResponseEntity.ok(result);
    }
}
