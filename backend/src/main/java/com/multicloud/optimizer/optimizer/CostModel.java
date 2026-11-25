package com.multicloud.optimizer.optimizer;

import com.multicloud.optimizer.domain.Price;
import com.multicloud.optimizer.domain.enums.PricingModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

/**
 * Mathematical Cost Model for Multicloud AI Workload Optimization.
 * Encapsulates Compute, Egress, Interruption risk penalty, and Feasibility checks.
 */
@Component
public class CostModel {

    // Assumed network throughput for cross-region/cloud data transfer:
    // 1 Gbps = 0.125 GB/s = 450 GB/hour
    private static final BigDecimal NETWORK_TRANSFER_GB_PER_HOUR = new BigDecimal("450.0");

    // Allowed GPU substitutions (e.g. higher-tier GPUs can substitute lower-tier)
    private static final Map<String, Set<String>> SUBSTITUTION_MAP = Map.of(
            "T4", Set.of("T4", "L4", "A10G", "A100", "H100"),
            "L4", Set.of("L4", "A10G", "A100", "H100"),
            "A10G", Set.of("A10G", "A100", "H100"),
            "A100", Set.of("A100", "H100"),
            "H100", Set.of("H100")
    );

    /**
     * Evaluates a candidate price option against the job specifications.
     */
    public OptionEvaluation evaluate(
            Price price,
            String requestedGpuModel,
            int requestedGpuCount,
            BigDecimal durationHours,
            BigDecimal dataGb,
            String dataProvider,
            String dataRegion,
            BigDecimal deadlineHours,
            boolean spotAllowed,
            boolean checkpointing,
            BigDecimal egressRateUsdPerGb
    ) {
        String instanceGpuModel = price.getInstanceType().getGpuModel();
        int instanceGpuCount = price.getInstanceType().getGpuCount();

        // 1. Check GPU model compatibility
        if (!isGpuCompatible(requestedGpuModel, instanceGpuModel)) {
            return OptionEvaluation.builder()
                    .priceOption(price)
                    .feasible(false)
                    .computeCost(BigDecimal.ZERO)
                    .egressCost(BigDecimal.ZERO)
                    .interruptionCost(BigDecimal.ZERO)
                    .totalCost(BigDecimal.ZERO)
                    .estFinishHours(BigDecimal.ZERO)
                    .rejectionReason(String.format("GPU model %s incompatible with requested %s", instanceGpuModel, requestedGpuModel))
                    .build();
        }

        // 2. Check Spot permission
        if (price.getPricingModel() == PricingModel.SPOT && !spotAllowed) {
            return OptionEvaluation.builder()
                    .priceOption(price)
                    .feasible(false)
                    .computeCost(BigDecimal.ZERO)
                    .egressCost(BigDecimal.ZERO)
                    .interruptionCost(BigDecimal.ZERO)
                    .totalCost(BigDecimal.ZERO)
                    .estFinishHours(BigDecimal.ZERO)
                    .rejectionReason("Spot instances are not allowed for this job")
                    .build();
        }

        // 3. Instance count rounding: n_o = ceil(requestedGpuCount / instanceGpuCount)
        int instancesNeeded = (int) Math.ceil((double) requestedGpuCount / instanceGpuCount);

        // 4. Compute cost: C_compute = n_o * price_hourly * durationHours
        BigDecimal computeCost = price.getHourlyPriceUsd()
                .multiply(BigDecimal.valueOf(instancesNeeded))
                .multiply(durationHours)
                .setScale(4, RoundingMode.HALF_UP);

        // 5. Egress cost: C_egress = dataGb * egressRateUsdPerGb
        BigDecimal effectiveEgressRate = egressRateUsdPerGb != null ? egressRateUsdPerGb : BigDecimal.ZERO;
        String destProvider = price.getInstanceType().getProvider().getName();
        String destRegion = price.getRegion();

        boolean isLocal = dataProvider.equalsIgnoreCase(destProvider) && dataRegion.equalsIgnoreCase(destRegion);
        if (isLocal) {
            effectiveEgressRate = BigDecimal.ZERO;
        }

        BigDecimal egressCost = dataGb.multiply(effectiveEgressRate).setScale(4, RoundingMode.HALF_UP);

        // 6. Interruption cost: C_interrupt = p_o * r * C_compute
        BigDecimal p = BigDecimal.ZERO;
        if (price.getPricingModel() == PricingModel.SPOT) {
            // interruptionRatePct is given in percent (e.g., 15.00 -> 0.1500)
            p = price.getInterruptionRatePct().divide(new BigDecimal("100.0"), 4, RoundingMode.HALF_UP);
        }
        // r = fraction of work lost per interruption: 0.1 with checkpointing, 0.5 without
        BigDecimal r = checkpointing ? new BigDecimal("0.10") : new BigDecimal("0.50");

        BigDecimal interruptionCost = computeCost.multiply(p).multiply(r).setScale(4, RoundingMode.HALF_UP);

        // 7. Total cost: C_total = C_compute + C_egress + C_interrupt
        BigDecimal totalCost = computeCost.add(egressCost).add(interruptionCost).setScale(4, RoundingMode.HALF_UP);

        // 8. Estimated finish time calculation:
        // q_o (queue hours) + transferHours + duration * (1 + p * r)
        BigDecimal queueHours = BigDecimal.valueOf(price.getAvgQueueMinutes())
                .divide(new BigDecimal("60.0"), 4, RoundingMode.HALF_UP);

        BigDecimal transferHours = BigDecimal.ZERO;
        if (!isLocal && dataGb.compareTo(BigDecimal.ZERO) > 0) {
            transferHours = dataGb.divide(NETWORK_TRANSFER_GB_PER_HOUR, 4, RoundingMode.HALF_UP);
        }

        BigDecimal durationMultiplier = BigDecimal.ONE.add(p.multiply(r));
        BigDecimal effectiveComputeHours = durationHours.multiply(durationMultiplier).setScale(4, RoundingMode.HALF_UP);

        BigDecimal estFinishHours = queueHours.add(transferHours).add(effectiveComputeHours).setScale(2, RoundingMode.HALF_UP);

        // 9. Feasibility check: estFinishHours <= deadlineHours
        boolean feasible = estFinishHours.compareTo(deadlineHours) <= 0;
        String rejectionReason = feasible ? null : String.format(
                "Estimated completion (%.2fh) exceeds deadline (%.2fh)",
                estFinishHours.doubleValue(), deadlineHours.doubleValue()
        );

        return OptionEvaluation.builder()
                .priceOption(price)
                .instancesNeeded(instancesNeeded)
                .computeCost(computeCost)
                .egressCost(egressCost)
                .interruptionCost(interruptionCost)
                .totalCost(totalCost)
                .estFinishHours(estFinishHours)
                .feasible(feasible)
                .rejectionReason(rejectionReason)
                .build();
    }

    public boolean isGpuCompatible(String requested, String candidate) {
        if (requested == null || candidate == null) return false;
        String reqUpper = requested.toUpperCase();
        String candUpper = candidate.toUpperCase();

        if (reqUpper.equals(candUpper)) return true;
        Set<String> validSubs = SUBSTITUTION_MAP.get(reqUpper);
        return validSubs != null && validSubs.contains(candUpper);
    }
}
