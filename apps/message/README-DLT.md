# Dead Letter Topic (DLT) 구현 가이드

## 개요
이 문서는 message 서비스에 구현된 Dead Letter Topic (DLT) 전략에 대한 설명과 사용법을 제공합니다.

## 구현된 기능

### 1. DLT 설정 (application.yml)
```yaml
spring:
  cloud:
    stream:
      bindings:
        emailsms-in-0:
          consumer:
            maxAttempts: 3              # 최대 재시도 횟수
            backOffInitialInterval: 1000 # 초기 백오프 간격 (1초)
            backOffMaxInterval: 10000    # 최대 백오프 간격 (10초)
            backOffMultiplier: 2.0       # 백오프 배수
      kafka:
        bindings:
          emailsms-in-0:
            consumer:
              enableDlq: true                              # DLQ 활성화
              dlqName: send-communication.message.dlq     # DLT 토픽 이름
```

### 2. 구현된 컴포넌트

#### DltMessageDTO
- DLT 메시지 정보를 담는 DTO
- 원본 메시지, 에러 정보, 시도 횟수 등 포함

#### DltMetrics
- Micrometer를 사용한 DLT 메트릭 수집
- Prometheus 연동으로 모니터링 가능

#### DltAlertService
- DLT 메시지 발생 시 알림 처리
- 일반 알림 vs 긴급 알림 구분

#### MessageFunctions 개선
- 에러 처리 로직 강화
- DLT 핸들러 추가
- 메트릭 수집 통합

## 사용 방법

### 1. 서비스 시작
```bash
mvn spring-boot:run
```

### 2. DLT 동작 테스트
메시지 처리 실패를 의도적으로 발생시키려면:
- accountNumber가 null인 메시지 전송 (email 실패)
- mobileNumber가 null/empty인 메시지 전송 (SMS 실패)

### 3. 모니터링
- **메트릭 확인**: http://localhost:9010/actuator/metrics
- **Prometheus 메트릭**: http://localhost:9010/actuator/prometheus
- **로그 확인**: 애플리케이션 로그에서 DLT 관련 로그 확인

## 메트릭 정보

### 수집되는 메트릭들
- `message.dlt.received`: DLT로 전송된 메시지 수
- `message.dlt.processing.time`: DLT 메시지 처리 시간
- `message.processing.failures`: 메시지 처리 실패 수

### 메트릭 태그
- `exception.class`: 발생한 예외 클래스
- `account.number`: 계정 번호
- `message.type`: 메시지 타입 (email/sms)
- `error.type`: 에러 타입

## 알림 시스템

### 일반 알림
- DLT 메시지 발생 시 기본 알림
- 로그 기반 알림

### 긴급 알림
다음 조건에서 긴급 알림 발생:
- 재시도 횟수 3회 이상
- NullPointerException 발생
- 고액 계정 (계정번호 > 1000000000)

### 확장 가능한 알림 채널
- Slack 웹훅
- 이메일 알림
- Jira 티켓 생성
- PagerDuty 연동

## 운영 가이드

### DLT 메시지 처리 전략
1. **즉시 처리**: 일시적 오류로 판단되는 경우
2. **수동 검토**: 복잡한 오류의 경우 운영팀 검토
3. **대체 처리**: 다른 채널을 통한 알림 발송
4. **데이터 복구**: 필요시 메시지 재처리

### 모니터링 대시보드 구성 예시
```prometheus
# DLT 메시지 발생률
rate(message_dlt_received_total[5m])

# DLT 처리 시간 분위수
histogram_quantile(0.95, message_dlt_processing_time_seconds_bucket)

# 메시지 실패율
rate(message_processing_failures_total[5m]) / rate(message_total[5m])
```

## 문제 해결

### 일반적인 문제들
1. **DLT 토픽 자동 생성 안됨**: `autoCreateTopics: true` 확인
2. **메트릭 수집 안됨**: actuator 의존성 확인
3. **알림 발송 안됨**: 로그 레벨 DEBUG로 설정하여 확인

### 로그 확인
```bash
# DLT 관련 로그 필터링
tail -f application.log | grep "DLT"

# 에러 로그 확인
tail -f application.log | grep "ERROR"
```

## 추가 개선 사항

### 향후 구현 고려사항
1. **데이터베이스 저장**: DLT 메시지 영구 저장
2. **재처리 API**: 수동 재처리 REST API
3. **대시보드 UI**: DLT 메시지 관리 웹 인터페이스
4. **자동 복구**: 특정 조건에서 자동 재시도

### 성능 최적화
1. **배치 처리**: 다수 DLT 메시지 일괄 처리
2. **비동기 처리**: DLT 처리 비동기화
3. **캐싱**: 반복적인 에러 패턴 캐싱