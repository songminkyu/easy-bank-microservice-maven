# Exception Handling Configuration Guide

## Overview
This guide explains the improved exception handling mechanism for Spring Cloud Functions in the message service.

## Configuration Changes

### 1. Test Environment (default application.yml)
- `maxAttempts: 1` - Immediate DLQ routing for testing
- `test-mode: true` - Enables test-specific behavior
- Forces immediate failure to verify DLQ functionality

### 2. Production Environment (application-prod.yml)
- `maxAttempts: 5` - Normal retry behavior for resilience
- `test-mode: false` - Enables production retry logic
- Longer retry delays for external service recovery

## Exception Categories

### Permanent Failures (No Retry)
These exceptions go directly to DLQ:
- `IllegalArgumentException` - Invalid input data
- `ValidationException` - Data validation failures
- `NullPointerException` - Required data missing
- `DataIntegrityViolationException` - Data consistency issues
- `SecurityException` - Authorization failures

### Transient Failures (Retry Enabled)
These exceptions trigger retry mechanism:
- `SocketTimeoutException` - Network timeouts
- `ConnectException` - Connection failures
- `ServiceUnavailableException` - External service down
- `HttpServerErrorException` - HTTP 5xx errors
- `ResourceAccessException` - Resource access issues

## Testing the Exception Handling

### Current Test Setup
```java
// Line 52 in MessageFunctions.java
throw new IllegalArgumentException("TESTING: Forced exception to verify DLQ behavior");
```

### Expected Behavior
1. **Test Mode**: Exception → Immediate DLQ → `handleDltMessage()` called
2. **Production Mode**: Transient exceptions → Retry 5 times → DLQ if all retries fail

### To Switch Between Modes
```bash
# Test mode (immediate DLQ)
java -jar message-service.jar

# Production mode (with retries)
java -jar message-service.jar --spring.profiles.active=prod
```

## Monitoring and Metrics

### Key Log Messages
```
Permanent failure - sending to DLQ. Account: {}, Error: {}
Transient failure - will retry. Account: {}, Error: {}
DLT Message processed: {}
```

### Metrics Tracked
- `dltMetrics.recordMessageFailure()`
- `dltMetrics.recordDltMessage()`
- Processing time and success/failure rates

## Troubleshooting

### Issue: Exceptions not behaving as expected
**Solution**: Check `maxAttempts` setting and active profile

### Issue: Too many retries in test
**Solution**: Use test profile with `maxAttempts: 1`

### Issue: No retries in production
**Solution**: Verify production profile is active and exception type allows retries

## Best Practices

1. **Use specific exception types** for better routing decisions
2. **Log with appropriate levels** (ERROR for permanent, WARN for transient)
3. **Include context** in error messages for debugging
4. **Test both retry and no-retry scenarios** during development
5. **Monitor DLT metrics** in production for early problem detection