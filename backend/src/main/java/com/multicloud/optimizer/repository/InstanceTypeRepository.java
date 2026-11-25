package com.multicloud.optimizer.repository;

import com.multicloud.optimizer.domain.InstanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstanceTypeRepository extends JpaRepository<InstanceType, Long> {
    Optional<InstanceType> findByInstanceNameIgnoreCase(String instanceName);
    List<InstanceType> findByGpuModelIgnoreCase(String gpuModel);
    List<InstanceType> findByProviderNameIgnoreCase(String providerName);
}
