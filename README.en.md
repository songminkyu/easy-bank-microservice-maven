# Master Microservices with Spring Boot, Docker, Kubernetes

Eazy Bank Microservices Sample using Spring Boot & Spring Cloud

## Project Overview

This project is a microservices architecture sample implemented using Spring Boot and Spring Cloud. It implements various features of the "Eazy Bank" banking application by separating them into microservices.

## Architecture

This project uses the following microservices architecture patterns:

- **Config Server**: Centralized configuration management
- **Service Discovery (Eureka)**: Service registration and discovery
- **API Gateway**: Client request routing and filtering
- **Circuit Breaker**: Fault isolation and resilience
- **Distributed Tracing**: Request tracking and monitoring
- **Event-Driven Architecture**: Asynchronous communication using Kafka

## Components

### Infrastructure Services

- **Config Server**: Centralized management of configurations for all microservices (Port: 8071)
- **Eureka Server**: Service discovery and load balancing (Port: 8070)
- **Gateway Server**: API gateway, routing, and filtering (Port: 8072)

### Core Microservices

- **Account**: Account management service (Port: 8080)
- **Loan**: Loan management service (Port: 8090)
- **Card**: Card management service (Port: 9000)
- **Message**: Message processing service (Port: 9010)

### Supporting Infrastructure

- **PostgreSQL**: Relational database
- **Redis**: Caching and session management
- **Kafka**: Event messaging
- **Keycloak**: Authentication and authorization
- **Debezium**: Change Data Capture (CDC)
- **Observability Stack**:
  - Prometheus: Metrics collection
  - Grafana: Dashboards and visualization
  - Loki: Log aggregation
  - Tempo: Distributed tracing

## Execution Order

Due to the nature of microservices architecture, the order of service startup is important. Services should be started in the following order:

1. Infrastructure services (PostgreSQL, Redis, Kafka, etc.)
2. Config Server
3. Eureka Server
4. Core microservices (Account, Loan, Card)
5. Gateway Server

## Deployment Options

### Docker Compose

The project provides Docker Compose configurations for various environments:

- **Default**: Basic local development environment
  ```bash
  cd k8s/docker-compose/default
  docker-compose up -d
  ```

- **Dev**: Development environment
  ```bash
  cd k8s/docker-compose/dev
  docker-compose up -d
  ```

- **QA**: QA environment
  ```bash
  cd k8s/docker-compose/qa
  docker-compose up -d
  ```

- **Prod**: Production environment
  ```bash
  cd k8s/docker-compose/prod
  docker-compose up -d
  ```

### Kubernetes

You can also deploy to Kubernetes using Helm charts:

```bash
# Deploy development environment
helm install eazybank-dev .k8s/helm/environments/dev-env

# Deploy QA environment
helm install eazybank-qa .k8s/helm/environments/qa-env

# Deploy production environment
helm install eazybank-prod .k8s/helm/environments/prod-env
```

## Development and Debugging

### Local Development Environment Setup

1. Install JDK 21
2. Install Maven 3.8.8 or higher
3. Install Docker and Docker Compose
4. Start infrastructure services:
   ```bash
   cd docker-compose/default
   docker-compose up -d postgresql redis kafka keycloak
   ```
5. Run each microservice in your IDE (refer to the execution order above)

### Debugging Methods

1. **Check Logs**:
   - Logs for each service can be viewed via the `/actuator/logfile` endpoint
   - When running with Docker Compose: `docker-compose logs -f [service-name]`
   - Centralized logs via Grafana Loki: http://localhost:3000

2. **Check Status**:
   - Status of each service can be checked via the `/actuator/health` endpoint
   - Eureka Dashboard: http://localhost:8070
   - Spring Boot Admin: http://localhost:8072/admin

3. **API Documentation**:
   - Swagger UI: http://localhost:8072/swagger-ui.html

4. **Distributed Tracing**:
   - Tempo UI (Grafana): http://localhost:3000/explore?orgId=1&left=%5B%22now-1h%22,%22now%22,%22Tempo%22,%7B%7D%5D

5. **Metrics Monitoring**:
   - Prometheus: http://localhost:9090
   - Grafana Dashboard: http://localhost:3000

### Common Troubleshooting

1. **Service Connection Issues**:
   - Check if Eureka server is running
   - Verify if services are registered with Eureka
   - Check network settings

2. **Configuration Issues**:
   - Check if Config Server is running
   - Verify if configuration files are in the correct location
   - Check environment variable settings

3. **Database Issues**:
   - Check if PostgreSQL is running
   - Verify database connection settings
   - Check if schemas and tables are created correctly

## Technology Stack

- **Languages**: Java 21, Kotlin 2.1
- **Frameworks**: Spring Boot 3.3, Spring Cloud 2023.0.1
- **Build Tool**: Maven 3.8.8
- **Database**: PostgreSQL
- **Caching**: Redis, Redisson
- **Messaging**: Kafka
- **Authentication**: Keycloak, OAuth2, JWT
- **API Documentation**: SpringDoc OpenAPI
- **Monitoring**: Prometheus, Grafana, Loki, Tempo
- **Containerization**: Docker, Jib
- **Orchestration**: Kubernetes, Helm

## Project Structure

```
easy-bank-msa-maven/
├── apps/                     # Application services
│   ├── account/              # Account service
│   ├── card/                 # Card service
│   ├── config-server/        # Configuration server
│   ├── eureka-server/        # Service discovery
│   ├── gateway-server/       # API gateway
│   ├── loan/                 # Loan service
│   ├── message/              # Message service
│   └── parent/               # Parent POM
└── k8s/                      # Kubernetes and infrastructure configurations
    ├── docker-compose/       # Docker Compose configurations
    │   ├── debezium/         # Debezium environment
    │   ├── default/          # Default environment
    │   ├── dev/              # Development environment
    │   ├── observability/    # Observability stack environment
    │   ├── prod/             # Production environment
    │   └── qa/               # QA environment
    ├── helm/                 # Kubernetes Helm charts
    ├── kubernetes/           # Kubernetes configurations
    ├── microservice-config/  # Microservice configurations
    └── microservice-k8s-config/ # Kubernetes microservice configurations
```

## Reference
[spring-boot-microservice-sample](https://github.com/susimsek/spring-boot-microservice-sample.git)