package io.github.susimsek.account.client;

import io.github.susimsek.account.dto.LoanDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "loan", fallback = LoanFallback.class)
public interface LoanFeignClient {

    @GetMapping(value = "/api/loan", consumes = "application/json")
    LoanDTO fetchLoanDetails(
        @RequestHeader("X-Correlation-Id") String correlationId,
        @RequestParam String mobileNumber);

}