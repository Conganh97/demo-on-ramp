
# Developer Guide - OnRamp Integration System

## Giới thiệu

Tài liệu này cung cấp hướng dẫn chi tiết cho developers muốn sử dụng, mở rộng hoặc đóng góp vào OnRamp Integration System. Hệ thống được thiết kế để dễ dàng tích hợp các dịch vụ fiat-to-crypto vào ứng dụng của bạn.

## Bắt đầu nhanh

### Thiết lập môi trường phát triển

#### Yêu cầu hệ thống

- **Java**: OpenJDK 17 hoặc cao hơn
- **Maven**: 3.6.0 hoặc cao hơn  
- **IDE**: IntelliJ IDEA, Eclipse, hoặc VS Code với Java extensions
- **Git**: Để clone repository

#### Clone và build project

```bash
# Clone repository
git clone <repository-url>
cd onramp-integration-system

# Build project
mvn clean compile

# Chạy tests
mvn test

# Package application
mvn clean package
```

#### Import vào IDE

**IntelliJ IDEA**:
1. File → Open → Chọn thư mục project
2. Import as Maven project
3. Wait for indexing to complete
4. Configure Project SDK (Java 17+)

**Eclipse**:
1. File → Import → Existing Maven Projects
2. Browse to project directory
3. Import project

### Cấu hình cơ bản

#### Application Properties

Tạo file `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: onramp-integration-system
  
onramp:
  providers:
    onramper:
      api-key: ${ONRAMPER_API_KEY:your-api-key-here}
      api-secret: ${ONRAMPER_API_SECRET:your-secret-here}
      base-url: https://api-stg.onramper.com  # Staging environment
      is-sandbox: true
      timeout: 30
      retry-attempts: 3
      enabled: true

logging:
  level:
    com.onramp.integration: DEBUG
    org.springframework.web.reactive: INFO
```

#### Environment Variables

Tạo file `.env` trong root directory:

```bash
ONRAMPER_API_KEY=your_onramper_api_key
ONRAMPER_API_SECRET=your_onramper_secret
```

**Lưu ý**: Không commit file `.env` vào git. Thêm vào `.gitignore`.

## Sử dụng hệ thống

### Dependency Injection với Spring

```java
@RestController
@RequestMapping("/api/onramp")
public class OnRampController {
    
    private final OnRampServiceFactory serviceFactory;
    
    @Autowired
    public OnRampController(OnRampServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }
    
    @GetMapping("/quote")
    public CompletableFuture<ResponseEntity<Quote>> getQuote(
            @RequestParam String provider,
            @RequestParam String fiatCurrency,
            @RequestParam String cryptoCurrency,
            @RequestParam Double amount) {
        
        try {
            OnRampService service = serviceFactory.createServiceWithDefaultConfig(provider);
            
            return service.getQuote(fiatCurrency, cryptoCurrency, amount, null)
                    .thenApply(quote -> ResponseEntity.ok(quote))
                    .exceptionally(throwable -> {
                        log.error("Error getting quote", throwable);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    });
                    
        } catch (Exception e) {
            log.error("Error creating service", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
            );
        }
    }
}
```

### Sử dụng trực tiếp trong Service

```java
@Service
public class TradingService {
    
    private final OnRampServiceFactory serviceFactory;
    
    @Autowired
    public TradingService(OnRampServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }
    
    public CompletableFuture<Order> buyBitcoin(String userId, Double usdAmount) {
        // Tạo cấu hình cho Onramper
        OnRampConfig config = OnRampConfig.builder()
                .providerName("onramper")
                .apiKey(getApiKeyForUser(userId))
                .baseUrl("https://api.onramper.com")
                .isSandbox(false)
                .build();
        
        OnRampService service = serviceFactory.createService("onramper", config);
        
        // Lấy báo giá trước
        return service.getQuote("USD", "BTC", usdAmount, null)
                .thenCompose(quote -> {
                    // Tạo đơn hàng với báo giá
                    String walletAddress = getUserWalletAddress(userId);
                    String redirectUrl = buildRedirectUrl(userId);
                    
                    return service.createOrder(
                        "USD", "BTC", 
                        usdAmount, null,
                        walletAddress, 
                        redirectUrl
                    );
                })
                .thenApply(order -> {
                    // Lưu order vào database
                    saveOrderToDatabase(order);
                    return order;
                });
    }
    
    private String getApiKeyForUser(String userId) {
        // Logic để lấy API key cho user
        return "user-specific-api-key";
    }
    
    private String getUserWalletAddress(String userId) {
        // Logic để lấy wallet address của user
        return "user-wallet-address";
    }
    
    private String buildRedirectUrl(String userId) {
        return "https://yourapp.com/onramp/callback?userId=" + userId;
    }
    
    private void saveOrderToDatabase(Order order) {
        // Logic để lưu order vào database
    }
}
```

