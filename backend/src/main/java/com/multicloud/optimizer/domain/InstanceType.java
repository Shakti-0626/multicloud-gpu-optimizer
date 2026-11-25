package com.multicloud.optimizer.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "instance_type")
public class InstanceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private CloudProvider provider;

    @Column(name = "instance_name", nullable = false, unique = true, length = 64)
    private String instanceName;

    @Column(name = "gpu_model", nullable = false, length = 32)
    private String gpuModel;

    @Column(name = "gpu_count", nullable = false)
    private Integer gpuCount;

    @Column(name = "vcpu", nullable = false)
    private Integer vcpu;

    @Column(name = "memory_gb", nullable = false)
    private Integer memoryGb;

    public InstanceType() {}

    public InstanceType(Long id, CloudProvider provider, String instanceName, String gpuModel, Integer gpuCount, Integer vcpu, Integer memoryGb) {
        this.id = id;
        this.provider = provider;
        this.instanceName = instanceName;
        this.gpuModel = gpuModel;
        this.gpuCount = gpuCount;
        this.vcpu = vcpu;
        this.memoryGb = memoryGb;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private CloudProvider provider;
        private String instanceName;
        private String gpuModel;
        private Integer gpuCount;
        private Integer vcpu;
        private Integer memoryGb;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder provider(CloudProvider provider) { this.provider = provider; return this; }
        public Builder instanceName(String instanceName) { this.instanceName = instanceName; return this; }
        public Builder gpuModel(String gpuModel) { this.gpuModel = gpuModel; return this; }
        public Builder gpuCount(Integer gpuCount) { this.gpuCount = gpuCount; return this; }
        public Builder vcpu(Integer vcpu) { this.vcpu = vcpu; return this; }
        public Builder memoryGb(Integer memoryGb) { this.memoryGb = memoryGb; return this; }

        public InstanceType build() {
            return new InstanceType(id, provider, instanceName, gpuModel, gpuCount, vcpu, memoryGb);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CloudProvider getProvider() { return provider; }
    public void setProvider(CloudProvider provider) { this.provider = provider; }

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
}
