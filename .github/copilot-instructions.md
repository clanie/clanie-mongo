# Mongo - Copilot Instructions

## Project Overview
This project provide components and configuration for using MongoDB in Spring Boot projects. 

## Tech Stack
- **Backend:** Java 25+, Spring Boot 3.x
- **Database:** MongoDB
- **Build Tool:** Maven 3.x
- **Testing:** JUnit 5, Spring Boot Test, Testcontainers
- **Key Libraries:** 
  - Spring Data MongoDB for data persistence


## Coding Standards

### Java
- Use Java 25 syntax and features where appropriate
- Follow Spring Boot best practices
- Use Lombok annotations to reduce boilerplate (e.g., `@Data`, `@Builder`, `@Slf4j`)
- Prefer constructor injection over field injection
- Use meaningful variable and method names
- Keep methods focused and single-purpose

### Testing
- Write unit tests
- Use `@SpringBootTest` for integration tests
- Use Testcontainers for MongoDB integration tests
- Mock external API calls in tests

## Security Guidelines
- Never commit sensitive credentials
- Use environment variables for secrets
