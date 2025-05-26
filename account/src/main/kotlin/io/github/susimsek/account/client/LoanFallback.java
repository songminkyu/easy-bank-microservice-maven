package io.github.susimsek.account.client;

import io.github.susimsek.account.dto.LoanDTO;
import org.springframework.stereotype.Component;

@Component
public class LoanFallback implements LoanFeignClient {

    @Override
    public LoanDTO fetchLoanDetails(String correlationId, String mobileNumber) {
        return null;
    }
}