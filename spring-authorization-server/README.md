# Spring Authorization Server

A production-ready OAuth2/OIDC Authorization Server built with Spring Boot 4.0 and Spring Security OAuth2 Authorization Server.

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 4.0.0 |
| Spring Security OAuth2 Authorization Server | Latest |
| PostgreSQL | 15+ |
| Gradle | 9.x |
| Lombok | Latest |
| Thymeleaf | Latest |

## Features

### Authentication & Authorization
- OAuth2 Authorization Server with OIDC support
- Authorization Code Grant (with and without PKCE)
- Client Credentials Grant
- JWT Access Tokens with custom claims
- Refresh Token support
- User consent page

### Security Features
- **Email Verification** - Users must verify email before login
- **Account Lockout** - 5 failed attempts = 15 minute lockout
- **Password Strength Validation** - Min 8 chars, uppercase, lowercase, digit, special char
- **CSRF Protection** - Cookie-based CSRF tokens for SPA clients
- **BCrypt Password Encoding** - Strength factor 12

### User Management
- User registration with email verification
- Role-based access control (RBAC)
- Fine-grained authorities/permissions
- Custom user claims in JWT tokens

### API Features
- Standardized API responses with `BaseResponse<T>`
- Global exception handling
- Request validation with detailed error messages
- Pagination support with metadata

## Project Structure

```
src/main/java/auth/res_server/demo/
├── config/                     # Configuration classes
│   ├── AsyncConfig.java
│   ├── CustomUserDetails.java
│   └── PasswordConfig.java
├── controller/                 # REST & MVC Controllers
│   ├── AuthController.java          # Login/Consent pages
│   ├── AuthorityController.java     # Authority management
│   ├── ClientController.java        # OAuth2 client management
│   ├── EmailVerificationController.java
│   ├── RoleController.java          # Role management
│   └── UserController.java          # User registration
├── domain/                     # JPA Entities
│   ├── Authority.java
│   ├── Authorization.java
│   ├── AuthorizationConsent.java
│   ├── Client.java
│   ├── EmailVerificationToken.java
│   ├── Role.java
│   └── User.java
├── dto/                        # Data Transfer Objects
│   ├── BaseResponse.java            # Standardized API response
│   ├── authority/
│   ├── client/
│   ├── email/
│   ├── role/
│   └── user/
├── exception/                  # Exception Handling
│   └── GlobalExceptionHandler.java
├── init/                       # Data Initialization
│   └── ClientDataInitializer.java
├── repository/                 # Spring Data Repositories
├── security/                   # Security Configuration
│   ├── AuthenticationEventListener.java
│   └── AuthorizationServerConfig.java
└── service/                    # Business Logic
    ├── impl/
    │   ├── AuthorityServiceImpl.java
    │   ├── CustomUserDetailsService.java
    │   ├── EmailServiceImpl.java
    │   ├── EmailVerificationServiceImpl.java
    │   ├── LoginAttemptServiceImpl.java
    │   ├── RoleServiceImpl.java
    │   └── UserServiceImpl.java
    ├── AuthorityService.java
    ├── ClientService.java
    ├── EmailService.java
    ├── EmailVerificationService.java
    ├── LoginAttemptService.java
    ├── RoleService.java
    └── UserService.java
```

## API Endpoints

### User Management
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/users` | Register new user | Public |

### Email Verification
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/auth/verify-email?token=` | Verify email | Public |
| POST | `/api/v1/auth/verify-email` | Verify email (JSON body) | Public |
| POST | `/api/v1/auth/resend-verification` | Resend verification email | Public |

### Role Management (Admin Only)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/roles` | Create role | `ROLE_ADMIN` |
| GET | `/api/v1/roles` | List all roles | `ROLE_ADMIN` |
| GET | `/api/v1/roles/{name}` | Get role by name | `ROLE_ADMIN` |
| GET | `/api/v1/roles/{id}/authorities` | Get role authorities | `ROLE_ADMIN` |
| POST | `/api/v1/roles/{id}/authorities` | Assign authorities to role | `ROLE_ADMIN` |
| DELETE | `/api/v1/roles/{id}/authorities/{authorityId}` | Remove authority from role | `ROLE_ADMIN` |

