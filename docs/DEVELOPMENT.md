# Development Guide - Would Like API

## Prerequisites
- **Java 17+** (OpenJDK or Oracle JDK)
- **Maven 3.8+**
- **Docker Desktop**
- **MySQL 8.0+** (for local development)
- **Git**
- **IDE**: IntelliJ IDEA (recommended) or Eclipse

## Local Development Setup

### 1. Environment Configuration
Create `.env` file in project root:
```env
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=wouldlike_db
DB_USERNAME=wouldlike_user
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION=86400000

# RabbitMQ Configuration
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# Application Configuration
SERVER_PORT=8080
ACTIVE_PROFILE=dev
```

### 2. Database Setup
```bash
# Using Docker (Recommended)
docker run --name wouldlike-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=wouldlike_db -e MYSQL_USER=wouldlike_user -e MYSQL_PASSWORD=your_password -p 3306:3306 -d mysql:8.0

# Or install MySQL locally and create database
mysql -u root -p
CREATE DATABASE wouldlike_db;
CREATE USER 'wouldlike_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON wouldlike_db.* TO 'wouldlike_user'@'localhost';
```

### 3. RabbitMQ Setup
```bash
# Using Docker
docker run -d --name wouldlike-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# Access management UI: http://localhost:15672 (guest/guest)
```

## Project Structure & Package Organization

```
src/main/java/com/wouldlike/
├── WouldLikeApplication.java           # Spring Boot main class
├── domain/                           # Domain Layer (Core - Innermost Ring)
│   ├── entities/
│   │   ├── User.java
│   │   ├── WishList.java
│   │   ├── WishListItem.java
│   │   └── Product.java
│   ├── valueobjects/
│   │   ├── UserId.java
│   │   ├── WishListId.java
│   │   ├── Email.java
│   │   └── Money.java
│   ├── repositories/                 # Ports (Interfaces)
│   │   ├── UserRepository.java
│   │   ├── WishListRepository.java
│   │   └── ProductRepository.java
│   ├── gateways/                     # Ports (Interfaces) - NEW!
│   │   ├── EmailGateway.java
│   │   ├── PaymentGateway.java
│   │   ├── FileStorageGateway.java
│   │   └── ProductCatalogGateway.java
│   ├── services/                     # Domain Services
│   │   └── WishListDomainService.java
│   └── events/                       # Domain Events
│       ├── WishListCreated.java
│       └── ItemAddedToWishList.java
├── application/                      # Application Layer (Use Cases - Second Ring)
│   ├── usecases/
│   │   ├── CreateWishListUseCase.java
│   │   ├── AddItemToWishListUseCase.java
│   │   └── GetUserWishListsUseCase.java
│   ├── dtos/
│   │   ├── CreateWishListRequest.java
│   │   ├── WishListResponse.java
│   │   └── UserResponse.java
│   ├── coordinators/                 # NEW! - For complex workflows
│   │   └── WishListWorkflowCoordinator.java
│   └── services/
│       └── WishListApplicationService.java
├── gateway/                          # Gateway Layer (Third Ring) - NEW!
│   ├── payment/
│   │   ├── StripePaymentGateway.java
│   │   ├── PayPalPaymentGateway.java
│   │   └── MockPaymentGateway.java
│   ├── email/
│   │   ├── SendGridEmailGateway.java
│   │   ├── SesEmailGateway.java
│   │   └── MockEmailGateway.java
│   ├── storage/
│   │   ├── S3FileStorageGateway.java
│   │   ├── LocalFileStorageGateway.java
│   │   └── MockFileStorageGateway.java
│   ├── external/
│   │   ├── ProductCatalogGateway.java
│   │   └── PriceComparisonGateway.java
│   └── cache/
│       └── RedisCacheGateway.java
├── infrastructure/                   # Infrastructure Layer (Fourth Ring)
│   ├── repositories/
│   │   ├── JpaUserRepository.java
│   │   ├── JpaWishListRepository.java
│   │   └── JpaProductRepository.java
│   ├── messaging/
│   │   ├── RabbitMQEventPublisher.java
│   │   └── WishListEventHandler.java
│   ├── config/
│   │   ├── DatabaseConfig.java
│   │   ├── MessagingConfig.java
│   │   └── GatewayConfig.java        # NEW!
│   └── migrations/
│       └── V1__initial_schema.sql
└── presentation/                     # Presentation Layer (Outermost Ring)
    ├── controllers/
    │   ├── WishListController.java
    │   ├── UserController.java
    │   └── AuthController.java
    ├── models/
    │   ├── requests/
    │   └── responses/
    ├── security/
    │   ├── JwtAuthenticationFilter.java
    │   └── JwtTokenProvider.java
    └── handlers/
        └── GlobalExceptionHandler.java
```

## Coding Standards & Best Practices

