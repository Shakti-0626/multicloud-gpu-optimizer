package com.multicloud.optimizer.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
    name = "egress_rate",
    uniqueConstraints = @UniqueConstraint(name = "uq_egress_route", columnNames = {"from_provider", "from_region", "to_provider", "to_region"})
)
public class EgressRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_provider", nullable = false, length = 32)
    private String fromProvider;

    @Column(name = "from_region", nullable = false, length = 64)
    private String fromRegion;

    @Column(name = "to_provider", nullable = false, length = 32)
    private String toProvider;

    @Column(name = "to_region", nullable = false, length = 64)
    private String toRegion;

    @Column(name = "usd_per_gb", nullable = false, precision = 12, scale = 4)
    private BigDecimal usdPerGb;

    public EgressRate() {}

    public EgressRate(Long id, String fromProvider, String fromRegion, String toProvider, String toRegion, BigDecimal usdPerGb) {
        this.id = id;
        this.fromProvider = fromProvider;
        this.fromRegion = fromRegion;
        this.toProvider = toProvider;
        this.toRegion = toRegion;
        this.usdPerGb = usdPerGb;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String fromProvider;
        private String fromRegion;
        private String toProvider;
        private String toRegion;
        private BigDecimal usdPerGb;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder fromProvider(String fromProvider) { this.fromProvider = fromProvider; return this; }
        public Builder fromRegion(String fromRegion) { this.fromRegion = fromRegion; return this; }
        public Builder toProvider(String toProvider) { this.toProvider = toProvider; return this; }
        public Builder toRegion(String toRegion) { this.toRegion = toRegion; return this; }
        public Builder usdPerGb(BigDecimal usdPerGb) { this.usdPerGb = usdPerGb; return this; }

        public EgressRate build() {
            return new EgressRate(id, fromProvider, fromRegion, toProvider, toRegion, usdPerGb);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromProvider() { return fromProvider; }
    public void setFromProvider(String fromProvider) { this.fromProvider = fromProvider; }

    public String getFromRegion() { return fromRegion; }
    public void setFromRegion(String fromRegion) { this.fromRegion = fromRegion; }

    public String getToProvider() { return toProvider; }
    public void setToProvider(String toProvider) { this.toProvider = toProvider; }

    public String getToRegion() { return toRegion; }
    public void setToRegion(String toRegion) { this.toRegion = toRegion; }

    public BigDecimal getUsdPerGb() { return usdPerGb; }
    public void setUsdPerGb(BigDecimal usdPerGb) { this.usdPerGb = usdPerGb; }
}
