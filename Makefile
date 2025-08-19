.PHONY: help build test clean docker-build docker-run docker-stop docker-logs

# Default target
help:
	@echo "Available commands:"
	@echo "  build         - Build the application"
	@echo "  test          - Run tests"
	@echo "  clean         - Clean build artifacts"
	@echo "  docker-build  - Build Docker image"
	@echo "  docker-run    - Start services with Docker Compose"
	@echo "  docker-stop   - Stop Docker Compose services"
	@echo "  docker-logs   - Show Docker Compose logs"
	@echo "  run           - Run application locally"
	@echo "  format        - Format code with Maven"

# Build the application
build:
	mvn clean compile

# Run tests
test:
	mvn test

# Clean build artifacts
clean:
	mvn clean

# Build Docker image
docker-build:
	docker build -t flickit-backend .

# Start services with Docker Compose
docker-run:
	docker-compose up -d

# Stop Docker Compose services
docker-stop:
	docker-compose down

# Show Docker Compose logs
docker-logs:
	docker-compose logs -f

# Run application locally
run:
	mvn spring-boot:run

# Format code with Maven
format:
	mvn spring-javaformat:apply

# Package application
package:
	mvn clean package -DskipTests

# Install dependencies
install:
	mvn clean install -DskipTests

# Run integration tests
test-integration:
	mvn test -Dtest="*IT"

# Run unit tests only
test-unit:
	mvn test -Dtest="*Test" -DfailIfNoTests=false

# Check code quality
check:
	mvn checkstyle:check
	mvn spotbugs:check

# Generate OpenAPI documentation
docs:
	mvn springdoc-openapi:generate

# Show application status
status:
	@echo "=== Application Status ==="
	@echo "Java version:"
	@java -version
	@echo ""
	@echo "Maven version:"
	@mvn -version
	@echo ""
	@echo "Docker version:"
	@docker --version
	@echo ""
	@echo "Docker Compose version:"
	@docker-compose --version
