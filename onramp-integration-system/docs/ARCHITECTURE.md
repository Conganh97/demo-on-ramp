# Kiến trúc hệ thống OnRamp Integration

## Tổng quan kiến trúc

OnRamp Integration System được thiết kế theo nguyên tắc **Clean Architecture** và **SOLID principles**, đảm bảo tính mở rộng, bảo trì và test được. Hệ thống sử dụng nhiều design patterns để tạo ra một kiến trúc linh hoạt và dễ hiểu.

## Các tầng kiến trúc

### 1. Domain Layer (Core)

Tầng này chứa các business logic và domain models, hoàn toàn độc lập với framework và external dependencies.

#### Core Interfaces

```java
// OnRampService - Interface chính định nghĩa contract cho tất cả providers
public interface OnRampService {
    // Async methods using CompletableFuture for better performance
    CompletableFuture<List<Asset>> getSupportedAssets();
    CompletableFuture<Quote> getQuote(String fiatCurrency, String cryptoCurrency, 
                                    Double fiatAmount, Double cryptoAmount);
    // ... other methods
}
```

**Lý do thiết kế**:
- Sử dụng `CompletableFuture` để hỗ trợ xử lý bất đồng bộ
- Interface segregation - mỗi method có trách nhiệm rõ ràng
- Return types là domain objects, không phụ thuộc vào external APIs

#### Domain Models

```java
@Data
@Builder
@Entity
public class Order {
    @Id
    private String orderId;
    private String externalOrderId;  // ID từ provider
    private String providerName;     // Để tracking
    private OrderStatus status;      // Enum để type safety
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // ... other fields
}
```

**Đặc điểm**:
- Sử dụng Lombok để giảm boilerplate code
- JPA annotations cho persistence
- Builder pattern cho object creation
- Immutable khi có thể

### 2. Application Layer (Services & Factories)

Tầng này orchestrate business logic và coordinate giữa các components.

#### Factory Pattern Implementation

```java
@Component
public class DefaultOnRampServiceFactory extends OnRampServiceFactory {
    private final Map<String, Class<? extends OnRampService>> providerRegistry;
    
    @Override
    public OnRampService createService(String providerName, OnRampConfig config) {
        // Validation
        if (!isProviderSupported(providerName)) {
            throw new ProviderNotSupportedException(providerName);
        }
        
        // Creation using Spring context
        Class<? extends OnRampService> serviceClass = providerRegistry.get(providerName);
        OnRampService service = applicationContext.getBean(serviceClass);
        
        // Configuration
        if (service instanceof ConfigurableOnRampService) {
            ((ConfigurableOnRampService) service).configure(config);
        }
        
        return service;
    }
}
```

**Lợi ích của Factory Pattern**:
- **Encapsulation**: Che giấu logic tạo object phức tạp
- **Flexibility**: Dễ dàng thêm providers mới
- **Testability**: Mock được factory trong tests
- **Configuration**: Centralized configuration logic

### 3. Infrastructure Layer (Providers)

Tầng này chứa implementations cụ thể cho từng provider.

#### Onramper Provider Implementation

```java
@Service
public class OnramperService implements OnRampService, ConfigurableOnRampService {
    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private OnRampConfig config;
    
    @Override
    public CompletableFuture<Quote> getQuote(String fiatCurrency, String cryptoCurrency, 
                                           Double fiatAmount, Double cryptoAmount) {
        validateConfiguration();
        validateQuoteParameters(fiatCurrency, cryptoCurrency, fiatAmount, cryptoAmount);

        String uri = String.format("/quotes/%s/%s", fiatCurrency.toLowerCase(), cryptoCurrency.toLowerCase());
        
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(uri);
                    if (fiatAmount != null) {
                        builder.queryParam("amount", fiatAmount);
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(OnramperQuoteResponse.class)
                .map(response -> convertToQuote(response, fiatCurrency, cryptoCurrency))
                .onErrorMap(this::handleWebClientError)
                .toFuture();
    }
}
```

**Adapter Pattern trong Provider**:
- Convert external API responses thành domain models
- Handle provider-specific error codes
- Normalize data formats

## Design Patterns được áp dụng

### 1. Factory Pattern

**Vấn đề giải quyết**: Tạo objects mà không expose creation logic, và refer đến newly created object sử dụng common interface.

**Implementation**:
```java
// Abstract Factory
public abstract class OnRampServiceFactory {
    public abstract OnRampService createService(String providerName, OnRampConfig config);
}

// Concrete Factory
@Component
public class DefaultOnRampServiceFactory extends OnRampServiceFactory {
    private final Map<String, Class<? extends OnRampService>> providerRegistry;
    // Implementation...
}
```

