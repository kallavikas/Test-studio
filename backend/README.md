# Backend API

A production-ready Spring Boot REST API with JWT authentication, user management, and product catalog functionality.

## Features

- **Authentication & Authorization**: JWT-based authentication with role-based access control
- **User Management**: Complete CRUD operations for users with different roles (USER, ADMIN, MODERATOR)
- **Product Management**: Product catalog with search, filtering, and inventory management
- **Database**: PostgreSQL with Flyway migrations
- **Security**: BCrypt password hashing, CORS configuration, input validation
- **Documentation**: OpenAPI 3.0 specification with Swagger UI
- **Testing**: Unit and integration tests with high coverage
- **Monitoring**: Health checks, metrics, and application monitoring
- **Containerization**: Docker support with multi-stage builds
- **Caching**: Redis integration for performance optimization

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Security** (JWT authentication)
- **Spring Data JPA** (data access)
- **PostgreSQL** (primary database)
- **Flyway** (database migrations)
- **Redis** (caching)
- **Maven** (build tool)
- **Docker** (containerization)
- **JUnit 5** (testing)
- **Testcontainers** (integration testing)

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- PostgreSQL 15+ (if running locally)

### Running with Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd backend
   ```

2. **Start the application with Docker Compose**
   ```bash
   docker-compose up -d
   ```

   This will start:
   - PostgreSQL database on port 5432
   - Redis cache on port 6379
   - Spring Boot application on port 8080
   - pgAdmin (development) on port 5050

3. **Access the application**
   - API: http://localhost:8080/api/v1
   - Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
   - Health Check: http://localhost:8080/api/v1/actuator/health
   - pgAdmin: http://localhost:5050 (admin@example.com / admin)

### Running Locally

1. **Set up PostgreSQL**
   ```bash
   # Using Docker
   docker run --name postgres-db -e POSTGRES_DB=backend_db \
     -e POSTGRES_USER=backend_user -e POSTGRES_PASSWORD=backend_password \
     -p 5432:5432 -d postgres:15-alpine
   ```

2. **Set environment variables**
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=backend_db
   export DB_USERNAME=backend_user
   export DB_PASSWORD=backend_password
   export JWT_SECRET=mySecretKey123456789012345678901234567890
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

### Development Mode

For development with hot reload:

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

This enables:
- Hot reload with Spring Boot DevTools
- Debug port 5005
- Volume mounting for source code

## API Documentation

### Authentication Endpoints

- `POST /api/v1/auth/register` - Register a new user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh JWT token
- `POST /api/v1/auth/logout` - User logout
- `POST /api/v1/auth/change-password` - Change password
- `GET /api/v1/auth/validate` - Validate JWT token

### User Management Endpoints

- `GET /api/v1/users` - Get all users (Admin only)
- `POST /api/v1/users` - Create user (Admin only)
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user (Admin only)
- `GET /api/v1/users/search?q={term}` - Search users
- `GET /api/v1/users/check/username/{username}` - Check username availability
- `GET /api/v1/users/check/email/{email}` - Check email availability

### Product Management Endpoints

- `GET /api/v1/products` - Get all products (public)
- `POST /api/v1/products` - Create product (Admin/Moderator)
- `GET /api/v1/products/{id}` - Get product by ID (public)
- `PUT /api/v1/products/{id}` - Update product (Admin/Moderator)
- `DELETE /api/v1/products/{id}` - Delete product (Admin only)
- `GET /api/v1/products/search?q={term}` - Search products (public)
- `GET /api/v1/products/category/{category}` - Get products by category (public)
- `GET /api/v1/products/available` - Get available products (public)
- `GET /api/v1/products/featured` - Get featured products (public)

### Default Users

The application comes with default users:

| Username | Email | Password | Role |
|----------|-------|----------|---------|
| admin | admin@example.com | admin123 | ADMIN |
| user | user@example.com | user123 | USER |

### Sample API Calls

1. **Register a new user**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "newuser",
       "email": "newuser@example.com",
       "password": "password123",
       "firstName": "New",
       "lastName": "User"
     }'
   ```

2. **Login**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "usernameOrEmail": "admin",
       "password": "admin123"
     }'
   ```

3. **Get all products**
   ```bash
   curl -X GET "http://localhost:8080/api/v1/products?page=0&size=10"
   ```

4. **Create a product** (requires authentication)
   ```bash
   curl -X POST http://localhost:8080/api/v1/products \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -d '{
       "name": "New Product",
       "description": "A great new product",
       "price": 29.99,
       "category": "Electronics",
       "stockQuantity": 100,
       "sku": "PROD-001"
     }'
   ```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Categories
```bash
# Unit tests only
mvn test -Dtest="*Test"