### Authority Management (Admin Only)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/authorities` | Create authority | `ROLE_ADMIN` |
| GET | `/api/v1/authorities` | List all authorities | `ROLE_ADMIN` |
| GET | `/api/v1/authorities/{name}` | Get authority by name | `ROLE_ADMIN` |
| DELETE | `/api/v1/authorities/{id}` | Delete authority | `ROLE_ADMIN` |

### OAuth2 Client Management (Admin Only)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/clients/pkce` | Create PKCE client | `ROLE_ADMIN` |
| POST | `/api/v1/clients/normal-auth` | Create Authorization Code client | `ROLE_ADMIN` |
| POST | `/api/v1/clients/client-credentials` | Create Client Credentials client | `ROLE_ADMIN` |

### OAuth2 Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/oauth2/authorize` | Authorization endpoint |
| POST | `/oauth2/token` | Token endpoint |
| GET | `/oauth2/jwks` | JWK Set endpoint |
| POST | `/oauth2/introspect` | Token introspection |
| POST | `/oauth2/revoke` | Token revocation |
| GET | `/userinfo` | OIDC UserInfo endpoint |
| GET | `/.well-known/openid-configuration` | OIDC Discovery |

> Admin endpoints require authentication with `ROLE_ADMIN`. Unauthenticated requests will receive a 401 Unauthorized response, and users without admin role will receive a 403 Forbidden response.

## API Response Format

### Success Response
```json
{
  "success": true,
  "status": 200,
  "message": "Success",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/users"
}
```

### Success with Pagination
```json
{
  "success": true,
  "status": 200,
  "message": "Success",
  "data": [ ... ],
  "meta": {
    "page": 0,
    "size": 10,
    "totalElements": 50,
    "totalPages": 5,
    "first": true,
    "last": false
  },
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/users"
}
```

### Error Response
```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "password": "Password must contain at least one uppercase letter...",
    "email": "Invalid email format"
  },
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/users"
}
```

## JWT Token Claims

Access tokens include the following custom claims:

| Claim | Description |
|-------|-------------|
| `sub` | User UUID |
| `email` | User email address |
| `email_verified` | Email verification status |
| `name` | Full name |
| `given_name` | First name |
| `family_name` | Last name |
| `phone_number` | Phone number |
| `gender` | Gender |
| `birthdate` | Date of birth |
| `picture` | Profile picture URL |
| `roles` | User roles (e.g., `ROLE_USER`, `ROLE_ADMIN`) |
| `permissions` | Fine-grained permissions |
| `scope` | Authorized scopes |

## Getting Started

### Prerequisites
- Java 21+
- PostgreSQL 15+
- Gradle 9+

### Database Setup

```bash
# Using Docker
docker run -d \
  --name auth-postgres \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=auth_db \
  -p 5555:5432 \
  postgres:15

# Or use docker-compose
docker-compose up -d
```

### Environment Variables

Create a `.env` file in the project root:

```env
# Database
DB_HOST=localhost
DB_PORT=5555
DB_NAME=auth_db
DB_USERNAME=admin
DB_PASSWORD=your_secure_password

# Mail (Gmail SMTP)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASS=your_app_password

# Application
APP_BASE_URL=http://localhost:9000
```

### Build & Run

```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Or run the JAR
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
```

The server will start at `http://localhost:9000`

## Security Configuration

### Password Requirements
- Minimum 8 characters
- Maximum 128 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one digit (0-9)
- At least one special character (@$!%*?&)

### Account Lockout Policy
- **Threshold**: 5 failed login attempts
- **Lock Duration**: 15 minutes
- **Auto-unlock**: Yes, after lock duration expires

### Email Verification
- Verification token expires in 24 hours
- Rate limit: 3 resend requests per hour
- Tokens are SHA-256 hashed in database

## OAuth2 Client Types

