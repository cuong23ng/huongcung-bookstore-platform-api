# Spring Security JWT Setup - Huong Cung Bookstore

## Overview
This document describes the Spring Security JWT authentication setup for the Huong Cung Bookstore platform.

## What's Been Implemented

### 1. Database Infrastructure
- **Docker Compose**: MySQL 8.0 container configuration
- **Database**: `huongcung_bookstore` with automatic schema creation
- **Connection**: Configured for Docker container access

### 2. Security Components
- **JWT Token Provider**: Token generation, validation, and parsing
- **JWT Authentication Filter**: Request interception and authentication
- **Custom User Details Service**: Spring Security integration
- **Security Configuration**: Role-based access control

### 3. Authentication API
- **Login Endpoint**: `POST /api/auth/login`
- **Register Endpoint**: `POST /api/auth/register`
- **Health Check**: `GET /api/auth/health`

### 4. Role-Based Access Control
- **ROLE_CUSTOMER**: Customer access
- **ROLE_ADMIN**: Full system access
- **ROLE_STORE_MANAGER**: Store management access
- **ROLE_SUPPORT_AGENT**: Customer support access

## Getting Started

### 1. Start the Database
```bash
# Navigate to Platform directory
cd Platform

# Start MySQL 8.0 container
docker-compose up -d

# Verify container is running
docker ps
```

### 2. Run the Application
```bash
# Start Spring Boot application
./mvnw spring-boot:run

# Or on Windows
mvnw.cmd spring-boot:run
```

### 3. Test the Setup

#### Register a New Customer
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Nguyen",
    "lastName": "Van A",
    "email": "customer@example.com",
    "password": "password123",
    "phone": "0123456789",
    "gender": "Male"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@example.com",
    "password": "password123"
  }'
```

#### Use JWT Token for Protected Endpoints
```bash
# Replace YOUR_JWT_TOKEN with the token from login response
curl -X GET http://localhost:8080/api/auth/health \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## API Endpoints

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - Customer registration
- `GET /api/auth/health` - Service health check

### Protected Endpoints (require JWT token)
- `/api/admin/**` - Admin only
- `/api/store-manager/**` - Store managers and admins
- `/api/support/**` - Support agents and admins
- `/api/customer/**` - Customers only
- `/api/books/**` - All users
- `/api/orders/**` - All authenticated users

## Configuration

### JWT Settings (application.properties)
```properties
jwt.secret=mySecretKey123456789012345678901234567890
jwt.expiration=86400000  # 24 hours
```

### Database Settings
You can configure via environment variables (recommended) to match `application.yml`:

```env
JDBC_DATABASE_HOST=localhost
JDBC_DATABASE_PORT=3306
JDBC_DATABASE_NAME=huongcungbookstore
JDBC_DATABASE_USERNAME=root
JDBC_DATABASE_PASSWORD=hungcuong
```

Or set them directly in `application.yml`/`application.properties` using the same values.

## Security Features

### Password Security
- BCrypt password encoding
- Minimum 6 character password requirement
- Password validation on registration

### JWT Security
- HMAC SHA-512 signing algorithm
- 24-hour token expiration
- Stateless authentication
- Role-based authorization

### Input Validation
- Email format validation
- Phone number validation (10-11 digits)
- Required field validation
- Global exception handling

## Next Steps

1. **Email Verification**: Implement email verification for new registrations
2. **Password Reset**: Add password reset functionality
3. **Token Refresh**: Implement token refresh mechanism
4. **Rate Limiting**: Add rate limiting for authentication endpoints
5. **Audit Logging**: Add security event logging

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure Docker container is running: `docker ps`
   - Check container logs: `docker logs huongcung-mysql`

2. **JWT Token Invalid**
   - Verify token is included in Authorization header
   - Check token expiration (24 hours)
   - Ensure token format: `Bearer <token>`

3. **Access Denied**
   - Verify user has correct role for endpoint
   - Check if user account is active
   - Ensure email is verified (currently disabled)

### Logs
Application logs are configured for DEBUG level. Check console output for detailed information about authentication and authorization processes.

## Security Considerations

- JWT secret should be changed in production
- Consider implementing token blacklisting for logout
- Add rate limiting to prevent brute force attacks
- Implement proper CORS configuration for production
- Use HTTPS in production environment
