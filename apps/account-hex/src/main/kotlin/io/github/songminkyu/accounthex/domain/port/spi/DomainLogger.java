package io.github.songminkyu.accounthex.domain.port.spi;

/**
 * Secondary port for domain logging.
 * This interface allows the domain to log messages without depending on any specific logging framework.
 */
@FunctionalInterface
public interface DomainLogger {
    
    /**
     * Logs a message.
     *
     * @param message the message to log
     */
    void log(String message);
    
    /**
     * Logs a message with a format and arguments.
     *
     * @param format the format string
     * @param args the arguments
     */
    default void log(String format, Object... args) {
        log(String.format(format, args));
    }
}