package com.multicloud.optimizer.domain;

import com.multicloud.optimizer.domain.enums.PricingModel;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "price",
    uniqueConstraints = @UniqueConstraint(name = "uq_instance_region_model", columnNames = {"instance_type_id", "region", "pricing_model"})
)
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "instance_type_id", nullable = false)
    private InstanceType instanceType;

    @Column(nullable = false, length = 64)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model", nullable = false, length = 16)
    private PricingModel pricingModel;

    @Column(name = "hourly_price_usd", nullable = false, precision = 12, scale = 4)
    private BigDecimal hourlyPriceUsd;

    @Column(name = "interruption_rate_pct", nullable = false, precision = 6, scale = 2)
    private BigDecimal interruptionRatePct;

    @Column(name = "avg_queue_minutes", nullable = false)
    private Integer avgQueueMinutes;

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    public Price() {}

    public Price(Long id, InstanceType instanceType, String region, PricingModel pricingModel, BigDecimal hourlyPriceUsd, BigDecimal interruptionRatePct, Integer avgQueueMinutes, LocalDate asOfDate) {
        this.id = id;
        this.instanceType = instanceType;
        this.region = region;
        this.pricingModel = pricingModel;
        this.hourlyPriceUsd = hourlyPriceUsd;
        this.interruptionRatePct = interruptionRatePct;
        this.avgQueueMinutes = avgQueueMinutes;
        this.asOfDate = asOfDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private InstanceType instanceType;
        private String region;
        private PricingModel pricingModel;
        private BigDecimal hourlyPriceUsd;
        private BigDecimal interruptionRatePct;
        private Integer avgQueueMinutes;
        private LocalDate asOfDate;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder instanceType(InstanceType instanceType) { this.instanceType = instanceType; return this; }
        public Builder region(String region) { this.region = region; return this; }
        public Builder pricingModel(PricingModel pricingModel) { this.pricingModel = pricingModel; return this; }
        public Builder hourlyPriceUsd(BigDecimal hourlyPriceUsd) { this.hourlyPriceUsd = hourlyPriceUsd; return this; }
        public Builder interruptionRatePct(BigDecimal interruptionRatePct) { this.interruptionRatePct = interruptionRatePct; return this; }
        public Builder avgQueueMinutes(Integer avgQueueMinutes) { this.avgQueueMinutes = avgQueueMinutes; return this; }
        public Builder asOfDate(LocalDate asOfDate) { this.asOfDate = asOfDate; return this; }

        public Price build() {
            return new Price(id, instanceType, region, pricingModel, hourlyPriceUsd, interruptionRatePct, avgQueueMinutes, asOfDate);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InstanceType getInstanceType() { return instanceType; }
    public void setInstanceType(InstanceType instanceType) { this.instanceType = instanceType; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public PricingModel getPricingModel() { return pricingModel; }
    public void setPricingModel(PricingModel pricingModel) { this.pricingModel = pricingModel; }

    public BigDecimal getHourlyPriceUsd() { return hourlyPriceUsd; }
    public void setHourlyPriceUsd(BigDecimal hourlyPriceUsd) { this.hourlyPriceUsd = hourlyPriceUsd; }

    public BigDecimal getInterruptionRatePct() { return interruptionRatePct; }
    public void setInterruptionRatePct(BigDecimal interruptionRatePct) { this.interruptionRatePct = interruptionRatePct; }

    public Integer getAvgQueueMinutes() { return avgQueueMinutes; }
    public void setAvgQueueMinutes(Integer avgQueueMinutes) { this.avgQueueMinutes = avgQueueMinutes; }

    public LocalDate getAsOfDate() { return asOfDate; }
    public void setAsOfDate(LocalDate asOfDate) { this.asOfDate = asOfDate; }
}
