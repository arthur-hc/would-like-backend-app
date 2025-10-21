# API Documentation

## Visão Geral

API REST para gerenciamento de listas de desejos seguindo padrões RESTful.

**Base URL**: `http://localhost:8080/api`

## Autenticação

🔄 **Em desenvolvimento**: JWT será implementado na próxima iteração.

Por enquanto, a API usa autenticação básica:
- **Usuário**: `user`
- **Senha**: `password`

## Recursos Principais

### 👤 Usuários

#### Listar todos os usuários
```http
GET /api/users
```

**Resposta de sucesso (200)**:
```json
[
  {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com"
  }
]
```

#### Buscar usuário por ID
```http
GET /api/users/{id}
```

**Resposta de sucesso (200)**:
```json
{
  "id": 1,
  "username": "john_doe", 
  "email": "john@example.com"
}
```

#### Criar usuário
```http
POST /api/users
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword"
}
```

**Resposta de sucesso (201)**:
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com"
}
```

#### Atualizar usuário
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "username": "john_updated",
  "email": "john_new@example.com",
  "password": "newPassword"
}
```

#### Excluir usuário
```http
DELETE /api/users/{id}
```

**Resposta de sucesso (204)**: Sem conteúdo

---

### 📋 Listas de Desejos

#### Buscar listas de um usuário
```http
GET /api/wishlists/user/{userId}
```

**Resposta de sucesso (200)**:
```json
[
  {
    "id": 1,
    "name": "Aniversário 2024",
    "description": "Presentes para meu aniversário",
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com"
    }
  }
]
```

#### Buscar lista por ID
```http
GET /api/wishlists/{id}
```

#### Criar lista para usuário
```http
POST /api/wishlists/user/{userId}
Content-Type: application/json

{
  "name": "Natal 2024",
  "description": "Presentes de Natal que eu gostaria de receber"
}
```

**Resposta de sucesso (201)**:
```json
{
  "id": 2,
  "name": "Natal 2024",
  "description": "Presentes de Natal que eu gostaria de receber",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com"
  }
}
```

#### Atualizar lista
```http
PUT /api/wishlists/{id}
Content-Type: application/json

{
  "name": "Natal 2024 - Atualizada",
  "description": "Lista atualizada de presentes"
}
```

#### Excluir lista
```http
DELETE /api/wishlists/{id}
```

---

### 🎁 Itens da Lista

#### Buscar itens de uma lista
```http
GET /api/items/wishlist/{wishlistId}
```

**Resposta de sucesso (200)**:
```json
[
  {
    "id": 1,
    "name": "Smartphone",
    "description": "iPhone 15 Pro Max",
    "price": 1299.99,
    "url": "https://apple.com/iphone-15-pro",
    "wishlist": {
      "id": 1,
      "name": "Aniversário 2024"
    }
  }
]
```

#### Buscar item por ID
```http
GET /api/items/{id}
```

#### Adicionar item à lista
```http
POST /api/items/wishlist/{wishlistId}
Content-Type: application/json

{
  "name": "Livro de Clean Architecture",
  "description": "Livro do Robert Martin sobre arquitetura limpa",
  "price": 89.90,
  "url": "https://amazon.com.br/clean-architecture"
}
```

**Resposta de sucesso (201)**:
```json
{
  "id": 2,
  "name": "Livro de Clean Architecture",
  "description": "Livro do Robert Martin sobre arquitetura limpa", 
  "price": 89.90,
  "url": "https://amazon.com.br/clean-architecture",
  "wishlist": {
    "id": 1,
    "name": "Aniversário 2024"
  }
}
```

#### Atualizar item
```http
PUT /api/items/{id}
Content-Type: application/json

{
  "name": "Livro Clean Architecture - 2ª Edição",
  "description": "Livro atualizado sobre arquitetura limpa",
  "price": 95.90,
  "url": "https://amazon.com.br/clean-architecture-2ed"
}
```

#### Excluir item
```http
DELETE /api/items/{id}
```

## Códigos de Status

### Sucesso
- **200 OK**: Operação bem-sucedida
- **201 Created**: Recurso criado com sucesso  
- **204 No Content**: Exclusão bem-sucedida

### Erro do Cliente
- **400 Bad Request**: Dados inválidos na requisição
- **401 Unauthorized**: Autenticação necessária
- **403 Forbidden**: Acesso negado
- **404 Not Found**: Recurso não encontrado

### Erro do Servidor
- **500 Internal Server Error**: Erro interno do servidor

## Exemplos de Uso

### Cenário: Criando uma lista completa

1. **Criar usuário**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -u user:password \
  -d '{"username":"maria","email":"maria@email.com","password":"123456"}'
```

2. **Criar lista para o usuário**
```bash
curl -X POST http://localhost:8080/api/wishlists/user/1 \
  -H "Content-Type: application/json" \
  -u user:password \
  -d '{"name":"Aniversário","description":"Meus desejos de aniversário"}'
```

3. **Adicionar itens à lista**
```bash
curl -X POST http://localhost:8080/api/items/wishlist/1 \
  -H "Content-Type: application/json" \
  -u user:password \
  -d '{"name":"Notebook","description":"MacBook Pro","price":3999.99,"url":"https://apple.com"}'
```

4. **Consultar lista completa**
```bash
curl http://localhost:8080/api/wishlists/1 \
  -u user:password
```

## Validações

### Usuário
- `username`: obrigatório, único
- `email`: obrigatório, formato válido
- `password`: obrigatório, mínimo 6 caracteres

### Lista de Desejos  
- `name`: obrigatório
- `description`: opcional
- `user`: deve existir

### Item
- `name`: obrigatório
- `description`: opcional
- `price`: opcional, formato decimal
- `url`: opcional, formato URL válido
- `wishlist`: deve existir

## Próximas Implementações

### 🔄 Em Desenvolvimento
- **JWT Authentication**: Substituir basic auth
- **Paginação**: Para listas grandes
- **Filtros**: Busca por nome, preço, etc.

### ⏳ Roadmap
- **Upload de imagens**: Para itens
- **Compartilhamento**: Listas públicas
- **Favoritos**: Marcar itens favoritos
- **Notificações**: Alertas de preço

## Testando a API

### Postman Collection
Importe a collection Postman disponível em `/docs/postman/` para testar todos os endpoints.

### Swagger UI
🔄 **Em desenvolvimento**: Interface Swagger será adicionada em breve.

### Scripts de Teste
```bash
# Executar testes de integração da API
mvn test -Dtest="*ControllerIntegrationTest"
```
