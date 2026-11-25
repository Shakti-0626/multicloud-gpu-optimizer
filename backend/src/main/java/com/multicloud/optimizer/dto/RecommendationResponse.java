package com.multicloud.optimizer.dto;

import com.multicloud.optimizer.domain.enums.PricingModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class RecommendationResponse {

    private Long jobId;
    private String jobName;
    private List<RecommendationItem> recommendations;
    private Map<String, BigDecimal> baselines;

    public RecommendationResponse() {}

    public RecommendationResponse(Long jobId, String jobName, List<RecommendationItem> recommendations, Map<String, BigDecimal> baselines) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.recommendations = recommendations;
        this.baselines = baselines;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long jobId;
        private String jobName;
        private List<RecommendationItem> recommendations;
        private Map<String, BigDecimal> baselines;

        public Builder jobId(Long jobId) { this.jobId = jobId; return this; }
        public Builder jobName(String jobName) { this.jobName = jobName; return this; }
        public Builder recommendations(List<RecommendationItem> recommendations) { this.recommendations = recommendations; return this; }
        public Builder baselines(Map<String, BigDecimal> baselines) { this.baselines = baselines; return this; }

        public RecommendationResponse build() {
            return new RecommendationResponse(jobId, jobName, recommendations, baselines);
        }
    }

    public static class RecommendationItem {
        private int rank;
        private String provider;
        private String region;
        private String instance;
        private PricingModel pricingModel;
        private int instancesNeeded;
        private BigDecimal computeCost;
        private BigDecimal egressCost;
        private BigDecimal interruptionCost;
        private BigDecimal totalCost;
        private BigDecimal estFinishHours;
        private String explanation;

        public RecommendationItem() {}

        public RecommendationItem(int rank, String provider, String region, String instance, PricingModel pricingModel, int instancesNeeded, BigDecimal computeCost, BigDecimal egressCost, BigDecimal interruptionCost, BigDecimal totalCost, BigDecimal estFinishHours, String explanation) {
            this.rank = rank;
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
            this.explanation = explanation;
        }

        public static ItemBuilder builder() {
            return new ItemBuilder();
        }

        public static class ItemBuilder {
            private int rank;
            private String provider;
            private String region;
            private String instance;
            private PricingModel pricingModel;
            private int instancesNeeded;
            private BigDecimal computeCost;
            private BigDecimal egressCost;
            private BigDecimal interruptionCost;
            private BigDecimal totalCost;
            private BigDecimal estFinishHours;
            private String explanation;

            public ItemBuilder rank(int rank) { this.rank = rank; return this; }
            public ItemBuilder provider(String provider) { this.provider = provider; return this; }
            public ItemBuilder region(String region) { this.region = region; return this; }
            public ItemBuilder instance(String instance) { this.instance = instance; return this; }
            public ItemBuilder pricingModel(PricingModel pricingModel) { this.pricingModel = pricingModel; return this; }
            public ItemBuilder instancesNeeded(int instancesNeeded) { this.instancesNeeded = instancesNeeded; return this; }
            public ItemBuilder computeCost(BigDecimal computeCost) { this.computeCost = computeCost; return this; }
            public ItemBuilder egressCost(BigDecimal egressCost) { this.egressCost = egressCost; return this; }
            public ItemBuilder interruptionCost(BigDecimal interruptionCost) { this.interruptionCost = interruptionCost; return this; }
            public ItemBuilder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
            public ItemBuilder estFinishHours(BigDecimal estFinishHours) { this.estFinishHours = estFinishHours; return this; }
            public ItemBuilder explanation(String explanation) { this.explanation = explanation; return this; }

            public RecommendationItem build() {
                return new RecommendationItem(rank, provider, region, instance, pricingModel, instancesNeeded, computeCost, egressCost, interruptionCost, totalCost, estFinishHours, explanation);
            }
        }

        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getInstance() { return instance; }
        public void setInstance(String instance) { this.instance = instance; }

        public PricingModel getPricingModel() { return pricingModel; }
        public void setPricingModel(PricingModel pricingModel) { this.pricingModel = pricingModel; }

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

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }

    public List<RecommendationItem> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationItem> recommendations) { this.recommendations = recommendations; }

    public Map<String, BigDecimal> getBaselines() { return baselines; }
    public void setBaselines(Map<String, BigDecimal> baselines) { this.baselines = baselines; }
}
