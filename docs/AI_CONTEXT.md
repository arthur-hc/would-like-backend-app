# AI Context Guide - Would Like API Project

## Project Overview for AI Assistants

This document provides comprehensive context for AI assistants working on the Would Like API project. Use this as a reference to understand the project's architecture, decisions, and constraints.

## Project Identity
- **Name**: Would Like API Backend System
- **Type**: RESTful API / Backend Service
- **Language**: Java 17+
- **Framework**: Spring Boot 3.x
- **Architecture Pattern**: Clean Architecture + Hexagonal (Ports and Adapters) Architecture + Domain-Driven Design (DDD)
- **Purpose**: Portfolio project demonstrating modern Java backend development

## Core Business Domain

### Primary Entity: WishList
A wish list belongs to a user and contains multiple items that the user desires to acquire.

### Key Domain Concepts
```java
// Domain Entities (Always preserve these relationships)
User (1) ←→ (*) WishList ←→ (*) WishListItem
         ↓
      UserId (Value Object)
         ↓
   WishListId (Value Object)
         ↓
  WishListItemId (Value Object)
```

### Business Rules (NEVER VIOLATE)
1. **User must exist** before creating a wish list
2. **Wish list names must be unique** per user
3. **Items cannot exist** without a parent wish list
4. **Users can only access their own** wish lists (privacy rule)
5. **Price must be positive** if specified
6. **Email must be unique** across all users

## Hexagonal Architecture Layers (STRICT SEPARATION)

### 1. Domain Layer (`com.wouldlike.domain`) - CORE (Innermost Ring) - Clean Architecture
**RULES**: 
- NO external dependencies (no Spring, no JPA annotations, no external libraries)
- Pure business logic only (Clean Architecture)
- Entities are rich objects with behavior (DDD)
- **Ports (Interfaces)** defined here for repositories and gateways (Hexagonal)
- Repository interfaces defined here (implementation elsewhere)

```java
// Correct Domain Entity Structure (Clean Architecture + DDD)
public class WishList {
    private WishListId id;
    private UserId userId;
    private String name;
    private List<WishListItem> items;
    
    // Business methods (not getters/setters)
    public void addItem(WishListItem item) {
        // Business validation logic here
    }
}

// Domain Ports (Interfaces) - Hexagonal Architecture in Clean Architecture Domain
public interface WishListRepository {  // Repository Port (Hexagonal)
    WishList save(WishList wishList);
    Optional<WishList> findById(WishListId id);
}

public interface EmailGateway {        // Gateway Port (Hexagonal)
    void sendWelcomeEmail(Email to, String firstName);
    void sendWishListSharedNotification(Email to, WishList wishList);
}
```

### 2. Application Layer (`com.wouldlike.application`) - Second Ring - Clean Architecture
**RULES**:
- Contains use cases (Clean Architecture application services)
- Orchestrates domain objects
- No direct database access
- DTOs for data transfer only
- **Depends only on domain layer interfaces (ports)** - Hexagonal principle

**🚫 CRITICAL RULE - NEVER INJECT USE CASES INTO OTHER USE CASES:**
```java
// ❌ WRONG - Creates tight coupling and circular dependencies
@Service
public class CreateWishListUseCase {
    private final AddItemToWishListUseCase addItemUseCase; // BAD PRACTICE!
    
    public CreateWishListUseCase(AddItemToWishListUseCase addItemUseCase) {
        this.addItemUseCase = addItemUseCase; // VIOLATION!
    }
}

// ✅ CORRECT - Use application coordinators for complex workflows
@Service
public class WishListWorkflowCoordinator {
    private final CreateWishListUseCase createWishListUseCase;
    private final AddItemToWishListUseCase addItemUseCase;
    private final EmailGateway emailGateway;          // Domain Port
    
    // Coordinates multiple use cases without coupling them
    public WishListResponse createWishListWithItems(CreateWishListWithItemsRequest request) {
        WishList wishList = createWishListUseCase.execute(request.getWishListData());
        for (ItemData itemData : request.getItems()) {
            addItemUseCase.execute(wishList.getId(), itemData);
        }
        // Use gateway for external communication
        emailGateway.sendWishListCreatedNotification(request.getUserEmail(), wishList);
        return mapToResponse(wishList);
    }
}

// ✅ CORRECT - Use cases depend only on domain ports
@Service
public class CreateUserUseCase {
    private final UserRepository userRepository;    // Domain Port
    private final EmailGateway emailGateway;        // Domain Port
    
    public UserResponse execute(CreateUserRequest request) {
        // Business logic using only domain interfaces
    }
}
```

