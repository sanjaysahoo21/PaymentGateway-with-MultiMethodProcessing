package com.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.dto.ErrorResponse;
import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MerchantAuthenticationFilter extends OncePerRequestFilter {

    private final MerchantRepository merchantRepository;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Set<String> publicPatterns = new HashSet<>(Arrays.asList(
            "/health",
            "/api/v1/test/merchant",
            "/api/v1/orders/*/public",
                "/api/v1/payments/public",
                "/api/v1/payments/*/public"
    ));

    public MerchantAuthenticationFilter(MerchantRepository merchantRepository, ObjectMapper objectMapper) {
        this.merchantRepository = merchantRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return publicPatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader("X-Api-Key");
        String apiSecret = request.getHeader("X-Api-Secret");

        if (apiKey == null || apiSecret == null) {
            writeAuthError(response, "Invalid API credentials");
            return;
        }

        Merchant merchant = merchantRepository.findByApiKeyAndApiSecret(apiKey, apiSecret)
                .orElse(null);
        if (merchant == null) {
            writeAuthError(response, "Invalid API credentials");
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                merchant, null, null
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute("authenticatedMerchant", merchant);

        filterChain.doFilter(request, response);
    }

    private void writeAuthError(HttpServletResponse response, String description) throws IOException {
        ErrorResponse error = new ErrorResponse("AUTHENTICATION_ERROR", description);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
