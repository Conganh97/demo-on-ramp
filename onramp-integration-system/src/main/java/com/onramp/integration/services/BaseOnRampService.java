package com.onramp.integration.services;

import ch.qos.logback.core.joran.conditional.IfAction;
import com.onramp.integration.core.OnRampService;
import com.onramp.integration.exceptions.InvalidConfigurationException;
import com.onramp.integration.exceptions.OnRampException;
import com.onramp.integration.factories.OnRampServiceFactory;
import com.onramp.integration.models.OnRampConfig;
import com.onramp.integration.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * Base class for OnRamp service implementations.
 * Provides common functionality and validation methods.
 */
@Slf4j
public abstract class BaseOnRampService implements OnRampService, OnRampServiceFactory.ConfigurableOnRampService {

    protected final RestTemplate restTemplate;
    protected OnRampConfig config;
    protected HttpClientUtil httpClient;
    protected String baseUrl;
    protected HttpHeaders defaultHeaders;

    protected BaseOnRampService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.defaultHeaders = new HttpHeaders();
    }

    @Override
    public void configure(OnRampConfig config) {
        validateConfigurationInput(config);
        
        this.config = config;
        this.baseUrl = determineBaseUrl(config);
        setupHeaders(config);
        
        this.httpClient = new HttpClientUtil(restTemplate, baseUrl, defaultHeaders);
        
        log.info("Configured {} service with base URL: {}", getProviderName(), baseUrl);
    }

    @Override
    public CompletableFuture<Boolean> validateConfiguration() {
        return CompletableFuture.completedFuture(
            config != null && 
            config.getApiKey() != null && 
            !config.getApiKey().trim().isEmpty() &&
            baseUrl != null
        );
    }

    @Override
    public CompletableFuture<Boolean> isServiceAvailable() {
        if (config == null || baseUrl == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        return httpClient.healthCheck(getHealthCheckPath());
    }

    /**
     * Validates configuration input.
     */
    protected void validateConfigurationInput(OnRampConfig config) {
        if (config == null) {
            throw new InvalidConfigurationException("Configuration cannot be null", getProviderName());
        }

        if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            throw new InvalidConfigurationException("API key cannot be null or empty", getProviderName());
        }
    }

    /**
     * Validates that the service is properly configured.
     */
    protected void validateServiceConfiguration() {
        if (config == null || baseUrl == null || httpClient == null) {
            throw new InvalidConfigurationException("Service not configured", getProviderName());
        }
    }

    /**
     * Validates quote request parameters.
     */
    protected void validateQuoteParameters(String fiatCurrency, String cryptoCurrency, 
                                         Double fiatAmount, Double cryptoAmount) {
        if (fiatCurrency == null || fiatCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Fiat currency code cannot be null or empty");
        }
        if (cryptoCurrency == null || cryptoCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Crypto currency code cannot be null or empty");
        }
        if (fiatAmount == null && cryptoAmount == null) {
            throw new IllegalArgumentException("Must provide either fiatAmount or cryptoAmount");
        }
        if (fiatAmount != null && cryptoAmount != null) {
            throw new IllegalArgumentException("Can only provide fiatAmount or cryptoAmount, not both");
        }
    }

    /**
     * Validates order creation parameters.
     */
    protected void validateOrderParameters(String fiatCurrency, String cryptoCurrency,
                                         Double fiatAmount, Double cryptoAmount,
                                         String walletAddress, String redirectUrl) {
        validateQuoteParameters(fiatCurrency, cryptoCurrency, fiatAmount, cryptoAmount);
        
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Wallet address cannot be null or empty");
        }
        if (redirectUrl == null || redirectUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Redirect URL cannot be null or empty");
        }
    }

    /**
     * Handles exceptions and converts them to OnRampException.
     */
    protected RuntimeException handleError(Throwable error, String operation) {
        log.error("Error during {}: {}", operation, error.getMessage());
        
        String errorMessage = String.format("%s API error: %s", getProviderName(), error.getMessage());
        return new OnRampException("API_ERROR", errorMessage, getProviderName(), error);
    }

    /**
     * Determines the base URL based on configuration.
     */
    protected abstract String determineBaseUrl(OnRampConfig config);

    /**
     * Sets up HTTP headers based on configuration.
     */
    protected abstract void setupHeaders(OnRampConfig config);

    /**
     * Gets the health check path for the provider.
     */
    protected abstract String getHealthCheckPath();
}
