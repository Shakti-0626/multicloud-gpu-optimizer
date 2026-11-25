package com.multicloud.optimizer.config;

import com.multicloud.optimizer.domain.CloudProvider;
import com.multicloud.optimizer.domain.EgressRate;
import com.multicloud.optimizer.domain.InstanceType;
import com.multicloud.optimizer.domain.Price;
import com.multicloud.optimizer.domain.enums.PricingModel;
import com.multicloud.optimizer.repository.CloudProviderRepository;
import com.multicloud.optimizer.repository.EgressRateRepository;
import com.multicloud.optimizer.repository.InstanceTypeRepository;
import com.multicloud.optimizer.repository.PriceRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Component
public class DataSeederRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederRunner.class);

    private final CloudProviderRepository providerRepo;
    private final InstanceTypeRepository instanceRepo;
    private final PriceRepository priceRepo;
    private final EgressRateRepository egressRepo;

    @Value("${app.data.prices-path:../data/prices.csv}")
    private String pricesCsvPath;

    @Value("${app.data.egress-path:../data/egress.csv}")
    private String egressCsvPath;

    public DataSeederRunner(CloudProviderRepository providerRepo, InstanceTypeRepository instanceRepo, PriceRepository priceRepo, EgressRateRepository egressRepo) {
        this.providerRepo = providerRepo;
        this.instanceRepo = instanceRepo;
        this.priceRepo = priceRepo;
        this.egressRepo = egressRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting DataSeederRunner to inspect and load baseline dataset...");
        seedPrices();
        seedEgress();
        log.info("DataSeederRunner completed. Database is primed with GPU catalog and egress rates.");
    }

    private Reader openReader(String configuredPath, String filename) {
        // 1. Check classpath (inside JAR)
        try {
            ClassPathResource cpr = new ClassPathResource("data/" + filename);
            if (cpr.exists()) {
                log.info("Found {} in classpath resources: data/{}", filename, filename);
                return new InputStreamReader(cpr.getInputStream(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.debug("Classpath lookup for data/{} failed: {}", filename, e.getMessage());
        }

        // 2. Check filesystem paths
        File file = resolveFile(configuredPath, filename);
        if (file != null && file.exists()) {
            try {
                log.info("Found {} in filesystem: {}", filename, file.getAbsolutePath());
                return new FileReader(file, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("Failed to open file {}: {}", file.getAbsolutePath(), e.getMessage());
            }
        }

        return null;
    }

    private File resolveFile(String configuredPath, String fallbackName) {
        if (configuredPath != null && !configuredPath.isBlank()) {
            File file = new File(configuredPath);
            if (file.exists()) return file;
        }

        File direct = new File(fallbackName);
        if (direct.exists()) return direct;

        File inData = new File("data/" + fallbackName);
        if (inData.exists()) return inData;

        File inBackendData = new File("backend/data/" + fallbackName);
        if (inBackendData.exists()) return inBackendData;

        File upData = new File("../data/" + fallbackName);
        if (upData.exists()) return upData;

        return null;
    }

    private void seedPrices() {
        Reader reader = openReader(pricesCsvPath, "prices.csv");
        if (reader == null) {
            log.warn("prices.csv could not be located on classpath or filesystem.");
            return;
        }

        try (reader;
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build())) {

            for (CSVRecord record : csvParser) {
                String providerName = record.get("provider");
                String instanceName = record.get("instance");
                String gpuModel = record.get("gpu_model");
                int gpuCount = Integer.parseInt(record.get("gpu_count"));
                String region = record.get("region");
                PricingModel pricingModel = PricingModel.valueOf(record.get("pricing_model").toUpperCase());
                BigDecimal hourlyUsd = new BigDecimal(record.get("hourly_usd"));
                BigDecimal interruptionPct = new BigDecimal(record.get("interruption_pct"));
                int queueMinutes = Integer.parseInt(record.get("queue_minutes"));
                LocalDate asOf = LocalDate.parse(record.get("as_of"));

                CloudProvider provider = providerRepo.findByNameIgnoreCase(providerName)
                        .orElseGet(() -> providerRepo.save(CloudProvider.builder().name(providerName.toUpperCase()).build()));

                InstanceType instanceType = instanceRepo.findByInstanceNameIgnoreCase(instanceName)
                        .orElseGet(() -> instanceRepo.save(InstanceType.builder()
                                .provider(provider)
                                .instanceName(instanceName)
                                .gpuModel(gpuModel.toUpperCase())
                                .gpuCount(gpuCount)
                                .vcpu(gpuCount * 8)
                                .memoryGb(gpuCount * 32)
                                .build()));

                if (priceRepo.findByInstanceTypeIdAndRegionAndPricingModel(instanceType.getId(), region, pricingModel).isEmpty()) {
                    priceRepo.save(Price.builder()
                            .instanceType(instanceType)
                            .region(region)
                            .pricingModel(pricingModel)
                            .hourlyPriceUsd(hourlyUsd)
                            .interruptionRatePct(interruptionPct)
                            .avgQueueMinutes(queueMinutes)
                            .asOfDate(asOf)
                            .build());
                }
            }
            log.info("Prices seed finished. Total prices in DB: {}", priceRepo.count());
        } catch (Exception e) {
            log.error("Failed to seed prices.csv: {}", e.getMessage(), e);
        }
    }

    private void seedEgress() {
        Reader reader = openReader(egressCsvPath, "egress.csv");
        if (reader == null) {
            log.warn("egress.csv could not be located on classpath or filesystem.");
            return;
        }

        try (reader;
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build())) {

            for (CSVRecord record : csvParser) {
                String fromProvider = record.get("from_provider").toUpperCase();
                String fromRegion = record.get("from_region");
                String toProvider = record.get("to_provider").toUpperCase();
                String toRegion = record.get("to_region");
                BigDecimal usdPerGb = new BigDecimal(record.get("usd_per_gb"));

                if (egressRepo.findByFromProviderIgnoreCaseAndFromRegionIgnoreCaseAndToProviderIgnoreCaseAndToRegionIgnoreCase(
                        fromProvider, fromRegion, toProvider, toRegion).isEmpty()) {
                    egressRepo.save(EgressRate.builder()
                            .fromProvider(fromProvider)
                            .fromRegion(fromRegion)
                            .toProvider(toProvider)
                            .toRegion(toRegion)
                            .usdPerGb(usdPerGb)
                            .build());
                }
            }
            log.info("Egress rates seed finished. Total egress rows in DB: {}", egressRepo.count());
        } catch (Exception e) {
            log.error("Failed to seed egress.csv: {}", e.getMessage(), e);
        }
    }
}
