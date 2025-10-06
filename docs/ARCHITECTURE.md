# System Architecture - Would Like API

## Architecture Overview
This project implements **Clean Architecture** combined with **Hexagonal (Ports and Adapters) Architecture** and **Domain-Driven Design (DDD)** principles to ensure maintainability, testability, and complete isolation from external dependencies.

## Clean Architecture + Hexagonal Architecture Integration

**Clean Architecture** provides the **layer structure and dependency rules**, while **Hexagonal Architecture** provides the **ports and adapters pattern** for external integrations. They work together perfectly:

- **Clean Architecture**: Defines the concentric layers and dependency inversion rules
- **Hexagonal Architecture**: Defines how external dependencies are abstracted through ports (interfaces) and adapters (implementations)
- **Domain-Driven Design**: Provides the business modeling approach

## Combined Architecture Principles
- **Domain at the center**: Pure business logic with no external dependencies (Clean Architecture)
- **Dependency Rule**: Dependencies point inward only (Clean Architecture)
- **Ports and Adapters**: Interfaces define contracts for external systems (Hexagonal)
- **Dependency Inversion**: All dependencies point inward toward the domain (Both)
- **External concerns isolation**: Infrastructure details are pluggable (Hexagonal)

## Architectural Layers (Clean Architecture Structure with Hexagonal Ports)

### 1. Domain Layer (Core) - Innermost Ring - Clean Architecture
- **Entities**: Core business objects (User, WishList, WishListItem, Product)
- **Value Objects**: Immutable objects (Email, ProductId, Money)
- **Domain Services**: Business logic that doesn't belong to entities
- **Repository Interfaces (Ports)**: Abstract data access contracts - **Hexagonal**
- **Gateway Interfaces (Ports)**: Abstract external service contracts - **Hexagonal**
- **Domain Events**: For decoupled communication

```
domain/
├── entities/              # Clean Architecture - Domain Entities
├── valueobjects/          # Clean Architecture - Value Objects
├── services/              # Clean Architecture - Domain Services
├── repositories/          # Hexagonal - Repository Ports (Interfaces)
├── gateways/             # Hexagonal - Gateway Ports (Interfaces)
└── events/               # Clean Architecture - Domain Events
```

### 2. Application Layer (Use Cases) - Second Ring - Clean Architecture
- **Use Cases**: Application-specific business rules (Clean Architecture)
- **DTOs**: Data transfer objects for layer communication
- **Application Services**: Orchestrate domain objects and external services
- **Event Handlers**: Handle domain events
- **Coordinators**: Complex workflow orchestration (prevents use case coupling)

**CRITICAL RULE**: Never inject one use case into another use case. This violates single responsibility principle and creates tight coupling, circular dependencies, and makes testing difficult. Use domain services or application coordinators instead.

```
application/
├── usecases/             # Clean Architecture - Use Cases
├── dtos/                 # Clean Architecture - Data Transfer Objects
├── services/             # Clean Architecture - Application Services
├── coordinators/         # Clean Architecture - Workflow Coordinators
└── handlers/             # Clean Architecture - Event Handlers
```

### 3. Gateway Layer (Adapters) - Third Ring - Hexagonal Architecture
**This is the Hexagonal Architecture contribution - Adapters that implement Domain Ports**

- **Payment Gateways**: Stripe, PayPal, etc. (Hexagonal Adapters)
- **Email Services**: SendGrid, Amazon SES, etc. (Hexagonal Adapters)
- **SMS Services**: Twilio, etc. (Hexagonal Adapters)
- **External APIs**: Product catalogs, price comparison (Hexagonal Adapters)
- **File Storage**: AWS S3, Azure Blob, etc. (Hexagonal Adapters)
- **Cache Services**: Redis implementations (Hexagonal Adapters)

```
gateway/                  # Hexagonal - Adapters Layer
├── payment/              # Hexagonal - Payment Adapters
│   ├── StripePaymentGateway.java
│   └── PayPalPaymentGateway.java
├── email/                # Hexagonal - Email Adapters
│   ├── SendGridEmailGateway.java
│   └── SesEmailGateway.java
├── storage/              # Hexagonal - Storage Adapters
│   ├── S3FileStorageGateway.java
│   └── LocalFileStorageGateway.java
├── external/             # Hexagonal - External API Adapters
│   ├── ProductCatalogGateway.java
│   └── PriceComparisonGateway.java
└── cache/                # Hexagonal - Cache Adapters
    └── RedisCacheGateway.java
```

### 4. Infrastructure Layer (Adapters) - Fourth Ring - Both Architectures
- **Repository Implementations**: JPA/Hibernate implementations (Hexagonal Adapters for Clean Architecture)
- **Message Queues**: RabbitMQ/Kafka implementations
- **Configuration**: Spring Boot configurations
- **Database Migrations**: Flyway scripts

```
infrastructure/           # Clean Architecture - Infrastructure Layer
├── repositories/         # Hexagonal - Repository Adapters
├── messaging/            # Hexagonal - Messaging Adapters
├── config/               # Clean Architecture - Configuration
└── migrations/           # Clean Architecture - Database Migrations
```

