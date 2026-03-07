# AI Portfolio Analyser

## Overview
AI Portfolio Analyser is a Spring Boot application that leverages AI to provide professional equity research and portfolio analysis. It integrates real-time news, market events, and fundamental analysis to deliver actionable insights for investors. The application supports multiple exchanges and risk profiles, and is designed with industry best practices for scalability, maintainability, and security.

## Features
- **REST API** for portfolio analysis
- **AI-powered stock assessment** using Spring AI and OpenAI models
- **Real-time news and event integration**
- **Support for multiple exchanges** (NSE, BSE, NYSE, NASDAQ)
- **Risk profile and investment horizon customization**
- **PostgreSQL database integration** via JOOQ
- **Swagger UI** for API documentation
- **Docker support** for containerized deployment

## Architecture
- **Spring Boot 4**
- **Layered structure**: Controller, Service, DAO, Domain
- **AI Integration**: Spring AI with OpenAI (Gemini model)
- **Database**: PostgreSQL, JOOQ for type-safe queries
- **Logging**: SLF4J
- **Dependency Injection**: Lombok annotations

## Getting Started
### Prerequisites
- Java 25
- Maven
- PostgreSQL database

### Build & Run
1. **Build the project:**
   ```powershell
   ./mvnw clean package
   ```
2. **Run the application:**
   ```powershell
   java -jar target/ai-portfolio-analyser-0.0.1-SNAPSHOT.jar
   ```
3. **Access API documentation:**
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Docker
Build and run the Docker image:
```powershell
# Build Docker image
docker build -t ai-portfolio-analyser .

# Run Docker container
docker run -p 8080:8080 ai-portfolio-analyser
```

## Configuration
Edit `src/main/resources/application.yaml` for environment-specific settings:
- Database URL, username, password
- AI model and API key
- SpringDoc Swagger UI

**Sensitive information** (API keys, DB credentials) should be managed via environment variables or secrets in production.

## API Endpoints
- `POST /api/portfolio/analyze` — Analyze portfolio based on exchange, stocks, horizon, and risk profile

## Best Practices
- **Sensitive data**: Never commit secrets to version control. Use environment variables or secret managers.
- **Logging**: Avoid logging sensitive information.
- **Testing**: Add unit and integration tests for all business logic.
- **Documentation**: Keep API docs updated via Swagger/OpenAPI.
- **CI/CD**: Integrate with GitHub Actions for automated build, test, and deployment.
- **Docker**: Use multi-stage builds and slim base images to minimize image size.
- **Code Quality**: Use SonarCloud for static analysis.

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit changes with clear messages
4. Submit a pull request

## License
This project is licensed under the MIT License.

## References
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring AI](https://docs.spring.io/spring-ai/reference/)
- [JOOQ](https://www.jooq.org/)
- [OpenAI](https://platform.openai.com/docs)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

---
For further help, see `HELP.md` or contact the maintainer.

