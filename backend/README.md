# Backend API

A comprehensive Spring Boot backend application with JWT authentication, PostgreSQL database, and RESTful APIs.

## Features

- **Authentication & Authorization**: JWT-based authentication with role-based access control
- **User Management**: Complete user CRUD operations with role management
- **Product Management**: Product catalog with search, filtering, and inventory management
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security with JWT tokens
- **Documentation**: OpenAPI/Swagger documentation
- **Health Checks**: Built-in health monitoring endpoints
- **Error Handling**: Global exception handling with structured error responses
- **Validation**: Input validation with proper error messages
- **Docker Support**: Containerized deployment with Docker Compose

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **JWT (JSON Web Tokens)**
- **Maven**
- **Docker & Docker Compose**
- **OpenAPI/Swagger**

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for containerized setup)
- PostgreSQL (if running without Docker)

### Option 1: Docker Compose (Recommended)

1. **Clone and navigate to the backend directory**:
   ```bash
   cd backend
   ```

2. **Copy environment file**:
   ```bash
   cp .env.example .env
   ```

3. **Start all services**:
   ```bash
   docker-compose up -d
   ```

4. **Check service health**:
   ```bash
   docker-compose ps
   curl http://localhost:8080/api/health
   ```

### Option 2: Local Development

1. **Set up PostgreSQL database**:
   ```sql
   CREATE DATABASE backend_db;
   CREATE USER postgres WITH PASSWORD 'password';
   GRANT ALL PRIVILEGES ON DATABASE backend_db TO postgres;
   ```

2. **Configure environment variables**:
   ```bash
   export DATABASE_URL=jdbc:postgresql://localhost:5432/backend_db
   export DATABASE_USERNAME=postgres
   export DATABASE_PASSWORD=password
   export JWT_SECRET=mySecretKey
   ```

3. **Build and run the application**:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

## API Documentation

Once the application is running, you can access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/api/health
- **Application Info**: http://localhost:8080/api/info

## API Endpoints

### Authentication
- `POST /api/auth/signin` - User login
- `POST /api/auth/signup` - User registration

### User Management
- `GET /api/users` - Get all users (Admin/Moderator only)
- `GET /api/users/{id}` - Get user by ID (Admin/Moderator only)
- `PUT /api/users/{id}` - Update user (Admin only)
- `DELETE /api/users/{id}` - Delete user (Admin only)

### Product Management
- `GET /api/products` - Get all products (with pagination, search, filters)
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product (Admin/Moderator only)
- `PUT /api/products/{id}` - Update product (Admin/Moderator only)
- `DELETE /api/products/{id}` - Delete product (Admin only)
- `GET /api/products/categories` - Get all categories
- `GET /api/products/low-stock` - Get low stock products (Admin/Moderator only)
- `PATCH /api/products/{id}/stock` - Update product stock (Admin/Moderator only)

### Health & Monitoring
- `GET /api/health` - Application health check
- `GET /api/info` - Application information
- `GET /actuator/health` - Spring Boot Actuator health
- `GET /actuator/info` - Spring Boot Actuator info
- `GET /actuator/metrics` - Application metrics

## Authentication

The API uses JWT (JSON Web Tokens) for authentication. To access protected endpoints:

1. **Sign up or sign in** to get a JWT token
2. **Include the token** in the Authorization header:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

### Default Users

| Username | Password | Role  |
|----------|----------|-------|
| admin    | admin123 | ADMIN |
| user     | user123  | USER  |

## Sample API Requests

### 1. User Registration
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

### 2. User Login
```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 3. Get Products (with pagination)
```bash
curl -X GET "http://localhost:8080/api/products?page=0&size=10&sortBy=name&sortDir=asc" \
  -H "Authorization: Bearer <your-jwt-token>"
```

### 4. Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "name": "New Product",
    "description": "Product description",
    "price": 99.99,
    "quantity": 100,
    "category": "Electronics"
  }'
```

### 5. Search Products
```bash
curl -X GET "http://localhost:8080/api/products?search=laptop" \
  -H "Authorization: Bearer <your-jwt-token>"
```

## Testing

### Run Unit Tests
```bash
./mvnw test
```

### Run Integration Tests
```bash
./mvnw test -Dtest=**/*IntegrationTest
```

### Run All Tests with Coverage
```bash
./mvnw clean test jacoco:report
```

## Database Management

### Database Migrations

The application uses Flyway for database migrations. Migration files are located in `src/main/resources/db/migration/`.

### Manual Database Setup

If you need to set up the database manually:

```bash
# Connect to PostgreSQL
psql -U postgres -h localhost

# Run migration scripts
\i src/main/resources/db/migration/V1__Create_initial_tables.sql
\i src/main/resources/db/migration/V2__Insert_sample_data.sql
```

## Production Deployment

### Environment Variables

For production deployment, set these environment variables:

```bash
# Database
DATABASE_URL=jdbc:postgresql://your-db-host:5432/your-db-name
DATABASE_USERNAME=your-db-username
DATABASE_PASSWORD=your-db-password

# JWT
JWT_SECRET=your-strong-secret-key
JWT_EXPIRATION=86400000

# Security
REQUIRE_SSL=true
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com

# Logging
LOG_LEVEL=WARN
SECURITY_LOG_LEVEL=ERROR
```

### Docker Production Build

```bash
# Build production image
docker build -t backend-api:latest .

# Run with production environment
docker run -d \
  --name backend-api \
  -p 8080:8080 \
  --env-file .env.production \
  backend-api:latest
```

## Monitoring and Observability

### Health Checks

- **Application Health**: `GET /api/health`
- **Liveness Probe**: `GET /actuator/health/liveness`
- **Readiness Probe**: `GET /actuator/health/readiness`

### Metrics

- **Application Metrics**: `GET /actuator/metrics`
- **Custom Metrics**: Available through Micrometer integration

### Logging

The application uses structured logging with configurable levels:

- **Application logs**: Configurable via `LOG_LEVEL`
- **Security logs**: Configurable via `SECURITY_LOG_LEVEL`
- **Format**: JSON format for production, console format for development

## Security Considerations

### JWT Security
- Use strong, unique JWT secrets in production
- Configure appropriate token expiration times
- Implement token refresh mechanism for long-lived sessions

### Database Security
- Use strong database passwords
- Enable SSL connections in production
- Regularly update database and application dependencies

### API Security
- Enable HTTPS in production
- Configure CORS properly for your frontend domains
- Implement rate limiting for public endpoints
- Regular security audits and dependency updates

## Troubleshooting

### Common Issues

1. **Database Connection Issues**:
   ```bash
   # Check database connectivity
   docker-compose logs postgres
   curl http://localhost:8080/api/health
   ```

2. **Authentication Issues**:
   ```bash
   # Verify JWT token
   curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users
   ```

3. **Application Not Starting**:
   ```bash
   # Check application logs
   docker-compose logs backend
   ./mvnw spring-boot:run -X
   ```

### Log Locations

- **Docker logs**: `docker-compose logs [service-name]`
- **Application logs**: Console output or configured log files
- **Database logs**: PostgreSQL container logs

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Make changes and add tests
4. Run tests: `./mvnw test`
5. Commit changes: `git commit -am 'Add new feature'`
6. Push to branch: `git push origin feature/new-feature`
7. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the repository
- Contact: support@example.com
- Documentation: Check the Swagger UI for detailed API documentation