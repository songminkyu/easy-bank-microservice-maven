# Message Retry Process Guide
메시지 재시도 프로세스 이해 및 테스트 가이드라인

## 📋 목차
1. [재시도 프로세스 개요](#재시도-프로세스-개요)
2. [메시지 처리 흐름](#메시지-처리-흐름)
3. [실패 유도 테스트 방법](#실패-유도-테스트-방법)
4. [재시도 동작 관찰](#재시도-동작-관찰)
5. [모니터링 및 알림](#모니터링-및-알림)

## 🔄 재시도 프로세스 개요

### 전략 패턴 기반 DLT 처리
현재 시스템은 4가지 전략으로 메시지 실패를 처리합니다:

1. **CriticalAccountDltStrategy** (우선순위: 100)
   - 조건: 계좌 잔액 ≥ 10억원
   - 동작: 즉시 에스컬레이션, 수동 개입 필요

2. **TransientErrorDltStrategy** (우선순위: 80)
   - 조건: 일시적 오류 (네트워크, 타임아웃 등)
   - 동작: 지수 백오프로 재시도 스케줄링

3. **PermanentErrorDltStrategy** (우선순위: 60)
   - 조건: 영구적 오류 (검증 실패, 잘못된 데이터 등)
   - 동작: 재시도 없이 아카이브

4. **DefaultDltStrategy** (우선순위: 10)
   - 조건: 기타 모든 경우
   - 동작: 표준 DLT 처리

## 🔍 메시지 처리 흐름

### 1. 정상 메시지 처리
```
[send-communication] → MessageFunctions.emailsms() → 성공
```

### 2. 실패 시 DLT 처리 흐름
```
[send-communication] → 처리 실패 → [send-communication.message.dlq] 
                                        ↓
                    DltStrategyManager.processDltMessage()
                                        ↓
                            전략 선택 (우선순위 순)
                                        ↓
                    ┌─────────────────────────────────────┐
                    │        전략별 처리 분기              │
                    └─────────────────────────────────────┘
                                        ↓
            ┌───────────────┬───────────────┬───────────────┐
            │ Critical      │ Transient     │ Permanent     │
            │ (즉시 에스컬)  │ (재시도)      │ (아카이브)     │
            └───────────────┴───────────────┴───────────────┘
                                        ↓
                        AlertStrategyManager.sendAlert()
                                        ↓
                            알림 전송 (Slack, Email 등)
```

### 3. 재시도 스케줄링 과정
```
TransientErrorDltStrategy 선택
            ↓
지수 백오프 지연 시간 계산: delay = baseDelay * (multiplier ^ retryAttempt)
            ↓
DltRetryService.scheduleRetry() 호출
            ↓
TaskScheduler로 미래 시점에 재시도 예약
            ↓
예약된 시간에 재시도 실행 (현재는 로깅만)
```

## 🧪 실패 유도 테스트 방법

### Method 1: Exception 강제 발생
**MessageFunctions.java 임시 수정**
```java
@Bean
public Function<AccountsMsgDTO, AccountsMsgDTO> emailsms() {
    return accountsMsgDTO -> {
        log.info("Processing message for account: {}", accountsMsgDTO.accountNumber());

        // 테스트용 실패 유도 코드 추가
        if (accountsMsgDTO.accountNumber().equals(123456789L)) {
            try {
                throw new java.net.SocketTimeoutException("Simulated network timeout for testing");
            } catch (SocketTimeoutException e) {
                throw new RuntimeException(e);
            }
        }
        
        // 정상 처리 로직...
        return accountsMsgDTO;
    };
}
```

### Method 2: 테스트 메시지 전송
**다양한 실패 시나리오 테스트**
```bash
# 1. 일시적 오류 (재시도 발생)
curl -X POST http://localhost:9010/test/send-message \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": 123456789,
    "name": "Test User",
    "email": "test@example.com",
    "phone": "1234567890"
  }'

# 2. 크리티컬 계좌 (즉시 에스컬레이션)
curl -X POST http://localhost:9010/test/send-message \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": 1500000000,
    "name": "VIP Customer",
    "email": "vip@example.com", 
    "phone": "9999999999"
  }'

# 3. 영구적 오류 (재시도 없음)
curl -X POST http://localhost:9010/test/send-message \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": 999999999,
    "name": null,
    "email": "invalid-email",
    "phone": "invalid-phone"
  }'
```

### Method 3: 외부 의존성 차단
**네트워크/서비스 차단으로 실패 유도**
```bash
# Kafka 일시 중단 (Docker 환경)
docker stop kafka-container

# 또는 방화벽 규칙으로 포트 차단
# Windows: netsh advfirewall firewall add rule name="Block Kafka" dir=out action=block protocol=TCP localport=9092
```

## 👀 재시도 동작 관찰

### 1. 로그 모니터링
**application.yml 로그 레벨 조정**
```yaml
logging:
  level:
    io.github.songminkyu.message: DEBUG
    org.springframework.cloud.stream: DEBUG
```

**관찰할 로그 패턴:**
```
# DLT 메시지 수신
INFO  [message] - Processing DLT message for account: 123456789

# 전략 선택
INFO  [message] - Selected DLT strategy 'TransientErrorDltStrategy' for account: 123456789

# 재시도 스케줄링
INFO  [message] - Scheduling retry for account 123456789 with delay 30000ms

# 재시도 실행
INFO  [message] - Executing scheduled retry for account 123456789
```

### 2. 메트릭스 모니터링
**Actuator 엔드포인트 활용**
```bash
# 전체 메트릭스 조회
curl http://localhost:9010/actuator/metrics

# DLT 처리 시간 메트릭스
curl http://localhost:9010/actuator/metrics/message.dlt.processing.time

# 헬스 체크
curl http://localhost:9010/actuator/health
```

### 3. 재시도 상태 확인
**DltRetryService 상태 조회 (개발용)**
```java
// 테스트 컨트롤러 추가 예시
@RestController
@RequestMapping("/test")
public class DltTestController {
    
    @Autowired
    private DltRetryService dltRetryService;
    
    @GetMapping("/retry-status")
    public Map<String, Object> getRetryStatus() {
        return Map.of(
            "scheduledRetryCount", dltRetryService.getScheduledRetryCount(),
            "timestamp", LocalDateTime.now()
        );
    }
}
```

## 📊 재시도 시나리오별 예상 동작

### 시나리오 1: 일반 계좌 + 네트워크 타임아웃
```
1. 메시지 처리 실패 (SocketTimeoutException)
2. DLT 큐로 이동
3. TransientErrorDltStrategy 선택
4. 첫 번째 재시도: 30초 후 예약
5. StandardAlertStrategy로 표준 알림 발송
6. 재시도 실패 시: 60초 후 두 번째 재시도
7. 최대 재시도 초과 시: 수동 개입 필요 상태로 변경
```

### 시나리오 2: 크리티컬 계좌 (10억 이상)
```
1. 메시지 처리 실패 (모든 오류 타입)
2. DLT 큐로 이동  
3. CriticalAccountDltStrategy 선택 (최고 우선순위)
4. 즉시 에스컬레이션 상태로 변경
5. CriticalAlertStrategy로 긴급 알림 발송
   - PagerDuty 알림 (설정 시)
   - 긴급 Slack 알림
   - 온콜팀 이메일
   - 긴급 Jira 티켓 생성
6. 수동 개입 필요 (재시도 없음)
```

### 시나리오 3: 영구적 오류
```
1. 검증 실패 (IllegalArgumentException)
2. DLT 큐로 이동
3. PermanentErrorDltStrategy 선택
4. 영구 실패로 분류 (재시도 없음)
5. StandardAlertStrategy로 표준 알림
6. 메시지 아카이브 처리
```

## 🎯 테스트 체크리스트

### ✅ 기본 기능 테스트
- [ ] 정상 메시지 처리 확인
- [ ] DLT 메시지 수신 확인  
- [ ] 전략 선택 로직 확인
- [ ] 재시도 스케줄링 확인
- [ ] 알림 발송 확인

### ✅ 전략별 동작 테스트
- [ ] CriticalAccountDltStrategy 동작 확인 (10억 이상 계좌)
- [ ] TransientErrorDltStrategy 동작 확인 (네트워크 오류)
- [ ] PermanentErrorDltStrategy 동작 확인 (검증 오류)
- [ ] DefaultDltStrategy 동작 확인 (기타 오류)

### ✅ 재시도 메커니즘 테스트
- [ ] 지수 백오프 지연 시간 확인
- [ ] 최대 재시도 횟수 제한 확인
- [ ] 재시도 취소 기능 확인
- [ ] 동시 다중 재시도 처리 확인

### ✅ 알림 시스템 테스트
- [ ] 크리티컬 알림 (CriticalAlertStrategy) 확인
- [ ] 표준 알림 (StandardAlertStrategy) 확인
- [ ] 알림 전략 우선순위 확인

## 🔧 설정 조정

### 재시도 설정 변경
**application.yml 수정**
```yaml
app:
  dlt:
    retry-strategy:
      max-dlt-retry-attempts: 3      # 재시도 횟수 증가
      base-retry-delay-ms: 10000     # 기본 지연시간 단축 (10초)
      backoff-multiplier: 1.5        # 백오프 배수 조정
      max-retry-delay-ms: 180000     # 최대 지연시간 조정 (3분)
```

### 크리티컬 계좌 임계값 변경
```yaml
app:
  dlt:
    critical-account:
      account-threshold: 500000000   # 5억으로 임계값 조정
```

## 🚨 주의사항

1. **프로덕션 환경 주의**: 테스트용 실패 코드는 개발환경에서만 사용
2. **리소스 모니터링**: 재시도가 과도하게 발생하지 않도록 모니터링 필요
3. **알림 스팸 방지**: 테스트 시 알림 설정 조정 고려
4. **로그 레벨 복원**: 디버깅 후 로그 레벨을 INFO로 복원

## 📝 추가 개선 아이디어

1. **재시도 이력 추적**: 데이터베이스에 재시도 이력 저장
2. **동적 설정 변경**: 런타임에 재시도 설정 변경 가능
3. **대시보드 구축**: 실시간 DLT 처리 현황 모니터링
4. **자동 복구**: 특정 조건에서 자동으로 원본 큐로 메시지 재전송