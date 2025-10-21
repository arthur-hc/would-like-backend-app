# Roadmap do Projeto

## Visão Geral

Evolução incremental do **Would Like Backend App**, priorizando entrega de valor e qualidade arquitetural.

## 🎯 MVP - Versão 0.1.0 (Atual)

### ✅ Funcionalidades Implementadas
- [x] Arquitetura Clean + Hexagonal
- [x] Casos de uso individuais e separados
- [x] CRUD completo de usuários
- [x] CRUD completo de listas de desejos
- [x] CRUD completo de itens
- [x] Relacionamentos JPA (User → Wishlist → Item)
- [x] Autenticação básica temporária
- [x] Banco H2 para desenvolvimento
- [x] Configuração MySQL para produção

### 🔄 Em Desenvolvimento
- [ ] Implementação JWT completa
- [ ] Validações de negócio robustas
- [ ] Testes unitários para todos os use cases
- [ ] Testes de integração para controllers
- [ ] Tratamento de exceções personalizado

---

## 📋 Fase 1 - Consolidação (v0.2.0)

**Objetivo**: Tornar o MVP robusto e pronto para produção

### Segurança & Autenticação
- [ ] JWT com refresh tokens
- [ ] Middleware de autenticação
- [ ] Autorização por recursos (usuário só acessa suas listas)
- [ ] Criptografia de senhas (BCrypt)

### Validações & Qualidade
- [ ] Bean Validation nas entidades
- [ ] Validações de negócio nos use cases
- [ ] Exception handlers globais
- [ ] Response DTOs para não expor entidades

### Testes
- [ ] Cobertura de testes > 80%
- [ ] Testes unitários para todos os use cases
- [ ] Testes de integração com Testcontainers
- [ ] Testes de segurança

### DevOps Básico
- [ ] Profiles de ambiente (dev, test, prod)
- [ ] Health checks
- [ ] Logs estruturados
- [ ] Docker container

**Entrega estimada**: 3-4 semanas

---

## 🚀 Fase 2 - Funcionalidades Avançadas (v0.3.0)

**Objetivo**: Expandir funcionalidades e melhorar UX

### Novas Funcionalidades
- [ ] **Compartilhamento de listas**
  - Listas públicas/privadas
  - Links de compartilhamento
  - Visualização sem login

- [ ] **Sistema de favoritos**
  - Marcar itens como favoritos
  - Prioridade de itens
  - Ordenação customizada

- [ ] **Busca e filtros**
  - Busca por nome de item
  - Filtro por faixa de preço
  - Filtro por categoria (novo campo)

- [ ] **Melhorias na API**
  - Paginação e ordenação
  - API versionada (/v1/)
  - Rate limiting básico

### Arquitetura
- [ ] **Domain Events**
  - Eventos para ações importantes
  - Handlers assíncronos
  
- [ ] **Value Objects**
  - Email, Money, URL como VOs
  - Validações de domínio

- [ ] **Specifications Pattern**
  - Consultas complexas e reutilizáveis

**Entrega estimada**: 4-5 semanas

---

## ⚡ Fase 3 - Performance e Escala (v0.4.0)

**Objetivo**: Otimizar para uso em produção

### Cache e Performance  
- [ ] **Redis Cache**
  - Cache de listas frequentemente acessadas
  - Cache de usuários autenticados
  - TTL configurável

- [ ] **Otimizações JPA**
  - Lazy loading otimizado
  - Query optimization
  - Connection pooling

### Observabilidade
- [ ] **Spring Boot Actuator**
  - Métricas de performance
  - Health checks avançados
  - Audit trail

- [ ] **Logging Avançado**
  - Correlation IDs
  - Structured logging (JSON)
  - Log aggregation ready

### Infraestrutura
- [ ] **Database Migration**
  - Flyway para versionamento
  - Scripts de migração
  
- [ ] **API Documentation**
  - OpenAPI/Swagger
  - Documentação interativa

**Entrega estimada**: 3-4 semanas

---

## 🌟 Fase 4 - Recursos Premium (v0.5.0)

**Objetivo**: Funcionalidades diferenciadas

### Integrações Externas
- [ ] **Monitoramento de preços**
  - Integração com APIs de e-commerce
  - Alertas de mudança de preço
  - Histórico de preços

- [ ] **Notificações**
  - Email notifications
  - Push notifications (webhook)
  - Notificações de aniversário

### Recursos Sociais  
- [ ] **Seguir usuários**
  - Lista de seguidores
  - Feed de atividades
  
- [ ] **Comentários e reviews**
  - Comentários em itens
  - Reviews de produtos

### Mensageria
- [ ] **RabbitMQ**
  - Processamento assíncrono
  - Event-driven architecture
  - Dead letter queues

**Entrega estimada**: 5-6 semanas

---

## 🔮 Futuro (v1.0+)

### Mobile & Frontend
- [ ] API mobile-first
- [ ] GraphQL endpoint
- [ ] Real-time updates (WebSockets)

### Machine Learning
- [ ] Recomendações personalizadas
- [ ] Análise de tendências
- [ ] Previsão de preços

### Microserviços
- [ ] Separação em serviços independentes
- [ ] Service mesh
- [ ] Kubernetes deployment

---

## 🎨 Critérios de Qualidade

### Para cada release:
- ✅ **Testes**: Cobertura > 80%
- ✅ **Documentação**: README e docs atualizadas  
- ✅ **Arquitetura**: Princípios Clean Architecture mantidos
- ✅ **Performance**: Tempo de resposta < 200ms (95th percentile)
- ✅ **Segurança**: Vulnerabilidades conhecidas corrigidas

### Definição de Pronto (DoD):
1. Funcionalidade implementada e testada
2. Testes automatizados passando
3. Code review aprovado
4. Documentação atualizada
5. Deploy em ambiente de teste validado

---

## 📊 Métricas de Sucesso

### Técnicas
- Tempo de resposta da API
- Cobertura de testes  
- Número de bugs em produção
- Tempo de build/deploy

### Produto
- Número de usuários ativos
- Número de listas criadas
- Items adicionados por semana
- Taxa de retenção de usuários

---

## 🤝 Como Contribuir

### Sugestão de Novas Features
1. Criar issue com template de feature request
2. Discussão e validação da proposta
3. Aprovação para desenvolvimento
4. Implementação seguindo guidelines

### Priorização
As features são priorizadas baseadas em:
1. **Impacto no usuário** (alto/médio/baixo)
2. **Complexidade técnica** (simples/média/complexa)  
3. **Dependências** (bloqueante/não-bloqueante)
4. **Recursos disponíveis** (tempo/pessoas)

Este roadmap é um documento vivo e pode ser ajustado conforme necessidades do projeto e feedback dos usuários.
