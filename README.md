# Would Like Backend App

Backend MVP para gerenciamento de listas de desejos utilizando princípios de Clean Architecture.

## 🎯 Objetivo

Desenvolver uma API REST simples e bem estruturada para gerenciamento de listas de desejos, seguindo princípios de arquitetura limpa, SOLID e DDD.

## 🏗️ Arquitetura

O projeto segue os princípios da **Clean Architecture** com **Arquitetura Hexagonal**:

- **Domain**: Entidades de negócio e regras
- **Application**: Casos de uso específicos e isolados
- **Infrastructure**: Adaptadores para banco de dados, web e segurança

### Estrutura de Pastas
```
src/main/java/com/wouldlike/backendapp/
├── domain/                     # Entidades de domínio
├── application/usecase/        # Casos de uso individuais
└── infrastructure/
    ├── persistence/            # Repositórios JPA
    ├── web/                   # Controllers REST
    └── config/                # Configurações
```

## 🚀 Tecnologias (MVP)

- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Security + JWT**
- **Spring Data JPA**
- **MySQL** (produção)
- **H2** (testes)
- **Maven**

## 📋 Funcionalidades Iniciais

### Usuários
- Criar usuário
- Buscar usuário por ID
- Listar todos os usuários
- Atualizar usuário
- Excluir usuário
- Buscar por username

### Listas de Desejos
- Criar lista para um usuário
- Buscar lista por ID
- Listar listas de um usuário
- Atualizar lista
- Excluir lista

### Itens da Lista
- Adicionar item à lista
- Buscar item por ID
- Listar itens de uma lista
- Atualizar item
- Excluir item

## 🛠️ Configuração

### Pré-requisitos
- Java 17+
- Maven 3.6+
- MySQL 8.0+ (para produção)

### Executar Localmente
```bash
# Compilar
mvn clean compile

# Executar testes
mvn test

# Executar aplicação (usa H2 em memória por padrão)
mvn spring-boot:run
```

### Configuração MySQL
No `application-prod.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wouldlike_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## 🔐 Autenticação

O projeto usa **JWT** para autenticação. Configure a chave secreta no `application.properties`:
```properties
jwt.secret=your-secret-key
jwt.expiration=86400000
```

## 🧪 Testes

- **Unitários**: Testam casos de uso isoladamente
- **Integração**: Testam controllers com Testcontainers + MySQL

```bash
mvn test
```

## 📚 Próximos Passos

1. ✅ MVP básico com CRUD
2. 🔄 Autenticação JWT
3. 🔄 Validações de negócio
4. 🔄 Testes automatizados
5. ⏳ Cache (Redis)
6. ⏳ Mensageria (RabbitMQ)
7. ⏳ Monitoramento (Actuator)

## 🤝 Contribuição

1. Siga os princípios da Clean Architecture
2. Cada caso de uso deve ser uma classe separada
3. Evite dependências entre serviços/casos de uso
4. Escreva testes para novos casos de uso
5. Mantenha a documentação atualizada

## 📖 Documentação

- `/docs/ARCHITECTURE.md` - Detalhes da arquitetura
- `/docs/DEVELOPMENT.md` - Guia de desenvolvimento
- `/docs/API.md` - Documentação da API
