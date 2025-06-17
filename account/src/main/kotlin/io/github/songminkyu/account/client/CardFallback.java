package io.github.songminkyu.account.client;

import io.github.songminkyu.account.dto.CardDTO;
import org.springframework.stereotype.Component;

@Component
public class CardFallback implements CardFeignClient {
    @Override
    public CardDTO fetchCardDetails(String correlationId, String mobileNumber) {
        return null;
    }
}