### 3. Gateway Layer (`com.wouldlike.gateway`) - Third Ring - Hexagonal Architecture
**RULES**:
- **Adapters** that implement domain gateway interfaces (Hexagonal)
- Handle external service communications
- Can depend on external libraries (Stripe, SendGrid, AWS SDK, etc.)
- Implements ports defined in domain layer (Clean Architecture domain)

```java
// Gateway Adapter implementing Domain Port (Hexagonal Adapter for Clean Architecture)
@Component
public class SendGridEmailGateway implements EmailGateway {
    
    @Override
    public void sendWelcomeEmail(Email to, String firstName) {
        // SendGrid specific implementation
        Mail mail = new Mail();
        mail.setFrom(new com.sendgrid.Email("noreply@wouldlike.com"));
        // ... SendGrid implementation
    }
}

@Component
public class StripePaymentGateway implements PaymentGateway {
    
    @Override
    public PaymentResult processPayment(Money amount, PaymentMethod method) {
        // Stripe specific implementation - Hexagonal Adapter
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amount.getAmountInCents())
            .setCurrency(amount.getCurrency().getCode().toLowerCase())
            .build();
        // ... Stripe implementation
    }
}
```

### 4. Infrastructure Layer (`com.wouldlike.infrastructure`) - Fourth Ring - Clean Architecture
**RULES**:
- JPA entities separate from domain entities (Clean Architecture)
- Repository implementations (Hexagonal adapters for Clean Architecture repository ports)
- External service integrations
- Spring configurations

### 5. Presentation Layer (`com.wouldlike.presentation`) - Outermost Ring - Clean Architecture
**RULES**:
- REST controllers only (Clean Architecture interface adapters)
- Request/Response models
- Authentication/Authorization
- Global exception handling

## Critical Design Patterns

### Ports and Adapters Pattern (HEXAGONAL ARCHITECTURE)
```java
// Port (Interface) - Always in Domain Layer
public interface EmailGateway {
    void sendWelcomeEmail(Email to, String firstName);
}

// Adapter (Implementation) - In Gateway Layer
@Component
public class SendGridEmailGateway implements EmailGateway {
    // SendGrid specific implementation
}

@Component
public class MockEmailGateway implements EmailGateway {
    // Mock implementation for testing
}
```

### Repository Pattern (DATABASE ABSTRACTION)
```java
// Domain Interface (Port) - NEVER change this
public interface WishListRepository {
    WishList save(WishList wishList);
    Optional<WishList> findById(WishListId id);
    List<WishList> findByUserId(UserId userId);
    void deleteById(WishListId id);
}

// Infrastructure Implementation (Adapter)
@Repository
public class JpaWishListRepository implements WishListRepository {
    // JPA-specific implementation with domain/entity mapping
}
```

### Value Objects Pattern
```java
// Always immutable, always with validation
public class Email {
    private final String value;
    
    private Email(String value) {
        if (!isValidEmail(value)) {
            throw new InvalidEmailException(value);
        }
        this.value = value;
    }
    
    public static Email of(String value) {
        return new Email(value);
    }
}
```

## Development Constraints & Rules

### Code Quality Requirements
- **Test Coverage**: Minimum 85% (MANDATORY)
- **Method Length**: Maximum 20 lines
- **Class Length**: Maximum 200 lines
- **Cyclomatic Complexity**: Maximum 10
- **No public fields** in entities
- **No anemic domain models** (rich behavior required)
- **NO USE CASE INJECTION** into other use cases
- **ALL external dependencies** must go through gateway interfaces

