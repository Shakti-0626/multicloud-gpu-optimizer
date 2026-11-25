package com.multicloud.optimizer.service;

import com.multicloud.optimizer.domain.EgressRate;
import com.multicloud.optimizer.domain.Job;
import com.multicloud.optimizer.domain.Placement;
import com.multicloud.optimizer.domain.Price;
import com.multicloud.optimizer.domain.enums.Strategy;
import com.multicloud.optimizer.dto.JobRequest;
import com.multicloud.optimizer.dto.RecommendationResponse;
import com.multicloud.optimizer.optimizer.GreedyOptimizer;
import com.multicloud.optimizer.repository.EgressRateRepository;
import com.multicloud.optimizer.repository.JobRepository;
import com.multicloud.optimizer.repository.PlacementRepository;
import com.multicloud.optimizer.repository.PriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final PriceRepository priceRepository;
    private final EgressRateRepository egressRateRepository;
    private final JobRepository jobRepository;
    private final PlacementRepository placementRepository;
    private final GreedyOptimizer greedyOptimizer;

    public RecommendationService(PriceRepository priceRepository, EgressRateRepository egressRateRepository, JobRepository jobRepository, PlacementRepository placementRepository, GreedyOptimizer greedyOptimizer) {
        this.priceRepository = priceRepository;
        this.egressRateRepository = egressRateRepository;
        this.jobRepository = jobRepository;
        this.placementRepository = placementRepository;
        this.greedyOptimizer = greedyOptimizer;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse recommendWithoutSaving(JobRequest req) {
        List<Price> candidatePrices = priceRepository.findAll();
        Map<String, BigDecimal> egressMap = buildEgressMap();

        GreedyOptimizer.OptimizationResult result = greedyOptimizer.optimizeSingleJob(
                req.getGpuModel(),
                req.getGpuCount(),
                req.getDurationHours(),
                req.getDataGb(),
                req.getDataProvider(),
                req.getDataRegion(),
                req.getDeadlineHours(),
                req.getSpotAllowed(),
                req.getCheckpointing(),
                candidatePrices,
                egressMap
        );

        List<RecommendationResponse.RecommendationItem> items = result.recommendations().stream()
                .map(r -> RecommendationResponse.RecommendationItem.builder()
                        .rank(r.rank())
                        .provider(r.evaluation().getPriceOption().getInstanceType().getProvider().getName())
                        .region(r.evaluation().getPriceOption().getRegion())
                        .instance(r.evaluation().getPriceOption().getInstanceType().getInstanceName())
                        .pricingModel(r.evaluation().getPriceOption().getPricingModel())
                        .instancesNeeded(r.evaluation().getInstancesNeeded())
                        .computeCost(r.evaluation().getComputeCost())
                        .egressCost(r.evaluation().getEgressCost())
                        .interruptionCost(r.evaluation().getInterruptionCost())
                        .totalCost(r.evaluation().getTotalCost())
                        .estFinishHours(r.evaluation().getEstFinishHours())
                        .explanation(r.explanation())
                        .build())
                .toList();

        return RecommendationResponse.builder()
                .jobId(null)
                .jobName(req.getName())
                .recommendations(items)
                .baselines(result.baselines())
                .build();
    }

    @Transactional
    public RecommendationResponse submitJobAndRecommend(JobRequest req) {
        RecommendationResponse rec = recommendWithoutSaving(req);

        Job job = Job.builder()
                .name(req.getName())
                .gpuModel(req.getGpuModel().toUpperCase())
                .gpuCount(req.getGpuCount())
                .durationHours(req.getDurationHours())
                .dataGb(req.getDataGb())
                .dataProvider(req.getDataProvider().toUpperCase())
                .dataRegion(req.getDataRegion())
                .deadlineHours(req.getDeadlineHours())
                .spotAllowed(req.getSpotAllowed())
                .checkpointing(req.getCheckpointing())
                .submittedAt(LocalDateTime.now())
                .build();

        Job savedJob = jobRepository.save(job);
        rec.setJobId(savedJob.getId());

        for (RecommendationResponse.RecommendationItem item : rec.getRecommendations()) {
            priceRepository.findAll().stream()
                    .filter(p -> p.getInstanceType().getInstanceName().equalsIgnoreCase(item.getInstance())
                            && p.getRegion().equalsIgnoreCase(item.getRegion())
                            && p.getPricingModel() == item.getPricingModel())
                    .findFirst()
                    .ifPresent(p -> {
                        Placement placement = Placement.builder()
                                .job(savedJob)
                                .instanceType(p.getInstanceType())
                                .region(p.getRegion())
                                .pricingModel(p.getPricingModel())
                                .computeCost(item.getComputeCost())
                                .egressCost(item.getEgressCost())
                                .interruptionCost(item.getInterruptionCost())
                                .totalCost(item.getTotalCost())
                                .estFinishHours(item.getEstFinishHours())
                                .strategy(Strategy.GREEDY)
                                .rank(item.getRank())
                                .explanation(item.getExplanation())
                                .build();
                        placementRepository.save(placement);
                    });
        }

        return rec;
    }

    @Transactional(readOnly = true)
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));
    }

    public Map<String, BigDecimal> buildEgressMap() {
        List<EgressRate> rates = egressRateRepository.findAll();
        Map<String, BigDecimal> map = new HashMap<>();
        for (EgressRate r : rates) {
            String key = GreedyOptimizer.getEgressKey(r.getFromProvider(), r.getFromRegion(), r.getToProvider(), r.getToRegion());
            map.put(key, r.getUsdPerGb());
        }
        return map;
    }
}
