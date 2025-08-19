# FlickIt Backend

A Spring Boot backend application for the FlickIt mobile app, providing event management, user authentication, AI-powered content generation, and real-time notifications.

## 🚀 Quick Start Guide

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose (for local development)

### Local Development
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd flickit-backend
   ```

2. **Run tests**
   ```bash
   mvn test
   ```

3. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

### Docker Setup
1. **Copy environment variables**
   ```bash
   cp env.example .env
   # Edit .env with your values
   ```

2. **Start services**
   ```bash
   docker-compose up -d
   ```

3. **Access services**
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - PostgreSQL: localhost:5432
   - PgAdmin: http://localhost:5050

## 📚 Documentation

- [Complete Documentation](docs/) - All project documentation
- [API Reference](api-reference.md) - Comprehensive API documentation
- [Development Guide](development-guide.md) - Setup and development workflow
- [Architecture Diagrams](architecture-diagrams.md) - System design and flows
- [E2E Test Scenarios](e2e-test-scenarios.md) - Comprehensive end-to-end test scenarios
- [Postman Collection](postman/README.md) - API testing collection

## 🏗️ Architecture

### Core Modules
- **User Management**: Registration, authentication, role-based access
- **Event Management**: CRUD operations, AI-powered content generation
- **Claim System**: Event claiming and management
- **Rating System**: Event and vendor rating functionality
- **Notification Service**: Real-time push notifications via FCM
- **AI Service**: OpenAI integration for content generation

### Technology Stack
- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Database**: PostgreSQL (production), H2 (testing)
- **Security**: Spring Security + JWT
- **Documentation**: OpenAPI 3 + Swagger UI
- **Testing**: JUnit 5 + Mockito
- **Containerization**: Docker + Docker Compose

## 🧪 Testing

### Test Categories
- **Unit Tests**: Service and repository layer testing
- **Integration Tests**: Controller and security testing
- **Test Configuration**: H2 in-memory database, mocked external services

### Running Tests
```bash
# All tests
mvn test

# Unit tests only
mvn test -Dtest="*Test"

# Integration tests only
mvn test -Dtest="*IT"

# Specific test class
mvn test -Dtest=UserServiceTest
```

## 🔧 Available Commands

Use the included `Makefile` for common development tasks:

```bash
# Build and test
make build          # Compile and test
make test           # Run tests
make clean          # Clean build artifacts

# Docker operations
make docker-build   # Build Docker image
make docker-run     # Start services
make docker-stop    # Stop services
make docker-logs    # View service logs

# Development
make run            # Start application
make format         # Format code
make docs           # Generate documentation
make status         # Show project status
```

## 📊 Project Status

### ✅ Completed Features
- User authentication and authorization
- Event management with AI content generation
- Claim and rating systems
- Notification service with FCM integration
- Comprehensive API documentation
- Docker containerization
- CI/CD pipeline with GitHub Actions
- Unit and integration test coverage

### 🔄 In Progress
- Role authorization testing
- OpenAPI enhancement with examples
- Performance optimization

### 📋 Planned Features
- Advanced search and filtering
- Analytics and reporting
- Multi-language support
- Advanced AI features

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For questions and support:
- Check the [documentation](docs/)
- Review [architecture diagrams](docs/architecture-diagrams.md)
- Open an issue for bugs or feature requests
- Contact the development team

---

**FlickIt Backend** - Powering the future of event discovery and management.