### Error Handling Best Practices

```java
@Service
public class OnRampService {
    
    public CompletableFuture<Quote> getQuoteWithErrorHandling(String provider, 
                                                            String fiatCurrency, 
                                                            String cryptoCurrency, 
                                                            Double amount) {
        try {
            OnRampService service = serviceFactory.createServiceWithDefaultConfig(provider);
            
            return service.getQuote(fiatCurrency, cryptoCurrency, amount, null)
                    .handle((quote, throwable) -> {
                        if (throwable != null) {
                            log.error("Error getting quote from {}: {}", provider, throwable.getMessage());
                            
                            if (throwable instanceof ProviderNotSupportedException) {
                                throw new IllegalArgumentException("Provider not supported: " + provider);
                            } else if (throwable instanceof InvalidConfigurationException) {
                                throw new IllegalStateException("Invalid configuration for provider: " + provider);
                            } else if (throwable instanceof OnRampException) {
                                OnRampException onRampException = (OnRampException) throwable;
                                throw new RuntimeException("OnRamp error: " + onRampException.getErrorCode());
                            } else {
                                throw new RuntimeException("Unexpected error", throwable);
                            }
                        }
                        return quote;
                    });
                    
        } catch (Exception e) {
            log.error("Error creating service for provider {}", provider, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

## Mở rộng hệ thống

### Thêm Provider mới

#### Bước 1: Tạo cấu trúc thư mục

```bash
mkdir -p src/main/java/com/onramp/integration/providers/moonpay
mkdir -p src/test/java/com/onramp/integration/providers/moonpay
```

#### Bước 2: Implement OnRampService

```java
package com.onramp.integration.providers.moonpay;

@Service
@Slf4j
public class MoonPayService implements OnRampService, ConfigurableOnRampService {
    
    private static final String PROVIDER_NAME = "moonpay";
    private static final String PRODUCTION_BASE_URL = "https://api.moonpay.com";
    private static final String STAGING_BASE_URL = "https://api.moonpay.com"; // MoonPay uses same URL
    
    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private OnRampConfig config;
    
    @Autowired
    public MoonPayService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    
    @Override
    public void configure(OnRampConfig config) {
        validateConfig(config);
        this.config = config;
        
        String baseUrl = config.getIsSandbox() ? STAGING_BASE_URL : PRODUCTION_BASE_URL;
        if (config.getBaseUrl() != null && !config.getBaseUrl().trim().isEmpty()) {
            baseUrl = config.getBaseUrl();
        }
        
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Accept", "application/json")
                .build();
                
        log.info("Configured MoonPayService with base URL: {}", baseUrl);
    }
    
