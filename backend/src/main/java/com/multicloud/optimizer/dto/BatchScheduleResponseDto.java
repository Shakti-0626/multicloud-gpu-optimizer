package com.multicloud.optimizer.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class BatchScheduleResponseDto {

    private boolean solved;
    private double totalCostUsd;
    private long solveTimeMs;
    private int totalJobs;
    private double savingsVsGreedyPct;
    private double savingsVsAllAwsPct;
    private double savingsVsAllGcpPct;
    private double savingsVsAllAzurePct;
    private List<ScheduledPlacementItem> placements;
    private Map<String, Integer> resourceGroupUsage;

    public BatchScheduleResponseDto() {}

    public BatchScheduleResponseDto(boolean solved, double totalCostUsd, long solveTimeMs, int totalJobs, double savingsVsGreedyPct, double savingsVsAllAwsPct, double savingsVsAllGcpPct, double savingsVsAllAzurePct, List<ScheduledPlacementItem> placements, Map<String, Integer> resourceGroupUsage) {
        this.solved = solved;
        this.totalCostUsd = totalCostUsd;
        this.solveTimeMs = solveTimeMs;
        this.totalJobs = totalJobs;
        this.savingsVsGreedyPct = savingsVsGreedyPct;
        this.savingsVsAllAwsPct = savingsVsAllAwsPct;
        this.savingsVsAllGcpPct = savingsVsAllGcpPct;
        this.savingsVsAllAzurePct = savingsVsAllAzurePct;
        this.placements = placements;
        this.resourceGroupUsage = resourceGroupUsage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean solved;
        private double totalCostUsd;
        private long solveTimeMs;
        private int totalJobs;
        private double savingsVsGreedyPct;
        private double savingsVsAllAwsPct;
        private double savingsVsAllGcpPct;
        private double savingsVsAllAzurePct;
        private List<ScheduledPlacementItem> placements;
        private Map<String, Integer> resourceGroupUsage;

        public Builder solved(boolean solved) { this.solved = solved; return this; }
        public Builder totalCostUsd(double totalCostUsd) { this.totalCostUsd = totalCostUsd; return this; }
        public Builder solveTimeMs(long solveTimeMs) { this.solveTimeMs = solveTimeMs; return this; }
        public Builder totalJobs(int totalJobs) { this.totalJobs = totalJobs; return this; }
        public Builder savingsVsGreedyPct(double savingsVsGreedyPct) { this.savingsVsGreedyPct = savingsVsGreedyPct; return this; }
        public Builder savingsVsAllAwsPct(double savingsVsAllAwsPct) { this.savingsVsAllAwsPct = savingsVsAllAwsPct; return this; }
        public Builder savingsVsAllGcpPct(double savingsVsAllGcpPct) { this.savingsVsAllGcpPct = savingsVsAllGcpPct; return this; }
        public Builder savingsVsAllAzurePct(double savingsVsAllAzurePct) { this.savingsVsAllAzurePct = savingsVsAllAzurePct; return this; }
        public Builder placements(List<ScheduledPlacementItem> placements) { this.placements = placements; return this; }
        public Builder resourceGroupUsage(Map<String, Integer> resourceGroupUsage) { this.resourceGroupUsage = resourceGroupUsage; return this; }

        public BatchScheduleResponseDto build() {
            return new BatchScheduleResponseDto(solved, totalCostUsd, solveTimeMs, totalJobs, savingsVsGreedyPct, savingsVsAllAwsPct, savingsVsAllGcpPct, savingsVsAllAzurePct, placements, resourceGroupUsage);
        }
    }

    public static class ScheduledPlacementItem {
        private String jobName;
        private String provider;
        private String region;
        private String instance;
        private String pricingModel;
        private int instancesNeeded;
        private BigDecimal computeCost;
        private BigDecimal egressCost;
        private BigDecimal interruptionCost;
        private BigDecimal totalCost;
        private BigDecimal estFinishHours;
        private String resourceGroup;

        public ScheduledPlacementItem() {}

        public ScheduledPlacementItem(String jobName, String provider, String region, String instance, String pricingModel, int instancesNeeded, BigDecimal computeCost, BigDecimal egressCost, BigDecimal interruptionCost, BigDecimal totalCost, BigDecimal estFinishHours, String resourceGroup) {
            this.jobName = jobName;
            this.provider = provider;
            this.region = region;
            this.instance = instance;
            this.pricingModel = pricingModel;
            this.instancesNeeded = instancesNeeded;
            this.computeCost = computeCost;
            this.egressCost = egressCost;
            this.interruptionCost = interruptionCost;
            this.totalCost = totalCost;
            this.estFinishHours = estFinishHours;
            this.resourceGroup = resourceGroup;
        }

        public static ItemBuilder builder() {
            return new ItemBuilder();
        }

        public static class ItemBuilder {
            private String jobName;
            private String provider;
            private String region;
            private String instance;
            private String pricingModel;
            private int instancesNeeded;
            private BigDecimal computeCost;
            private BigDecimal egressCost;
            private BigDecimal interruptionCost;
            private BigDecimal totalCost;
            private BigDecimal estFinishHours;
            private String resourceGroup;

            public ItemBuilder jobName(String jobName) { this.jobName = jobName; return this; }
            public ItemBuilder provider(String provider) { this.provider = provider; return this; }
            public ItemBuilder region(String region) { this.region = region; return this; }
            public ItemBuilder instance(String instance) { this.instance = instance; return this; }
            public ItemBuilder pricingModel(String pricingModel) { this.pricingModel = pricingModel; return this; }
            public ItemBuilder instancesNeeded(int instancesNeeded) { this.instancesNeeded = instancesNeeded; return this; }
            public ItemBuilder computeCost(BigDecimal computeCost) { this.computeCost = computeCost; return this; }
            public ItemBuilder egressCost(BigDecimal egressCost) { this.egressCost = egressCost; return this; }
            public ItemBuilder interruptionCost(BigDecimal interruptionCost) { this.interruptionCost = interruptionCost; return this; }
            public ItemBuilder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
            public ItemBuilder estFinishHours(BigDecimal estFinishHours) { this.estFinishHours = estFinishHours; return this; }
            public ItemBuilder resourceGroup(String resourceGroup) { this.resourceGroup = resourceGroup; return this; }

            public ScheduledPlacementItem build() {
                return new ScheduledPlacementItem(jobName, provider, region, instance, pricingModel, instancesNeeded, computeCost, egressCost, interruptionCost, totalCost, estFinishHours, resourceGroup);
            }
        }

        public String getJobName() { return jobName; }
        public void setJobName(String jobName) { this.jobName = jobName; }

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getInstance() { return instance; }
        public void setInstance(String instance) { this.instance = instance; }

        public String getPricingModel() { return pricingModel; }
        public void setPricingModel(String pricingModel) { this.pricingModel = pricingModel; }

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

    public boolean isSolved() { return solved; }
    public void setSolved(boolean solved) { this.solved = solved; }

    public double getTotalCostUsd() { return totalCostUsd; }
    public void setTotalCostUsd(double totalCostUsd) { this.totalCostUsd = totalCostUsd; }

    public long getSolveTimeMs() { return solveTimeMs; }
    public void setSolveTimeMs(long solveTimeMs) { this.solveTimeMs = solveTimeMs; }

    public int getTotalJobs() { return totalJobs; }
    public void setTotalJobs(int totalJobs) { this.totalJobs = totalJobs; }

    public double getSavingsVsGreedyPct() { return savingsVsGreedyPct; }
    public void setSavingsVsGreedyPct(double savingsVsGreedyPct) { this.savingsVsGreedyPct = savingsVsGreedyPct; }

    public double getSavingsVsAllAwsPct() { return savingsVsAllAwsPct; }
    public void setSavingsVsAllAwsPct(double savingsVsAllAwsPct) { this.savingsVsAllAwsPct = savingsVsAllAwsPct; }

    public double getSavingsVsAllGcpPct() { return savingsVsAllGcpPct; }
    public void setSavingsVsAllGcpPct(double savingsVsAllGcpPct) { this.savingsVsAllGcpPct = savingsVsAllGcpPct; }

    public double getSavingsVsAllAzurePct() { return savingsVsAllAzurePct; }
    public void setSavingsVsAllAzurePct(double savingsVsAllAzurePct) { this.savingsVsAllAzurePct = savingsVsAllAzurePct; }

    public List<ScheduledPlacementItem> getPlacements() { return placements; }
    public void setPlacements(List<ScheduledPlacementItem> placements) { this.placements = placements; }

    public Map<String, Integer> getResourceGroupUsage() { return resourceGroupUsage; }
    public void setResourceGroupUsage(Map<String, Integer> resourceGroupUsage) { this.resourceGroupUsage = resourceGroupUsage; }
}
