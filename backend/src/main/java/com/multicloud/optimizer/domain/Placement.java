package com.multicloud.optimizer.domain;

import com.multicloud.optimizer.domain.enums.PricingModel;
import com.multicloud.optimizer.domain.enums.Strategy;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "placement")
public class Placement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "instance_type_id", nullable = false)
    private InstanceType instanceType;

    @Column(nullable = false, length = 64)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model", nullable = false, length = 16)
    private PricingModel pricingModel;

    @Column(name = "compute_cost", nullable = false, precision = 12, scale = 4)
    private BigDecimal computeCost;

    @Column(name = "egress_cost", nullable = false, precision = 12, scale = 4)
    private BigDecimal egressCost;

    @Column(name = "interruption_cost", nullable = false, precision = 12, scale = 4)
    private BigDecimal interruptionCost;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 4)
    private BigDecimal totalCost;

    @Column(name = "est_finish_hours", nullable = false, precision = 8, scale = 2)
    private BigDecimal estFinishHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Strategy strategy;

    @Column(nullable = false)
    private Integer rank;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    public Placement() {}

    public Placement(Long id, Job job, InstanceType instanceType, String region, PricingModel pricingModel, BigDecimal computeCost, BigDecimal egressCost, BigDecimal interruptionCost, BigDecimal totalCost, BigDecimal estFinishHours, Strategy strategy, Integer rank, String explanation) {
        this.id = id;
        this.job = job;
        this.instanceType = instanceType;
        this.region = region;
        this.pricingModel = pricingModel;
        this.computeCost = computeCost;
        this.egressCost = egressCost;
        this.interruptionCost = interruptionCost;
        this.totalCost = totalCost;
        this.estFinishHours = estFinishHours;
        this.strategy = strategy;
        this.rank = rank;
        this.explanation = explanation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Job job;
        private InstanceType instanceType;
        private String region;
        private PricingModel pricingModel;
        private BigDecimal computeCost;
        private BigDecimal egressCost;
        private BigDecimal interruptionCost;
        private BigDecimal totalCost;
        private BigDecimal estFinishHours;
        private Strategy strategy;
        private Integer rank;
        private String explanation;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder job(Job job) { this.job = job; return this; }
        public Builder instanceType(InstanceType instanceType) { this.instanceType = instanceType; return this; }
        public Builder region(String region) { this.region = region; return this; }
        public Builder pricingModel(PricingModel pricingModel) { this.pricingModel = pricingModel; return this; }
        public Builder computeCost(BigDecimal computeCost) { this.computeCost = computeCost; return this; }
        public Builder egressCost(BigDecimal egressCost) { this.egressCost = egressCost; return this; }
        public Builder interruptionCost(BigDecimal interruptionCost) { this.interruptionCost = interruptionCost; return this; }
        public Builder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
        public Builder estFinishHours(BigDecimal estFinishHours) { this.estFinishHours = estFinishHours; return this; }
        public Builder strategy(Strategy strategy) { this.strategy = strategy; return this; }
        public Builder rank(Integer rank) { this.rank = rank; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }

        public Placement build() {
            return new Placement(id, job, instanceType, region, pricingModel, computeCost, egressCost, interruptionCost, totalCost, estFinishHours, strategy, rank, explanation);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public InstanceType getInstanceType() { return instanceType; }
    public void setInstanceType(InstanceType instanceType) { this.instanceType = instanceType; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public PricingModel getPricingModel() { return pricingModel; }
    public void setPricingModel(PricingModel pricingModel) { this.pricingModel = pricingModel; }

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

    public Strategy getStrategy() { return strategy; }
    public void setStrategy(Strategy strategy) { this.strategy = strategy; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
