package com.multicloud.optimizer.repository;

import com.multicloud.optimizer.domain.Placement;
import com.multicloud.optimizer.domain.enums.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlacementRepository extends JpaRepository<Placement, Long> {
    List<Placement> findByJobId(Long jobId);
    List<Placement> findByJobIdAndStrategy(Long jobId, Strategy strategy);
}
