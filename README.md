# Would Like - Backend System

## Project Overview
A scalable Would Like API built with Java Spring Boot, implementing Clean Architecture and Domain-Driven Design principles. This is the first component of a multi-part system designed to demonstrate modern software engineering practices.

## Core Features
- **Would Like Management**: Create, manage, and organize personal wish lists
- **User Management**: User registration, authentication, and profiles
- **Product Integration**: Abstract product management with future e-commerce integration
- **Recommendation Engine**: AI-powered product suggestions (future feature)
- **Social Features**: Share and discover wish lists (future expansion)

## Technology Stack
- **Framework**: Java 17+ with Spring Boot 3.x
- **Architecture**: Clean Architecture + Domain-Driven Design (DDD)
- **Database**: MySQL with abstract repository pattern
- **Containerization**: Docker & Kubernetes
- **Message Queues**: RabbitMQ (primary), Kafka (future)
- **Testing**: JUnit 5, TestContainers, H2 in-memory DB
- **Build Tool**: Maven
- **Cloud**: TBD (Azure/AWS/GCP with free tier priority)

## Project Goals
1. **Learning & Portfolio**: Demonstrate modern Java backend development skills
2. **Scalability**: Design for horizontal scaling and microservices architecture
3. **Best Practices**: Implement SOLID principles, clean code, and comprehensive testing
4. **Future Expansion**: Foundation for web/mobile frontends and additional microservices
5. **Database Abstraction**: Easy database migration capability

## Quick Start
```bash
# Prerequisites: Java 17+, Docker, Maven

# Clone and setup
git clone <repository-url>
cd would-like-backend-app

# Build and run with Docker
docker-compose up --build

# Or run locally
mvn spring-boot:run
```

## Project Structure
```
src/
├── main/java/com/wouldlike/
│   ├── application/        # Application layer (use cases)
│   ├── domain/            # Domain layer (entities, value objects)
│   ├── infrastructure/    # Infrastructure layer (repositories, external services)
│   └── presentation/      # Presentation layer (controllers, DTOs)
└── test/                  # Test suites
```

## Documentation
- [Architecture Guide](./docs/ARCHITECTURE.md)
- [Development Setup](./docs/DEVELOPMENT.md)
- [Project Roadmap](./docs/PROJECT_ROADMAP.md)
- [API Documentation](./docs/API.md)
- [Deployment Guide](./docs/DEPLOYMENT.md)

## Current Status
🚧 **In Development** - Initial architecture and core features

## Contributing
This is a personal portfolio project, but suggestions and feedback are welcome!

## License
MIT License - See [LICENSE](./LICENSE) for details
