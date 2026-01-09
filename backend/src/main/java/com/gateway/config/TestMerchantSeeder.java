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

@Configuration
public class TestMerchantSeeder {
    private static final Logger log = LoggerFactory.getLogger(TestMerchantSeeder.class);

    @Value("${app.test-merchant.email:test@example.com}")
    private String testEmail;

    @Value("${app.test-merchant.api-key:key_test_abc123}")
    private String testApiKey;

    @Value("${app.test-merchant.api-secret:secret_test_xyz789}")
    private String testApiSecret;

    @Bean
    public ApplicationRunner seedTestMerchant(MerchantRepository merchantRepository) {
        return args -> {
            merchantRepository.findByEmail(testEmail).ifPresentOrElse(existing -> {
                log.info("Test merchant already present with email {}", testEmail);
            }, () -> {
                Merchant merchant = new Merchant();
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
