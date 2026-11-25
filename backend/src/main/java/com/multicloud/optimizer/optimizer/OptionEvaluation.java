package com.multicloud.optimizer.optimizer;

import com.multicloud.optimizer.domain.Price;
import java.math.BigDecimal;

public class OptionEvaluation {
    private Price priceOption;
    private int instancesNeeded;
    private BigDecimal computeCost;
    private BigDecimal egressCost;
    private BigDecimal interruptionCost;
    private BigDecimal totalCost;
    private BigDecimal estFinishHours;
    private boolean feasible;
    private String rejectionReason;

    public OptionEvaluation() {}

    public OptionEvaluation(Price priceOption, int instancesNeeded, BigDecimal computeCost, BigDecimal egressCost, BigDecimal interruptionCost, BigDecimal totalCost, BigDecimal estFinishHours, boolean feasible, String rejectionReason) {
        this.priceOption = priceOption;
        this.instancesNeeded = instancesNeeded;
        this.computeCost = computeCost;
        this.egressCost = egressCost;
        this.interruptionCost = interruptionCost;
        this.totalCost = totalCost;
        this.estFinishHours = estFinishHours;
        this.feasible = feasible;
        this.rejectionReason = rejectionReason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Price priceOption;
        private int instancesNeeded;
        private BigDecimal computeCost = BigDecimal.ZERO;
        private BigDecimal egressCost = BigDecimal.ZERO;
        private BigDecimal interruptionCost = BigDecimal.ZERO;
        private BigDecimal totalCost = BigDecimal.ZERO;
        private BigDecimal estFinishHours = BigDecimal.ZERO;
        private boolean feasible;
        private String rejectionReason;

        public Builder priceOption(Price priceOption) { this.priceOption = priceOption; return this; }
        public Builder instancesNeeded(int instancesNeeded) { this.instancesNeeded = instancesNeeded; return this; }
        public Builder computeCost(BigDecimal computeCost) { this.computeCost = computeCost; return this; }
        public Builder egressCost(BigDecimal egressCost) { this.egressCost = egressCost; return this; }
        public Builder interruptionCost(BigDecimal interruptionCost) { this.interruptionCost = interruptionCost; return this; }
        public Builder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
        public Builder estFinishHours(BigDecimal estFinishHours) { this.estFinishHours = estFinishHours; return this; }
        public Builder feasible(boolean feasible) { this.feasible = feasible; return this; }
        public Builder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }

        public OptionEvaluation build() {
            return new OptionEvaluation(priceOption, instancesNeeded, computeCost, egressCost, interruptionCost, totalCost, estFinishHours, feasible, rejectionReason);
        }
    }

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

    public boolean isFeasible() { return feasible; }
    public void setFeasible(boolean feasible) { this.feasible = feasible; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
