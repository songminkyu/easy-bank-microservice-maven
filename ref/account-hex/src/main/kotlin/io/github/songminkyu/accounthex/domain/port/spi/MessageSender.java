package io.github.songminkyu.accounthex.domain.port.spi;

import io.github.songminkyu.accounthex.domain.model.dto.AccountsMsgDTO;

/**
 * Secondary port for sending messages.
 * This interface defines the operations for sending messages to external systems.
 */
public interface MessageSender {

    /**
     * Sends a communication message.
     *
     * @param accountsMsgDTO the message to send
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendCommunication(AccountsMsgDTO accountsMsgDTO);
}