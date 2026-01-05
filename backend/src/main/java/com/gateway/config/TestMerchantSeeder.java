package com.gateway.config;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.UUID;

/**
 * Configuration class for seeding a test merchant on application startup.
 * This allows developers and QA to immediately test the payment gateway without manual merchant creation.
 * The test merchant is created only once; subsequent runs will skip creation if it already exists.
 * Configuration values can be overridden via application.properties or environment variables.
 */
@Configuration
public class TestMerchantSeeder {
    private static final Logger log = LoggerFactory.getLogger(TestMerchantSeeder.class);

    /**
     * Test merchant email address.
     * Default: test@example.com
     * Override via: app.test-merchant.email property
     */
    @Value("${app.test-merchant.email:test@example.com}")
    private String testEmail;

    /**
     * Test merchant API key.
     * Default: key_test_abc123
     * Override via: app.test-merchant.api-key property
     */
    @Value("${app.test-merchant.api-key:key_test_abc123}")
    private String testApiKey;

    /**
     * Test merchant API secret.
     * Default: secret_test_xyz789
     * Override via: app.test-merchant.api-secret property
     */
    @Value("${app.test-merchant.api-secret:secret_test_xyz789}")
    private String testApiSecret;

    /**
     * Spring Boot ApplicationRunner that executes on startup to seed the test merchant.
     * If a merchant with the test email already exists, it logs that it's already present.
     * Otherwise, it creates a new merchant with the configured credentials.
     * @param merchantRepository the merchant repository for database access
     * @return ApplicationRunner bean that executes during application startup
     */
    @Bean
    public ApplicationRunner seedTestMerchant(MerchantRepository merchantRepository) {
        return args -> {
            merchantRepository.findByEmail(testEmail).ifPresentOrElse(existing -> {
                log.info("Test merchant already present with email {}", testEmail);
            }, () -> {
                Merchant merchant = new Merchant();
                // Use a fixed UUID for consistent testing across application restarts
                merchant.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
                merchant.setName("Test Merchant");
                merchant.setEmail(testEmail);
                merchant.setApiKey(testApiKey);
                merchant.setApiSecret(testApiSecret);
                merchant.setActive(true);
                Instant now = Instant.now();
                merchant.setCreatedAt(now);
                merchant.setUpdatedAt(now);
                merchantRepository.save(merchant);
                log.info("Seeded test merchant {}", testEmail);
            });
        };
    }
}
