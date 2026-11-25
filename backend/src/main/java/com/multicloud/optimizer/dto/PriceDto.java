package com.multicloud.optimizer.dto;

import com.multicloud.optimizer.domain.Price;
import com.multicloud.optimizer.domain.enums.PricingModel;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PriceDto {
    private Long id;
    private String provider;
    private String instanceName;
    private String gpuModel;
    private Integer gpuCount;
    private Integer vcpu;
    private Integer memoryGb;
    private String region;
    private PricingModel pricingModel;
    private BigDecimal hourlyPriceUsd;
    private BigDecimal interruptionRatePct;
    private Integer avgQueueMinutes;
    private LocalDate asOfDate;

    public PriceDto() {}

    public PriceDto(Long id, String provider, String instanceName, String gpuModel, Integer gpuCount, Integer vcpu, Integer memoryGb, String region, PricingModel pricingModel, BigDecimal hourlyPriceUsd, BigDecimal interruptionRatePct, Integer avgQueueMinutes, LocalDate asOfDate) {
        this.id = id;
        this.provider = provider;
        this.instanceName = instanceName;
        this.gpuModel = gpuModel;
        this.gpuCount = gpuCount;
        this.vcpu = vcpu;
        this.memoryGb = memoryGb;
        this.region = region;
        this.pricingModel = pricingModel;
        this.hourlyPriceUsd = hourlyPriceUsd;
        this.interruptionRatePct = interruptionRatePct;
        this.avgQueueMinutes = avgQueueMinutes;
        this.asOfDate = asOfDate;
    }

    public static PriceDto fromEntity(Price p) {
        return new PriceDto(
                p.getId(),
                p.getInstanceType().getProvider().getName(),
                p.getInstanceType().getInstanceName(),
                p.getInstanceType().getGpuModel(),
                p.getInstanceType().getGpuCount(),
                p.getInstanceType().getVcpu(),
                p.getInstanceType().getMemoryGb(),
                p.getRegion(),
                p.getPricingModel(),
                p.getHourlyPriceUsd(),
                p.getInterruptionRatePct(),
                p.getAvgQueueMinutes(),
                p.getAsOfDate()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }

    public String getGpuModel() { return gpuModel; }
    public void setGpuModel(String gpuModel) { this.gpuModel = gpuModel; }

    public Integer getGpuCount() { return gpuCount; }
    public void setGpuCount(Integer gpuCount) { this.gpuCount = gpuCount; }

    public Integer getVcpu() { return vcpu; }
    public void setVcpu(Integer vcpu) { this.vcpu = vcpu; }

    public Integer getMemoryGb() { return memoryGb; }
    public void setMemoryGb(Integer memoryGb) { this.memoryGb = memoryGb; }

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