### 5. Presentation Layer (Adapters) - Outermost Ring - Both Architectures
- **REST Controllers**: HTTP endpoints (Clean Architecture Controllers + Hexagonal Input Adapters)
- **Request/Response Models**: API contracts
- **Exception Handlers**: Global error handling
- **Security Configuration**: Authentication/Authorization

```
presentation/             # Clean Architecture - Interface Adapters
├── controllers/          # Clean Architecture + Hexagonal Input Adapters
├── models/               # Clean Architecture - Interface Models
├── security/             # Clean Architecture - Security Adapters
└── handlers/             # Clean Architecture - Exception Adapters
```

## Clean Architecture + Hexagonal Architecture - Ports and Adapters

### Domain Ports (Interfaces in Domain Layer) - Hexagonal Architecture
```java
// Repository Port (Domain Layer) - Hexagonal Port for Clean Architecture
public interface WishListRepository {
    WishList save(WishList wishList);
    Optional<WishList> findById(WishListId id);
    List<WishList> findByUserId(UserId userId);
    void deleteById(WishListId id);
}

// Gateway Ports (Domain Layer) - Hexagonal Ports for External Services
public interface EmailGateway {
    void sendWelcomeEmail(Email to, String firstName);
    void sendWishListSharedNotification(Email to, WishList wishList);
    void sendPasswordResetEmail(Email to, String resetToken);
}

public interface PaymentGateway {
    PaymentResult processPayment(Money amount, PaymentMethod method);
    PaymentResult refundPayment(PaymentId paymentId, Money amount);
    PaymentStatus getPaymentStatus(PaymentId paymentId);
}
```

### Infrastructure Adapters (Implementations) - Hexagonal Adapters
```java
// Repository Adapter (Infrastructure Layer) - Hexagonal Adapter for Clean Architecture Repository
@Repository
public class JpaWishListRepository implements WishListRepository {
    @Autowired
    private WishListJpaRepository jpaRepository;
    
    @Override
    public WishList save(WishList wishList) {
        WishListEntity entity = WishListMapper.toEntity(wishList);
        WishListEntity saved = jpaRepository.save(entity);
        return WishListMapper.toDomain(saved);
    }
    // ... other implementations
}
```

### Gateway Adapters (Gateway Layer) - Hexagonal Adapters for External Services
```java
// Email Gateway Adapter (Gateway Layer) - Hexagonal Adapter
@Component
public class SendGridEmailGateway implements EmailGateway {
    
    @Override
    public void sendWelcomeEmail(Email to, String firstName) {
        // SendGrid specific implementation
        Mail mail = new Mail();
        mail.setFrom(new com.sendgrid.Email("noreply@wouldlike.com"));
        mail.addTo(new com.sendgrid.Email(to.getValue()));
        mail.setSubject("Welcome to Would Like!");
        // ... SendGrid implementation
    }
}

// Payment Gateway Adapter (Gateway Layer) - Hexagonal Adapter
@Component
public class StripePaymentGateway implements PaymentGateway {
    
    @Override
    public PaymentResult processPayment(Money amount, PaymentMethod method) {
        // Stripe specific implementation
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amount.getAmountInCents())
            .setCurrency(amount.getCurrency().getCode().toLowerCase())
            .build();
        // ... Stripe implementation
    }
}
```

### Clean Architecture Use Case with Hexagonal Ports
```java
// Use Case (Clean Architecture) using Hexagonal Ports
@Service
public class CreateUserUseCase {
    private final UserRepository userRepository;      // Hexagonal Port
    private final EmailGateway emailGateway;          // Hexagonal Port
    
    public CreateUserUseCase(UserRepository userRepository, 
                           EmailGateway emailGateway) {
        this.userRepository = userRepository;
        this.emailGateway = emailGateway;
    }
    
    public UserResponse execute(CreateUserRequest request) {
        // Clean Architecture Use Case Logic
        
        // Validate business rules
        if (userRepository.existsByEmail(Email.of(request.getEmail()))) {
            throw new UserAlreadyExistsException(request.getEmail());
        }
        
        // Create domain entity
        User user = User.create(
            Email.of(request.getEmail()),
            request.getFirstName(),
            request.getLastName()
        );
        
        // Save user through Hexagonal Port
        User savedUser = userRepository.save(user);
        
        // Send welcome email through Hexagonal Port
        emailGateway.sendWelcomeEmail(
            savedUser.getEmail(), 
            savedUser.getFirstName()
        );
        
        return UserMapper.toResponse(savedUser);
    }
}
```

## Why Clean Architecture + Hexagonal Architecture Work Together

### **Clean Architecture Provides:**
1. **Layer Structure**: Clear separation of concerns across layers
2. **Dependency Rules**: Dependencies always point inward
3. **Use Case Focus**: Application logic is use-case driven
4. **Testability**: Each layer can be tested independently

### **Hexagonal Architecture Provides:**
1. **Port/Adapter Pattern**: Clean abstraction of external dependencies
2. **Multiple Implementations**: Easy to swap external services
3. **External Isolation**: Business logic completely isolated from external concerns
4. **Pluggable Architecture**: Add/remove external services without core changes

### **Together They Provide:**
1. **Complete Isolation**: Business logic is pure and testable
2. **Flexible Integrations**: Easy to add/change external services
3. **Clear Boundaries**: Well-defined interfaces between all components
4. **Scalable Design**: Ready for microservices decomposition