### Dependency Rules (Clean Architecture + Hexagonal Architecture)
```
Domain (Core) ← Application ← Gateway/Infrastructure ← Presentation

CLEAN ARCHITECTURE DEPENDENCY RULES:
✅ Application → Domain (Clean Architecture dependency rule)
✅ Infrastructure → Domain (Clean Architecture dependency rule)
✅ Presentation → Application (Clean Architecture dependency rule)

HEXAGONAL ARCHITECTURE INTEGRATION:
✅ Gateway → Domain (implements domain ports/interfaces)
✅ Infrastructure → Domain (implements domain ports/interfaces)  
✅ All external dependencies through ports (Hexagonal principle)

NEVER:
❌ Domain → Application/Gateway/Infrastructure/Presentation (Clean Architecture violation)
❌ Use Case → Use Case (Clean Architecture + good practice)
❌ Domain → External Libraries (Both architectures violation)
```

### Architectural Testing (MANDATORY)
```java
// Add ArchUnit tests to prevent violations of BOTH architectures
@ArchTest
static final ArchRule cleanArchitectureDependencyRule = 
    noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..application..", "..gateway..", "..infrastructure..", "..presentation..");

@ArchTest
static final ArchRule hexagonalPortsImplementationRule =
    classes().that().resideInAPackage("..gateway..")
        .and().areNotInterfaces()
        .should().implement(JavaClass.Predicates.resideInAPackage("..domain.gateways.."));

@ArchTest
static final ArchRule cleanArchitectureUseCaseIsolation =
    noClasses().that().resideInAPackage("..application.usecases..")
        .should().dependOnClassesThat().resideInAPackage("..application.usecases..");

@ArchTest 
static final ArchRule hexagonalExternalDependencyIsolation =
    noClasses().that().resideInAnyPackage("..domain..", "..application..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("com.stripe..", "com.sendgrid..", "software.amazon.awssdk..");
```

### Spring Boot Configuration
```yaml
# Always use these profiles
spring.profiles.active: dev | test | prod

# Required dependencies in pom.xml
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-boot-starter-test
- testcontainers (for integration tests)
- archunit-junit5 (for architectural testing)
# Gateway dependencies (add as needed)
- stripe-java (payment gateway)
- sendgrid-java (email gateway)
- aws-java-sdk-s3 (file storage gateway)
```

## Database Design Principles

### Table Naming Convention
- `users` (not user - reserved word)
- `wish_lists` (snake_case)
- `wish_list_items`
- Always plural table names

### Required Columns (EVERY TABLE)
```sql
id UUID PRIMARY KEY DEFAULT gen_random_uuid()
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
version INT DEFAULT 1  -- for optimistic locking
```

### Foreign Key Constraints (ALWAYS ENFORCE)
```sql
-- Example: wish_lists table
user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
```

## Testing Strategy (MANDATORY PATTERNS)

### Unit Test Structure
```java
@DisplayName("WishList Entity Tests")
class WishListTest {
    
    @Test
    @DisplayName("Should create wish list with valid data")
    void shouldCreateWishListWithValidData() {
        // Given
        UserId userId = UserId.of("user-123");
        String name = "My Birthday Wishes";
        
        // When
        WishList wishList = WishList.create(userId, name);
        
        // Then
        assertThat(wishList.getName()).isEqualTo(name);
        assertThat(wishList.getUserId()).isEqualTo(userId);
    }
}
```

### Integration Test Pattern
```java
@SpringBootTest
@TestContainers
class WishListRepositoryIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb");
    
    // Test with real database
}
```

### Use Case Isolation Testing
```java
// ✅ CORRECT - Mock all dependencies, no other use cases
@ExtendWith(MockitoExtension.class)
class CreateWishListUseCaseTest {
    
    @Mock
    private WishListRepository wishListRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private CreateWishListUseCase createWishListUseCase;
    
    // Test only this use case in isolation
}
```

## Security Implementation Rules

### Authentication (JWT Strategy)
```java
// ALWAYS implement these endpoints
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

### Authorization Rules
- **Own resources only**: Users can only access their own wish lists
- **Admin role**: Can access all resources (future feature)
- **Public wish lists**: Readable by anyone (future feature)

### Security Headers (ALWAYS ENABLE)
```yaml
security:
  headers:
    content-type-options: true
    frame-options: deny
    xss-protection: true
