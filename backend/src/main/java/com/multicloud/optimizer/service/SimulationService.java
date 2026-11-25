package com.multicloud.optimizer.service;

import com.multicloud.optimizer.domain.Price;
import com.multicloud.optimizer.domain.enums.PricingModel;
import com.multicloud.optimizer.dto.BatchScheduleRequestDto;
import com.multicloud.optimizer.dto.BatchScheduleResponseDto;
import com.multicloud.optimizer.dto.JobRequest;
import com.multicloud.optimizer.dto.SimulationDto;
import com.multicloud.optimizer.optimizer.CostModel;
import com.multicloud.optimizer.optimizer.GreedyOptimizer;
import com.multicloud.optimizer.optimizer.OptionEvaluation;
import com.multicloud.optimizer.repository.PriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimulationService {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    private final PriceRepository priceRepository;
    private final RecommendationService recommendationService;
    private final SchedulingService schedulingService;
    private final CostModel costModel;

    private final Map<String, SimulationDto> simulationCache = new ConcurrentHashMap<>();

    public SimulationService(PriceRepository priceRepository, RecommendationService recommendationService, SchedulingService schedulingService, CostModel costModel) {
        this.priceRepository = priceRepository;
        this.recommendationService = recommendationService;
        this.schedulingService = schedulingService;
        this.costModel = costModel;
    }

    public SimulationDto runSimulation(String scenarioName, int customJobCount) {
        long startTime = System.currentTimeMillis();
        String scenarioKey = (scenarioName != null ? scenarioName.toLowerCase() : "mixed");
        int count = customJobCount > 0 ? customJobCount : getDefaultJobCount(scenarioKey);

        List<JobRequest> jobs = generateSyntheticJobs(scenarioKey, count);
        List<Price> candidatePrices = priceRepository.findAll();
        Map<String, BigDecimal> egressMap = recommendationService.buildEgressMap();

        Map<String, List<OptionEvaluation>> strategyResults = new HashMap<>();
        strategyResults.put("ALL_AWS", new ArrayList<>());
        strategyResults.put("ALL_GCP", new ArrayList<>());
        strategyResults.put("ALL_AZURE", new ArrayList<>());
        strategyResults.put("GREEDY", new ArrayList<>());

        long greedyStart = System.currentTimeMillis();
        for (JobRequest j : jobs) {
            OptionEvaluation bestAws = null;
            OptionEvaluation bestGcp = null;
            OptionEvaluation bestAzure = null;
            OptionEvaluation bestGreedy = null;

            for (Price p : candidatePrices) {
                String routeKey = GreedyOptimizer.getEgressKey(
                        j.getDataProvider(), j.getDataRegion(),
                        p.getInstanceType().getProvider().getName(), p.getRegion()
                );
                BigDecimal egressRate = egressMap.getOrDefault(routeKey, BigDecimal.ZERO);

                OptionEvaluation eval = costModel.evaluate(
                        p,
                        j.getGpuModel(),
                        j.getGpuCount(),
                        j.getDurationHours(),
                        j.getDataGb(),
                        j.getDataProvider(),
                        j.getDataRegion(),
                        j.getDeadlineHours(),
                        j.getSpotAllowed(),
                        j.getCheckpointing(),
                        egressRate
                );

                if (eval.isFeasible()) {
                    String prov = p.getInstanceType().getProvider().getName().toUpperCase();
                    if ("AWS".equals(prov)) {
                        if (bestAws == null || eval.getTotalCost().compareTo(bestAws.getTotalCost()) < 0) bestAws = eval;
                    } else if ("GCP".equals(prov)) {
                        if (bestGcp == null || eval.getTotalCost().compareTo(bestGcp.getTotalCost()) < 0) bestGcp = eval;
                    } else if ("AZURE".equals(prov)) {
                        if (bestAzure == null || eval.getTotalCost().compareTo(bestAzure.getTotalCost()) < 0) bestAzure = eval;
                    }

                    if (bestGreedy == null || eval.getTotalCost().compareTo(bestGreedy.getTotalCost()) < 0) {
                        bestGreedy = eval;
                    }
                }
            }

            if (bestAws != null) strategyResults.get("ALL_AWS").add(bestAws);
            if (bestGcp != null) strategyResults.get("ALL_GCP").add(bestGcp);
            if (bestAzure != null) strategyResults.get("ALL_AZURE").add(bestAzure);
            if (bestGreedy != null) strategyResults.get("GREEDY").add(bestGreedy);
        }
        long greedyRuntime = System.currentTimeMillis() - greedyStart;

        int gpuCap = scenarioKey.contains("crunch") ? 16 : 64;
        BatchScheduleRequestDto batchReq = BatchScheduleRequestDto.builder()
                .jobs(jobs)
                .defaultGpuCapacity(gpuCap)
                .build();

        BatchScheduleResponseDto ilpResponse = schedulingService.scheduleBatch(batchReq);

        List<SimulationDto.StrategyMetric> metrics = new ArrayList<>();
        metrics.add(buildMetricFromEvals("ALL_AWS", strategyResults.get("ALL_AWS"), jobs.size(), greedyRuntime / 4));
        metrics.add(buildMetricFromEvals("ALL_GCP", strategyResults.get("ALL_GCP"), jobs.size(), greedyRuntime / 4));
        metrics.add(buildMetricFromEvals("ALL_AZURE", strategyResults.get("ALL_AZURE"), jobs.size(), greedyRuntime / 4));
        metrics.add(buildMetricFromEvals("GREEDY", strategyResults.get("GREEDY"), jobs.size(), greedyRuntime));

        double ilpCompute = 0, ilpEgress = 0, ilpInterrupt = 0;
        int ilpSpotCount = 0;
        Map<String, Integer> cloudCount = new HashMap<>();

        for (BatchScheduleResponseDto.ScheduledPlacementItem item : ilpResponse.getPlacements()) {
            ilpCompute += item.getComputeCost().doubleValue();
            ilpEgress += item.getEgressCost().doubleValue();
            ilpInterrupt += item.getInterruptionCost().doubleValue();
            if ("SPOT".equalsIgnoreCase(item.getPricingModel())) ilpSpotCount++;
            cloudCount.put(item.getProvider().toUpperCase(), cloudCount.getOrDefault(item.getProvider().toUpperCase(), 0) + 1);
        }

        double ilpTotal = ilpResponse.getTotalCostUsd();
        double ilpDeadlineHit = ilpResponse.getPlacements().size() > 0
                ? ((double) ilpResponse.getPlacements().size() / jobs.size()) * 100.0 : 0.0;
        double ilpSpotPct = jobs.size() > 0 ? ((double) ilpSpotCount / jobs.size()) * 100.0 : 0.0;
        double ilpEgressShare = ilpTotal > 0 ? (ilpEgress / ilpTotal) * 100.0 : 0.0;

        metrics.add(SimulationDto.StrategyMetric.builder()
                .strategy("ILP")
                .totalCostUsd(Math.round(ilpTotal * 100.0) / 100.0)
                .computeCostUsd(Math.round(ilpCompute * 100.0) / 100.0)
                .egressCostUsd(Math.round(ilpEgress * 100.0) / 100.0)
                .interruptionCostUsd(Math.round(ilpInterrupt * 100.0) / 100.0)
                .deadlineHitRatePct(Math.round(ilpDeadlineHit * 10.0) / 10.0)
                .spotUsagePct(Math.round(ilpSpotPct * 10.0) / 10.0)
                .egressSharePct(Math.round(ilpEgressShare * 10.0) / 10.0)
                .runtimeMs(ilpResponse.getSolveTimeMs())
                .build());

        Map<String, Double> savings = new HashMap<>();
        double awsCost = metrics.get(0).getTotalCostUsd();
        double gcpCost = metrics.get(1).getTotalCostUsd();
        double azureCost = metrics.get(2).getTotalCostUsd();
        double greedyCost = metrics.get(3).getTotalCostUsd();

        savings.put("vsAllAws", awsCost > 0 ? Math.round(((awsCost - ilpTotal) / awsCost) * 1000.0) / 10.0 : 0.0);
        savings.put("vsAllGcp", gcpCost > 0 ? Math.round(((gcpCost - ilpTotal) / gcpCost) * 1000.0) / 10.0 : 0.0);
        savings.put("vsAllAzure", azureCost > 0 ? Math.round(((azureCost - ilpTotal) / azureCost) * 1000.0) / 10.0 : 0.0);
        savings.put("vsGreedy", greedyCost > 0 ? Math.round(((greedyCost - ilpTotal) / greedyCost) * 1000.0) / 10.0 : 0.0);

        Map<String, Double> dist = new HashMap<>();
        int placedCount = ilpResponse.getPlacements().size();
        dist.put("AWS", placedCount > 0 ? Math.round((cloudCount.getOrDefault("AWS", 0) * 100.0 / placedCount) * 10.0) / 10.0 : 0.0);
        dist.put("GCP", placedCount > 0 ? Math.round((cloudCount.getOrDefault("GCP", 0) * 100.0 / placedCount) * 10.0) / 10.0 : 0.0);
        dist.put("AZURE", placedCount > 0 ? Math.round((cloudCount.getOrDefault("AZURE", 0) * 100.0 / placedCount) * 10.0) / 10.0 : 0.0);

        long totalTime = System.currentTimeMillis() - startTime;

        SimulationDto simDto = SimulationDto.builder()
                .scenarioName(scenarioName.toUpperCase())
                .jobCount(jobs.size())
                .executionTimeMs(totalTime)
                .strategyMetrics(metrics)
                .savingsVsBaselines(savings)
                .cloudPlacementDistribution(dist)
                .build();

        simulationCache.put(scenarioKey, simDto);
        return simDto;
    }

    public SimulationDto getCachedOrRun(String scenarioName) {
        String key = scenarioName.toLowerCase();
        if (simulationCache.containsKey(key)) {
            return simulationCache.get(key);
        }
        return runSimulation(scenarioName, 0);
    }

    private SimulationDto.StrategyMetric buildMetricFromEvals(String strategy, List<OptionEvaluation> evals, int totalJobs, long runtimeMs) {
        double totalCost = 0, compute = 0, egress = 0, interrupt = 0;
        int spotCount = 0;

        for (OptionEvaluation e : evals) {
            totalCost += e.getTotalCost().doubleValue();
            compute += e.getComputeCost().doubleValue();
            egress += e.getEgressCost().doubleValue();
            interrupt += e.getInterruptionCost().doubleValue();
            if (e.getPriceOption().getPricingModel() == PricingModel.SPOT) spotCount++;
        }

        double deadlineHit = totalJobs > 0 ? ((double) evals.size() / totalJobs) * 100.0 : 0.0;
        double spotPct = evals.size() > 0 ? ((double) spotCount / evals.size()) * 100.0 : 0.0;
        double egressShare = totalCost > 0 ? (egress / totalCost) * 100.0 : 0.0;

        return SimulationDto.StrategyMetric.builder()
                .strategy(strategy)
                .totalCostUsd(Math.round(totalCost * 100.0) / 100.0)
                .computeCostUsd(Math.round(compute * 100.0) / 100.0)
                .egressCostUsd(Math.round(egress * 100.0) / 100.0)
                .interruptionCostUsd(Math.round(interrupt * 100.0) / 100.0)
                .deadlineHitRatePct(Math.round(deadlineHit * 10.0) / 10.0)
                .spotUsagePct(Math.round(spotPct * 10.0) / 10.0)
                .egressSharePct(Math.round(egressShare * 10.0) / 10.0)
                .runtimeMs(runtimeMs)
                .build();
    }

    private int getDefaultJobCount(String scenario) {
        return switch (scenario) {
            case "training-heavy" -> 50;
            case "inference-burst" -> 200;
            case "mixed", "capacity-crunch", "spot-volatile" -> 100;
            default -> 50;
        };
    }

    private List<JobRequest> generateSyntheticJobs(String scenario, int count) {
        Random rng = new Random(42);
        List<JobRequest> list = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String name = "job-" + i;
            String gpuModel;
            int gpuCount;
            double duration;
            double dataGb;
            double deadline;
            boolean spotAllowed = true;
            boolean checkpointing = true;

            String[] providers = {"AWS", "GCP", "AZURE"};
            String dataProvider = providers[rng.nextInt(providers.length)];
            String dataRegion = switch (dataProvider) {
                case "AWS" -> rng.nextBoolean() ? "us-east-1" : "us-west-2";
                case "GCP" -> rng.nextBoolean() ? "us-central1" : "europe-west4";
                default -> rng.nextBoolean() ? "eastus" : "westeurope";
            };

            switch (scenario) {
                case "training-heavy" -> {
                    gpuModel = rng.nextBoolean() ? "A100" : "H100";
                    gpuCount = rng.nextBoolean() ? 4 : 8;
                    duration = 8.0 + rng.nextDouble() * 40.0;
                    dataGb = 100.0 + rng.nextDouble() * 400.0;
                    deadline = duration * (1.8 + rng.nextDouble() * 1.5);
                }
                case "inference-burst" -> {
                    gpuModel = rng.nextBoolean() ? "T4" : (rng.nextBoolean() ? "L4" : "A10G");
                    gpuCount = rng.nextBoolean() ? 1 : 2;
                    duration = 0.5 + rng.nextDouble() * 1.5;
                    dataGb = 5.0 + rng.nextDouble() * 25.0;
                    deadline = duration * (1.2 + rng.nextDouble() * 0.5);
                }
                case "capacity-crunch" -> {
                    gpuModel = rng.nextBoolean() ? "A100" : "H100";
                    gpuCount = 8;
                    duration = 6.0 + rng.nextDouble() * 10.0;
                    dataGb = 50.0 + rng.nextDouble() * 150.0;
                    deadline = duration * 2.5;
                }
                case "spot-volatile" -> {
                    gpuModel = "A100";
                    gpuCount = 4;
                    duration = 4.0 + rng.nextDouble() * 12.0;
                    dataGb = 80.0 + rng.nextDouble() * 120.0;
                    deadline = duration * 3.0;
                    checkpointing = rng.nextBoolean();
                }
                default -> {
                    boolean isTraining = rng.nextDouble() < 0.4;
                    if (isTraining) {
                        gpuModel = rng.nextBoolean() ? "A100" : "H100";
                        gpuCount = rng.nextBoolean() ? 4 : 8;
                        duration = 10.0 + rng.nextDouble() * 20.0;
                        dataGb = 150.0 + rng.nextDouble() * 250.0;
                        deadline = duration * 2.0;
                    } else {
                        gpuModel = rng.nextBoolean() ? "T4" : "A10G";
                        gpuCount = 1;
                        duration = 1.0 + rng.nextDouble() * 3.0;
                        dataGb = 10.0 + rng.nextDouble() * 40.0;
                        deadline = duration * 1.8;
                    }
                }
            }

            list.add(JobRequest.builder()
                    .name(name)
                    .gpuModel(gpuModel)
                    .gpuCount(gpuCount)
                    .durationHours(BigDecimal.valueOf(duration).setScale(2, RoundingMode.HALF_UP))
                    .dataGb(BigDecimal.valueOf(dataGb).setScale(2, RoundingMode.HALF_UP))
                    .dataProvider(dataProvider)
                    .dataRegion(dataRegion)
                    .deadlineHours(BigDecimal.valueOf(deadline).setScale(2, RoundingMode.HALF_UP))
                    .spotAllowed(spotAllowed)
                    .checkpointing(checkpointing)
                    .build());
        }

        return list;
    }
}
