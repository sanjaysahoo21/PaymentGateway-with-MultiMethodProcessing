package com.gateway.controllers;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Test controller for retrieving test merchant credentials.
 * Useful for API testing and documentation purposes.
 * The test merchant is automatically seeded on application startup.
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final MerchantRepository merchantRepository;

    public TestController(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    /**
     * Get test merchant credentials for development/testing.
     * Returns test merchant with API key and secret for making authenticated requests.
     * @return test merchant credentials (id, email, api_key, api_secret)
     */
    @GetMapping("/merchant")
    public ResponseEntity<?> getTestMerchant() {
        Optional<Merchant> merchantOpt = merchantRepository.findByEmail("test@example.com");
        if (merchantOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new HashMap<>());
        }
        Merchant merchant = merchantOpt.get();
        Map<String, Object> body = new HashMap<>();
        body.put("id", merchant.getId());
        body.put("email", merchant.getEmail());
        body.put("api_key", merchant.getApiKey());
        body.put("api_secret", merchant.getApiSecret());
        body.put("seeded", true);
        return ResponseEntity.ok(body);
    }
}
