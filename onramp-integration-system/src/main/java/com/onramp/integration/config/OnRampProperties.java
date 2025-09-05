package com.onramp.integration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration properties for OnRamp providers.
 */
@Data
@Component
@ConfigurationProperties(prefix = "onramp")
public class OnRampProperties {
    private Map<String, ProviderConfig> providers = new HashMap<>();

    @Data
    public static class ProviderConfig {
        private String apiKey;
        private String apiSecret;
        private String baseUrl;
        private String webhookUrl;
        private boolean enabled;
        private boolean sandbox = true;
        private int timeout = 30;
        private int retryAttempts = 3;
        private int priority = 1;
        private String description;
        private Map<String, Object> additionalConfig = new HashMap<>();
    }

    public OnRampProperties() {
        Environment environment = new StandardEnvironment();
        loadProviderConfigurations(environment);
    }

    private void loadProviderConfigurations(Environment environment) {
        Set<String> providerNames = discoverProviderNames(environment);
        
        for (String providerName : providerNames) {
            ProviderConfig config = loadProviderConfig(environment, providerName);
            providers.put(providerName, config);
        }
    }

    private Set<String> discoverProviderNames(Environment environment) {
        Set<String> providerNames = new java.util.HashSet<>();
        
        if (environment instanceof ConfigurableEnvironment configurableEnv) {
            for (PropertySource<?> propertySource : configurableEnv.getPropertySources()) {
                if (propertySource.getSource() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> source = (Map<String, Object>) propertySource.getSource();
                    
                    providerNames.addAll(
                        source.keySet().stream()
                            .filter(key -> key.startsWith("onramp.providers."))
                            .map(this::extractProviderName)
                            .filter(name -> name != null && !name.isEmpty())
                            .collect(Collectors.toSet())
                    );
                }
            }
        }
        
        if (providerNames.isEmpty()) {
            discoverProvidersFromSystemProperties(providerNames);
        }
        
        return providerNames;
    }
    
    private void discoverProvidersFromSystemProperties(Set<String> providerNames) {
        String[] commonProviders = {"onramper", "moonpay", "transak", "ramp", "banxa"};
        
        for (String provider : commonProviders) {
            String apiKeyProperty = "onramp.providers." + provider + ".api-key";
            if (System.getProperty(apiKeyProperty) != null || 
                System.getenv(apiKeyProperty.toUpperCase().replace(".", "_").replace("-", "_")) != null) {
                providerNames.add(provider);
            }
        }
    }

    private String extractProviderName(String propertyKey) {
        String prefix = "onramp.providers.";
        if (!propertyKey.startsWith(prefix)) {
            return null;
        }
        
        String remaining = propertyKey.substring(prefix.length());
        int dotIndex = remaining.indexOf('.');
        
        return dotIndex > 0 ? remaining.substring(0, dotIndex) : remaining;
    }

    private ProviderConfig loadProviderConfig(Environment environment, String providerName) {
        String prefix = "onramp.providers." + providerName;
        
        ProviderConfig config = new ProviderConfig();
        config.setApiKey(environment.getProperty(prefix + ".api-key", "demo-api-key"));
        config.setApiSecret(environment.getProperty(prefix + ".api-secret", ""));
        config.setBaseUrl(environment.getProperty(prefix + ".base-url"));
        config.setWebhookUrl(environment.getProperty(prefix + ".webhook-url", ""));
        config.setSandbox(environment.getProperty(prefix + ".sandbox", Boolean.class, true));
        config.setTimeout(environment.getProperty(prefix + ".timeout", Integer.class, 30));
        config.setRetryAttempts(environment.getProperty(prefix + ".retry-attempts", Integer.class, 3));
        config.setEnabled(environment.getProperty(prefix + ".enabled", Boolean.class, true));
        config.setPriority(environment.getProperty(prefix + ".priority", Integer.class, 1));
        config.setDescription(environment.getProperty(prefix + ".description"));
        
        Map<String, Object> additionalConfig = loadAdditionalConfig(environment, prefix + ".additional-config");
        config.setAdditionalConfig(additionalConfig);
        
        return config;
    }

    private Map<String, Object> loadAdditionalConfig(Environment environment, String prefix) {
        Map<String, Object> additionalConfig = new HashMap<>();
        
        additionalConfig.put("max-connections", environment.getProperty(prefix + ".max-connections", Integer.class, 100));
        additionalConfig.put("connection-timeout", environment.getProperty(prefix + ".connection-timeout", Integer.class, 30000));
        
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configurableEnv = (ConfigurableEnvironment) environment;
            
            for (PropertySource<?> propertySource : configurableEnv.getPropertySources()) {
                if (propertySource.getSource() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> source = (Map<String, Object>) propertySource.getSource();
                    
                    source.entrySet().stream()
                        .filter(entry -> entry.getKey().startsWith(prefix + "."))
                        .forEach(entry -> {
                            String key = entry.getKey().substring((prefix + ".").length());
                            if (!additionalConfig.containsKey(key)) {
                                additionalConfig.put(key, entry.getValue());
                            }
                        });
                }
            }
        }
        
        return additionalConfig;
    }

    /**
     * Gets configuration for a specific provider.
     * Provider names are case-sensitive.
     */
    public ProviderConfig getProviderConfig(String providerConfigName) {
        if (providerConfigName == null) {
            return null;
        }
        return providers.get(providerConfigName.toLowerCase());
    }

    /**
     * Checks if a provider exists in configuration (regardless of enabled status).
     */
    public boolean hasProviderConfig(String providerName) {
        return getProviderConfig(providerName) != null;
    }
}
