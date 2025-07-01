# Master Microservices with Spring Boot, Docker, Kubernetes

Eazy Bank Microservices Sample using Spring Boot & Spring Cloud

## 프로젝트 개요

이 프로젝트는 Spring Boot와 Spring Cloud를 사용하여 구현된 마이크로서비스 아키텍처 샘플입니다. 은행 애플리케이션인 "Eazy Bank"의 다양한 기능을 마이크로서비스로 분리하여 구현했습니다.

## 아키텍처

이 프로젝트는 다음과 같은 마이크로서비스 아키텍처 패턴을 사용합니다:

- **Config Server**: 중앙 집중식 구성 관리
- **Service Discovery (Eureka)**: 서비스 등록 및 발견
- **API Gateway**: 클라이언트 요청 라우팅 및 필터링
- **Circuit Breaker**: 장애 격리 및 복원력
- **Distributed Tracing**: 요청 추적 및 모니터링
- **Event-Driven Architecture**: Kafka를 사용한 비동기 통신

## 컴포넌트

### 인프라 서비스

- **Config Server**: 모든 마이크로서비스의 구성을 중앙에서 관리 (포트: 8071)
- **Eureka Server**: 서비스 디스커버리 및 로드 밸런싱 (포트: 8070)
- **Gateway Server**: API 게이트웨이, 라우팅 및 필터링 (포트: 8072)

### 핵심 마이크로서비스

- **Account**: 계정 관리 서비스 (포트: 8080)
- **Loan**: 대출 관리 서비스 (포트: 8090)
- **Card**: 카드 관리 서비스 (포트: 9000)
- **Message**: 메시지 처리 서비스 (포트: 9010)

### 지원 인프라

- **PostgreSQL**: 관계형 데이터베이스
- **Redis**: 캐싱 및 세션 관리
- **Kafka**: 이벤트 메시징
- **Keycloak**: 인증 및 권한 부여
- **Debezium**: 변경 데이터 캡처(CDC)
- **Observability Stack**:
  - Prometheus: 메트릭 수집
  - Grafana: 대시보드 및 시각화
  - Loki: 로그 집계
  - Tempo: 분산 추적

## 실행 순서

마이크로서비스 아키텍처의 특성상 서비스 시작 순서가 중요합니다. 다음 순서로 서비스를 시작해야 합니다:

1. 인프라 서비스 (PostgreSQL, Redis, Kafka 등)
2. Config Server
3. Eureka Server
4. 핵심 마이크로서비스 (Account, Loan, Card)
5. Gateway Server

## 배포 옵션

### Docker Compose

프로젝트는 여러 환경에 대한 Docker Compose 구성을 제공합니다:

- **Default**: 기본 로컬 개발 환경
  ```bash
  cd k8s/docker-compose/default
  docker-compose up -d
  ```

- **Dev**: 개발 환경
  ```bash
  cd k8s/docker-compose/dev
  docker-compose up -d
  ```

- **QA**: QA 환경
  ```bash
  cd k8s/docker-compose/qa
  docker-compose up -d
  ```

- **Prod**: 프로덕션 환경
  ```bash
  cd k8s/docker-compose/prod
  docker-compose up -d
  ```

### Kubernetes

Helm 차트를 사용하여 Kubernetes에 배포할 수도 있습니다:

```bash
# 개발 환경 배포
helm install eazybank-dev .k8s/helm/environments/dev-env

# QA 환경 배포
helm install eazybank-qa .k8s/helm/environments/qa-env

# 프로덕션 환경 배포
helm install eazybank-prod .k8s/helm/environments/prod-env
```

## 개발 및 디버깅

### 로컬 개발 환경 설정

1. JDK 21 설치
2. Maven 3.8.8 이상 설치
3. Docker 및 Docker Compose 설치
4. 인프라 서비스 시작:
   ```bash
   cd docker-compose/default
   docker-compose up -d postgresql redis kafka keycloak
   ```
5. 각 마이크로서비스를 IDE에서 실행 (위의 실행 순서 참조)

### 디버깅 방법

1. **로그 확인**:
   - 각 서비스의 로그는 `/actuator/logfile` 엔드포인트를 통해 확인 가능
   - Docker Compose로 실행 시: `docker-compose logs -f [서비스명]`
   - Grafana Loki를 통해 중앙 집중식 로그 확인: http://localhost:3000

2. **상태 확인**:
   - 각 서비스의 상태는 `/actuator/health` 엔드포인트를 통해 확인 가능
   - Eureka 대시보드: http://localhost:8070
   - Spring Boot Admin: http://localhost:8072/admin

3. **API 문서**:
   - Swagger UI: http://localhost:8072/swagger-ui.html

4. **분산 추적**:
   - Tempo UI (Grafana): http://localhost:3000/explore?orgId=1&left=%5B%22now-1h%22,%22now%22,%22Tempo%22,%7B%7D%5D

5. **메트릭 모니터링**:
   - Prometheus: http://localhost:9090
   - Grafana 대시보드: http://localhost:3000

### 일반적인 문제 해결

1. **서비스 연결 문제**:
   - Eureka 서버가 실행 중인지 확인
   - 서비스가 Eureka에 등록되었는지 확인
   - 네트워크 설정 확인

2. **구성 문제**:
   - Config Server가 실행 중인지 확인
   - 구성 파일이 올바른 위치에 있는지 확인
   - 환경 변수 설정 확인

3. **데이터베이스 문제**:
   - PostgreSQL이 실행 중인지 확인
   - 데이터베이스 연결 설정 확인
   - 스키마 및 테이블이 올바르게 생성되었는지 확인

## 기술 스택

- **언어**: Java 21, Kotlin 2.1
- **프레임워크**: Spring Boot 3.3, Spring Cloud 2023.0.1
- **빌드 도구**: Maven 3.8.8
- **데이터베이스**: PostgreSQL
- **캐싱**: Redis, Redisson
- **메시징**: Kafka
- **인증**: Keycloak, OAuth2, JWT
- **API 문서**: SpringDoc OpenAPI
- **모니터링**: Prometheus, Grafana, Loki, Tempo
- **컨테이너화**: Docker, Jib
- **오케스트레이션**: Kubernetes, Helm

## 프로젝트 구조

```
easy-bank-msa-maven
├── apps/                     # 애플리케이션 서비스
│   ├── account/              # 계정 서비스
│   ├── card/                 # 카드 서비스
│   ├── config-server/        # 구성 서버
│   ├── eureka-server/        # 서비스 디스커버리
│   ├── gateway-server/       # API 게이트웨이
│   ├── loan/                 # 대출 서비스
│   ├── message/              # 메시지 서비스
│   └── parent/               # 부모 POM
└── k8s/                      # Kubernetes 및 인프라 구성
    ├── docker-compose/       # Docker Compose 구성
    │   ├── debezium/         # Debezium 환경
    │   ├── default/          # 기본 환경
    │   ├── dev/              # 개발 환경
    │   ├── observability/    # 관측성 스택 환경
    │   ├── prod/             # 프로덕션 환경
    │   └── qa/               # QA 환경
    ├── helm/                 # Kubernetes Helm 차트
    ├── kubernetes/           # Kubernetes 구성
    ├── microservice-config/  # 마이크로서비스 구성
    └── microservice-k8s-config/ # Kubernetes 마이크로서비스 구성
```

## 참고
[spring-boot-microservice-sample](https://github.com/susimsek/spring-boot-microservice-sample.git)