```

## Error Handling Patterns

### Domain Exceptions
```java
// Always extend from base domain exception
public class WishListNotFoundException extends WishListDomainException {
    public WishListNotFoundException(WishListId id) {
        super("Wish list not found with id: " + id.getValue());
    }
}
```

### API Error Response Format (NEVER CHANGE)
```json
{
  "error": {
    "code": "WISHLIST_NOT_FOUND",
    "message": "Wish list not found with id: 123",
    "timestamp": "2025-01-04T10:00:00Z",
    "path": "/api/v1/wishlists/123"
  }
}
```

## API Design Rules

### REST Endpoint Patterns (ALWAYS FOLLOW)
```
GET    /api/v1/wishlists           # Get user's wish lists
POST   /api/v1/wishlists           # Create wish list
GET    /api/v1/wishlists/{id}      # Get specific wish list
PUT    /api/v1/wishlists/{id}      # Update wish list
DELETE /api/v1/wishlists/{id}      # Delete wish list

POST   /api/v1/wishlists/{id}/items     # Add item
PUT    /api/v1/wishlists/{id}/items/{itemId}  # Update item
DELETE /api/v1/wishlists/{id}/items/{itemId}  # Remove item
```

### Response Pagination (ALWAYS IMPLEMENT)
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "currentPage": 0,
  "pageSize": 10
}
```

## Message Queue Integration

### Event Publishing Pattern
```java
// Domain Events (ALWAYS implement)
public class WishListCreated implements DomainEvent {
    private final WishListId wishListId;
    private final UserId userId;
    private final Instant occurredOn;
}

// Publisher (Infrastructure layer)
@Component
public class RabbitMQEventPublisher implements DomainEventPublisher {
    public void publish(DomainEvent event) {
        // RabbitMQ publishing logic
    }
}
```

## Performance Requirements

### Response Time Targets
- **GET requests**: < 100ms (95th percentile)
- **POST/PUT requests**: < 200ms (95th percentile)
- **DELETE requests**: < 50ms (95th percentile)

### Caching Strategy
```java
// Method-level caching
@Cacheable(value = "wishlists", key = "#userId")
public List<WishList> findByUserId(UserId userId) {
    // Implementation
}
```

## Docker Configuration Requirements

### Dockerfile Pattern (ALWAYS USE)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/wouldlike-api.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app.jar"]
```

## Environment-Specific Configurations

### Required Environment Variables
```bash
# Database
DB_HOST=localhost
DB_NAME=wouldlike_db
DB_USERNAME=wouldlike_user
DB_PASSWORD=secure_password

# JWT
JWT_SECRET=your-256-bit-secret
JWT_EXPIRATION=86400000

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

## Common Pitfalls to Avoid

### ❌ NEVER DO THIS
1. **Inject use cases into other use cases** (creates coupling)
2. **Put external library calls in domain or application layers**
3. **Make domain entities depend on infrastructure concerns**
4. **Implement gateway interfaces outside the gateway layer**
5. Put business logic in controllers
6. Use JPA entities as DTOs
7. Access repositories from domain entities
8. Expose internal IDs in API responses
9. Use database auto-increment IDs (use UUIDs)
10. Skip input validation
11. Return null (use Optional instead)
12. Ignore exception handling
13. Hard-code configuration values
14. Skip tests for new features

### ✅ ALWAYS DO THIS
1. **Keep use cases isolated and independent**
2. **Use coordinators for complex workflows that need multiple use cases**
3. **Define gateway interfaces in domain layer, implement in gateway layer**
4. **Use gateway layer for all external service communications**
5. **Keep external libraries isolated in gateway/infrastructure layers**
6. Validate input at domain boundaries
7. Use value objects for important concepts
8. Keep layers separated according to hexagonal architecture
9. Write tests before implementation (TDD preferred)
10. Use meaningful exception messages
11. Log important business events
12. Implement health checks
13. Use transactions appropriately
14. Follow REST conventions
15. Document API changes
16. **Add ArchUnit tests to prevent architectural violations**

