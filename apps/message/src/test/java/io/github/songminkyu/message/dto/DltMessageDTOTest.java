package io.github.songminkyu.message.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DLT Message DTO Tests")
class DltMessageDTOTest {

    @Test
    @DisplayName("DltMessageDTO 생성자 테스트")
    void shouldCreateDltMessageDTO() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "John Doe", "john@example.com", "1234567890");
        String errorMessage = "Processing error occurred";
        String exceptionClass = "RuntimeException";
        LocalDateTime failedAt = LocalDateTime.now();
        int attemptCount = 3;
        String lastFailureReason = "Database connection failed";

        // When
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            errorMessage,
            exceptionClass,
            failedAt,
            attemptCount,
            lastFailureReason
        );

        // Then
        assertThat(dltMessage.originalMessage()).isEqualTo(originalMessage);
        assertThat(dltMessage.errorMessage()).isEqualTo(errorMessage);
        assertThat(dltMessage.exceptionClass()).isEqualTo(exceptionClass);
        assertThat(dltMessage.failedAt()).isEqualTo(failedAt);
        assertThat(dltMessage.attemptCount()).isEqualTo(attemptCount);
        assertThat(dltMessage.lastFailureReason()).isEqualTo(lastFailureReason);
    }

    @Test
    @DisplayName("from 메서드로 DltMessageDTO 생성 테스트")
    void shouldCreateDltMessageDTOFromException() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "John Doe", "john@example.com", "1234567890");
        RuntimeException exception = new RuntimeException("Test error message");
        int attemptCount = 2;

        // When
        DltMessageDTO dltMessage = DltMessageDTO.from(originalMessage, exception, attemptCount);

        // Then
        assertThat(dltMessage.originalMessage()).isEqualTo(originalMessage);
        assertThat(dltMessage.errorMessage()).isEqualTo("Test error message");
        assertThat(dltMessage.exceptionClass()).isEqualTo("RuntimeException");
        assertThat(dltMessage.attemptCount()).isEqualTo(attemptCount);
        assertThat(dltMessage.lastFailureReason()).isEqualTo("Test error message");
        assertThat(dltMessage.failedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("중첩 예외가 있는 경우 from 메서드 테스트")
    void shouldCreateDltMessageDTOFromExceptionWithCause() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(987654321L, "Jane Doe", "jane@example.com", "9876543210");
        IllegalStateException cause = new IllegalStateException("Root cause error");
        RuntimeException exception = new RuntimeException("Main error", cause);
        int attemptCount = 1;

        // When
        DltMessageDTO dltMessage = DltMessageDTO.from(originalMessage, exception, attemptCount);

        // Then
        assertThat(dltMessage.originalMessage()).isEqualTo(originalMessage);
        assertThat(dltMessage.errorMessage()).isEqualTo("Main error");
        assertThat(dltMessage.exceptionClass()).isEqualTo("RuntimeException");
        assertThat(dltMessage.attemptCount()).isEqualTo(attemptCount);
        assertThat(dltMessage.lastFailureReason()).isEqualTo("Root cause error");
        assertThat(dltMessage.failedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("null 원인이 있는 예외의 from 메서드 테스트")
    void shouldCreateDltMessageDTOFromExceptionWithNullCause() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(555666777L, "Bob Smith", "bob@example.com", "5556667777");
        NullPointerException exception = new NullPointerException("Null pointer occurred");
        int attemptCount = 5;

        // When
        DltMessageDTO dltMessage = DltMessageDTO.from(originalMessage, exception, attemptCount);

        // Then
        assertThat(dltMessage.originalMessage()).isEqualTo(originalMessage);
        assertThat(dltMessage.errorMessage()).isEqualTo("Null pointer occurred");
        assertThat(dltMessage.exceptionClass()).isEqualTo("NullPointerException");
        assertThat(dltMessage.attemptCount()).isEqualTo(attemptCount);
        assertThat(dltMessage.lastFailureReason()).isEqualTo("Null pointer occurred");
        assertThat(dltMessage.failedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("동등성 테스트 - 같은 값을 가진 두 객체")
    void shouldBeEqualForSameValues() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "John Doe", "john@example.com", "1234567890");
        LocalDateTime now = LocalDateTime.now();
        
        DltMessageDTO dltMessage1 = new DltMessageDTO(
            originalMessage, "Error", "Exception", now, 1, "Reason"
        );
        DltMessageDTO dltMessage2 = new DltMessageDTO(
            originalMessage, "Error", "Exception", now, 1, "Reason"
        );

        // Then
        assertThat(dltMessage1).isEqualTo(dltMessage2);
        assertThat(dltMessage1.hashCode()).isEqualTo(dltMessage2.hashCode());
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void shouldHaveProperToStringRepresentation() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "John Doe", "john@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage, "Test error", "TestException", LocalDateTime.now(), 2, "Test reason"
        );

        // When
        String toString = dltMessage.toString();

        // Then
        assertThat(toString).contains("DltMessageDTO");
        assertThat(toString).contains("originalMessage");
        assertThat(toString).contains("errorMessage=Test error");
        assertThat(toString).contains("exceptionClass=TestException");
        assertThat(toString).contains("attemptCount=2");
        assertThat(toString).contains("lastFailureReason=Test reason");
    }
}