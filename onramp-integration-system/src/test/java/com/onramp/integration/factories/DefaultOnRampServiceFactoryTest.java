package com.onramp.integration.factories;

import com.onramp.integration.core.OnRampService;
import com.onramp.integration.models.OnRampConfig;
import com.onramp.integration.providers.onramper.OnramperService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho DefaultOnRampServiceFactory.
 */
@ExtendWith(MockitoExtension.class)
class DefaultOnRampServiceFactoryTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private OnramperService onramperService;

    private DefaultOnRampServiceFactory factory;
    private OnRampConfig validConfig;

    @BeforeEach
    void setUp() {
        factory = new DefaultOnRampServiceFactory(applicationContext);
        
        validConfig = OnRampConfig.builder()
                .providerName("onramper")
                .apiKey("test-api-key")
                .baseUrl("https://api-stg.onramper.com")
                .isSandbox(true)
                .timeout(30)
                .retryAttempts(3)
                .enabled(true)
                .build();
    }

    @Test
    void testCreateServiceWithValidProvider() {
        // Arrange
        when(applicationContext.getBean(OnramperService.class)).thenReturn(onramperService);

        // Act
        OnRampService result = factory.createService("onramper", validConfig);

        // Assert
        assertNotNull(result);
        assertEquals(onramperService, result);
        verify(applicationContext).getBean(OnramperService.class);
        verify(onramperService).configure(validConfig);
    }

    @Test
    void testCreateServiceWithInvalidProvider() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.createService("invalid-provider", validConfig)
        );
        
        assertEquals("Nhà cung cấp không được hỗ trợ: invalid-provider", exception.getMessage());
    }

    @Test
    void testCreateServiceWithNullProviderName() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.createService(null, validConfig)
        );
        
        assertEquals("Tên nhà cung cấp không được null hoặc rỗng", exception.getMessage());
    }

    @Test
    void testCreateServiceWithEmptyProviderName() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.createService("   ", validConfig)
        );
        
        assertEquals("Tên nhà cung cấp không được null hoặc rỗng", exception.getMessage());
    }

    @Test
    void testCreateServiceWithNullConfig() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.createService("onramper", null)
        );
        
        assertEquals("Cấu hình không được null", exception.getMessage());
    }

    @Test
    void testCreateServiceWithCaseInsensitiveProviderName() {
        // Arrange
        when(applicationContext.getBean(OnramperService.class)).thenReturn(onramperService);

        // Act
        OnRampService result1 = factory.createService("ONRAMPER", validConfig);
        OnRampService result2 = factory.createService("OnRamper", validConfig);
        OnRampService result3 = factory.createService("onramper", validConfig);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        verify(applicationContext, times(3)).getBean(OnramperService.class);
    }

    @Test
    void testCreateServiceWithApplicationContextException() {
        // Arrange
        when(applicationContext.getBean(OnramperService.class))
            .thenThrow(new RuntimeException("Bean creation failed"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> factory.createService("onramper", validConfig)
        );
        
        assertEquals("Không thể tạo service cho nhà cung cấp: onramper", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    void testIsProviderSupported() {
        // Act & Assert
        assertTrue(factory.isProviderSupported("onramper"));
        assertTrue(factory.isProviderSupported("ONRAMPER"));
        assertTrue(factory.isProviderSupported("OnRamper"));
        assertFalse(factory.isProviderSupported("moonpay"));
        assertFalse(factory.isProviderSupported("invalid"));
        assertFalse(factory.isProviderSupported(null));
        assertFalse(factory.isProviderSupported(""));
    }

    @Test
    void testGetSupportedProviders() {
        // Act
        String[] supportedProviders = factory.getSupportedProviders();

        // Assert
        assertNotNull(supportedProviders);
        assertEquals(1, supportedProviders.length);
        assertEquals("onramper", supportedProviders[0]);
    }

    @Test
    void testCreateServiceWithDefaultConfig() {
        // Arrange
        when(applicationContext.getBean(OnramperService.class)).thenReturn(onramperService);

        // Act
        OnRampService result = factory.createServiceWithDefaultConfig("onramper");

        // Assert
        assertNotNull(result);
        assertEquals(onramperService, result);
        verify(applicationContext).getBean(OnramperService.class);
        verify(onramperService).configure(any(OnRampConfig.class));
    }

    @Test
    void testCreateServiceWithDefaultConfigForUnsupportedProvider() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.createServiceWithDefaultConfig("unsupported-provider")
        );
        
        assertEquals("Không có cấu hình mặc định cho nhà cung cấp: unsupported-provider", exception.getMessage());
    }

    @Test
    void testCreateDefaultConfigForOnramper() {
        // Arrange
        when(applicationContext.getBean(OnramperService.class)).thenReturn(onramperService);

        // Act
        OnRampService result = factory.createServiceWithDefaultConfig("onramper");

        // Assert
        assertNotNull(result);
        
        // Verify that configure was called with a valid config
        verify(onramperService).configure(argThat(config -> 
            config != null &&
            "onramper".equals(config.getProviderName()) &&
            "https://api.onramper.com".equals(config.getBaseUrl()) &&
            config.getIsSandbox() &&
            config.getTimeout() == 30 &&
            config.getRetryAttempts() == 3 &&
            config.getEnabled()
        ));
    }
}