# Integration tests only
mvn test -Dtest="*IntegrationTest"
```

### Test Coverage
```bash
mvn jacoco:report
# View report at target/site/jacoco/index.html
```

## Database

### Migrations

Database migrations are managed by Flyway and located in `src/main/resources/db/migration/`:

- `V1__Create_users_table.sql` - User and role tables
- `V2__Create_products_table.sql` - Product table with sample data

### Manual Migration
```bash
# Run migrations
mvn flyway:migrate

# Clean database (development only)
mvn flyway:clean
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | localhost |
| `DB_PORT` | Database port | 5432 |
| `DB_NAME` | Database name | backend_db |
| `DB_USERNAME` | Database username | backend_user |
| `DB_PASSWORD` | Database password | backend_password |
| `JWT_SECRET` | JWT signing secret | (required) |
| `JWT_EXPIRATION` | JWT expiration time (ms) | 86400000 |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | 604800000 |
| `SERVER_PORT` | Application port | 8080 |
| `CORS_ALLOWED_ORIGINS` | CORS allowed origins | http://localhost:3000 |
| `LOG_LEVEL` | Application log level | INFO |
| `SPRING_PROFILES_ACTIVE` | Active Spring profiles | development |

### Profiles

- `development` - Development configuration with debug logging
- `test` - Test configuration with H2 database
- `production` - Production configuration with optimized settings
- `docker` - Docker-specific configuration

## Monitoring

### Health Checks

- Application health: `GET /api/v1/actuator/health`
- Database health: Included in health endpoint
- Custom health indicators for external services

### Metrics

- Prometheus metrics: `GET /api/v1/actuator/prometheus`
- Application metrics: `GET /api/v1/actuator/metrics`
- Custom business metrics included

### Logging

Structured logging with:
- Request/response logging
- Security event logging
- Performance metrics
- Error tracking with correlation IDs

## Security

### Authentication
- JWT tokens with configurable expiration
- Refresh token rotation
- Password hashing with BCrypt
- Account lockout protection

### Authorization
- Role-based access control (RBAC)
- Method-level security annotations
- Resource-level permissions

### Input Validation
- Bean validation with custom validators
- SQL injection prevention
- XSS protection
- CSRF protection for state-changing operations

### CORS
- Configurable CORS policies
- Environment-specific origins
- Credential support

## Deployment

### Docker Production Build

```bash
# Build production image
docker build -t backend-api:latest .

# Run production container
docker run -d -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e DB_PASSWORD=your-db-password \
  -e JWT_SECRET=your-jwt-secret \
  backend-api:latest
```

### Kubernetes Deployment

Sample Kubernetes manifests are provided in the `k8s/` directory:

```bash
kubectl apply -f k8s/
```

### CI/CD

GitHub Actions workflow (`.github/workflows/ci.yml`) includes:
- Automated testing
- Code quality checks
- Security scanning
- Docker image building
- Deployment automation

## Performance

### Caching Strategy

- Redis for session storage
- Application-level caching for frequently accessed data
- Database query optimization
- Connection pooling with HikariCP

### Optimization

- Lazy loading for JPA entities
- Pagination for large datasets
- Database indexing strategy
- Async processing for heavy operations

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Check database is running
   docker-compose ps
   
   # Check logs
   docker-compose logs postgres
   ```

2. **JWT Token Invalid**
   - Check JWT secret configuration
   - Verify token hasn't expired
   - Check system clock synchronization

3. **Permission Denied**
   - Verify user roles and permissions
   - Check authentication headers
   - Review security configuration

### Debugging

1. **Enable Debug Logging**
   ```yaml
   logging:
     level:
       com.example.backend: DEBUG
       org.springframework.security: DEBUG
   ```

2. **Remote Debugging**
   ```bash
   # Development mode with debug port
   docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
   # Connect debugger to localhost:5005
   ```

### Performance Monitoring

1. **Application Metrics**
   - Monitor `/actuator/metrics` endpoint
   - Set up Prometheus + Grafana
   - Configure alerting rules

2. **Database Performance**
   - Monitor slow queries
   - Check connection pool metrics
   - Analyze query execution plans

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`
5. Submit a pull request

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc for public APIs
- Maintain test coverage above 80%

### Pre-commit Checks

```bash
# Run tests
mvn test

# Check code style
mvn checkstyle:check

# Security scan
mvn dependency-check:check
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:

- Create an issue in the repository
- Contact the development team
- Check the documentation and FAQ

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and release notes.
