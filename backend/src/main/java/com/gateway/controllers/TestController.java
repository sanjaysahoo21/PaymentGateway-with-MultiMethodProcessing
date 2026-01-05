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

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final MerchantRepository merchantRepository;

    public TestController(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

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
        body.put("seeded", true);
        return ResponseEntity.ok(body);
    }
}
