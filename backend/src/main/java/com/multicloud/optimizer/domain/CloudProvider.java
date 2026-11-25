package com.multicloud.optimizer.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "cloud_provider")
public class CloudProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String name; // AWS, GCP, AZURE

    public CloudProvider() {}

    public CloudProvider(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public CloudProvider build() {
            return new CloudProvider(id, name);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
