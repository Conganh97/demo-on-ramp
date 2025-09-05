package com.onramp.integration.factories;

import com.onramp.integration.core.OnRampService;
import com.onramp.integration.models.OnRampConfig;
import com.onramp.integration.providers.onramper.OnramperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Concrete Factory implementation để tạo ra các instance của OnRampService.
 * Sử dụng Factory Pattern để tách biệt việc tạo object khỏi việc sử dụng object.
 */
@Component
@Slf4j
public class DefaultOnRampServiceFactory extends OnRampServiceFactory {

    private final ApplicationContext applicationContext;
    private final Map<String, Class<? extends OnRampService>> providerRegistry;

    @Autowired
    public DefaultOnRampServiceFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.providerRegistry = new HashMap<>();
        initializeProviderRegistry();
    }

    /**
     * Khởi tạo registry của các nhà cung cấp được hỗ trợ.
     */
    private void initializeProviderRegistry() {
        // Đăng ký các nhà cung cấp được hỗ trợ
        providerRegistry.put("onramper", OnramperService.class);
        // Có thể thêm các nhà cung cấp khác trong tương lai:
        // providerRegistry.put("moonpay", MoonpayService.class);
        // providerRegistry.put("banxa", BanxaService.class);
        
        log.info("Đã khởi tạo provider registry với {} nhà cung cấp", providerRegistry.size());
    }

    @Override
    public OnRampService createService(String providerName, OnRampConfig config) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được null hoặc rỗng");
        }

        if (config == null) {
            throw new IllegalArgumentException("Cấu hình không được null");
        }

        String normalizedProviderName = providerName.toLowerCase().trim();
        
        if (!isProviderSupported(normalizedProviderName)) {
            throw new IllegalArgumentException("Nhà cung cấp không được hỗ trợ: " + providerName);
        }

        try {
            Class<? extends OnRampService> serviceClass = providerRegistry.get(normalizedProviderName);
            OnRampService service = applicationContext.getBean(serviceClass);
            
            // Cấu hình service nếu cần thiết
            if (service instanceof ConfigurableOnRampService) {
                ((ConfigurableOnRampService) service).configure(config);
            }
            
            log.info("Đã tạo thành công service cho nhà cung cấp: {}", providerName);
            return service;
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo service cho nhà cung cấp {}: {}", providerName, e.getMessage());
            throw new IllegalStateException("Không thể tạo service cho nhà cung cấp: " + providerName, e);
        }
    }

    @Override
    public boolean isProviderSupported(String providerName) {
        if (providerName == null) {
            return false;
        }
        return providerRegistry.containsKey(providerName.toLowerCase().trim());
    }

    @Override
    public String[] getSupportedProviders() {
        return providerRegistry.keySet().toArray(new String[0]);
    }

    @Override
    public OnRampService createServiceWithDefaultConfig(String providerName) {
        // Trong thực tế, cấu hình mặc định sẽ được load từ application.properties
        // Ở đây chúng ta tạo một cấu hình mẫu
        OnRampConfig defaultConfig = createDefaultConfig(providerName);
        return createService(providerName, defaultConfig);
    }

    /**
     * Tạo cấu hình mặc định cho nhà cung cấp.
     * Trong thực tế, thông tin này sẽ được load từ application.properties hoặc database.
     */
    private OnRampConfig createDefaultConfig(String providerName) {
        switch (providerName.toLowerCase()) {
            case "onramper":
                return OnRampConfig.builder()
                        .providerName("onramper")
                        .apiKey("${onramper.api.key:}")
                        .baseUrl("https://api.onramper.com")
                        .isSandbox(true) // Mặc định sử dụng sandbox
                        .timeout(30)
                        .retryAttempts(3)
                        .enabled(true)
                        .priority(1)
                        .description("Onramper aggregator service")
                        .build();
            default:
                throw new IllegalArgumentException("Không có cấu hình mặc định cho nhà cung cấp: " + providerName);
        }
    }

    /**
     * Interface để đánh dấu các service có thể được cấu hình.
     */
    public interface ConfigurableOnRampService {
        void configure(OnRampConfig config);
    }
}

