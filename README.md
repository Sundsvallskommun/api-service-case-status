# CaseStatus

_Provides updated status information about cases in underlying systems, ensuring that users are always informed about the current state and progress of their cases._

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Maven**
- **MariaDB**
- **Git**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

   ```bash
   git clone git@github.com:Sundsvallskommun/api-service-case-status.git
   cd api-service-case-status
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#Configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible. See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   ```bash
   mvn spring-boot:run
   ```

## Dependencies

This microservice depends on the following services:

- **CaseManagement**
  - **Purpose:** Serves as link between clients and subsystems
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/service_name)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Citizen**
  - **Purpose:** To information about a citizen.
  - **Repository:** Not available at this moment.
  - **Additional Notes:** Citizen is a API serving data from [Metadatakatalogen](https://utveckling.sundsvall.se/digital-infrastruktur/metakatalogen).
- **Open-e Platform**
  - **Purpose:** This service retrieves messages from the Open-e Platform.
  - **Repository:** [Open-ePlatform](https://github.com/Open-ePlatform/Open-ePlatform)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Party**
  - **Purpose:** Translate partyId to legalId and reverse for both private citizens and businesses
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-party)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **SupportManagement**
  - **Purpose:** Managages cases primarily related to support related subjects.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-support-management)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Alternatively, refer to the `openapi.yml` file located in `src/main/resources` for the OpenAPI specification.

## Usage

### API Endpoints

Refer to the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X GET http://localhost:8080/{municipalityId}/{externalCaseId}/oepstatus
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in `application.yml`.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings:**

  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/your_database
      username: your_db_username
      password: your_db_password
  ```
- **External Service URLs:**

  ```yaml
  integration:
    case-management:
      base-url: https://your_service_url/casemanagement
    citizen:
      base-url: https://your_service_url/citizen
    open-e:
      base-url: https://your_service_url/opene
      password: somePassword
      username: someUsername
    party:
      base-url: https://your_service_url/party
    support-management:
      base-url: https:/your_service_url/supportmanagement


  security:
    oauth2:
      client:
        provider:
          citizen:
            token-uri: https://token_url
          case-management:
            token-uri: https://token_url
          party:
            token-uri: https://token_url
          support-management:
            token-uri: https://token_url
        registration:
          citizen:
            client-id: the-client-id
            client-secret: the-client-secret
          case-management:
            client-id: the-client-id
            client-secret: the-client-secret
          party:
            client-id: the-client-id
            client-secret: the-client-secret
          support-management:
            client-id: the-client-id
            client-secret: the-client
  ```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by default so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
spring:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are correctly configured.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-status&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-status)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-status&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-status)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-status&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-status)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-status&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-status)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-status&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-status)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-status&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-status)

---

© 2024 Sundsvalls kommun
