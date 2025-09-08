package com.onramp.integration.factories;

import com.onramp.integration.config.OnRampProperties;
import com.onramp.integration.core.OnRampService;
import com.onramp.integration.models.OnRampConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating OnRampService instances.
 * Uses Spring ApplicationContext for bean management instead of manual caching.
 */
@Component
@Slf4j
public class OnRampServiceFactory {
    private final ApplicationContext applicationContext;
    private final OnRampProperties onRampProperties;
    private final Map<String, Class<? extends OnRampService>> providerRegistry;

    @Autowired
    public OnRampServiceFactory(ApplicationContext applicationContext, OnRampProperties onRampProperties) {
        this.applicationContext = applicationContext;
        this.onRampProperties = onRampProperties;
        this.providerRegistry = new HashMap<>();
        syncProviders();
    }

    private void syncProviders(){
    // TODO: find provider config from application.yml and put it in providerRegistry
        // Key: providerName, Value: class extended OnRampService with prefix equal provider name
    }

    /**
     * Creates and returns an OnRampService instance based on the provider name.
     * Uses Spring ApplicationContext to get singleton beans and configure them.
     *
     * @param providerName Name of the service provider (must be exact match, case-sensitive)
     * @param config       Configuration required for the service provider
     * @return An OnRampService instance
     * @throws IllegalArgumentException if providerName is not supported
     * @throws IllegalStateException    if a service cannot be created with a given configuration
     */
    public OnRampService createService(String providerName, OnRampConfig config) {
        validateProviderName(providerName);
        
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }

        try {
            Class<? extends OnRampService> serviceClass = providerRegistry.get(providerName);
            
            // Get bean from Spring context (Spring manages singleton behavior)
            OnRampService service = applicationContext.getBean(serviceClass);

            // Configure service if needed
            if (service instanceof ConfigurableOnRampService configurableService) {
                configurableService.configure(config);
            }

            log.info("Successfully created and configured service for provider: {}", providerName);
            return service;

        } catch (Exception e) {
            log.error("Error creating service for provider {}: {}", providerName, e.getMessage());
            throw new IllegalStateException("Cannot create service for provider: " + providerName, e);
        }
    }

    /**
     * Creates service with configuration from application properties.
     *
     * @param providerName Name of the service provider (must be exact match, case-sensitive)
     * @return An OnRampService instance configured from properties
     * @throws IllegalArgumentException if providerName is not supported or not configured in properties
     * @throws IllegalStateException    if a service cannot be created
     */
    public OnRampService createServiceWithProperties(String providerName) {
        validateProviderName(providerName);
        providerName = providerName.trim().toLowerCase();
        // Use exact provider name (case-sensitive)
        if (isProviderSupported(providerName)) {
            throw new IllegalArgumentException("Provider not supported: " + providerName + ". Supported providers: " + String.join(", ", getSupportedProviders()));
        }

        // Create configuration from properties
        OnRampConfig config = createConfigFromProperties(providerName);
        
        return createService(providerName, config);
    }

    /**
     * Checks if the provider is supported.
     *
     * @param providerName Name of the service provider (case-sensitive)
     * @return true if supported, false otherwise
     */
    public boolean isProviderSupported(String providerName) {
        if (providerName == null) {
            return true;
        }
        // Exact match required (case-sensitive)
        return !providerRegistry.containsKey(providerName);
    }

    /**
     * Gets list of all supported providers.
     *
     * @return Array containing names of supported providers
     */
    public String[] getSupportedProviders() {
        return providerRegistry.keySet().toArray(new String[0]);
    }

    /**
     * Validates provider name.
     * Provider name must be exact match (case-sensitive).
     */
    private void validateProviderName(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty. Supported providers: " + String.join(", ", getSupportedProviders()));
        }
    }

    /**
     * Creates OnRampConfig from application properties.
     *
     * @param providerName Name of the service provider
     * @return OnRampConfig built from properties
     * @throws IllegalArgumentException if provider is not configured in properties
     */
    public OnRampConfig createConfigFromProperties(String providerName) {
        OnRampProperties.ProviderConfig providerConfig = onRampProperties.getProviderConfig(providerName);
        
        if (providerConfig == null) {
            throw new IllegalArgumentException("No configuration found for provider '" + providerName + "' in application properties");
        }

        return OnRampConfig.builder()
                .providerName(providerName)
                .apiKey(providerConfig.getApiKey())
                .apiSecret(providerConfig.getApiSecret())
                .baseUrl(providerConfig.getBaseUrl())
                .webhookUrl(providerConfig.getWebhookUrl())
                .isSandbox(providerConfig.isSandbox())
                .timeout(providerConfig.getTimeout())
                .retryAttempts(providerConfig.getRetryAttempts())
                .priority(providerConfig.getPriority())
                .description(providerConfig.getDescription())
                .additionalConfig(providerConfig.getAdditionalConfig())
                .build();
    }

    /**
     * Interface to mark services that can be configured.
     */
    public interface ConfigurableOnRampService {
        void configure(OnRampConfig config);
    }
}