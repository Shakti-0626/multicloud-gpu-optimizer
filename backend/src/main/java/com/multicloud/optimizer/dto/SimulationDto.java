package com.multicloud.optimizer.dto;

import java.util.List;
import java.util.Map;

public class SimulationDto {

    private String scenarioName;
    private int jobCount;
    private long executionTimeMs;

    private List<StrategyMetric> strategyMetrics;
    private Map<String, Double> savingsVsBaselines;
    private Map<String, Double> cloudPlacementDistribution; // AWS%, GCP%, AZURE%

    public SimulationDto() {}

    public SimulationDto(String scenarioName, int jobCount, long executionTimeMs, List<StrategyMetric> strategyMetrics, Map<String, Double> savingsVsBaselines, Map<String, Double> cloudPlacementDistribution) {
        this.scenarioName = scenarioName;
        this.jobCount = jobCount;
        this.executionTimeMs = executionTimeMs;
        this.strategyMetrics = strategyMetrics;
        this.savingsVsBaselines = savingsVsBaselines;
        this.cloudPlacementDistribution = cloudPlacementDistribution;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String scenarioName;
        private int jobCount;
        private long executionTimeMs;
        private List<StrategyMetric> strategyMetrics;
        private Map<String, Double> savingsVsBaselines;
        private Map<String, Double> cloudPlacementDistribution;

        public Builder scenarioName(String scenarioName) { this.scenarioName = scenarioName; return this; }
        public Builder jobCount(int jobCount) { this.jobCount = jobCount; return this; }
        public Builder executionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; return this; }
        public Builder strategyMetrics(List<StrategyMetric> strategyMetrics) { this.strategyMetrics = strategyMetrics; return this; }
        public Builder savingsVsBaselines(Map<String, Double> savingsVsBaselines) { this.savingsVsBaselines = savingsVsBaselines; return this; }
        public Builder cloudPlacementDistribution(Map<String, Double> cloudPlacementDistribution) { this.cloudPlacementDistribution = cloudPlacementDistribution; return this; }

        public SimulationDto build() {
            return new SimulationDto(scenarioName, jobCount, executionTimeMs, strategyMetrics, savingsVsBaselines, cloudPlacementDistribution);
        }
    }

    public static class StrategyMetric {
        private String strategy;
        private double totalCostUsd;
        private double computeCostUsd;
        private double egressCostUsd;
        private double interruptionCostUsd;
        private double deadlineHitRatePct;
        private double spotUsagePct;
        private double egressSharePct;
        private long runtimeMs;

        public StrategyMetric() {}

        public StrategyMetric(String strategy, double totalCostUsd, double computeCostUsd, double egressCostUsd, double interruptionCostUsd, double deadlineHitRatePct, double spotUsagePct, double egressSharePct, long runtimeMs) {
            this.strategy = strategy;
            this.totalCostUsd = totalCostUsd;
            this.computeCostUsd = computeCostUsd;
            this.egressCostUsd = egressCostUsd;
            this.interruptionCostUsd = interruptionCostUsd;
            this.deadlineHitRatePct = deadlineHitRatePct;
            this.spotUsagePct = spotUsagePct;
            this.egressSharePct = egressSharePct;
            this.runtimeMs = runtimeMs;
        }

        public static MetricBuilder builder() {
            return new MetricBuilder();
        }

        public static class MetricBuilder {
            private String strategy;
            private double totalCostUsd;
            private double computeCostUsd;
            private double egressCostUsd;
            private double interruptionCostUsd;
            private double deadlineHitRatePct;
            private double spotUsagePct;
            private double egressSharePct;
            private long runtimeMs;

            public MetricBuilder strategy(String strategy) { this.strategy = strategy; return this; }
            public MetricBuilder totalCostUsd(double totalCostUsd) { this.totalCostUsd = totalCostUsd; return this; }
            public MetricBuilder computeCostUsd(double computeCostUsd) { this.computeCostUsd = computeCostUsd; return this; }
            public MetricBuilder egressCostUsd(double egressCostUsd) { this.egressCostUsd = egressCostUsd; return this; }
            public MetricBuilder interruptionCostUsd(double interruptionCostUsd) { this.interruptionCostUsd = interruptionCostUsd; return this; }
            public MetricBuilder deadlineHitRatePct(double deadlineHitRatePct) { this.deadlineHitRatePct = deadlineHitRatePct; return this; }
            public MetricBuilder spotUsagePct(double spotUsagePct) { this.spotUsagePct = spotUsagePct; return this; }
            public MetricBuilder egressSharePct(double egressSharePct) { this.egressSharePct = egressSharePct; return this; }
            public MetricBuilder runtimeMs(long runtimeMs) { this.runtimeMs = runtimeMs; return this; }

            public StrategyMetric build() {
                return new StrategyMetric(strategy, totalCostUsd, computeCostUsd, egressCostUsd, interruptionCostUsd, deadlineHitRatePct, spotUsagePct, egressSharePct, runtimeMs);
            }
        }

        public String getStrategy() { return strategy; }
        public void setStrategy(String strategy) { this.strategy = strategy; }

        public double getTotalCostUsd() { return totalCostUsd; }
        public void setTotalCostUsd(double totalCostUsd) { this.totalCostUsd = totalCostUsd; }

        public double getComputeCostUsd() { return computeCostUsd; }
        public void setComputeCostUsd(double computeCostUsd) { this.computeCostUsd = computeCostUsd; }

        public double getEgressCostUsd() { return egressCostUsd; }
        public void setEgressCostUsd(double egressCostUsd) { this.egressCostUsd = egressCostUsd; }

        public double getInterruptionCostUsd() { return interruptionCostUsd; }
        public void setInterruptionCostUsd(double interruptionCostUsd) { this.interruptionCostUsd = interruptionCostUsd; }

        public double getDeadlineHitRatePct() { return deadlineHitRatePct; }
        public void setDeadlineHitRatePct(double deadlineHitRatePct) { this.deadlineHitRatePct = deadlineHitRatePct; }

        public double getSpotUsagePct() { return spotUsagePct; }
        public void setSpotUsagePct(double spotUsagePct) { this.spotUsagePct = spotUsagePct; }

        public double getEgressSharePct() { return egressSharePct; }
        public void setEgressSharePct(double egressSharePct) { this.egressSharePct = egressSharePct; }

        public long getRuntimeMs() { return runtimeMs; }
        public void setRuntimeMs(long runtimeMs) { this.runtimeMs = runtimeMs; }
    }

    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }

    public int getJobCount() { return jobCount; }
    public void setJobCount(int jobCount) { this.jobCount = jobCount; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public List<StrategyMetric> getStrategyMetrics() { return strategyMetrics; }
    public void setStrategyMetrics(List<StrategyMetric> strategyMetrics) { this.strategyMetrics = strategyMetrics; }

    public Map<String, Double> getSavingsVsBaselines() { return savingsVsBaselines; }
    public void setSavingsVsBaselines(Map<String, Double> savingsVsBaselines) { this.savingsVsBaselines = savingsVsBaselines; }

    public Map<String, Double> getCloudPlacementDistribution() { return cloudPlacementDistribution; }
    public void setCloudPlacementDistribution(Map<String, Double> cloudPlacementDistribution) { this.cloudPlacementDistribution = cloudPlacementDistribution; }
}
