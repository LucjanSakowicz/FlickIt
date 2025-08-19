# 🐳 FlickIt Backend - Deployment Guide

Complete deployment and operations guide for the FlickIt Backend application, including Docker setup, CI/CD pipeline, environment configuration, and monitoring.

## 📋 Table of Contents

- [**Docker Setup**](#docker-setup)
- [**CI/CD Pipeline**](#cicd-pipeline)
- [**Environment Configuration**](#environment-configuration)
- [**Production Deployment**](#production-deployment)
- [**Monitoring & Logging**](#monitoring--logging)
- [**Scaling & Performance**](#scaling--performance)
- [**Security & Compliance**](#security--compliance)

---

## 🐳 Docker Setup

### **Local Development Environment**

#### **Docker Compose Services**

```yaml
# docker-compose.yml
version: '3.8'

services:
  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: flickit
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  pgadmin:
    image: dpage/pgadmin4:latest
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@local
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "5050:80"
    depends_on:
      - db

  backend:
    build: .
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/flickit
    ports:
      - "8080:8080"
    depends_on:
      - db

volumes:
  postgres_data:
```

#### **Dockerfile**

```dockerfile
# Multi-stage build for optimized image
FROM maven:3.9.4-openjdk-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-jre-slim

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **Running Local Services**

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f backend

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up -d --build
```

---

## 🔄 CI/CD Pipeline

### **GitHub Actions Workflow**

#### **Workflow Structure**

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      - name: Run tests
        run: mvn test
      - name: Build application
        run: mvn clean package -DskipTests
      - name: Upload test results
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: target/surefire-reports/

  build-docker:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      - name: Login to Container Registry
        uses: docker/login-action@v2
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v4
        with:
          images: ghcr.io/${{ github.repository }}
      - name: Build and push Docker image
        uses: docker/build-push-action@v4
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}

  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'sarif'
          output: 'trivy-results.sarif'
      - name: Upload Trivy scan results
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'
```

#### **Pipeline Stages**

1. **Test Stage**
   - Code checkout
   - Java 17 setup
   - Maven dependency caching
   - Unit and integration tests
   - Test result upload

2. **Build Stage**
   - Docker image building
   - Container registry login
   - Image tagging and pushing
   - Security scanning

3. **Deploy Stage** (Production)
   - Environment validation
   - Database migration
   - Application deployment
   - Health checks

### **Automated Testing**

#### **Test Execution**

```bash
# Local testing
mvn test

# Integration testing
mvn test -Dtest="*IT"

# Performance testing
mvn test -Dtest="*PerformanceTest"

# Security testing
mvn test -Dtest="*SecurityTest"
```

#### **Test Coverage Requirements**

- **Unit Tests**: Minimum 80% coverage
- **Integration Tests**: All API endpoints covered
- **Security Tests**: Role-based access control
- **Performance Tests**: Response time < 2 seconds

---

## ⚙️ Environment Configuration

### **Configuration Profiles**

#### **Development Profile**

```yaml
# application-dev.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/flickit_dev
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  security:
    jwt:
      secret: dev-secret-key-change-in-production
      expiration-ms: 86400000

logging:
  level:
    com.flickit: DEBUG
    org.springframework.security: DEBUG
```

#### **Production Profile**

```yaml
# application-prod.yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration-ms: ${JWT_EXPIRATION_MS}

logging:
  level:
    com.flickit: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 100MB
    max-history: 30
```

### **Environment Variables**

#### **Required Variables**

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/flickit
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=secure_password

# JWT Security
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRATION_MS=86400000

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# External Services
OPENAI_API_KEY=your-openai-api-key
FCM_SERVER_KEY=your-firebase-server-key
```

#### **Optional Variables**

```bash
# Performance
JVM_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC

# Monitoring
ACTUATOR_ENABLED=true
METRICS_ENABLED=true

# Security
CORS_ALLOWED_ORIGINS=https://app.flickit.com
RATE_LIMIT_ENABLED=true
```

---

## 🚀 Production Deployment

### **Deployment Strategies**

#### **Blue-Green Deployment**

1. **Blue Environment** (Current Production)
   - Running stable version
   - Handles all traffic

2. **Green Environment** (New Version)
   - Deploy new version
   - Run health checks
   - Switch traffic when ready

3. **Rollback Plan**
   - Keep blue environment running
   - Switch back if issues occur

#### **Canary Deployment**

1. **Initial Rollout**
   - Deploy to 5% of users
   - Monitor metrics and errors

2. **Gradual Increase**
   - Increase to 25%, 50%, 100%
   - Stop if issues detected

3. **Full Deployment**
   - Complete rollout when stable

### **Infrastructure Requirements**

#### **Minimum Specifications**

- **CPU**: 2 cores
- **Memory**: 4GB RAM
- **Storage**: 20GB SSD
- **Network**: 100 Mbps

#### **Recommended Specifications**

- **CPU**: 4+ cores
- **Memory**: 8GB+ RAM
- **Storage**: 50GB+ SSD
- **Network**: 1 Gbps

#### **Scaling Configuration**

```yaml
# docker-compose.prod.yml
services:
  backend:
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '2.0'
          memory: 4G
        reservations:
          cpus: '1.0'
          memory: 2G
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
```

### **Database Migration**

#### **Migration Strategy**

1. **Schema Changes**
   - Use Flyway or Liquibase
   - Version control all changes
   - Test migrations in staging

2. **Data Migration**
   - Backup before changes
   - Run during maintenance window
   - Validate data integrity

3. **Rollback Plan**
   - Keep migration scripts
   - Test rollback procedures
   - Document rollback steps

---

## 📊 Monitoring & Logging

### **Application Monitoring**

#### **Health Checks**

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Check database connectivity
            // Check external services
            // Check system resources
            
            return Health.up()
                .withDetail("database", "connected")
                .withDetail("ai-service", "healthy")
                .withDetail("memory", Runtime.getRuntime().freeMemory())
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

#### **Metrics Collection**

```yaml
# application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info,prometheus
  endpoint:
    health:
      show-details: when-authorized
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

### **Logging Configuration**

#### **Log Levels**

```yaml
logging:
  level:
    root: INFO
    com.flickit: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### **Log Format**

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 100MB
    max-history: 30
```

### **Alerting & Notifications**

#### **Critical Alerts**

- **Application Down**: Immediate notification
- **High Error Rate**: 5-minute notification
- **Database Issues**: 10-minute notification
- **Performance Degradation**: 15-minute notification

#### **Alert Channels**

- **Slack**: #flickit-alerts channel
- **Email**: oncall@flickit.com
- **SMS**: Critical issues only
- **PagerDuty**: Escalation for urgent issues

---

## 📈 Scaling & Performance

### **Horizontal Scaling**

#### **Load Balancer Configuration**

```nginx
# nginx.conf
upstream flickit_backend {
    least_conn;
    server backend1:8080;
    server backend2:8080;
    server backend3:8080;
}

server {
    listen 80;
    server_name api.flickit.com;
    
    location / {
        proxy_pass http://flickit_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

#### **Database Scaling**

1. **Read Replicas**
   - Primary for writes
   - Replicas for reads
   - Automatic failover

2. **Connection Pooling**
   - HikariCP configuration
   - Monitor connection usage
   - Adjust pool size based on load

### **Performance Optimization**

#### **Caching Strategy**

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "events", "users", "ratings"
        );
    }
}
```

#### **Database Optimization**

1. **Indexes**
   ```sql
   CREATE INDEX idx_events_location ON events USING GIST (ST_MakePoint(lat, lon));
   CREATE INDEX idx_events_expires ON events (expires_at);
   CREATE INDEX idx_ratings_event ON ratings (event_id);
   ```

2. **Query Optimization**
   - Use pagination
   - Limit result sets
   - Optimize JOIN operations

#### **JVM Tuning**

```bash
# Production JVM options
JVM_OPTS="-Xmx4g -Xms2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+UseCompressedOops \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/heapdump.hprof"
```

---

## 🔒 Security & Compliance

### **Security Measures**

#### **Authentication & Authorization**

1. **JWT Security**
   - Strong secret keys
   - Short expiration times
   - Refresh token rotation

2. **Role-Based Access Control**
   - Principle of least privilege
   - Regular access reviews
   - Audit logging

#### **Data Protection**

1. **Encryption**
   - TLS 1.3 for transport
   - Database encryption at rest
   - Sensitive data encryption

2. **Input Validation**
   - SQL injection prevention
   - XSS protection
   - Rate limiting

### **Compliance Requirements**

#### **GDPR Compliance**

1. **Data Processing**
   - Lawful basis for processing
   - Data minimization
   - Purpose limitation

2. **User Rights**
   - Right to access
   - Right to deletion
   - Right to portability

#### **Security Standards**

1. **OWASP Top 10**
   - Regular security audits
   - Vulnerability scanning
   - Penetration testing

2. **ISO 27001**
   - Information security management
   - Risk assessment
   - Security controls

---

## 🆘 Emergency Procedures

### **Incident Response**

#### **Severity Levels**

1. **Critical (P0)**
   - Application completely down
   - Data loss or corruption
   - Security breach

2. **High (P1)**
   - Major functionality broken
   - Performance severely degraded
   - Database connectivity issues

3. **Medium (P2)**
   - Minor functionality issues
   - Performance degradation
   - Non-critical errors

#### **Response Procedures**

1. **Immediate Response**
   - Assess impact
   - Notify stakeholders
   - Implement workarounds

2. **Investigation**
   - Collect logs and metrics
   - Analyze root cause
   - Develop fix

3. **Resolution**
   - Deploy fix
   - Verify resolution
   - Monitor stability

### **Rollback Procedures**

#### **Application Rollback**

```bash
# Rollback to previous version
docker tag ghcr.io/flickit/backend:previous ghcr.io/flickit/backend:latest
docker-compose up -d backend

# Verify rollback
curl -f http://localhost:8080/actuator/health
```

#### **Database Rollback**

```bash
# Restore from backup
pg_restore -h localhost -U postgres -d flickit backup.dump

# Verify data integrity
psql -h localhost -U postgres -d flickit -c "SELECT COUNT(*) FROM users;"
```

---

## 📚 Additional Resources

### **Documentation**
- [API Reference](./api-reference.md)
- [Architecture Diagrams](./architecture-diagrams.md)
- [Development Guide](./development-guide.md)

### **Tools & Services**
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Alerting**: PagerDuty, Slack
- **CI/CD**: GitHub Actions, Docker Hub

### **Support Contacts**
- **DevOps Team**: devops@flickit.com
- **On-Call Engineer**: oncall@flickit.com
- **Emergency**: +1-555-0123

---

**🐳 FlickIt Backend Deployment** - Complete guide for deployment and operations.

*Last updated: August 19, 2025*