### 1. Naming Conventions
- **Classes**: PascalCase (e.g., `WishListService`)
- **Methods**: camelCase (e.g., `createWishList`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_ITEMS_PER_LIST`)
- **Packages**: lowercase (e.g., `com.wouldlike.domain.entities`)

### 2. Code Quality Rules
- **Method Length**: Maximum 20 lines
- **Class Length**: Maximum 200 lines
- **Cyclomatic Complexity**: Maximum 10
- **Parameter Count**: Maximum 5 parameters
- **Line Length**: Maximum 120 characters

### 3. Critical Architecture Rules - Hexagonal Architecture
**🚫 NEVER inject a use case into another use case:**
```java
// ❌ WRONG - Creates coupling and circular dependencies
@Service
public class CreateWishListUseCase {
    private final AddItemToWishListUseCase addItemUseCase; // BAD!
    
    public CreateWishListUseCase(AddItemToWishListUseCase addItemUseCase) {
        this.addItemUseCase = addItemUseCase;
    }
}

// ✅ CORRECT - Use application coordinators for complex workflows
@Service
public class WishListWorkflowCoordinator {
    private final CreateWishListUseCase createWishListUseCase;
    private final AddItemToWishListUseCase addItemUseCase;
    private final EmailGateway emailGateway;                    # Gateway Port
    
    // This coordinator orchestrates multiple use cases and external services
    public WishListResponse createWishListWithItems(CreateWishListWithItemsRequest request) {
        WishList wishList = createWishListUseCase.execute(request.getWishListData());
        
        for (ItemData itemData : request.getItems()) {
            addItemUseCase.execute(wishList.getId(), itemData);
        }
        
        // Use gateway for external communication
        emailGateway.sendWishListCreatedNotification(
            request.getUserEmail(), 
            wishList
        );
        
        return mapToResponse(wishList);
    }
}

// ✅ CORRECT - Domain ports (interfaces) in domain layer
// Domain Layer - Gateway Port (Interface)
public interface EmailGateway {
    void sendWishListCreatedNotification(Email to, WishList wishList);
    void sendWelcomeEmail(Email to, String firstName);
}

// Gateway Layer - Gateway Adapter (Implementation)
@Component
public class SendGridEmailGateway implements EmailGateway {
    @Override
    public void sendWishListCreatedNotification(Email to, WishList wishList) {
        // SendGrid specific implementation
    }
}
```

**🔒 Hexagonal Architecture Rules:**
```java
// ✅ CORRECT - Dependencies point inward
@Service
public class CreateUserUseCase {
    private final UserRepository userRepository;        // Domain Port
    private final EmailGateway emailGateway;            // Domain Port
    // Use cases depend only on domain interfaces (ports)
}

// ❌ WRONG - Don't depend on concrete implementations
@Service
public class CreateUserUseCase {
    private final JpaUserRepository jpaUserRepository;  // Infrastructure class - BAD!
    private final SendGridEmailGateway emailService;    // Gateway class - BAD!
}
```

### 4. Documentation Standards
```java
/**
 * Creates a new wish list for the specified user.
 * 
 * @param userId the unique identifier of the user
 * @param name the name of the wish list
 * @param description optional description of the wish list
 * @return the created wish list
 * @throws UserNotFoundException if the user doesn't exist
 * @throws DuplicateWishListNameException if user already has a wish list with the same name
 */
public WishList createWishList(UserId userId, String name, String description) {
    // Implementation
}
```

### 5. Exception Handling Strategy
```java
// Domain Exceptions
public class WishListDomainException extends RuntimeException {
    public WishListDomainException(String message) {
        super(message);
    }
}

// Application Exceptions
public class WishListNotFoundException extends WishListDomainException {
    public WishListNotFoundException(WishListId id) {
        super("Wish list not found with id: " + id.getValue());
    }
}
```

## Testing Strategy

### 1. Unit Tests (85%+ Coverage)
```java
// Domain Entity Test Example - Pure unit test
@Test
void shouldCreateWishListWithValidData() {
    // Given
    UserId userId = UserId.of("user-123");
    String name = "My Birthday Wishes";
    
    // When
    WishList wishList = WishList.create(userId, name);
    
    // Then
    assertThat(wishList.getName()).isEqualTo(name);
    assertThat(wishList.getUserId()).isEqualTo(userId);
    assertThat(wishList.getItems()).isEmpty();
}

// Use Case Test with Mocked Dependencies (Hexagonal Architecture)
@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {
    
    @Mock
    private UserRepository userRepository;        // Mock the port
    
    @Mock
    private EmailGateway emailGateway;           // Mock the gateway port
    
    @InjectMocks
    private CreateUserUseCase createUserUseCase;
    
    @Test
    void shouldCreateUserAndSendWelcomeEmail() {
        // Given
        CreateUserRequest request = new CreateUserRequest("john@example.com", "John", "Doe");
        User expectedUser = User.create(Email.of("john@example.com"), "John", "Doe");
        
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(expectedUser);
        
        // When
        UserResponse response = createUserUseCase.execute(request);
        
        // Then
        verify(userRepository).save(any(User.class));
        verify(emailGateway).sendWelcomeEmail(Email.of("john@example.com"), "John");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }
}
```

### 2. Integration Tests - Hexagonal Architecture
```java
@SpringBootTest
@TestContainers
class WishListRepositoryIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    void shouldSaveAndRetrieveWishList() {
        // Test implementation using real database
        // Tests the infrastructure adapter (JpaWishListRepository)
    }
}

