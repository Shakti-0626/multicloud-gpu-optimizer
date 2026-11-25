package com.multicloud.optimizer.optimizer;

import com.multicloud.optimizer.domain.Price;
import com.multicloud.optimizer.domain.enums.PricingModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GreedyOptimizer {

    private final CostModel costModel;
    private final ExplanationGenerator explanationGenerator;

    public GreedyOptimizer(CostModel costModel, ExplanationGenerator explanationGenerator) {
        this.costModel = costModel;
        this.explanationGenerator = explanationGenerator;
    }

    public record OptimizationResult(
            List<RankedOption> recommendations,
            Map<String, BigDecimal> baselines
    ) {}

    public record RankedOption(
            int rank,
            OptionEvaluation evaluation,
            String explanation
    ) {}

    public OptimizationResult optimizeSingleJob(
            String requestedGpuModel,
            int requestedGpuCount,
            BigDecimal durationHours,
            BigDecimal dataGb,
            String dataProvider,
            String dataRegion,
            BigDecimal deadlineHours,
            boolean spotAllowed,
            boolean checkpointing,
            List<Price> candidatePrices,
            Map<String, BigDecimal> egressRateMap
    ) {
        List<OptionEvaluation> allEvaluations = new ArrayList<>();

        for (Price price : candidatePrices) {
            String routeKey = getEgressKey(
                    dataProvider, dataRegion,
                    price.getInstanceType().getProvider().getName(), price.getRegion()
            );
            BigDecimal egressRate = egressRateMap.getOrDefault(routeKey, BigDecimal.ZERO);

            OptionEvaluation eval = costModel.evaluate(
                    price,
                    requestedGpuModel,
                    requestedGpuCount,
                    durationHours,
                    dataGb,
                    dataProvider,
                    dataRegion,
                    deadlineHours,
                    spotAllowed,
                    checkpointing,
                    egressRate
            );
            allEvaluations.add(eval);
        }

        List<OptionEvaluation> feasible = allEvaluations.stream()
                .filter(OptionEvaluation::isFeasible)
                .sorted(Comparator.comparing(OptionEvaluation::getTotalCost))
                .toList();

        Map<String, BigDecimal> baselines = new HashMap<>();
        for (String provider : List.of("AWS", "GCP", "AZURE")) {
            Optional<OptionEvaluation> cheapestProvider = feasible.stream()
                    .filter(e -> e.getPriceOption().getInstanceType().getProvider().getName().equalsIgnoreCase(provider))
                    .findFirst();

            if (cheapestProvider.isPresent()) {
                baselines.put(provider.toLowerCase(), cheapestProvider.get().getTotalCost());
            } else {
                baselines.put(provider.toLowerCase(), null);
            }
        }

        Map<String, OptionEvaluation> onDemandMap = allEvaluations.stream()
                .filter(e -> e.getPriceOption().getPricingModel() == PricingModel.ON_DEMAND)
                .collect(Collectors.toMap(
                        e -> e.getPriceOption().getInstanceType().getInstanceName() + "@" + e.getPriceOption().getRegion(),
                        e -> e,
                        (existing, replacement) -> existing
                ));

        List<RankedOption> ranked = new ArrayList<>();
        int rank = 1;
        for (OptionEvaluation eval : feasible.stream().limit(5).toList()) {
            String key = eval.getPriceOption().getInstanceType().getInstanceName() + "@" + eval.getPriceOption().getRegion();
            OptionEvaluation onDemandEq = onDemandMap.get(key);

            String explanation = explanationGenerator.generateExplanation(
                    eval,
                    onDemandEq,
                    dataProvider,
                    dataRegion,
                    checkpointing
            );

            ranked.add(new RankedOption(rank++, eval, explanation));
        }

        return new OptimizationResult(ranked, baselines);
    }

    public static String getEgressKey(String fromProvider, String fromRegion, String toProvider, String toRegion) {
        return (fromProvider + ":" + fromRegion + "->" + toProvider + ":" + toRegion).toUpperCase();
    }
}
