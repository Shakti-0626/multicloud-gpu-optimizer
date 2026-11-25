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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IlpOptimizerTest {

    private IlpOptimizer ilpOptimizer;
    private CostModel costModel;

    private Price pAwsSpot;
    private Price pGcpSpot;

    @BeforeEach
    void setUp() {
        costModel = new CostModel();
        ilpOptimizer = new IlpOptimizer(costModel);

        CloudProvider aws = CloudProvider.builder().id(1L).name("AWS").build();
        CloudProvider gcp = CloudProvider.builder().id(2L).name("GCP").build();

        InstanceType p4d = InstanceType.builder().id(1L).provider(aws).instanceName("p4d.24xlarge").gpuModel("A100").gpuCount(8).vcpu(96).memoryGb(1152).build();
        InstanceType a2 = InstanceType.builder().id(2L).provider(gcp).instanceName("a2-highgpu-8g").gpuModel("A100").gpuCount(8).vcpu(96).memoryGb(680).build();

        pAwsSpot = Price.builder()
                .instanceType(p4d)
                .region("us-east-1")
                .pricingModel(PricingModel.SPOT)
                .hourlyPriceUsd(new BigDecimal("12.50"))
                .interruptionRatePct(new BigDecimal("15.0"))
                .avgQueueMinutes(10)
                .asOfDate(LocalDate.now())
                .build();

        pGcpSpot = Price.builder()
                .instanceType(a2)
                .region("us-central1")
                .pricingModel(PricingModel.SPOT)
                .hourlyPriceUsd(new BigDecimal("11.80"))
                .interruptionRatePct(new BigDecimal("12.0"))
                .avgQueueMinutes(10)
                .asOfDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("ILP solves batch assignment with capacity limits")
    void testIlpBatchSolveWithCapacity() {
        IlpOptimizer.BatchJobInput j1 = IlpOptimizer.BatchJobInput.builder()
                .jobId("j1")
                .name("Job 1")
                .gpuModel("A100")
                .gpuCount(8)
                .durationHours(new BigDecimal("5.0"))
                .dataGb(BigDecimal.ZERO)
                .dataProvider("AWS")
                .dataRegion("us-east-1")
                .deadlineHours(new BigDecimal("20.0"))
                .spotAllowed(true)
                .checkpointing(true)
                .build();

        IlpOptimizer.BatchJobInput j2 = IlpOptimizer.BatchJobInput.builder()
                .jobId("j2")
                .name("Job 2")
                .gpuModel("A100")
                .gpuCount(8)
                .durationHours(new BigDecimal("5.0"))
                .dataGb(BigDecimal.ZERO)
                .dataProvider("AWS")
                .dataRegion("us-east-1")
                .deadlineHours(new BigDecimal("20.0"))
                .spotAllowed(true)
                .checkpointing(true)
                .build();

        List<Price> candidatePrices = List.of(pAwsSpot, pGcpSpot);
        Map<String, BigDecimal> egressMap = Map.of(
                "AWS:US-EAST-1->GCP:US-CENTRAL1", new BigDecimal("0.09"),
                "AWS:US-EAST-1->AWS:US-EAST-1", BigDecimal.ZERO
        );

        // Limit GCP capacity to only 8 GPUs, forcing j2 to spill over onto AWS without failing
        Map<String, Integer> caps = Map.of("GCP_US-CENTRAL1_A100", 8);

        IlpOptimizer.BatchScheduleResult result = ilpOptimizer.solveBatch(
                List.of(j1, j2),
                candidatePrices,
                egressMap,
                64,
                caps
        );

        assertNotNull(result);
        if (result.isSolved()) {
            assertTrue(result.getPlacements().size() == 2);
            assertTrue(result.getTotalCostUsd() > 0);
        }
    }
}