    @Override
    public CompletableFuture<Quote> getQuote(String fiatCurrency, String cryptoCurrency, 
                                           Double fiatAmount, Double cryptoAmount) {
        validateConfiguration();
        validateQuoteParameters(fiatCurrency, cryptoCurrency, fiatAmount, cryptoAmount);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v3/currencies/{crypto}/quote")
                    .queryParam("baseCurrency", fiatCurrency.toLowerCase())
                    .queryParam("quoteCurrency", cryptoCurrency.toLowerCase())
                    .queryParam("baseAmount", fiatAmount)
                    .build(cryptoCurrency.toLowerCase()))
                .retrieve()
                .bodyToMono(MoonPayQuoteResponse.class)
                .map(response -> convertToQuote(response, fiatCurrency, cryptoCurrency))
                .doOnSuccess(quote -> log.info("Retrieved quote from MoonPay: {} {} -> {} {}", 
                    quote.getFiatAmount(), quote.getFiatCurrency(),
                    quote.getCryptoAmount(), quote.getCryptoCurrency()))
                .doOnError(error -> log.error("Error getting quote from MoonPay: {}", error.getMessage()))
                .onErrorMap(this::handleWebClientError)
                .toFuture();
    }
    
    @Override
    public CompletableFuture<Order> createOrder(String fiatCurrency, String cryptoCurrency,
                                              Double fiatAmount, Double cryptoAmount,
                                              String walletAddress, String redirectUrl) {
        validateConfiguration();
        validateOrderParameters(fiatCurrency, cryptoCurrency, fiatAmount, cryptoAmount, walletAddress, redirectUrl);
        
        MoonPayOrderRequest request = MoonPayOrderRequest.builder()
                .baseCurrency(fiatCurrency.toLowerCase())
                .quoteCurrency(cryptoCurrency.toLowerCase())
                .baseAmount(fiatAmount)
                .walletAddress(walletAddress)
                .redirectURL(redirectUrl)
                .build();
        
        return webClient.post()
                .uri("/v3/transactions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MoonPayOrderResponse.class)
                .map(this::convertToOrder)
                .doOnSuccess(order -> log.info("Created MoonPay order: {}", order.getOrderId()))
                .doOnError(error -> log.error("Error creating MoonPay order: {}", error.getMessage()))
                .onErrorMap(this::handleWebClientError)
                .toFuture();
    }
    
    // Implement other required methods...
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
    
    // Private helper methods
    private void validateConfig(OnRampConfig config) {
        if (config == null) {
            throw new InvalidConfigurationException("Configuration cannot be null", PROVIDER_NAME);
        }
        if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            throw new InvalidConfigurationException("API key cannot be null or empty", PROVIDER_NAME);
        }
    }
    
    private void validateConfiguration() {
        if (config == null || webClient == null) {
            throw new InvalidConfigurationException("Service not configured", PROVIDER_NAME);
        }
    }
    
    private Quote convertToQuote(MoonPayQuoteResponse response, String fiatCurrency, String cryptoCurrency) {
        return Quote.builder()
                .fiatCurrency(fiatCurrency)
                .cryptoCurrency(cryptoCurrency)
                .fiatAmount(response.getBaseAmount())
                .cryptoAmount(response.getQuoteAmount())
                .exchangeRate(response.getQuoteAmount() / response.getBaseAmount())
                .fee(response.getFeeAmount())
                .totalFiatAmount(response.getTotalAmount())
                .providerName(PROVIDER_NAME)
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // MoonPay quotes expire in 15 minutes
                .build();
    }
    
    private Order convertToOrder(MoonPayOrderResponse response) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .externalOrderId(response.getId())
                .providerName(PROVIDER_NAME)
                .fiatCurrency(response.getBaseCurrency().toUpperCase())
                .cryptoCurrency(response.getQuoteCurrency().toUpperCase())
                .fiatAmount(response.getBaseAmount())
                .cryptoAmount(response.getQuoteAmount())
                .walletAddress(response.getWalletAddress())
                .redirectUrl(response.getRedirectURL())
                .status(mapMoonPayStatusToOrderStatus(response.getStatus()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    private OrderStatus mapMoonPayStatusToOrderStatus(String moonPayStatus) {
        switch (moonPayStatus.toLowerCase()) {
            case "waitingpayment":
                return OrderStatus.PENDING_PAYMENT;
            case "pending":
                return OrderStatus.PROCESSING;
            case "completed":
                return OrderStatus.COMPLETED;
            case "failed":
                return OrderStatus.FAILED;
            default:
                return OrderStatus.PENDING_PAYMENT;
        }
    }
    
    private Throwable handleWebClientError(Throwable error) {
        if (error instanceof WebClientResponseException) {
            WebClientResponseException webError = (WebClientResponseException) error;
            String errorMessage = String.format("MoonPay API error: %d - %s", 
                webError.getStatusCode().value(), webError.getResponseBodyAsString());
            return new OnRampException("API_ERROR", errorMessage, PROVIDER_NAME, error);
        }
        return new OnRampException("NETWORK_ERROR", "Connection error to MoonPay: " + error.getMessage(), PROVIDER_NAME, error);
    }
}
```

#### Bước 3: Tạo DTOs cho MoonPay

```java
// MoonPayOrderRequest.java
@Data
@Builder
public class MoonPayOrderRequest {
    private String baseCurrency;
    private String quoteCurrency;
    private Double baseAmount;
    private String walletAddress;
    private String redirectURL;
}

// MoonPayQuoteResponse.java
@Data
public class MoonPayQuoteResponse {
    private Double baseAmount;
    private Double quoteAmount;
    private Double feeAmount;
    private Double totalAmount;
    private String baseCurrency;
    private String quoteCurrency;
}

// MoonPayOrderResponse.java
@Data
public class MoonPayOrderResponse {
    private String id;
    private String status;
    private String baseCurrency;
    private String quoteCurrency;
    private Double baseAmount;
    private Double quoteAmount;
    private String walletAddress;
    private String redirectURL;
}
```

#### Bước 4: Đăng ký Provider trong Factory

```java
// Trong DefaultOnRampServiceFactory.initializeProviderRegistry()
private void initializeProviderRegistry() {
    providerRegistry.put("onramper", OnramperService.class);
    providerRegistry.put("moonpay", MoonPayService.class);  // Thêm dòng này
    
    log.info("Initialized provider registry with {} providers", providerRegistry.size());
}
```

#### Bước 5: Viết Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class MoonPayServiceTest {
    
    @Mock
    private WebClient.Builder webClientBuilder;
    
    @Mock
    private WebClient webClient;
    
    private MoonPayService moonPayService;
    private OnRampConfig validConfig;
    
    @BeforeEach
    void setUp() {
        moonPayService = new MoonPayService(webClientBuilder);
        
        validConfig = OnRampConfig.builder()
                .providerName("moonpay")
                .apiKey("test-api-key")
                .baseUrl("https://api.moonpay.com")
                .isSandbox(false)
                .build();
        
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
    }
    
    @Test
    void testConfigureWithValidConfig() {
        assertDoesNotThrow(() -> moonPayService.configure(validConfig));
        assertEquals("moonpay", moonPayService.getProviderName());
    }
    
    @Test
    void testGetQuoteSuccess() throws ExecutionException, InterruptedException {
        // Setup mocks and test implementation
        // Similar to OnramperServiceTest
    }
    
    // More tests...
}
```

### Tạo Custom Configuration

```java
@Configuration
@EnableConfigurationProperties(OnRampProvidersConfig.class)
public class OnRampConfiguration {
    
    @Bean
    @ConditionalOnProperty(name = "onramp.providers.moonpay.enabled", havingValue = "true")
    public MoonPayService moonPayService(WebClient.Builder webClientBuilder) {
        return new MoonPayService(webClientBuilder);
    }
    
    @Bean
    @ConditionalOnProperty(name = "onramp.providers.onramper.enabled", havingValue = "true")
    public OnramperService onramperService(WebClient.Builder webClientBuilder) {
        return new OnramperService(webClientBuilder);
    }
}
```

## Testing Guidelines

### Unit Testing

#### Test Structure

```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    
    // Arrange - Setup mocks and data
    @Mock
    private Dependency dependency;
    
    private ServiceUnderTest service;
    
    @BeforeEach
    void setUp() {
        service = new ServiceUnderTest(dependency);
    }
    
    @Test
    void testMethodName_WhenCondition_ThenExpectedResult() {
        // Arrange
        when(dependency.method()).thenReturn(expectedValue);
        
        // Act
        Result result = service.methodUnderTest();
        
        // Assert
        assertThat(result).isEqualTo(expectedValue);
        verify(dependency).method();
    }
}
```

#### Testing Async Methods

```java
@Test
void testAsyncMethod() throws ExecutionException, InterruptedException {
    // Arrange
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(ResponseClass.class)).thenReturn(Mono.just(mockResponse));
    
    // Act
    CompletableFuture<Result> future = service.asyncMethod();
    Result result = future.get();
    
    // Assert
    assertNotNull(result);
    assertEquals(expectedValue, result.getValue());
}
```

#### Testing Error Scenarios

```java
@Test
void testMethodWithError() {
    // Arrange
    when(dependency.method()).thenThrow(new RuntimeException("Test error"));
    
    // Act & Assert
    assertThrows(CustomException.class, () -> service.methodUnderTest());
}

@Test
void testAsyncMethodWithError() {
    // Arrange
    when(responseSpec.bodyToMono(ResponseClass.class))
        .thenReturn(Mono.error(new WebClientResponseException(404, "Not Found", null, null, null)));
    
    // Act
    CompletableFuture<Result> future = service.asyncMethod();
    
    // Assert
    ExecutionException exception = assertThrows(ExecutionException.class, future::get);
    assertTrue(exception.getCause() instanceof OnRampException);
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
    void testCompleteFlow() {
        // Test complete flow from factory to service
        OnRampService service = factory.createServiceWithDefaultConfig("onramper");
        
        assertNotNull(service);
        assertTrue(service.isServiceAvailable().join());
    }
}
```

### Test Configuration

```yaml
# src/test/resources/application-test.yml
spring:
  profiles:
    active: test

onramp:
  providers:
    onramper:
      api-key: test-api-key
      base-url: https://api-stg.onramper.com
      is-sandbox: true
      timeout: 10
      retry-attempts: 1

logging:
  level:
    com.onramp.integration: DEBUG
```

## Debugging và Troubleshooting

### Logging Configuration

```yaml
# application.yml
logging:
  level:
    com.onramp.integration: DEBUG
    org.springframework.web.reactive: DEBUG
    reactor.netty.http.client: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/onramp-integration.log
```

### Common Issues và Solutions

#### 1. WebClient Timeout

**Problem**: Requests timeout khi call external APIs

**Solution**:
```java
@Bean
public WebClient.Builder webClientBuilder() {
    HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)  // Increase timeout
            .responseTimeout(Duration.ofSeconds(60));
            
    return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient));
}
```

#### 2. API Key Issues

**Problem**: 401 Unauthorized errors

**Solution**:
```java
// Check API key format
private void validateApiKey(String apiKey) {
    if (apiKey == null || !apiKey.startsWith("pk_")) {
        throw new InvalidConfigurationException("Invalid API key format");
    }
}
```

#### 3. JSON Parsing Errors

**Problem**: Cannot deserialize JSON responses

**Solution**:
```java
// Add proper Jackson annotations
@JsonProperty("field_name")
private String fieldName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseDto {
    // Fields
}
```

### Debugging Tools

#### Enable Request/Response Logging

```java
@Bean
public WebClient.Builder webClientBuilder() {
    return WebClient.builder()
            .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
                return Mono.just(clientRequest);
            }))
            .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                log.debug("Response: {}", clientResponse.statusCode());
                return Mono.just(clientResponse);
            }));
}
```

#### Health Check Endpoint

```java
@RestController
public class HealthController {
    
    @Autowired
    private OnRampServiceFactory factory;
    
    @GetMapping("/health/onramp")
    public Map<String, Object> checkOnRampHealth() {
        Map<String, Object> health = new HashMap<>();
        
        for (String provider : factory.getSupportedProviders()) {
            try {
                OnRampService service = factory.createServiceWithDefaultConfig(provider);
                boolean available = service.isServiceAvailable().get(5, TimeUnit.SECONDS);
                health.put(provider, available ? "UP" : "DOWN");
            } catch (Exception e) {
                health.put(provider, "ERROR: " + e.getMessage());
            }
        }
        
        return health;
    }
}
```

## Performance Optimization

### Connection Pooling

```java
@Bean
public WebClient.Builder webClientBuilder() {
    ConnectionProvider connectionProvider = ConnectionProvider.builder("onramp-pool")
            .maxConnections(100)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .build();
            
    HttpClient httpClient = HttpClient.create(connectionProvider);
    
    return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient));
}
```

### Caching

```java
@Service
public class CachedOnRampService {
    
    @Cacheable(value = "quotes", key = "#fiatCurrency + '_' + #cryptoCurrency + '_' + #amount")
    public CompletableFuture<Quote> getCachedQuote(String fiatCurrency, String cryptoCurrency, Double amount) {
        return onRampService.getQuote(fiatCurrency, cryptoCurrency, amount, null);
    }
}
```

### Async Processing

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "onrampExecutor")
    public Executor onrampExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("OnRamp-");
        executor.initialize();
        return executor;
    }
}

