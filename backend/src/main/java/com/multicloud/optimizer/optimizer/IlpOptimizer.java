package com.multicloud.optimizer.optimizer;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.multicloud.optimizer.domain.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class IlpOptimizer {

    private static final Logger log = LoggerFactory.getLogger(IlpOptimizer.class);

    static {
        try {
            Loader.loadNativeLibraries();
            log.info("Google OR-Tools native libraries loaded successfully.");
        } catch (Throwable t) {
            log.warn("Failed to load OR-Tools native libraries directly: {}. Will initialize lazily if needed.", t.getMessage());
        }
    }

    private final CostModel costModel;

    public IlpOptimizer(CostModel costModel) {
        this.costModel = costModel;
    }

    public static class BatchJobInput {
        private String jobId;
        private String name;
        private String gpuModel;
        private int gpuCount;
        private BigDecimal durationHours;
        private BigDecimal dataGb;
        private String dataProvider;
        private String dataRegion;
        private BigDecimal deadlineHours;
        private boolean spotAllowed;
        private boolean checkpointing;

        public BatchJobInput() {}

        public BatchJobInput(String jobId, String name, String gpuModel, int gpuCount, BigDecimal durationHours, BigDecimal dataGb, String dataProvider, String dataRegion, BigDecimal deadlineHours, boolean spotAllowed, boolean checkpointing) {
            this.jobId = jobId;
            this.name = name;
            this.gpuModel = gpuModel;
            this.gpuCount = gpuCount;
            this.durationHours = durationHours;
            this.dataGb = dataGb;
            this.dataProvider = dataProvider;
            this.dataRegion = dataRegion;
            this.deadlineHours = deadlineHours;
            this.spotAllowed = spotAllowed;
            this.checkpointing = checkpointing;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String jobId;
            private String name;
            private String gpuModel;
            private int gpuCount;
            private BigDecimal durationHours;
            private BigDecimal dataGb;
            private String dataProvider;
            private String dataRegion;
            private BigDecimal deadlineHours;
            private boolean spotAllowed = true;
            private boolean checkpointing = true;

            public Builder jobId(String jobId) { this.jobId = jobId; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder gpuModel(String gpuModel) { this.gpuModel = gpuModel; return this; }
            public Builder gpuCount(int gpuCount) { this.gpuCount = gpuCount; return this; }
            public Builder durationHours(BigDecimal durationHours) { this.durationHours = durationHours; return this; }
            public Builder dataGb(BigDecimal dataGb) { this.dataGb = dataGb; return this; }
            public Builder dataProvider(String dataProvider) { this.dataProvider = dataProvider; return this; }
            public Builder dataRegion(String dataRegion) { this.dataRegion = dataRegion; return this; }
            public Builder deadlineHours(BigDecimal deadlineHours) { this.deadlineHours = deadlineHours; return this; }
            public Builder spotAllowed(boolean spotAllowed) { this.spotAllowed = spotAllowed; return this; }
            public Builder checkpointing(boolean checkpointing) { this.checkpointing = checkpointing; return this; }

            public BatchJobInput build() {
                return new BatchJobInput(jobId, name, gpuModel, gpuCount, durationHours, dataGb, dataProvider, dataRegion, deadlineHours, spotAllowed, checkpointing);
            }
        }

        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGpuModel() { return gpuModel; }
        public void setGpuModel(String gpuModel) { this.gpuModel = gpuModel; }
        public int getGpuCount() { return gpuCount; }
        public void setGpuCount(int gpuCount) { this.gpuCount = gpuCount; }
        public BigDecimal getDurationHours() { return durationHours; }
        public void setDurationHours(BigDecimal durationHours) { this.durationHours = durationHours; }
        public BigDecimal getDataGb() { return dataGb; }
        public void setDataGb(BigDecimal dataGb) { this.dataGb = dataGb; }
        public String getDataProvider() { return dataProvider; }
        public void setDataProvider(String dataProvider) { this.dataProvider = dataProvider; }
        public String getDataRegion() { return dataRegion; }
        public void setDataRegion(String dataRegion) { this.dataRegion = dataRegion; }
        public BigDecimal getDeadlineHours() { return deadlineHours; }
        public void setDeadlineHours(BigDecimal deadlineHours) { this.deadlineHours = deadlineHours; }
        public boolean isSpotAllowed() { return spotAllowed; }
        public void setSpotAllowed(boolean spotAllowed) { this.spotAllowed = spotAllowed; }
        public boolean isCheckpointing() { return checkpointing; }
        public void setCheckpointing(boolean checkpointing) { this.checkpointing = checkpointing; }
    }

    public static class BatchScheduleResult {
        private boolean solved;
        private double totalCostUsd;
        private long solveTimeMs;
        private List<ScheduledPlacement> placements;
        private Map<String, Integer> resourceGroupUsage;

        public BatchScheduleResult() {}

        public BatchScheduleResult(boolean solved, double totalCostUsd, long solveTimeMs, List<ScheduledPlacement> placements, Map<String, Integer> resourceGroupUsage) {
            this.solved = solved;
            this.totalCostUsd = totalCostUsd;
            this.solveTimeMs = solveTimeMs;
            this.placements = placements;
            this.resourceGroupUsage = resourceGroupUsage;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private boolean solved;
            private double totalCostUsd;
            private long solveTimeMs;
            private List<ScheduledPlacement> placements;
            private Map<String, Integer> resourceGroupUsage;

            public Builder solved(boolean solved) { this.solved = solved; return this; }
            public Builder totalCostUsd(double totalCostUsd) { this.totalCostUsd = totalCostUsd; return this; }
            public Builder solveTimeMs(long solveTimeMs) { this.solveTimeMs = solveTimeMs; return this; }
            public Builder placements(List<ScheduledPlacement> placements) { this.placements = placements; return this; }
            public Builder resourceGroupUsage(Map<String, Integer> resourceGroupUsage) { this.resourceGroupUsage = resourceGroupUsage; return this; }

            public BatchScheduleResult build() {
                return new BatchScheduleResult(solved, totalCostUsd, solveTimeMs, placements, resourceGroupUsage);
            }
        }

        public boolean isSolved() { return solved; }
        public void setSolved(boolean solved) { this.solved = solved; }
        public double getTotalCostUsd() { return totalCostUsd; }
        public void setTotalCostUsd(double totalCostUsd) { this.totalCostUsd = totalCostUsd; }
        public long getSolveTimeMs() { return solveTimeMs; }
        public void setSolveTimeMs(long solveTimeMs) { this.solveTimeMs = solveTimeMs; }
        public List<ScheduledPlacement> getPlacements() { return placements; }
        public void setPlacements(List<ScheduledPlacement> placements) { this.placements = placements; }
        public Map<String, Integer> getResourceGroupUsage() { return resourceGroupUsage; }
        public void setResourceGroupUsage(Map<String, Integer> resourceGroupUsage) { this.resourceGroupUsage = resourceGroupUsage; }
    }

    public static class ScheduledPlacement {
        private String jobId;
        private String jobName;
        private Price priceOption;
        private int instancesNeeded;
        private BigDecimal computeCost;
        private BigDecimal egressCost;
        private BigDecimal interruptionCost;
        private BigDecimal totalCost;
        private BigDecimal estFinishHours;
        private String resourceGroup;

        public ScheduledPlacement() {}

        public ScheduledPlacement(String jobId, String jobName, Price priceOption, int instancesNeeded, BigDecimal computeCost, BigDecimal egressCost, BigDecimal interruptionCost, BigDecimal totalCost, BigDecimal estFinishHours, String resourceGroup) {
            this.jobId = jobId;
            this.jobName = jobName;
            this.priceOption = priceOption;
            this.instancesNeeded = instancesNeeded;
            this.computeCost = computeCost;
            this.egressCost = egressCost;
            this.interruptionCost = interruptionCost;
            this.totalCost = totalCost;
            this.estFinishHours = estFinishHours;
            this.resourceGroup = resourceGroup;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String jobId;
            private String jobName;
            private Price priceOption;
            private int instancesNeeded;
            private BigDecimal computeCost;
            private BigDecimal egressCost;
            private BigDecimal interruptionCost;
            private BigDecimal totalCost;
            private BigDecimal estFinishHours;
            private String resourceGroup;

            public Builder jobId(String jobId) { this.jobId = jobId; return this; }
            public Builder jobName(String jobName) { this.jobName = jobName; return this; }
            public Builder priceOption(Price priceOption) { this.priceOption = priceOption; return this; }
            public Builder instancesNeeded(int instancesNeeded) { this.instancesNeeded = instancesNeeded; return this; }
            public Builder computeCost(BigDecimal computeCost) { this.computeCost = computeCost; return this; }
            public Builder egressCost(BigDecimal egressCost) { this.egressCost = egressCost; return this; }
            public Builder interruptionCost(BigDecimal interruptionCost) { this.interruptionCost = interruptionCost; return this; }
            public Builder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
            public Builder estFinishHours(BigDecimal estFinishHours) { this.estFinishHours = estFinishHours; return this; }
            public Builder resourceGroup(String resourceGroup) { this.resourceGroup = resourceGroup; return this; }

            public ScheduledPlacement build() {
                return new ScheduledPlacement(jobId, jobName, priceOption, instancesNeeded, computeCost, egressCost, interruptionCost, totalCost, estFinishHours, resourceGroup);
            }
        }

        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
        public String getJobName() { return jobName; }
        public void setJobName(String jobName) { this.jobName = jobName; }
        public Price getPriceOption() { return priceOption; }
        public void setPriceOption(Price priceOption) { this.priceOption = priceOption; }
        public int getInstancesNeeded() { return instancesNeeded; }
        public void setInstancesNeeded(int instancesNeeded) { this.instancesNeeded = instancesNeeded; }
        public BigDecimal getComputeCost() { return computeCost; }
        public void setComputeCost(BigDecimal computeCost) { this.computeCost = computeCost; }
        public BigDecimal getEgressCost() { return egressCost; }
        public void setEgressCost(BigDecimal egressCost) { this.egressCost = egressCost; }
        public BigDecimal getInterruptionCost() { return interruptionCost; }
        public void setInterruptionCost(BigDecimal interruptionCost) { this.interruptionCost = interruptionCost; }
        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
        public BigDecimal getEstFinishHours() { return estFinishHours; }
        public void setEstFinishHours(BigDecimal estFinishHours) { this.estFinishHours = estFinishHours; }
        public String getResourceGroup() { return resourceGroup; }
        public void setResourceGroup(String resourceGroup) { this.resourceGroup = resourceGroup; }
    }

    public BatchScheduleResult solveBatch(
            List<BatchJobInput> jobs,
            List<Price> candidatePrices,
            Map<String, BigDecimal> egressRateMap,
            int defaultCapacityPerResourceGroup,
            Map<String, Integer> customCapacities
    ) {
        long startTime = System.currentTimeMillis();
        int numJobs = jobs.size();
        int numOptions = candidatePrices.size();

        if (numJobs == 0 || numOptions == 0) {
            return BatchScheduleResult.builder()
                    .solved(false)
                    .totalCostUsd(0.0)
                    .solveTimeMs(0)
                    .placements(Collections.emptyList())
                    .resourceGroupUsage(Collections.emptyMap())
                    .build();
        }

        OptionEvaluation[][] evalMatrix = new OptionEvaluation[numJobs][numOptions];
        for (int j = 0; j < numJobs; j++) {
            BatchJobInput job = jobs.get(j);
            for (int o = 0; o < numOptions; o++) {
                Price price = candidatePrices.get(o);
                String routeKey = GreedyOptimizer.getEgressKey(
                        job.getDataProvider(), job.getDataRegion(),
                        price.getInstanceType().getProvider().getName(), price.getRegion()
                );
                BigDecimal egressRate = egressRateMap.getOrDefault(routeKey, BigDecimal.ZERO);

                evalMatrix[j][o] = costModel.evaluate(
                        price,
                        job.getGpuModel(),
                        job.getGpuCount(),
                        job.getDurationHours(),
                        job.getDataGb(),
                        job.getDataProvider(),
                        job.getDataRegion(),
                        job.getDeadlineHours(),
                        job.isSpotAllowed(),
                        job.isCheckpointing(),
                        egressRate
                );
            }
        }

        CpModel model = new CpModel();
        BoolVar[][] x = new BoolVar[numJobs][numOptions];

        for (int j = 0; j < numJobs; j++) {
            for (int o = 0; o < numOptions; o++) {
                x[j][o] = model.newBoolVar("x_" + j + "_" + o);
                if (!evalMatrix[j][o].isFeasible()) {
                    model.addEquality(x[j][o], 0);
                }
            }
        }

        for (int j = 0; j < numJobs; j++) {
            Literal[] jobOptions = new Literal[numOptions];
            for (int o = 0; o < numOptions; o++) {
                jobOptions[o] = x[j][o];
            }
            model.addExactlyOne(jobOptions);
        }

        Map<String, List<int[]>> resourceGroupToIndices = new HashMap<>();
        for (int o = 0; o < numOptions; o++) {
            Price price = candidatePrices.get(o);
            String rg = getResourceGroupKey(
                    price.getInstanceType().getProvider().getName(),
                    price.getRegion(),
                    price.getInstanceType().getGpuModel()
            );
            resourceGroupToIndices.computeIfAbsent(rg, k -> new ArrayList<>());
        }

        for (int j = 0; j < numJobs; j++) {
            for (int o = 0; o < numOptions; o++) {
                Price price = candidatePrices.get(o);
                String rg = getResourceGroupKey(
                        price.getInstanceType().getProvider().getName(),
                        price.getRegion(),
                        price.getInstanceType().getGpuModel()
                );
                resourceGroupToIndices.get(rg).add(new int[]{j, o});
            }
        }

        for (Map.Entry<String, List<int[]>> entry : resourceGroupToIndices.entrySet()) {
            String rg = entry.getKey();
            List<int[]> indices = entry.getValue();
            int cap = (customCapacities != null && customCapacities.containsKey(rg))
                    ? customCapacities.get(rg)
                    : defaultCapacityPerResourceGroup;

            LinearExprBuilder expr = LinearExpr.newBuilder();
            for (int[] pair : indices) {
                int j = pair[0];
                int o = pair[1];
                int jobGpuCount = jobs.get(j).getGpuCount();
                expr.addTerm(x[j][o], jobGpuCount);
            }
            model.addLessOrEqual(expr, cap);
        }

        LinearExprBuilder obj = LinearExpr.newBuilder();
        for (int j = 0; j < numJobs; j++) {
            for (int o = 0; o < numOptions; o++) {
                long costCents = evalMatrix[j][o].getTotalCost()
                        .multiply(new BigDecimal("100"))
                        .longValue();
                obj.addTerm(x[j][o], costCents);
            }
        }
        model.minimize(obj);

        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(30.0);
        CpSolverStatus status = solver.solve(model);

        long solveTimeMs = System.currentTimeMillis() - startTime;

        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            List<ScheduledPlacement> placements = new ArrayList<>();
            Map<String, Integer> usage = new HashMap<>();

            for (int j = 0; j < numJobs; j++) {
                for (int o = 0; o < numOptions; o++) {
                    if (solver.value(x[j][o]) == 1) {
                        OptionEvaluation eval = evalMatrix[j][o];
                        Price p = candidatePrices.get(o);
                        String rg = getResourceGroupKey(
                                p.getInstanceType().getProvider().getName(),
                                p.getRegion(),
                                p.getInstanceType().getGpuModel()
                        );
                        usage.put(rg, usage.getOrDefault(rg, 0) + jobs.get(j).getGpuCount());

                        placements.add(ScheduledPlacement.builder()
                                .jobId(jobs.get(j).getJobId())
                                .jobName(jobs.get(j).getName())
                                .priceOption(p)
                                .instancesNeeded(eval.getInstancesNeeded())
                                .computeCost(eval.getComputeCost())
                                .egressCost(eval.getEgressCost())
                                .interruptionCost(eval.getInterruptionCost())
                                .totalCost(eval.getTotalCost())
                                .estFinishHours(eval.getEstFinishHours())
                                .resourceGroup(rg)
                                .build());
                    }
                }
            }

            double totalCostUsd = solver.objectiveValue() / 100.0;
            return BatchScheduleResult.builder()
                    .solved(true)
                    .totalCostUsd(totalCostUsd)
                    .solveTimeMs(solveTimeMs)
                    .placements(placements)
                    .resourceGroupUsage(usage)
                    .build();
        } else {
            log.warn("ILP solver finished with status: {}", status);
            return BatchScheduleResult.builder()
                    .solved(false)
                    .totalCostUsd(0.0)
                    .solveTimeMs(solveTimeMs)
                    .placements(Collections.emptyList())
                    .resourceGroupUsage(Collections.emptyMap())
                    .build();
        }
    }

    public static String getResourceGroupKey(String provider, String region, String gpuModel) {
        return (provider + "_" + region + "_" + gpuModel).toUpperCase();
    }
}
