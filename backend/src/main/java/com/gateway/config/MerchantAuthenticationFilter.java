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

/**
 * Custom authentication filter for merchant API key validation.
 * Extracts X-Api-Key and X-Api-Secret headers, validates them against database,
 * and sets authenticated Merchant in SecurityContext. Permits public endpoints without auth.
 */
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

    /**
     * Create authentication filter.
     * @param merchantRepository repository for merchant API credential lookup
     * @param objectMapper JSON mapper for error serialization
     */
    public MerchantAuthenticationFilter(MerchantRepository merchantRepository, ObjectMapper objectMapper) {
        this.merchantRepository = merchantRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Skip filter for public endpoints and OPTIONS requests.
     * @param request the HTTP request
     * @return true if request is to public endpoint or OPTIONS, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return publicPatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * Perform merchant authentication using API key and secret headers.
     * Validates credentials against database and sets authenticated merchant in SecurityContext.
     * @param request the HTTP request with X-Api-Key and X-Api-Secret headers
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if filter fails
     * @throws IOException if response writing fails
     */
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
