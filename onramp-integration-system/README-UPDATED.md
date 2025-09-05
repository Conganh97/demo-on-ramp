# OnRamp Integration System - Updated

## Overview

This project has been updated to use **RestTemplate** instead of WebClient and includes several optimizations:

### Key Changes Made

1. **WebClient → RestTemplate Migration**
   - Replaced all WebClient calls with RestTemplate
   - Updated configuration from WebClientConfig to RestTemplateConfig
   - Added Apache HttpClient 5 for connection pooling and timeouts

2. **Singleton Pattern Implementation**
   - Implemented singleton pattern for OnRamp service providers
   - Services are created once and cached for reuse
   - Thread-safe concurrent cache with ConcurrentHashMap
   - Cache management endpoints for monitoring and control
   - Significant performance improvement by avoiding repeated service creation

3. **Code Optimization**
   - Created `BaseOnRampService` abstract class to reduce code duplication
   - Added `HttpClientUtil` utility class for common HTTP operations
   - Simplified API methods using functional programming patterns
   - Improved error handling and validation

4. **Configuration Management**
   - Added `OnRampProperties` for externalized configuration
   - Created `application.yml` with proper configuration structure
   - Support for environment variables and profiles

5. **Architecture Improvements**
   - Better separation of concerns
   - Cleaner factory pattern implementation
   - Enhanced exception handling
   - Added REST controller for API endpoints
   - Cache management and monitoring capabilities

## Project Structure

```
src/main/java/com/onramp/integration/
├── config/
│   ├── OnRampProperties.java          # Configuration properties
│   └── RestTemplateConfig.java        # RestTemplate configuration
├── controllers/
│   ├── OnRampController.java          # REST API endpoints
│   └── OnRampManagementController.java # Cache management endpoints
├── demo/
│   └── SingletonDemo.java             # Singleton pattern demonstration
├── core/
│   └── OnRampService.java            # Core service interface
├── exceptions/                        # Custom exceptions
├── factories/                         # Factory pattern implementation
├── models/                           # Data models and DTOs
├── providers/
│   └── onramper/                     # Onramper implementation
├── services/
│   └── BaseOnRampService.java        # Base service class
└── utils/
    └── HttpClientUtil.java           # HTTP utility class
```

## Configuration

### Application Properties (application.yml)

```yaml
onramp:
  providers:
    onramper:
      api-key: ${ONRAMPER_API_KEY:demo-api-key}
      api-secret: ${ONRAMPER_API_SECRET:}
      base-url: ${ONRAMPER_BASE_URL:https://api-stg.onramper.com}
      sandbox: true
      timeout: 30
      retry-attempts: 3
      enabled: true
```

### Environment Variables

- `ONRAMPER_API_KEY`: Your Onramper API key
- `ONRAMPER_API_SECRET`: Your Onramper API secret (optional)
- `ONRAMPER_BASE_URL`: Base URL for Onramper API

## API Endpoints

### Core API Endpoints

#### Get Supported Providers
```
GET /api/onramp/providers
```

### Get Supported Assets
```
GET /api/onramp/{provider}/assets
```

### Get Quote
```
POST /api/onramp/{provider}/quote
Content-Type: application/json

{
  "fiatCurrency": "USD",
  "cryptoCurrency": "BTC",
  "fiatAmount": 100.0
}
```

### Create Order
```
POST /api/onramp/{provider}/order
Content-Type: application/json

{
  "fiatCurrency": "USD",
  "cryptoCurrency": "BTC",
  "fiatAmount": 100.0,
  "walletAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
  "redirectUrl": "https://yourapp.com/success"
}
```

### Get Order Status
```
GET /api/onramp/{provider}/order/{orderId}
```

#### Health Check
```
GET /api/onramp/{provider}/health
```

### Cache Management Endpoints

#### Get Cache Information
```
GET /api/onramp/management/cache/info
```

#### Clear All Cache
```
DELETE /api/onramp/management/cache
```