@Service
public class AsyncOnRampService {
    
    @Async("onrampExecutor")
    public CompletableFuture<List<Quote>> getQuotesFromAllProviders(String fiatCurrency, String cryptoCurrency, Double amount) {
        List<CompletableFuture<Quote>> futures = new ArrayList<>();
        
        for (String provider : factory.getSupportedProviders()) {
            OnRampService service = factory.createServiceWithDefaultConfig(provider);
            futures.add(service.getQuote(fiatCurrency, cryptoCurrency, amount, null));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList()));
    }
}
```

## Security Best Practices

### API Key Management

```java
@Component
public class ApiKeyManager {
    
    @Value("${onramp.encryption.key}")
    private String encryptionKey;
    
    public String encryptApiKey(String apiKey) {
        // Implement encryption logic
        return encrypt(apiKey, encryptionKey);
    }
    
    public String decryptApiKey(String encryptedApiKey) {
        // Implement decryption logic
        return decrypt(encryptedApiKey, encryptionKey);
    }
}
```

### Input Validation

```java
@Component
public class OnRampValidator {
    
    public void validateCurrency(String currency) {
        if (currency == null || !currency.matches("^[A-Z]{3,4}$")) {
            throw new IllegalArgumentException("Invalid currency format");
        }
    }
    
