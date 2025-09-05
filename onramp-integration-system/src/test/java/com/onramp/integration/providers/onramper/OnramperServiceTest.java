package com.onramp.integration.providers.onramper;

import com.onramp.integration.models.*;
import com.onramp.integration.exceptions.InvalidConfigurationException;
import com.onramp.integration.exceptions.OnRampException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho OnramperService.
 */
@ExtendWith(MockitoExtension.class)
class OnramperServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    private OnramperService onramperService;
    private OnRampConfig validConfig;

    @BeforeEach
    void setUp() {
        onramperService = new OnramperService(webClientBuilder);
        
        validConfig = OnRampConfig.builder()
                .providerName("onramper")
                .apiKey("test-api-key")
                .baseUrl("https://api-stg.onramper.com")
                .isSandbox(true)
                .timeout(30)
                .retryAttempts(3)
                .enabled(true)
                .build();

        // Setup WebClient mock chain
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void testConfigureWithValidConfig() {
        // Act
        assertDoesNotThrow(() -> onramperService.configure(validConfig));
        
        // Assert
        assertEquals("onramper", onramperService.getProviderName());
        verify(webClientBuilder).baseUrl("https://api-stg.onramper.com");
        verify(webClientBuilder).defaultHeader("Authorization", "test-api-key");
        verify(webClientBuilder).defaultHeader("Accept", "application/json");
    }

    @Test
    void testConfigureWithNullConfig() {
        // Act & Assert
        InvalidConfigurationException exception = assertThrows(
            InvalidConfigurationException.class,
            () -> onramperService.configure(null)
        );
        
        assertEquals("Cấu hình không được null", exception.getMessage());
        assertEquals("onramper", exception.getProviderName());
    }

    @Test
    void testConfigureWithNullApiKey() {
        // Arrange
        OnRampConfig invalidConfig = OnRampConfig.builder()
                .providerName("onramper")
                .apiKey(null)
                .baseUrl("https://api-stg.onramper.com")
                .build();

        // Act & Assert
        InvalidConfigurationException exception = assertThrows(
            InvalidConfigurationException.class,
            () -> onramperService.configure(invalidConfig)
        );
        
        assertEquals("API key không được null hoặc rỗng", exception.getMessage());
    }

    @Test
    void testConfigureWithEmptyApiKey() {
        // Arrange
        OnRampConfig invalidConfig = OnRampConfig.builder()
                .providerName("onramper")
                .apiKey("   ")
                .baseUrl("https://api-stg.onramper.com")
                .build();

        // Act & Assert
        InvalidConfigurationException exception = assertThrows(
            InvalidConfigurationException.class,
            () -> onramperService.configure(invalidConfig)
        );
        
        assertEquals("API key không được null hoặc rỗng", exception.getMessage());
    }

    @Test
    void testGetSupportedAssetsSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        onramperService.configure(validConfig);
        
        OnramperSupportedResponse mockResponse = new OnramperSupportedResponse();
        // Setup mock response data here
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/supported")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OnramperSupportedResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        CompletableFuture<List<Asset>> future = onramperService.getSupportedAssets();
        List<Asset> result = future.get();

        // Assert
        assertNotNull(result);
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/supported");
    }

    @Test
    void testGetSupportedAssetsWithoutConfiguration() {
        // Act & Assert
        InvalidConfigurationException exception = assertThrows(
            InvalidConfigurationException.class,
            () -> onramperService.getSupportedAssets()
        );
        
        assertEquals("Service chưa được cấu hình", exception.getMessage());
    }

    @Test
    void testGetQuoteWithValidParameters() throws ExecutionException, InterruptedException {
        // Arrange
        onramperService.configure(validConfig);
        
        OnramperQuoteResponse mockResponse = new OnramperQuoteResponse();
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OnramperQuoteResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        CompletableFuture<Quote> future = onramperService.getQuote("USD", "BTC", 100.0, null);
        Quote result = future.get();

        // Assert
        assertNotNull(result);
        assertEquals("USD", result.getFiatCurrency());
        assertEquals("BTC", result.getCryptoCurrency());
        assertEquals("onramper", result.getProviderName());
    }

    @Test
    void testGetQuoteWithInvalidParameters() {
        // Arrange
        onramperService.configure(validConfig);

        // Act & Assert - null fiat currency
        IllegalArgumentException exception1 = assertThrows(
            IllegalArgumentException.class,
            () -> onramperService.getQuote(null, "BTC", 100.0, null)
        );
        assertEquals("Mã tiền pháp định không được null hoặc rỗng", exception1.getMessage());

        // Act & Assert - null crypto currency
        IllegalArgumentException exception2 = assertThrows(
            IllegalArgumentException.class,
            () -> onramperService.getQuote("USD", null, 100.0, null)
        );
        assertEquals("Mã tiền điện tử không được null hoặc rỗng", exception2.getMessage());

        // Act & Assert - both amounts null
        IllegalArgumentException exception3 = assertThrows(
            IllegalArgumentException.class,
            () -> onramperService.getQuote("USD", "BTC", null, null)
        );
        assertEquals("Phải cung cấp fiatAmount hoặc cryptoAmount", exception3.getMessage());

        // Act & Assert - both amounts provided
        IllegalArgumentException exception4 = assertThrows(
            IllegalArgumentException.class,
            () -> onramperService.getQuote("USD", "BTC", 100.0, 0.001)
        );
        assertEquals("Chỉ có thể cung cấp fiatAmount hoặc cryptoAmount, không phải cả hai", exception4.getMessage());
    }

    @Test
    void testCreateOrderWithValidParameters() throws ExecutionException, InterruptedException {
        // Arrange
        onramperService.configure(validConfig);
        
        OnramperOrderResponse mockResponse = new OnramperOrderResponse();
        OnramperOrderResponse.TransactionInformation txInfo = new OnramperOrderResponse.TransactionInformation();
        txInfo.setId("test-order-id");
        txInfo.setStatus("pending");
        mockResponse.setTransactionInformation(txInfo);
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/checkout/intent")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OnramperOrderResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        CompletableFuture<Order> future = onramperService.createOrder(
            "USD", "BTC", 100.0, null, 
            "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", 
            "https://example.com/callback"
        );
        Order result = future.get();

        // Assert
        assertNotNull(result);
        assertEquals("onramper", result.getProviderName());
        assertEquals(OrderStatus.PENDING_PAYMENT, result.getStatus());
    }

    @Test
    void testCreateOrderWithInvalidParameters() {
        // Arrange
        onramperService.configure(validConfig);

        // Act & Assert - null wallet address
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> onramperService.createOrder("USD", "BTC", 100.0, null, null, "https://example.com/callback")
        );
        assertEquals("Địa chỉ ví không được null hoặc rỗng", exception.getMessage());

        // Act & Assert - null redirect URL
        IllegalArgumentException exception2 = assertThrows(
            IllegalArgumentException.class,
            () -> onramperService.createOrder("USD", "BTC", 100.0, null, "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", null)
        );
        assertEquals("URL chuyển hướng không được null hoặc rỗng", exception2.getMessage());
    }

    @Test
    void testGetOrderStatusWithValidOrderId() throws ExecutionException, InterruptedException {
        // Arrange
        onramperService.configure(validConfig);
        
        OnramperTransactionResponse mockResponse = new OnramperTransactionResponse();
        mockResponse.setId("test-order-id");
        mockResponse.setStatus("completed");
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/transactions/{transactionId}", "test-order-id")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OnramperTransactionResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        CompletableFuture<Order> future = onramperService.getOrderStatus("test-order-id");
        Order result = future.get();

        // Assert
        assertNotNull(result);
        assertEquals("onramper", result.getProviderName());
    }

    @Test
    void testGetOrderStatusWithNullOrderId() {
        // Arrange
        onramperService.configure(validConfig);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> onramperService.getOrderStatus(null)
        );
        assertEquals("Order ID không được null hoặc rỗng", exception.getMessage());
    }

    @Test
    void testIsServiceAvailableWhenConfigured() throws ExecutionException, InterruptedException {
        // Arrange
        onramperService.configure(validConfig);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/supported")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("success"));

        // Act
        CompletableFuture<Boolean> future = onramperService.isServiceAvailable();
        Boolean result = future.get();

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsServiceAvailableWhenNotConfigured() throws ExecutionException, InterruptedException {
        // Act
        CompletableFuture<Boolean> future = onramperService.isServiceAvailable();
        Boolean result = future.get();

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateConfigurationWhenValid() throws ExecutionException, InterruptedException {
        // Arrange
        onramperService.configure(validConfig);

        // Act
        CompletableFuture<Boolean> future = onramperService.validateConfiguration();
        Boolean result = future.get();

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateConfigurationWhenInvalid() throws ExecutionException, InterruptedException {
        // Act
        CompletableFuture<Boolean> future = onramperService.validateConfiguration();
        Boolean result = future.get();

        // Assert
        assertFalse(result);
    }

    @Test
    void testWebClientErrorHandling() {
        // Arrange
        onramperService.configure(validConfig);
        
        WebClientResponseException webError = WebClientResponseException.create(
            404, "Not Found", null, null, null
        );
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/supported")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OnramperSupportedResponse.class)).thenReturn(Mono.error(webError));

        // Act & Assert
        CompletableFuture<List<Asset>> future = onramperService.getSupportedAssets();
        
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof OnRampException);
        
        OnRampException onRampException = (OnRampException) exception.getCause();
        assertEquals("API_ERROR", onRampException.getErrorCode());
        assertEquals("onramper", onRampException.getProviderName());
    }
}

