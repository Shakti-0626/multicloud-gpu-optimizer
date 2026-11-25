package com.multicloud.optimizer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public class BatchScheduleRequestDto {

    @NotEmpty(message = "Job list cannot be empty")
    @Valid
    private List<JobRequest> jobs;

    private Integer defaultGpuCapacity = 64;

    private Map<String, Integer> customCapacities;

    public BatchScheduleRequestDto() {}

    public BatchScheduleRequestDto(List<JobRequest> jobs, Integer defaultGpuCapacity, Map<String, Integer> customCapacities) {
        this.jobs = jobs;
        this.defaultGpuCapacity = defaultGpuCapacity != null ? defaultGpuCapacity : 64;
        this.customCapacities = customCapacities;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<JobRequest> jobs;
        private Integer defaultGpuCapacity = 64;
        private Map<String, Integer> customCapacities;

        public Builder jobs(List<JobRequest> jobs) { this.jobs = jobs; return this; }
        public Builder defaultGpuCapacity(Integer defaultGpuCapacity) { this.defaultGpuCapacity = defaultGpuCapacity; return this; }
        public Builder customCapacities(Map<String, Integer> customCapacities) { this.customCapacities = customCapacities; return this; }

        public BatchScheduleRequestDto build() {
            return new BatchScheduleRequestDto(jobs, defaultGpuCapacity, customCapacities);
        }
    }

    public List<JobRequest> getJobs() { return jobs; }
    public void setJobs(List<JobRequest> jobs) { this.jobs = jobs; }

    public Integer getDefaultGpuCapacity() { return defaultGpuCapacity; }
    public void setDefaultGpuCapacity(Integer defaultGpuCapacity) { this.defaultGpuCapacity = defaultGpuCapacity; }

    public Map<String, Integer> getCustomCapacities() { return customCapacities; }
    public void setCustomCapacities(Map<String, Integer> customCapacities) { this.customCapacities = customCapacities; }
}