## Gateway Layer Patterns

### Multiple Implementation Pattern
```java
// Domain Port
public interface PaymentGateway {
    PaymentResult processPayment(Money amount, PaymentMethod method);
}

// Multiple Gateway Implementations
@Component("stripePaymentGateway")
public class StripePaymentGateway implements PaymentGateway { }

@Component("paypalPaymentGateway") 
public class PayPalPaymentGateway implements PaymentGateway { }

@Component("mockPaymentGateway")
@Profile("test")
public class MockPaymentGateway implements PaymentGateway { }

// Configuration
@Configuration
public class GatewayConfiguration {
    
    @Bean
    @Primary
    @ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
    public PaymentGateway stripePaymentGateway() {
        return new StripePaymentGateway();
    }
    
    @Bean
    @ConditionalOnProperty(name = "payment.provider", havingValue = "paypal")
    public PaymentGateway paypalPaymentGateway() {
        return new PayPalPaymentGateway();
    }
}
```

### Gateway Error Handling
```java
@Component
public class SendGridEmailGateway implements EmailGateway {
    
    @Override
    public void sendWelcomeEmail(Email to, String firstName) {
        try {
            // SendGrid implementation
        } catch (IOException e) {
            throw new EmailGatewayException("Failed to send welcome email", e);
        }
    }
}

// Domain Exception for Gateway Failures
public class EmailGatewayException extends WishListDomainException {
    public EmailGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Complex Workflow Management with Gateways
```java
// ✅ CORRECT approach for complex workflows with external services
@Service
public class PurchaseWorkflowCoordinator {
    private final GetWishListUseCase getWishListUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final EmailGateway emailGateway;           // External service
    private final PaymentGateway paymentGateway;       // External service
    
    // Orchestrates multiple use cases and external services
    public PurchaseResult coordinatePurchase(PurchaseRequest request) {
        // 1. Get wish list (use case)
        WishList wishList = getWishListUseCase.execute(request.getWishListId());
        
        // 2. Process payment (use case + gateway)
        PaymentResult payment = processPaymentUseCase.execute(request.getPaymentData());
        
        // 3. Send confirmation email (gateway)
        emailGateway.sendPurchaseConfirmation(request.getUserEmail(), wishList, payment);
        
        // 4. Update external systems if needed
        // More gateway calls...
        
        return PurchaseResult.success(payment, wishList);
    }
}
```

## Troubleshooting Guide for AI

### When Tests Fail
1. Check layer separation violations
2. **Verify no use case dependencies on other use cases**
3. Verify mock configurations
4. Ensure test data builders are used
5. Check transaction boundaries

### When Build Fails
1. Verify Maven dependencies
2. Check Java version compatibility
3. Ensure proper package structure (`com.wouldlike.*`)
4. Validate Spring Boot configurations

### When Runtime Errors Occur
1. Check database connectivity
2. Verify environment variables
3. Ensure proper exception handling
4. Check security configurations

### When Architecture Tests Fail
1. Check for use case coupling violations
2. Verify layer dependency rules
3. Ensure proper package structure
4. Review import statements for violations

## AI Assistant Guidelines

### When Making Changes
1. **Always read existing code** before modifying
2. **Preserve architectural layers** strictly
3. **NEVER inject use cases into other use cases**
4. **Add tests** for any new functionality
5. **Update documentation** when changing APIs
6. **Consider backward compatibility**
7. **Follow existing patterns** consistently
8. **Use coordinators for complex workflows**

### When Answering Questions
1. Reference this guide for architectural decisions
2. Suggest improvements that align with Clean Architecture
3. Always consider testability
4. Prioritize maintainability over quick fixes
5. Explain trade-offs and alternatives
6. **Always prevent use case coupling**

### When Implementing Complex Features
1. **Use application coordinators** instead of coupling use cases
2. **Consider domain services** for complex business logic
3. **Implement proper event handling** for async operations
4. **Maintain single responsibility** for each use case

This guide should be referenced for every code change, architectural decision, and feature implementation. It ensures consistency and maintains the project's high standards while preventing common architectural violations.