    public void validateAmount(Double amount) {
        if (amount == null || amount <= 0 || amount > 1000000) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }
    
    public void validateWalletAddress(String address) {
        if (address == null || address.length() < 26 || address.length() > 62) {
            throw new IllegalArgumentException("Invalid wallet address");
        }
    }
}
```

### Rate Limiting

```java
@Component
public class RateLimitingService {
    
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String provider, String userId) {
        String key = provider + ":" + userId;
        RateLimiter rateLimiter = rateLimiters.computeIfAbsent(key, 
            k -> RateLimiter.create(10.0)); // 10 requests per second
            
        return rateLimiter.tryAcquire();
    }
}
```

## Deployment

### Docker Configuration

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/onramp-integration-system-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  onramp-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - ONRAMPER_API_KEY=${ONRAMPER_API_KEY}
      - ONRAMPER_API_SECRET=${ONRAMPER_API_SECRET}
    volumes:
      - ./logs:/app/logs
```

### Production Configuration

```yaml
# application-production.yml
spring:
  profiles:
    active: production

onramp:
  providers:
    onramper:
      api-key: ${ONRAMPER_API_KEY}
      api-secret: ${ONRAMPER_API_SECRET}
      base-url: https://api.onramper.com
      is-sandbox: false
      timeout: 30
      retry-attempts: 3

logging:
  level:
    com.onramp.integration: INFO
    org.springframework.web.reactive: WARN
  file:
    name: /app/logs/onramp-integration.log
```

## Kết luận

Developer Guide này cung cấp tất cả thông tin cần thiết để:

- Thiết lập và sử dụng OnRamp Integration System
- Mở rộng hệ thống với providers mới
- Test và debug hiệu quả
- Optimize performance
- Deploy an toàn

Để có thêm thông tin chi tiết, tham khảo:
- [Architecture Documentation](ARCHITECTURE.md)
- [API Reference](API_REFERENCE.md)
- [Contributing Guidelines](CONTRIBUTING.md)