### PKCE Client (Recommended for SPAs/Mobile)
```json
POST /api/v1/clients/pkce
{
  "clientName": "My SPA App",
  "redirectUris": ["http://localhost:3000/callback"],
  "scopes": ["openid", "profile", "email"]
}
```

### Authorization Code Client
```json
POST /api/v1/clients/normal-auth
{
  "clientName": "My Web App",
  "clientSecret": "your-secret",
  "redirectUris": ["http://localhost:8080/callback"],
  "scopes": ["openid", "profile", "email"]
}
```

### Client Credentials Client (Machine-to-Machine)
```json
POST /api/v1/clients/client-credentials
{
  "clientName": "My Backend Service",
  "clientSecret": "your-secret",
  "scopes": ["api.read", "api.write"]
}
```

## Database Schema

### Core Tables
- `users` - User accounts
- `roles` - Role definitions
- `authorities` - Permission definitions
- `users_roles` - User-Role mapping
- `roles_authorities` - Role-Authority mapping

### OAuth2 Tables
- `clients` - OAuth2 registered clients
- `authorizations` - OAuth2 authorizations
- `authorization_consent` - User consent records

### Email Verification
- `email_verification_tokens` - Verification tokens

## Development

### Running Tests
```bash
./gradlew test
```

### Code Style
The project uses standard Java conventions with Lombok for boilerplate reduction.

## Architecture with API Gateway (BFF Pattern)

This authorization server is designed to work with an API Gateway using the **Backend-for-Frontend (BFF)** pattern.

### Architecture Overview

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│   API Gateway   │────▶│  Auth Server    │
│   (Next.js)     │     │   (Port 8888)   │     │  (Port 9000)    │
│                 │     │                 │     │                 │
│  - No tokens    │     │  - OAuth2 Client│     │  - Issues tokens│
│  - Session only │     │  - Stores tokens│     │  - OIDC Provider│
│                 │     │  - Token relay  │     │  - User mgmt    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### Session & Token Expiration

| Component | Lifetime | Purpose |
|-----------|----------|---------|
| Access Token | 30 minutes | Short-lived for security |
| Refresh Token | 3-7 days | Used to obtain new access tokens |
| Gateway Session | Match refresh token | Stores tokens server-side |
| Browser Session | Cookie-based | Links browser to gateway session |

### Token Revocation on Logout

When a user logs out, the API Gateway performs a complete token revocation:

```
POST /logout (Gateway)
     │
     ├─── POST /oauth2/revoke (access_token)
     │         └── Auth Server invalidates access token
     │
     ├─── POST /oauth2/revoke (refresh_token)
     │         └── Auth Server invalidates refresh token
     │
     ├─── Remove OAuth2AuthorizedClient from storage
     │
     ├─── Invalidate gateway session
     │
     ├─── Clear cookies (SESSION, XSRF-TOKEN)
     │
     └─── GET /connect/logout (OIDC logout)
               └── Auth Server clears its session
```

### Security Benefits

| Attack Vector | Protection |
|---------------|------------|
| **Session Hijacking** | HttpOnly + Secure + SameSite cookies |
| **CSRF** | CSRF tokens on state-changing requests |
| **Token Theft** | Tokens never exposed to browser (BFF pattern) |
| **Post-logout Token Use** | Token revocation invalidates stolen tokens |
| **XSS** | Tokens stored server-side, not in localStorage |

## Production Considerations

Before deploying to production, ensure you:

1. **Externalize Secrets** - Use environment variables or secret management (Vault, AWS Secrets Manager)
2. **Enable HTTPS** - Configure TLS/SSL certificates
3. **Persist JWT Keys** - Store RSA keys in database/vault instead of generating at startup
4. **Configure Logging** - Set log level to INFO/WARN, avoid logging sensitive data
5. **Add Rate Limiting** - Implement rate limiting on authentication endpoints
6. **Database Migrations** - Use Flyway or Liquibase instead of `ddl-auto: update`
7. **Monitor & Alert** - Add Spring Actuator and configure health checks


