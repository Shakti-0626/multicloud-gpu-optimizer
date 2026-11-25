package com.multicloud.optimizer.repository;

import com.multicloud.optimizer.domain.Price;
import com.multicloud.optimizer.domain.enums.PricingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    @Query("SELECT p FROM Price p " +
           "WHERE (:provider IS NULL OR LOWER(p.instanceType.provider.name) = LOWER(:provider)) " +
           "AND (:gpuModel IS NULL OR LOWER(p.instanceType.gpuModel) = LOWER(:gpuModel)) " +
           "AND (:region IS NULL OR LOWER(p.region) = LOWER(:region)) " +
           "AND (:pricingModel IS NULL OR p.pricingModel = :pricingModel)")
    List<Price> findByFilters(
            @Param("provider") String provider,
            @Param("gpuModel") String gpuModel,
            @Param("region") String region,
            @Param("pricingModel") PricingModel pricingModel
    );

    Optional<Price> findByInstanceTypeIdAndRegionAndPricingModel(
            Long instanceTypeId,
            String region,
            PricingModel pricingModel
    );
}
