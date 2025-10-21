# Guia de Desenvolvimento

## Configuração do Ambiente

### Pré-requisitos
- **Java 17+**
- **Maven 3.6+** 
- **MySQL 8.0+** (produção)
- **IDE** com suporte a Spring Boot (IntelliJ IDEA recomendado)

### Setup Inicial

1. **Clonar o repositório**
```bash
git clone <repository-url>
cd would-like-backend-app
```

2. **Configurar banco local (desenvolvimento)**
```bash
# O projeto usa H2 em memória por padrão
# Para MySQL local, configure application-dev.properties
```

3. **Executar aplicação**
```bash
mvn clean compile
mvn spring-boot:run
```

4. **Verificar funcionamento**
```bash
curl http://localhost:8080/api/users
```

## Estrutura de Desenvolvimento

### Criando um Novo Caso de Uso

**Exemplo**: Adicionar funcionalidade "Favoritar Item"

1. **Criar o Use Case**
```java
// src/main/java/com/wouldlike/backendapp/application/usecase/
@Component
public class FavoriteItemUseCase {
    
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    
    public FavoriteItemUseCase(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }
    
    public void execute(Long userId, Long itemId) {
        // Validações e lógica específica
        User user = userRepository.findById(userId).orElseThrow();
        Item item = itemRepository.findById(itemId).orElseThrow();
        
        // Implementar lógica de favoritar
        // NÃO chamar outros use cases
    }
}
```

2. **Adicionar endpoint no Controller**
```java
@PostMapping("/items/{itemId}/favorite")
public ResponseEntity<Void> favoriteItem(
    @PathVariable Long itemId,
    @RequestParam Long userId) {
    
    favoriteItemUseCase.execute(userId, itemId);
    return ResponseEntity.ok().build();
}
```

3. **Criar testes**
```java
@Test
void shouldFavoriteItem() {
    // Arrange
    // Act
    // Assert
}
```

### Regras de Desenvolvimento

#### ✅ Boas Práticas

1. **Um Use Case = Uma Responsabilidade**
   - Cada classe faz apenas uma coisa
   - Métodos `execute()` com parâmetros claros

2. **Injeção de Dependência por Constructor**
   ```java
   public CreateUserUseCase(UserRepository userRepository) {
       this.userRepository = userRepository;
   }
   ```

3. **Validações no Use Case**
   ```java
   public User execute(User user) {
       if (user.getUsername() == null) {
           throw new IllegalArgumentException("Username é obrigatório");
       }
       return userRepository.save(user);
   }
   ```

4. **Tratamento de Erros Específicos**
   ```java
   User user = userRepository.findById(id)
       .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
   ```

#### ❌ Anti-Padrões

1. **Use Case chamando Use Case**
   ```java
   // ERRADO
   public class CreateWishlistUseCase {
       @Autowired
       private CreateUserUseCase createUserUseCase; // ❌
   }
   ```

2. **Lógica de negócio no Controller**
   ```java
   // ERRADO
   @PostMapping("/users")
   public User createUser(@RequestBody User user) {
       if (user.getUsername() == null) { // ❌ Validação no controller
           throw new BadRequestException("Username obrigatório");
       }
       return createUserUseCase.execute(user);
   }
   ```

3. **Repository com lógica de negócio**
   ```java
   // ERRADO
   public interface UserRepository extends JpaRepository<User, Long> {
       default User createUserWithValidation(User user) { // ❌
           // Lógica de negócio no repository
       }
   }
   ```

## Configuração de Ambiente

### Perfis de Ambiente

#### Desenvolvimento (padrão)
```properties
# application.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

#### Produção
```properties
# application-prod.properties
spring.datasource.url=jdbc:mysql://localhost:3306/wouldlike_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

### Variáveis de Ambiente
```bash
# Desenvolvimento
export SPRING_PROFILES_ACTIVE=dev

# Produção
export SPRING_PROFILES_ACTIVE=prod
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export JWT_SECRET=your-secret-key
```

## Testes

### Estrutura de Testes

```
src/test/java/com/wouldlike/backendapp/
├── usecase/           # Testes unitários dos use cases
├── controller/        # Testes de integração dos controllers
└── repository/        # Testes de repositório
```

### Testes Unitários (Use Cases)
```java
@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private CreateUserUseCase useCase;
    
    @Test
    void shouldCreateUser() {
        // Given
        User user = new User("john", "john@email.com", "password");
        when(userRepository.save(user)).thenReturn(user);
        
        // When
        User result = useCase.execute(user);
        
        // Then
        assertThat(result).isEqualTo(user);
        verify(userRepository).save(user);
    }
}
```

### Testes de Integração (Controllers)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateUser() {
        // Given
        User user = new User("john", "john@email.com", "password");
        
        // When
        ResponseEntity<User> response = restTemplate.postForEntity("/api/users", user, User.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

### Executar Testes
```bash
# Todos os testes
mvn test

# Apenas testes unitários
mvn test -Dtest="*UseCaseTest"

# Apenas testes de integração
mvn test -Dtest="*IntegrationTest"
```

## Debugging

### Logs Úteis
```properties
# application-dev.properties
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.com.wouldlike.backendapp=DEBUG
```

### Troubleshooting Comum

1. **Erro de dependência circular**
   - Verificar se use cases não estão se chamando
   - Revisar injeções de dependência

2. **Erro de JPA/Hibernate**
   - Verificar relacionamentos nas entidades
   - Conferir configurações de banco

3. **Erro de autenticação**
   - Verificar configuração JWT
   - Testar endpoints sem autenticação primeiro

## Contribuição

### Workflow de Desenvolvimento

1. **Criar branch para feature**
```bash
git checkout -b feature/nome-da-feature
```

2. **Implementar seguindo as regras**
   - Criar use case primeiro
   - Adicionar testes
   - Atualizar controller se necessário

3. **Executar testes e validações**
```bash
mvn clean test
mvn clean compile
```

4. **Commit e push**
```bash
git add .
git commit -m "feat: adicionar funcionalidade X"
git push origin feature/nome-da-feature
```

5. **Criar Pull Request**
   - Descrever mudanças
   - Incluir testes
   - Revisar código

Esta estrutura garante que o desenvolvimento seja consistente e mantenha a qualidade arquitetural do projeto.
