# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build System & Development Commands

**Build Tool**: Gradle with Gradle Wrapper
- Build: `./gradlew build`
- Run application: `./gradlew bootRun`
- Run tests: `./gradlew test`
- Clean: `./gradlew clean`

**Java Version**: 17 with toolchain support

**Spring Profiles**:
- `local`: Local development (default profile group: "dev" -> "local")
- `dev`: Development environment 
- `prod`: Production environment

**Database Migration**: Flyway
- Migrations located in `src/main/resources/db/migration/`
- Auto-runs on application startup

**Docker Development**:
- Full stack: `docker-compose up` (includes PostgreSQL, Redis, Elasticsearch, Kibana)
- Application runs on port 8081 when using Docker

## Architecture Overview

**Core Domain Structure**:
- `datasync/`: TMDB data synchronization pipeline with Spring Batch
- `domain/`: Business domain modules (auth, media, person, post, review, etc.)
- `client/`: External API clients (TMDB, OAuth2 providers)
- `global/`: Cross-cutting concerns (security, validation, exceptions)

**Key Technologies**:
- Spring Boot 3.3.1 with Spring Security & OAuth2
- PostgreSQL with Spring Data JPA
- Redis for caching
- Elasticsearch for search functionality
- Spring Batch for TMDB data synchronization
- JWT for authentication

## TMDB Sync Pipeline Architecture

The TMDB synchronization system is a multi-stage Spring Batch pipeline:

1. **Export Seeding Step**: Downloads TMDB daily export files and populates work queue
2. **Detail Processing Step**: Processes work queue items to fetch detailed data from TMDB API
3. **Data Services**: `MovieSyncService` and `TvSyncService` handle database persistence with full delta sync (delete-insert pattern for relationships)

**Key Components**:
- `InitFullSyncJobConfig`: Main batch configuration
- `TmdbWorkQueue`: Priority-based work queue for processing
- Delta DTOs: `MovieDelta`, `TvDelta` contain complete entity graphs
- Sync Services: Handle transactional persistence with relationship cleanup

## Database Schema

**Core Entities**:
- Movies, TV Series, Collections, People, Networks, Production Companies
- Many-to-many relationships via junction tables (MovieGenre, TvCast, etc.)
- Audit fields via `BaseEntity` with JPA auditing

**Migration Strategy**: 
- Flyway handles schema evolution
- Baseline version set to 1 for existing schemas

## Security Architecture

- JWT-based authentication with access/refresh token pattern
- OAuth2 integration (Google, Kakao, Naver)
- Role-based access control (USER, ADMIN)
- Password reset via email tokens
- Rate limiting for sensitive endpoints

## Search & Performance

- Elasticsearch integration with custom document repositories
- Redis caching for frequently accessed data  
- Pagination support with configurable page sizes
- Performance testing in repository layer

## Testing Strategy

- JUnit 5 with Spring Boot Test
- Separate test configurations using H2 database
- MockWebServer for external API testing
- Repository performance tests
- Integration tests for OAuth2 flows

## Configuration Files

- `application.yml`: Base configuration
- `application-{profile}.yml`: Environment-specific overrides
- Docker Compose for local development stack
- Swagger/OpenAPI documentation enabled

## Development Notes

- Uses Lombok for boilerplate reduction
- Jackson for JSON serialization with ISO date formatting
- OkHttp for TMDB API client
- Spring Batch for ETL operations
- Actuator endpoints enabled for monitoring