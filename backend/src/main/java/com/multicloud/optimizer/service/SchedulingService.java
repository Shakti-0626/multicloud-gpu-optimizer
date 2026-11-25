package com.multicloud.optimizer.service;

import com.multicloud.optimizer.domain.Price;
import com.multicloud.optimizer.dto.BatchScheduleRequestDto;
import com.multicloud.optimizer.dto.BatchScheduleResponseDto;
import com.multicloud.optimizer.dto.JobRequest;
import com.multicloud.optimizer.optimizer.GreedyOptimizer;
import com.multicloud.optimizer.optimizer.IlpOptimizer;
import com.multicloud.optimizer.repository.PriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingService.class);

    private final PriceRepository priceRepository;
    private final RecommendationService recommendationService;
    private final IlpOptimizer ilpOptimizer;
    private final GreedyOptimizer greedyOptimizer;

    public SchedulingService(PriceRepository priceRepository, RecommendationService recommendationService, IlpOptimizer ilpOptimizer, GreedyOptimizer greedyOptimizer) {
        this.priceRepository = priceRepository;
        this.recommendationService = recommendationService;
        this.ilpOptimizer = ilpOptimizer;
        this.greedyOptimizer = greedyOptimizer;
    }

    @Transactional(readOnly = true)
    public BatchScheduleResponseDto scheduleBatch(BatchScheduleRequestDto req) {
        List<Price> candidatePrices = priceRepository.findAll();
        Map<String, BigDecimal> egressMap = recommendationService.buildEgressMap();

        List<IlpOptimizer.BatchJobInput> batchInputs = new ArrayList<>();
        double greedyTotalCost = 0.0;
        double allAwsTotalCost = 0.0;
        double allGcpTotalCost = 0.0;
        double allAzureTotalCost = 0.0;

        for (int i = 0; i < req.getJobs().size(); i++) {
            JobRequest j = req.getJobs().get(i);
            String jobId = "job-" + (i + 1);

            batchInputs.add(IlpOptimizer.BatchJobInput.builder()
                    .jobId(jobId)
                    .name(j.getName())
                    .gpuModel(j.getGpuModel())
                    .gpuCount(j.getGpuCount())
                    .durationHours(j.getDurationHours())
                    .dataGb(j.getDataGb())
                    .dataProvider(j.getDataProvider())
                    .dataRegion(j.getDataRegion())
                    .deadlineHours(j.getDeadlineHours())
                    .spotAllowed(j.getSpotAllowed())
                    .checkpointing(j.getCheckpointing())
                    .build());

            GreedyOptimizer.OptimizationResult greedyRes = greedyOptimizer.optimizeSingleJob(
                    j.getGpuModel(),
                    j.getGpuCount(),
                    j.getDurationHours(),
                    j.getDataGb(),
                    j.getDataProvider(),
                    j.getDataRegion(),
                    j.getDeadlineHours(),
                    j.getSpotAllowed(),
                    j.getCheckpointing(),
                    candidatePrices,
                    egressMap
            );

            if (!greedyRes.recommendations().isEmpty()) {
                greedyTotalCost += greedyRes.recommendations().get(0).evaluation().getTotalCost().doubleValue();
            }
            if (greedyRes.baselines().get("aws") != null) {
                allAwsTotalCost += greedyRes.baselines().get("aws").doubleValue();
            }
            if (greedyRes.baselines().get("gcp") != null) {
                allGcpTotalCost += greedyRes.baselines().get("gcp").doubleValue();
            }
            if (greedyRes.baselines().get("azure") != null) {
                allAzureTotalCost += greedyRes.baselines().get("azure").doubleValue();
            }
        }

        int capacity = req.getDefaultGpuCapacity() != null ? req.getDefaultGpuCapacity() : 64;

        IlpOptimizer.BatchScheduleResult ilpResult = ilpOptimizer.solveBatch(
                batchInputs,
                candidatePrices,
                egressMap,
                capacity,
                req.getCustomCapacities()
        );

        if (!ilpResult.isSolved()) {
            return BatchScheduleResponseDto.builder()
                    .solved(false)
                    .totalCostUsd(0.0)
                    .solveTimeMs(ilpResult.getSolveTimeMs())
                    .totalJobs(req.getJobs().size())
                    .placements(Collections.emptyList())
                    .resourceGroupUsage(Collections.emptyMap())
                    .build();
        }

        double ilpCost = ilpResult.getTotalCostUsd();

        double savingsVsGreedy = greedyTotalCost > 0 ? ((greedyTotalCost - ilpCost) / greedyTotalCost) * 100.0 : 0.0;
        double savingsVsAws = allAwsTotalCost > 0 ? ((allAwsTotalCost - ilpCost) / allAwsTotalCost) * 100.0 : 0.0;
        double savingsVsGcp = allGcpTotalCost > 0 ? ((allGcpTotalCost - ilpCost) / allGcpTotalCost) * 100.0 : 0.0;
        double savingsVsAzure = allAzureTotalCost > 0 ? ((allAzureTotalCost - ilpCost) / allAzureTotalCost) * 100.0 : 0.0;

        List<BatchScheduleResponseDto.ScheduledPlacementItem> placementItems = ilpResult.getPlacements().stream()
                .map(p -> BatchScheduleResponseDto.ScheduledPlacementItem.builder()
                        .jobName(p.getJobName())
                        .provider(p.getPriceOption().getInstanceType().getProvider().getName())
                        .region(p.getPriceOption().getRegion())
                        .instance(p.getPriceOption().getInstanceType().getInstanceName())
                        .pricingModel(p.getPriceOption().getPricingModel().name())
                        .instancesNeeded(p.getInstancesNeeded())
                        .computeCost(p.getComputeCost())
                        .egressCost(p.getEgressCost())
                        .interruptionCost(p.getInterruptionCost())
                        .totalCost(p.getTotalCost())
                        .estFinishHours(p.getEstFinishHours())
                        .resourceGroup(p.getResourceGroup())
                        .build())
                .toList();

        return BatchScheduleResponseDto.builder()
                .solved(true)
                .totalCostUsd(Math.round(ilpCost * 100.0) / 100.0)
                .solveTimeMs(ilpResult.getSolveTimeMs())
                .totalJobs(req.getJobs().size())
                .savingsVsGreedyPct(Math.round(savingsVsGreedy * 10.0) / 10.0)
                .savingsVsAllAwsPct(Math.round(savingsVsAws * 10.0) / 10.0)
                .savingsVsAllGcpPct(Math.round(savingsVsGcp * 10.0) / 10.0)
                .savingsVsAllAzurePct(Math.round(savingsVsAzure * 10.0) / 10.0)
                .placements(placementItems)
                .resourceGroupUsage(ilpResult.getResourceGroupUsage())
                .build();
    }
}