// Gateway Integration Test
@SpringBootTest
class EmailGatewayIntegrationTest {
    
    @Autowired
    @Qualifier("sendGridEmailGateway")  // Test specific implementation
    private EmailGateway emailGateway;
    
    @Test
    void shouldSendEmailThroughSendGrid() {
        // Integration test with real SendGrid (or test environment)
    }
}
```

### 3. Architectural Tests - Hexagonal Architecture
```java
@AnalyzeClasses(packages = "com.wouldlike")
class HexagonalArchitectureTest {
    
    @ArchTest
    static final ArchRule domainLayerShouldNotDependOnOuterLayers = 
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..gateway..", "..infrastructure..", "..presentation..");
    
    @ArchTest
    static final ArchRule applicationLayerShouldOnlyDependOnDomain = 
        classes().that().resideInAPackage("..application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..domain..", "..application..", "java..", "javax..", "org.springframework.stereotype..");
    
    @ArchTest
    static final ArchRule useCasesShouldNotInjectOtherUseCases =
        noClasses().that().resideInAPackage("..application.usecases..")
            .should().dependOnClassesThat().resideInAPackage("..application.usecases..");
    
    @ArchTest
    static final ArchRule gatewaysShouldImplementDomainPorts =
        classes().that().resideInAPackage("..gateway..")
            .and().areNotInterfaces()
            .should().implement(JavaClass.Predicates.resideInAPackage("..domain.gateways.."));
    
    @ArchTest
    static final ArchRule repositoriesShouldImplementDomainPorts =
        classes().that().resideInAPackage("..infrastructure.repositories..")
            .should().implement(JavaClass.Predicates.resideInAPackage("..domain.repositories.."));
    
    @ArchTest
    static final ArchRule onlyGatewayLayerShouldDependOnExternalLibraries =
        noClasses().that().resideInAnyPackage("..domain..", "..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.stripe..", "com.sendgrid..", "software.amazon.awssdk..");
}
```

### 4. Test Data Builders with Gateway Mocks
```java
public class WishListTestDataBuilder {
    private UserId userId = UserId.of("default-user");
    private String name = "Default Wish List";
    
    public WishListTestDataBuilder withUserId(UserId userId) {
        this.userId = userId;
        return this;
    }
    
    public WishListTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public WishList build() {
        return WishList.create(userId, name);
    }
}

// Gateway Test Doubles
@TestConfiguration
public static class TestGatewayConfiguration {
    
    @Bean
    @Primary
    public EmailGateway mockEmailGateway() {
        return Mockito.mock(EmailGateway.class);
    }
    
    @Bean
    @Primary
    public PaymentGateway mockPaymentGateway() {
        return Mockito.mock(PaymentGateway.class);
    }
}
```

## Build & Run Commands

### Development Mode
```bash
# Run with live reload
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Package application
mvn clean package

# Run with Docker Compose
docker-compose up --build
```

### Production Build
```bash
# Create production JAR
mvn clean package -Pprod

# Build Docker image
docker build -t wishlist-api:latest .

# Run production container
docker run -p 8080:8080 --env-file .env.prod wishlist-api:latest
```

## CI/CD Pipeline Requirements

### 1. Build Pipeline Stages
- **Compile**: Maven compile
- **Test**: Unit tests + Integration tests
- **Quality Gate**: SonarQube analysis (80%+ coverage, 0 bugs)
- **Security Scan**: OWASP dependency check
- **Package**: Docker image creation
- **Deploy**: Environment-specific deployment

### 2. Quality Gates
- **Test Coverage**: Minimum 85%
- **Code Quality**: SonarQube quality gate pass
- **Security**: No high/critical vulnerabilities
- **Performance**: API response time < 200ms

### 3. Deployment Strategy
- **Development**: Auto-deploy on merge to `develop`
- **Staging**: Auto-deploy on merge to `main`
- **Production**: Manual approval required

## IDE Configuration

### IntelliJ IDEA Setup
1. Install plugins:
   - SonarLint
   - JPA Buddy
   - Docker
   - Database Navigator

2. Code style configuration:
   - Import `checkstyle.xml` (to be created)
   - Set line length to 120
   - Enable auto-formatting on save

3. Run configurations:
   - Spring Boot application
   - JUnit test suite
   - Docker Compose

## Troubleshooting Common Issues

### Database Connection Issues
```bash
# Check MySQL container status
docker ps

# View MySQL logs
docker logs wishlist-mysql

# Connect to MySQL container
docker exec -it wishlist-mysql mysql -u wishlist_user -p
```

### Application Startup Issues
```bash
# Check application logs
tail -f logs/application.log

# Verify active profile
java -jar target/wishlist-api.jar --spring.profiles.active=dev

# Health check endpoint
curl http://localhost:8080/actuator/health
```

## Performance Monitoring
- **Application Metrics**: http://localhost:8080/actuator/metrics
- **Health Check**: http://localhost:8080/actuator/health
- **Database Metrics**: Connection pool status via actuator
- **Memory Usage**: JVM heap monitoring
