# 🧪 FlickIt Backend - Development Guide

Complete development guide for the FlickIt Backend application, including setup, testing, code quality standards, and troubleshooting.

## 📋 Table of Contents

- [**Development Setup**](#development-setup)
- [**Project Structure**](#project-structure)
- [**Testing Strategy**](#testing-strategy)
- [**Code Quality**](#code-quality)
- [**Development Workflow**](#development-workflow)
- [**Troubleshooting**](#troubleshooting)
- [**Best Practices**](#best-practices)

---

## 🚀 Development Setup

### **Prerequisites**

- **Java**: 17+ (OpenJDK or Oracle JDK)
- **Maven**: 3.6+
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Git**: Latest version
- **Docker**: 20.10+ (for local development)
- **Docker Compose**: 2.0+

### **Initial Setup**

1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd flickit-backend
   ```

2. **Install Dependencies**
   ```bash
   mvn clean install
   ```

3. **Configure Environment**
   ```bash
   cp env.example .env
   # Edit .env with your local values
   ```

4. **Start Local Services**
   ```bash
   docker-compose up -d
   ```

5. **Run Application**
   ```bash
   mvn spring-boot:run
   ```

### **IDE Configuration**

#### **IntelliJ IDEA**
- Import as Maven project
- Set JDK 17 as project SDK
- Enable annotation processing
- Configure Spring Boot run configuration

#### **Eclipse**
- Import as Maven project
- Install Spring Tools Suite (STS) plugin
- Configure Spring Boot run configuration

#### **VS Code**
- Install Java Extension Pack
- Install Spring Boot Extension Pack
- Install Maven for Java extension

---

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/flickit/
│   │   ├── ai/           # AI service module
│   │   ├── auth/         # Authentication & security
│   │   ├── claim/        # Event claiming system
│   │   ├── config/       # Configuration classes
│   │   ├── event/        # Event management
│   │   ├── notification/ # Push notification service
│   │   ├── rating/       # Rating system
│   │   └── user/         # User management
│   └── resources/
│       ├── application.yaml
│       └── db/migration/
├── test/
│   ├── java/com/flickit/
│   │   ├── ai/
│   │   ├── auth/
│   │   ├── claim/
│   │   ├── event/
│   │   ├── notification/
│   │   ├── rating/
│   │   ├── security/
│   │   └── user/
│   └── resources/
│       └── application-test.yaml
```

### **Module Organization**

Each module follows the same structure:
- **Controller**: REST endpoints
- **Service**: Business logic
- **Repository**: Data access
- **DTO**: Data transfer objects
- **Model**: JPA entities
- **Test**: Unit and integration tests

---

## 🧪 Testing Strategy

### **Test Categories**

#### **Unit Tests**
- **Location**: `src/test/java/.../service/`
- **Purpose**: Test individual service methods
- **Framework**: JUnit 5 + Mockito
- **Coverage**: Business logic, validation, edge cases

**Example Unit Test:**
```java
@Test
void createEvent_shouldSaveAndReturnEventDto() {
    // given
    CreateEventRequest request = new CreateEventRequest();
    request.setTitleVendor("Test Event");
    request.setLat(50.0);
    request.setLon(20.0);
    
    // when
    EventDto result = eventService.createEvent(request, vendorId);
    
    // then
    assertThat(result.getTitle()).isEqualTo("Test Event");
    verify(eventRepository).save(any(EventEntity.class));
}
```

#### **Integration Tests**
- **Location**: `src/test/java/.../controller/`
- **Purpose**: Test complete request-response cycles
- **Framework**: Spring Boot Test + MockMvc
- **Coverage**: API contracts, security, data persistence

**Example Integration Test:**
```java
@Test
@WithMockUser(roles = "VENDOR")
void createEvent_shouldSucceedWithVendorRole() throws Exception {
    // given
    CreateEventRequest request = new CreateEventRequest();
    request.setTitleVendor("Test Event");
    
    // when & then
    mockMvc.perform(post("/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Test Event"));
}
```

#### **Security Tests**
- **Location**: `src/test/java/com/flickit/security/`
- **Purpose**: Test role-based access control
- **Framework**: Spring Security Test
- **Coverage**: Authorization rules, role validation

### **Test Configuration**

#### **Test Profiles**
```yaml
# application-test.yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  security:
    user:
      name: test
      password: test
```

#### **Test Database**
- **H2 In-Memory**: Fast, isolated test environment
- **Schema**: Auto-generated from entities
- **Data**: Test data inserted in `@BeforeEach`

### **Running Tests**

```bash
# All tests
mvn test

# Unit tests only
mvn test -Dtest="*Test"

# Integration tests only
mvn test -Dtest="*IT"

# Specific test class
mvn test -Dtest=UserServiceTest

# With coverage report
mvn test jacoco:report
```

---

## 📏 Code Quality

### **Coding Standards**

#### **Java Conventions**
- **Naming**: camelCase for methods/variables, PascalCase for classes
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Maximum 120 characters
- **Imports**: Organized and static imports for assertions

#### **Spring Boot Conventions**
- **Annotations**: Use specific annotations (`@RestController`, `@Service`)
- **Dependency Injection**: Constructor injection preferred
- **Exception Handling**: Use `@ControllerAdvice` for global handling
- **Validation**: Use Bean Validation annotations

#### **Database Conventions**
- **Naming**: snake_case for table/column names
- **Indexes**: Named indexes for performance
- **Constraints**: Explicit constraint names
- **Migrations**: Versioned schema changes

### **Code Review Checklist**

- [ ] **Functionality**: Does the code do what it's supposed to?
- [ ] **Testing**: Are there appropriate tests?
- [ ] **Documentation**: Is the code self-documenting?
- [ ] **Performance**: Are there obvious performance issues?
- [ ] **Security**: Are there security vulnerabilities?
- [ ] **Maintainability**: Is the code easy to understand and modify?

### **Static Analysis**

#### **Maven Plugins**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.2.1</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
    </configuration>
</plugin>

<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
</plugin>
```

#### **IDE Integration**
- **IntelliJ IDEA**: Built-in inspections
- **Eclipse**: Checkstyle plugin
- **VS Code**: SonarLint extension

---

## 🔄 Development Workflow

### **Feature Development**

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/user-rating-system
   ```

2. **Implement Feature**
   - Write tests first (TDD approach)
   - Implement business logic
   - Add API endpoints
   - Update documentation

3. **Test Locally**
   ```bash
   mvn test
   mvn spring-boot:run
   ```

4. **Commit Changes**
   ```bash
   git add .
   git commit -m "feat: implement user rating system"
   ```

5. **Push and Create PR**
   ```bash
   git push origin feature/user-rating-system
   # Create Pull Request on GitHub
   ```

### **Code Review Process**

1. **Automated Checks**
   - Build passes
   - Tests pass
   - Code coverage maintained
   - Static analysis clean

2. **Peer Review**
   - At least one approval required
   - Address all review comments
   - Update documentation if needed

3. **Merge to Main**
   - Squash commits
   - Delete feature branch
   - Update release notes

### **Release Process**

1. **Version Bump**
   ```bash
   mvn versions:set -DnewVersion=1.1.0
   ```

2. **Create Release Branch**
   ```bash
   git checkout -b release/1.1.0
   ```

3. **Final Testing**
   - Run full test suite
   - Integration testing
   - Performance testing

4. **Tag Release**
   ```bash
   git tag -a v1.1.0 -m "Release version 1.1.0"
   git push origin v1.1.0
   ```

---

## 🔧 Troubleshooting

### **Common Issues**

#### **Build Failures**

**Problem**: Maven build fails with dependency issues
```bash
# Solution: Clean and reinstall
mvn clean install -U
```

**Problem**: Java version mismatch
```bash
# Check Java version
java -version

# Set JAVA_HOME if needed
export JAVA_HOME=/path/to/java17
```

#### **Test Failures**

**Problem**: Tests fail with database connection issues
```bash
# Solution: Check H2 database configuration
# Ensure application-test.yaml is loaded
```

**Problem**: Security tests fail with authentication issues
```bash
# Solution: Check @WithMockUser annotations
# Verify Spring Security test dependencies
```

#### **Runtime Issues**

**Problem**: Application fails to start
```bash
# Check logs
tail -f logs/application.log

# Verify environment variables
echo $JWT_SECRET
echo $DATABASE_URL
```

**Problem**: Database connection fails
```bash
# Check PostgreSQL status
docker-compose ps

# Restart database
docker-compose restart db
```

### **Debugging Tips**

#### **Enable Debug Logging**
```yaml
# application.yaml
logging:
  level:
    com.flickit: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

#### **Use IDE Debugger**
- Set breakpoints in service methods
- Inspect variables and state
- Step through code execution

#### **Database Inspection**
```bash
# Connect to PostgreSQL
docker-compose exec db psql -U postgres -d flickit

# List tables
\dt

# Check data
SELECT * FROM users LIMIT 5;
```

---

## 📚 Best Practices

### **General Development**

1. **Write Self-Documenting Code**
   - Use descriptive names
   - Keep methods small and focused
   - Add comments for complex logic

2. **Follow SOLID Principles**
   - Single Responsibility
   - Open/Closed
   - Liskov Substitution
   - Interface Segregation
   - Dependency Inversion

3. **Use Design Patterns Appropriately**
   - Repository Pattern for data access
   - Service Layer for business logic
   - DTO Pattern for data transfer
   - Factory Pattern for object creation

### **Spring Boot Specific**

1. **Configuration Management**
   - Use profiles for different environments
   - Externalize sensitive configuration
   - Use `@ConfigurationProperties` for complex config

2. **Exception Handling**
   - Use `@ControllerAdvice` for global handling
   - Return consistent error responses
   - Log errors appropriately

3. **Security Best Practices**
   - Use `@PreAuthorize` for method-level security
   - Validate input data
   - Use HTTPS in production
   - Implement rate limiting

### **Testing Best Practices**

1. **Test Structure**
   - Arrange-Act-Assert pattern
   - Use descriptive test names
   - Test both happy path and edge cases

2. **Mocking Strategy**
   - Mock external dependencies
   - Use `@MockBean` for Spring context
   - Verify important interactions

3. **Test Data Management**
   - Use `@BeforeEach` for setup
   - Clean up test data
   - Use test-specific configuration

---

## 🆘 Getting Help

### **Internal Resources**
- **Code Review**: Ask team members for feedback
- **Documentation**: Check this guide and API reference
- **Architecture**: Review [architecture diagrams](./architecture-diagrams.md)

### **External Resources**
- **Spring Boot**: [Official Documentation](https://spring.io/projects/spring-boot)
- **JUnit 5**: [User Guide](https://junit.org/junit5/docs/current/user-guide/)
- **Mockito**: [Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

### **Team Support**
- **Slack**: #flickit-backend channel
- **Email**: backend-team@flickit.com
- **Office Hours**: Daily 10:00-11:00 AM

---

**🧪 FlickIt Backend Development** - Complete guide for developers and contributors.

*Last updated: August 19, 2025*
