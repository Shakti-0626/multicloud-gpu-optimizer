package com.multicloud.optimizer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class JobRequest {

    @NotBlank(message = "Job name is required")
    private String name;

    @NotBlank(message = "GPU model is required (e.g. A100, H100, A10G, T4, L4)")
    private String gpuModel;

    @NotNull(message = "GPU count is required")
    @Min(value = 1, message = "GPU count must be at least 1")
    private Integer gpuCount;

    @NotNull(message = "Duration in hours is required")
    private BigDecimal durationHours;

    @NotNull(message = "Data size in GB is required")
    private BigDecimal dataGb;

    @NotBlank(message = "Data provider is required (e.g. AWS, GCP, AZURE)")
    private String dataProvider;

    @NotBlank(message = "Data region is required (e.g. us-east-1, us-central1, eastus)")
    private String dataRegion;

    @NotNull(message = "Deadline in hours is required")
    private BigDecimal deadlineHours;

    @NotNull(message = "spotAllowed flag is required")
    private Boolean spotAllowed;

    @NotNull(message = "checkpointing flag is required")
    private Boolean checkpointing;

    public JobRequest() {}

    public JobRequest(String name, String gpuModel, Integer gpuCount, BigDecimal durationHours, BigDecimal dataGb, String dataProvider, String dataRegion, BigDecimal deadlineHours, Boolean spotAllowed, Boolean checkpointing) {
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String gpuModel;
        private Integer gpuCount;
        private BigDecimal durationHours;
        private BigDecimal dataGb;
        private String dataProvider;
        private String dataRegion;
        private BigDecimal deadlineHours;
        private Boolean spotAllowed = true;
        private Boolean checkpointing = true;

        public Builder name(String name) { this.name = name; return this; }
        public Builder gpuModel(String gpuModel) { this.gpuModel = gpuModel; return this; }
        public Builder gpuCount(Integer gpuCount) { this.gpuCount = gpuCount; return this; }
        public Builder durationHours(BigDecimal durationHours) { this.durationHours = durationHours; return this; }
        public Builder dataGb(BigDecimal dataGb) { this.dataGb = dataGb; return this; }
        public Builder dataProvider(String dataProvider) { this.dataProvider = dataProvider; return this; }
        public Builder dataRegion(String dataRegion) { this.dataRegion = dataRegion; return this; }
        public Builder deadlineHours(BigDecimal deadlineHours) { this.deadlineHours = deadlineHours; return this; }
        public Builder spotAllowed(Boolean spotAllowed) { this.spotAllowed = spotAllowed; return this; }
        public Builder checkpointing(Boolean checkpointing) { this.checkpointing = checkpointing; return this; }

        public JobRequest build() {
            return new JobRequest(name, gpuModel, gpuCount, durationHours, dataGb, dataProvider, dataRegion, deadlineHours, spotAllowed, checkpointing);
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGpuModel() { return gpuModel; }
    public void setGpuModel(String gpuModel) { this.gpuModel = gpuModel; }

    public Integer getGpuCount() { return gpuCount; }
    public void setGpuCount(Integer gpuCount) { this.gpuCount = gpuCount; }

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

    public Boolean getSpotAllowed() { return spotAllowed; }
    public void setSpotAllowed(Boolean spotAllowed) { this.spotAllowed = spotAllowed; }

    public Boolean getCheckpointing() { return checkpointing; }
    public void setCheckpointing(Boolean checkpointing) { this.checkpointing = checkpointing; }
}
