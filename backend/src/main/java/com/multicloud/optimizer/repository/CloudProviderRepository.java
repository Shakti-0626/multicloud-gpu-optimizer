package com.multicloud.optimizer.repository;

import com.multicloud.optimizer.domain.CloudProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CloudProviderRepository extends JpaRepository<CloudProvider, Long> {
    Optional<CloudProvider> findByNameIgnoreCase(String name);
}