**Lợi ích**:
- Loose coupling giữa client code và concrete classes
- Dễ dàng extend với providers mới
- Centralized object creation logic

### 2. Strategy Pattern

**Vấn đề giải quyết**: Định nghĩa family of algorithms, encapsulate từng algorithm, và make them interchangeable.

**Implementation**:
```java
// Strategy Interface
public interface OnRampService {
    CompletableFuture<Quote> getQuote(...);
}

// Concrete Strategies
@Service
public class OnramperService implements OnRampService { /* ... */ }

@Service  
public class MoonPayService implements OnRampService { /* ... */ }
```

**Lợi ích**:
- Runtime algorithm selection
- Easy to add new providers
- Consistent interface across providers

### 3. Adapter Pattern

**Vấn đề giải quyết**: Convert interface của một class thành interface khác mà clients expect.

**Implementation**:
```java
// Target interface (domain model)
public class Quote {
    private String fiatCurrency;
    private String cryptoCurrency;
    // ...
}

// Adaptee (external API response)
public class OnramperQuoteResponse {
    @JsonProperty("message")
    private List<OnramperQuote> message;
    // ...
}

// Adapter method
private Quote convertToQuote(OnramperQuoteResponse response, String fiatCurrency, String cryptoCurrency) {
    // Convert external format to domain model
    return Quote.builder()
            .fiatCurrency(fiatCurrency)
            .cryptoCurrency(cryptoCurrency)
            .providerName(PROVIDER_NAME)
            // Map other fields...
            .build();
}
```

### 4. Builder Pattern

**Vấn đề giải quyết**: Construct complex objects step by step.

**Implementation với Lombok**:
```java
@Data
@Builder
public class OnRampConfig {
    private String providerName;
    private String apiKey;
    private String baseUrl;
    @Builder.Default
    private Boolean isSandbox = false;
    @Builder.Default
    private Integer timeout = 30;
    // ...
}

// Usage
OnRampConfig config = OnRampConfig.builder()
    .providerName("onramper")
    .apiKey("api-key")
    .baseUrl("https://api.onramper.com")
    .build();
```

### 5. Dependency Injection Pattern

**Implementation với Spring**:
```java
@Service
public class OnramperService implements OnRampService {
    private final WebClient.Builder webClientBuilder;
    
    @Autowired
    public OnramperService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
}
```

## Error Handling Strategy

### Exception Hierarchy

```java
// Base exception
public class OnRampException extends RuntimeException {
    private final String errorCode;
    private final String providerName;
}

// Specific exceptions
public class ProviderNotSupportedException extends OnRampException { }
public class InvalidConfigurationException extends OnRampException { }
public class ApiException extends OnRampException { }
```

### Error Handling trong Providers

```java
private Throwable handleWebClientError(Throwable error) {
    if (error instanceof WebClientResponseException) {
        WebClientResponseException webError = (WebClientResponseException) error;
        String errorMessage = String.format("Lỗi API Onramper: %d - %s", 
            webError.getStatusCode().value(), webError.getResponseBodyAsString());
        return new OnRampException("API_ERROR", errorMessage, PROVIDER_NAME, error);
    }
    return new OnRampException("NETWORK_ERROR", "Lỗi kết nối: " + error.getMessage(), PROVIDER_NAME, error);
}
```

## Configuration Management

### Externalized Configuration

```yaml
# application.yml
onramp:
  providers:
    onramper:
      api-key: ${ONRAMPER_API_KEY:}
      api-secret: ${ONRAMPER_API_SECRET:}
      base-url: https://api.onramper.com
      is-sandbox: false
      timeout: 30
      retry-attempts: 3
      enabled: true
    moonpay:
      api-key: ${MOONPAY_API_KEY:}
      base-url: https://api.moonpay.com
      is-sandbox: false
```

### Configuration Classes

```java
@ConfigurationProperties(prefix = "onramp.providers")
@Data
public class OnRampProvidersConfig {
    private Map<String, OnRampConfig> providers = new HashMap<>();
}
```

## Async Processing Architecture

### CompletableFuture Usage

```java
public CompletableFuture<Quote> getQuote(...) {
    return webClient.get()
            .uri(...)
            .retrieve()
            .bodyToMono(OnramperQuoteResponse.class)
            .map(this::convertToQuote)
            .doOnSuccess(quote -> log.info("Quote retrieved: {}", quote))
            .doOnError(error -> log.error("Error getting quote: {}", error.getMessage()))
            .onErrorMap(this::handleWebClientError)
            .toFuture();
}
```

