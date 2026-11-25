package com.multicloud.optimizer.optimizer;

import com.multicloud.optimizer.domain.CloudProvider;
import com.multicloud.optimizer.domain.InstanceType;
import com.multicloud.optimizer.domain.Price;
import com.multicloud.optimizer.domain.enums.PricingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CostModelTest {

    private CostModel costModel;
    private CloudProvider aws;
    private CloudProvider gcp;
    private InstanceType p4d;
    private InstanceType a2;

    @BeforeEach
    void setUp() {
        costModel = new CostModel();
        aws = CloudProvider.builder().id(1L).name("AWS").build();
        gcp = CloudProvider.builder().id(2L).name("GCP").build();

        p4d = InstanceType.builder()
                .id(1L)
                .provider(aws)
                .instanceName("p4d.24xlarge")
                .gpuModel("A100")
                .gpuCount(8)
                .vcpu(96)
                .memoryGb(1152)
                .build();

        a2 = InstanceType.builder()
                .id(2L)
                .provider(gcp)
                .instanceName("a2-highgpu-8g")
                .gpuModel("A100")
                .gpuCount(8)
                .vcpu(96)
                .memoryGb(680)
                .build();
    }

    @Test
    @DisplayName("Case 1: On-demand has exactly zero interruption cost")
    void testOnDemandComputeCostZeroInterruption() {
        Price p = Price.builder()
                .instanceType(p4d)
                .region("us-east-1")
                .pricingModel(PricingModel.ON_DEMAND)
                .hourlyPriceUsd(new BigDecimal("32.77"))
                .interruptionRatePct(BigDecimal.ZERO)
                .avgQueueMinutes(5)
                .asOfDate(LocalDate.now())
                .build();

        OptionEvaluation eval = costModel.evaluate(
                p, "A100", 8, new BigDecimal("10.0"), BigDecimal.ZERO,
                "AWS", "us-east-1", new BigDecimal("24.0"), true, true, BigDecimal.ZERO
        );

        assertTrue(eval.isFeasible());
        assertEquals(new BigDecimal("327.7000"), eval.getComputeCost());
        assertEquals(new BigDecimal("0.0000"), eval.getInterruptionCost());
        assertEquals(new BigDecimal("327.7000"), eval.getTotalCost());
    }

    @Test
    @DisplayName("Case 2: Spot compute cost with checkpointing (r = 0.1)")
    void testSpotComputeCostWithInterruptionAndCheckpointing() {
        Price p = Price.builder()
                .instanceType(p4d)
                .region("us-east-1")
                .pricingModel(PricingModel.SPOT)
                .hourlyPriceUsd(new BigDecimal("12.50"))
                .interruptionRatePct(new BigDecimal("15.00")) // 15%
                .avgQueueMinutes(20)
                .asOfDate(LocalDate.now())
                .build();

        OptionEvaluation eval = costModel.evaluate(
                p, "A100", 8, new BigDecimal("10.0"), BigDecimal.ZERO,
                "AWS", "us-east-1", new BigDecimal("24.0"), true, true, BigDecimal.ZERO
        );

        assertTrue(eval.isFeasible());
        assertEquals(new BigDecimal("125.0000"), eval.getComputeCost());
        // interruption: 125.00 * 0.15 * 0.10 = 1.875 -> 1.8750
        assertEquals(new BigDecimal("1.8750"), eval.getInterruptionCost());
        assertEquals(new BigDecimal("126.8750"), eval.getTotalCost());
    }

    @Test
    @DisplayName("Case 3: Spot compute cost without checkpointing (r = 0.5) has 5x higher risk penalty")
    void testSpotComputeCostWithoutCheckpointing() {
        Price p = Price.builder()
                .instanceType(p4d)
                .region("us-east-1")
                .pricingModel(PricingModel.SPOT)
                .hourlyPriceUsd(new BigDecimal("12.50"))
                .interruptionRatePct(new BigDecimal("15.00"))
                .avgQueueMinutes(20)
                .asOfDate(LocalDate.now())
                .build();

        OptionEvaluation eval = costModel.evaluate(
                p, "A100", 8, new BigDecimal("10.0"), BigDecimal.ZERO,
                "AWS", "us-east-1", new BigDecimal("24.0"), true, false, BigDecimal.ZERO
        );

        assertTrue(eval.isFeasible());
        // interruption: 125.00 * 0.15 * 0.50 = 9.3750
        assertEquals(new BigDecimal("9.3750"), eval.getInterruptionCost());
        assertEquals(new BigDecimal("134.3750"), eval.getTotalCost());
    }

    @Test
    @DisplayName("Case 4: Local data residing in same provider and region has zero egress cost")
    void testLocalDataZeroEgressCost() {
        Price p = Price.builder()
                .instanceType(p4d)
                .region("us-east-1")
                .pricingModel(PricingModel.ON_DEMAND)
                .hourlyPriceUsd(new BigDecimal("10.00"))
                .interruptionRatePct(BigDecimal.ZERO)
                .avgQueueMinutes(5)
                .asOfDate(LocalDate.now())
                .build();

        OptionEvaluation eval = costModel.evaluate(
                p, "A100", 8, new BigDecimal("1.0"), new BigDecimal("500.0"),
                "AWS", "us-east-1", new BigDecimal("24.0"), true, true, new BigDecimal("0.09")
        );

        assertEquals(new BigDecimal("0.0000"), eval.getEgressCost());
    }

    @Test
    @DisplayName("Case 5: Cross-cloud data transfer accurately assesses egress cost")
    void testCrossCloudEgressCost() {
        Price p = Price.builder()
                .instanceType(a2)
                .region("us-central1")
                .pricingModel(PricingModel.ON_DEMAND)
                .hourlyPriceUsd(new BigDecimal("29.40"))
                .interruptionRatePct(BigDecimal.ZERO)
                .avgQueueMinutes(5)
                .asOfDate(LocalDate.now())
                .build();

        // 200 GB moving from AWS us-east-1 to GCP us-central1 @ $0.09/GB = $18.00
        OptionEvaluation eval = costModel.evaluate(
                p, "A100", 8, new BigDecimal("1.0"), new BigDecimal("200.0"),
                "AWS", "us-east-1", new BigDecimal("24.0"), true, true, new BigDecimal("0.09")
        );

        assertEquals(new BigDecimal("18.0000"), eval.getEgressCost());
    }

    @Test
    @DisplayName("Case 6: Instance count rounding ceiling math")
    void testInstanceCountRoundingUp() {
        Price p = Price.builder()
                .instanceType(p4d) // 8 GPUs per instance
                .region("us-east-1")
                .pricingModel(PricingModel.ON_DEMAND)
                .hourlyPriceUsd(new BigDecimal("10.00"))
                .interruptionRatePct(BigDecimal.ZERO)
                .avgQueueMinutes(5)
                .asOfDate(LocalDate.now())
                .build();

        // 4 GPUs requested -> 1 instance needed
        OptionEvaluation eval1 = costModel.evaluate(
                p, "A100", 4, new BigDecimal("1.0"), BigDecimal.ZERO,
                "AWS", "us-east-1", new BigDecimal("24.0"), true, true, BigDecimal.ZERO
        );
        assertEquals(1, eval1.getInstancesNeeded());

        // 12 GPUs requested -> 2 instances needed (ceil(12/8) = 2)
        OptionEvaluation eval2 = costModel.evaluate(
                p, "A100", 12, new BigDecimal("1.0"), BigDecimal.ZERO,
                "AWS", "us-east-1", new BigDecimal("24.0"), true, true, BigDecimal.ZERO
        );
        assertEquals(2, eval2.getInstancesNeeded());
        assertEquals(new BigDecimal("20.0000"), eval2.getComputeCost());
    }

    @Test
    @DisplayName("Case 7: Deadline rejection when estimated finish time exceeds limit")
    void testDeadlineRejectionWhenTimeExceeded() {
        Price p = Price.builder()
                .instanceType(p4d)
                .region("us-east-1")
                .pricingModel(PricingModel.ON_DEMAND)
                .hourlyPriceUsd(new BigDecimal("32.77"))
                .interruptionRatePct(BigDecimal.ZERO)
                .avgQueueMinutes(30) // 0.5 hours queue
                .asOfDate(LocalDate.now())
                .build();

        // 10 hour duration + 0.5 hour queue = 10.5h > 10.0h deadline
        OptionEvaluation eval = costModel.evaluate(
                p, "A100", 8, new BigDecimal("10.0"), BigDecimal.ZERO,
                "AWS", "us-east-1", new BigDecimal("10.0"), true, true, BigDecimal.ZERO
        );

        assertFalse(eval.isFeasible());
        assertTrue(eval.getRejectionReason().contains("exceeds deadline"));
    }

    @Test
    @DisplayName("Case 8: Spot candidate rejected when job disallows spot")
    void testSpotRejectedWhenSpotNotAllowed() {
        Price p = Price.builder()
                .instanceType(p4d)
                .region("us-east-1")
                .pricingModel(PricingModel.SPOT)
                .hourlyPriceUsd(new BigDecimal("12.50"))
                .interruptionRatePct(new BigDecimal("15.00"))
                .avgQueueMinutes(10)
                .asOfDate(LocalDate.now())
                .build();

        OptionEvaluation eval = costModel.evaluate(
                p, "A100", 8, new BigDecimal("5.0"), BigDecimal.ZERO,
                "AWS", "us-east-1", new BigDecimal("24.0"), false, true, BigDecimal.ZERO
        );

        assertFalse(eval.isFeasible());
        assertEquals("Spot instances are not allowed for this job", eval.getRejectionReason());
    }

    @Test
    @DisplayName("Case 9: Incompatible GPU model rejected")
    void testGpuModelIncompatible() {
        InstanceType t4 = InstanceType.builder()
                .id(3L)
                .provider(aws)
                .instanceName("g4dn.xlarge")
                .gpuModel("T4")
                .gpuCount(1)
                .vcpu(4)
                .memoryGb(16)
                .build();

        Price p = Price.builder()
                .instanceType(t4)
                .region("us-east-1")
                .pricingModel(PricingModel.ON_DEMAND)
                .hourlyPriceUsd(new BigDecimal("0.50"))
                .interruptionRatePct(BigDecimal.ZERO)
                .avgQueueMinutes(5)
                .asOfDate(LocalDate.now())
                .build();

        // Job requests H100, but candidate only has T4
        OptionEvaluation eval = costModel.evaluate(
                p, "H100", 1, new BigDecimal("2.0"), BigDecimal.ZERO,
                "AWS", "us-east-1", new BigDecimal("24.0"), true, true, BigDecimal.ZERO
        );

        assertFalse(eval.isFeasible());
        assertTrue(eval.getRejectionReason().contains("incompatible with requested"));
    }

    @Test
    @DisplayName("Case 10: Higher-tier GPU substitution permitted")
    void testGpuSubstitutionAllowed() {
        // Job requests T4, candidate is A100 (which is an allowed upward substitute)
        assertTrue(costModel.isGpuCompatible("T4", "A100"));
        assertTrue(costModel.isGpuCompatible("A100", "H100"));
        assertFalse(costModel.isGpuCompatible("H100", "T4"));
    }
}
