package com.multicloud.optimizer.repository;

import com.multicloud.optimizer.domain.EgressRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EgressRateRepository extends JpaRepository<EgressRate, Long> {
    Optional<EgressRate> findByFromProviderIgnoreCaseAndFromRegionIgnoreCaseAndToProviderIgnoreCaseAndToRegionIgnoreCase(
            String fromProvider,
            String fromRegion,
            String toProvider,
            String toRegion
    );
}
