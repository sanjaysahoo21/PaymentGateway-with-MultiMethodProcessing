package com.gateway.repositories;

import com.gateway.models.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Merchant entity.
 * Provides database access and custom query methods for merchant lookup.
 * Used for API key/secret validation in the authentication filter.
 */
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    /**
     * Find a merchant by their email address.
     * @param email the merchant's email
     * @return Optional containing the merchant if found, empty otherwise
     */
    Optional<Merchant> findByEmail(String email);

    /**
     * Find a merchant by their API key.
     * @param apiKey the unique API key issued to the merchant
     * @return Optional containing the merchant if found, empty otherwise
     */
    Optional<Merchant> findByApiKey(String apiKey);

    /**
     * Find a merchant by both API key and API secret (used for authentication).
     * Both credentials must match for the merchant to be returned.
     * @param apiKey the merchant's API key
     * @param apiSecret the merchant's API secret
     * @return Optional containing the merchant if both credentials are valid, empty otherwise
     */
    Optional<Merchant> findByApiKeyAndApiSecret(String apiKey, String apiSecret);
}
