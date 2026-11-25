package com.multicloud.optimizer.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "gpu_model", nullable = false, length = 32)
    private String gpuModel;

    @Column(name = "gpu_count", nullable = false)
    private Integer gpuCount;

    @Column(name = "duration_hours", nullable = false, precision = 8, scale = 2)
    private BigDecimal durationHours;

    @Column(name = "data_gb", nullable = false, precision = 10, scale = 2)
    private BigDecimal dataGb;

    @Column(name = "data_provider", nullable = false, length = 32)
    private String dataProvider;

    @Column(name = "data_region", nullable = false, length = 64)
    private String dataRegion;

    @Column(name = "deadline_hours", nullable = false, precision = 8, scale = 2)
    private BigDecimal deadlineHours;

    @Column(name = "spot_allowed", nullable = false)
    private Boolean spotAllowed;

    @Column(name = "checkpointing", nullable = false)
    private Boolean checkpointing;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Placement> placements = new ArrayList<>();

    public Job() {}

    public Job(Long id, String name, String gpuModel, Integer gpuCount, BigDecimal durationHours, BigDecimal dataGb, String dataProvider, String dataRegion, BigDecimal deadlineHours, Boolean spotAllowed, Boolean checkpointing, LocalDateTime submittedAt, List<Placement> placements) {
        this.id = id;
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
        this.submittedAt = submittedAt;
        this.placements = placements != null ? placements : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String gpuModel;
        private Integer gpuCount;
        private BigDecimal durationHours;
        private BigDecimal dataGb;
        private String dataProvider;
        private String dataRegion;
        private BigDecimal deadlineHours;
        private Boolean spotAllowed;
        private Boolean checkpointing;
        private LocalDateTime submittedAt;
        private List<Placement> placements = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
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
        public Builder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }
        public Builder placements(List<Placement> placements) { this.placements = placements; return this; }

        public Job build() {
            return new Job(id, name, gpuModel, gpuCount, durationHours, dataGb, dataProvider, dataRegion, deadlineHours, spotAllowed, checkpointing, submittedAt, placements);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public List<Placement> getPlacements() { return placements; }
    public void setPlacements(List<Placement> placements) { this.placements = placements; }
}