#### Evict Specific Provider
```
DELETE /api/onramp/management/cache/{provider}
```

#### Check Provider Cache Status
```
GET /api/onramp/management/cache/{provider}
```

#### Warm Up Provider Cache
```
POST /api/onramp/management/cache/{provider}/warmup
```

#### Warm Up All Providers
```
POST /api/onramp/management/cache/warmup-all
```

## Running the Application

1. **Set environment variables:**
   ```bash
   export ONRAMPER_API_KEY=your-api-key
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the application:**
   - API: http://localhost:8080/api/onramp
   - H2 Console: http://localhost:8080/h2-console
   - Health: http://localhost:8080/actuator/health

## Singleton Pattern Benefits

### Performance Improvements
- **Single Instance Creation**: Each provider service is created only once
- **Memory Efficiency**: Reduced memory footprint by reusing instances
- **Faster Response Times**: No overhead of creating new instances for each request
- **Connection Reuse**: HTTP connections are maintained across requests

### Thread Safety
- **ConcurrentHashMap**: Thread-safe cache implementation
- **Synchronized Creation**: Double-check locking pattern prevents race conditions
- **Immutable Configuration**: Once created, service instances are immutable

### Cache Management
- **Real-time Monitoring**: View cache status and statistics
- **Manual Control**: Clear cache or evict specific providers
- **Warmup Capabilities**: Pre-load services for better performance
- **Automatic Cleanup**: Built-in cache management methods

## Key Benefits of the Update

1. **Performance**: RestTemplate with connection pooling + singleton pattern for optimal efficiency
2. **Simplicity**: Cleaner code with less reactive complexity
3. **Maintainability**: Better separation of concerns and reusable components
4. **Configuration**: Externalized configuration with environment variable support
5. **Monitoring**: Built-in health checks, metrics, and cache monitoring endpoints
6. **Resource Management**: Singleton pattern reduces resource consumption
7. **Scalability**: Better performance under high load with cached instances

## Adding New Providers

To add a new OnRamp provider:

1. Create a new service class extending `BaseOnRampService`
2. Implement the required abstract methods
3. Add the provider to the registry in `DefaultOnRampServiceFactory`
4. Add configuration in `application.yml`

Example:
```java
@Service
public class MoonPayService extends BaseOnRampService {
    // Implementation
}
```

## Dependencies

- Spring Boot 3.2.0
- Apache HttpClient 5
- Lombok
- Jackson
- H2 Database (for demo)
- Spring Boot Actuator

## Singleton Pattern Usage

### Automatic Caching
Services are automatically cached when created:

```java
// First call - creates and caches the service
OnRampService service1 = factory.createServiceWithDefaultConfig("onramper");

// Second call - returns cached instance
OnRampService service2 = factory.createServiceWithDefaultConfig("onramper");

// service1 == service2 (same instance)
```

### Manual Cache Access
```java
// Get cached service without creating
OnRampService cached = factory.getCachedService("onramper");

// Returns null if not cached
if (cached != null) {
    // Use cached service
}
```

### Cache Management
```java
// Clear specific provider
factory.evictService("onramper");

// Clear all cache
factory.clearCache();

// Get cache size
int size = factory.getCacheSize();
```

## Testing

The project includes unit tests and integration tests. Run tests with:

```bash
mvn test
```

### Singleton Demo
The application includes a demonstration that runs on startup:

```bash
# Run with demo
mvn spring-boot:run

# Skip demo
mvn spring-boot:run -Dspring-boot.run.arguments=skip-demo
```

## Production Considerations

1. Replace H2 with a production database
2. Configure proper logging levels
3. Set up monitoring and alerting
4. Use secure API key management
5. Configure rate limiting and circuit breakers
6. **Cache Monitoring**: Monitor cache hit rates and memory usage
7. **Cache Warmup**: Consider warming up cache during application startup
8. **Cache Expiration**: Implement cache expiration for long-running applications
9. **Memory Management**: Monitor memory usage with singleton instances