**Lợi ích**:
- Non-blocking operations
- Better resource utilization
- Composable async operations
- Error handling trong async context

## Testing Architecture

### Unit Testing Strategy

```java
@ExtendWith(MockitoExtension.class)
class OnramperServiceTest {
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;
    
    @Test
    void testGetQuoteWithValidParameters() {
        // Arrange
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        // ...
        
        // Act
        CompletableFuture<Quote> future = onramperService.getQuote("USD", "BTC", 100.0, null);
        Quote result = future.get();
        
        // Assert
        assertNotNull(result);
        assertEquals("USD", result.getFiatCurrency());
    }
}
```

### Integration Testing

```java
@SpringBootTest
@TestPropertySource(properties = {
    "onramp.providers.onramper.api-key=test-key",
    "onramp.providers.onramper.base-url=https://api-stg.onramper.com"
})
class OnRampIntegrationTest {
    @Autowired
    private OnRampServiceFactory factory;
    
    @Test
    void testFactoryCreatesValidService() {
        OnRampService service = factory.createServiceWithDefaultConfig("onramper");
        assertNotNull(service);
        assertEquals("onramper", service.getProviderName());
    }
}
```

## Performance Considerations

### WebClient Configuration

```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024));
    }
}
```

### Connection Pooling

- Reactor Netty connection pooling
- Configurable timeouts
- Memory limits for large responses

## Security Considerations

### API Key Management

```java
@Data
@Builder
public class OnRampConfig {
    @JsonProperty("api_key")
    private String apiKey;
    
    @JsonProperty("api_secret")
    private String apiSecret;  // Sensitive data
}
```

**Best Practices**:
- Store API keys trong environment variables
- Không log sensitive information
- Use HTTPS cho tất cả API calls
- Validate input parameters

### Input Validation

```java
private void validateQuoteParameters(String fiatCurrency, String cryptoCurrency, 
                                   Double fiatAmount, Double cryptoAmount) {
    if (fiatCurrency == null || fiatCurrency.trim().isEmpty()) {
        throw new IllegalArgumentException("Mã tiền pháp định không được null hoặc rỗng");
    }
    // More validations...
}
```

## Monitoring và Logging

### Structured Logging

```java
@Slf4j
public class OnramperService {
    public CompletableFuture<Quote> getQuote(...) {
        return webClient.get()
                .doOnSuccess(quote -> log.info("Quote retrieved successfully: provider={}, fiat={}, crypto={}, amount={}", 
                    PROVIDER_NAME, fiatCurrency, cryptoCurrency, fiatAmount))
                .doOnError(error -> log.error("Failed to get quote: provider={}, error={}", 
                    PROVIDER_NAME, error.getMessage()));
    }
}
```

### Metrics và Health Checks

```java
@Component
public class OnRampHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check provider availability
        return Health.up()
                .withDetail("providers", getSupportedProviders())
                .build();
    }
}
```

## Extensibility Guidelines

### Thêm Provider mới

1. **Tạo package mới**: `com.onramp.integration.providers.newprovider`
2. **Implement interfaces**: `OnRampService`, `ConfigurableOnRampService`
3. **Tạo DTOs**: Request/Response objects cho API
4. **Đăng ký trong Factory**: Add vào `providerRegistry`
5. **Viết tests**: Unit và integration tests
6. **Update documentation**: API docs và examples

### Code Example cho Provider mới

```java
@Service
public class NewProviderService implements OnRampService, ConfigurableOnRampService {
    private static final String PROVIDER_NAME = "newprovider";
    
    @Override
    public CompletableFuture<Quote> getQuote(...) {
        // Provider-specific implementation
        return webClient.get()
                .uri("/api/v1/quote")
                .retrieve()
                .bodyToMono(NewProviderQuoteResponse.class)
                .map(this::convertToQuote)
                .toFuture();
    }
    
    private Quote convertToQuote(NewProviderQuoteResponse response) {
        // Adapter logic
        return Quote.builder()
                .providerName(PROVIDER_NAME)
                // Map fields...
                .build();
    }
}
```

## Kết luận

Kiến trúc OnRamp Integration System được thiết kế để:

- **Maintainable**: Clean separation of concerns
- **Extensible**: Easy to add new providers
- **Testable**: Comprehensive testing strategy
- **Performant**: Async processing và connection pooling
- **Secure**: Proper handling của sensitive data
- **Observable**: Structured logging và monitoring

Việc sử dụng multiple design patterns tạo ra một hệ thống linh hoạt và robust, đáp ứng được yêu cầu của một sàn giao dịch tiền điện tử hiện đại.

