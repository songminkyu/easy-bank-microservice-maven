package io.github.susimsek.account.client;

import io.github.susimsek.account.dto.CardDTO;
import org.springframework.stereotype.Component;

@Component
public class CardFallback implements CardFeignClient {
    @Override
    public CardDTO fetchCardDetails(String correlationId, String mobileNumber) {
        return null;
    }
}