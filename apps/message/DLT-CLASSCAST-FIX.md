# DLT ClassCastException 완전 해결 가이드

## 🚨 해결된 문제

### 오류 위치
```
MessageFunctions.java:133 - handleDltMessage 함수 내 헤더 접근
java.lang.ClassCastException: class [B cannot be cast to class java.lang.String
```

## 🔧 적용된 수정사항

### 1. **안전한 헤더 접근 메서드 추가**
```java
// ❌ BEFORE (직접 캐스팅 - 실패)
String errorMessage = (String) message.getHeaders().get("x-exception-message");

// ✅ AFTER (안전한 변환)
String errorMessage = getStringFromHeader(message.getHeaders(), "x-exception-message");

// 헬퍼 메서드
private String getStringFromHeader(MessageHeaders headers, String headerName) {
    Object value = headers.get(headerName);
    if (value == null) return null;
    if (value instanceof String) return (String) value;
    if (value instanceof byte[]) return new String((byte[]) value, StandardCharsets.UTF_8);
    return value.toString();
}
```

### 2. **Function 정의 문법 수정**
```yaml
# ❌ BEFORE (잘못된 세미콜론)
spring.cloud.function.definition: email|sms;handleDltMessage;

# ✅ AFTER (올바른 파이프)
spring.cloud.function.definition: email|sms|handleDltMessage
```

### 3. **DLT 메시지 Content-Type 설정**
```yaml
spring.cloud.stream:
  default:
    contentType: application/json  # 전체 기본값
  bindings:
    handleDltMessage-in-0:
      contentType: application/json  # DLT 전용 설정
```

### 4. **디버깅 로그 강화**
```java
log.error("🚨 DLT MESSAGE RECEIVED! Starting DLT processing...");
log.debug("📋 DLT Message headers: {}", message.getHeaders());
log.debug("📋 DLT Message payload type: {}", message.getPayload().getClass().getSimpleName());
```

## 🧪 테스트 방법

### 1. **애플리케이션 재시작**
다음 로그들이 순서대로 나타나는지 확인:
```
🚀 MessageFunctions initialized
🔧 Creating email() Function bean
🔧 Creating sms() Function bean
🔧 handleDltMessage bean created successfully!
```

### 2. **Kafka 메시지 전송**
```bash
kafka-console-producer --bootstrap-server localhost:9092 --topic send-communication

# JSON 메시지 입력:
{"accountNumber": 123, "name": "Test User", "email": "test@example.com", "mobileNumber": "010-1234-5678"}
```

### 3. **예상 로그 시퀀스**
```
1. ★ Processing email for account: 123 ★
2. 🔥 Permanent failure - this should trigger DLQ routing
3. 🚨 DLT MESSAGE RECEIVED! Starting DLT processing...
4. 📋 DLT Message headers: {x-exception-message=..., x-exception-fqcn=...}
5. 📋 DLT Message payload type: AccountsMsgDTO
6. Processing message from DLT: Account Number: 123, Name: Test User...
7. DLT Message processed: DltMessageDTO[...]
```

## 🎯 핵심 해결 포인트

### 1. **Type-Safe Header Access**
- byte array, String, Object 모든 타입 안전하게 처리
- UTF-8 인코딩으로 byte array → String 변환
- null 안전성 보장

### 2. **Content-Type 명시**
- JSON 역직렬화 강제 설정
- DLT 메시지 전용 Content-Type 설정
- 기본값과 특정 바인딩 설정 조합

### 3. **Function 문법 정확성**
- 파이프(`|`) 사용으로 함수 구분
- 세미콜론(`;`) 사용 금지
- 조합 함수 vs 개별 함수 구분

## 🔍 트러블슈팅

### 여전히 ClassCastException 발생 시:

1. **헤더 타입 확인**:
   ```java
   log.debug("Header value type: {}", message.getHeaders().get("x-exception-message").getClass());
   ```

2. **로그 레벨 설정**:
   ```yaml
   logging.level:
     io.github.songminkyu.message: DEBUG
   ```

3. **Kafka 토픽 초기화**:
   ```bash
   kafka-topics --delete --topic send-communication.message.dlq --bootstrap-server localhost:9092
   ```

이제 DLT 메시지 처리에서 ClassCastException이 완전히 해결되어야 합니다! 🎉