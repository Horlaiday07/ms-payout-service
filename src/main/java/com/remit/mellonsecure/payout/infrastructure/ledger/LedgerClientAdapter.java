package com.remit.mellonsecure.payout.infrastructure.ledger;

import com.remit.mellonsecure.payout.domain.port.LedgerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LedgerClientAdapter implements LedgerClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${payout.ledger.base-url}")
    private String baseUrl;

    @Value("${payout.ledger.timeout:10000}")
    private int timeout;

    @Value("${payout.ledger.balance-path:/api/v1/accounts/{accountId}/balance}")
    private String balancePath;

    @Value("${payout.ledger.debit-path:/api/v1/ledger/debit}")
    private String debitPath;

    @Override
    public BigDecimal getBalance(String accountId) {
        log.debug("Fetching balance for accountId={}", accountId);
        try {
            String response = getWebClient()
                    .get()
                    .uri(balancePath.replace("{accountId}", accountId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(timeout));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);
            return BigDecimal.valueOf(json.path("balance").asDouble(0.0));
        } catch (Exception e) {
            log.error("Failed to fetch balance: accountId={}", accountId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean hasSufficientBalance(String accountId, BigDecimal amount) {
        BigDecimal balance = getBalance(accountId);
        return balance.compareTo(amount) >= 0;
    }

    @Override
    public void reserveFunds(String accountId, String paymentReference, BigDecimal amount) {
        log.info("Reserving funds: accountId={}, paymentReference={}, amount={}", accountId, paymentReference, amount);
        try {
            Map<String, Object> body = Map.of(
                    "accountId", accountId,
                    "paymentReference", paymentReference,
                    "amount", amount.doubleValue()
            );
            getWebClient()
                    .post()
                    .uri(debitPath)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block(Duration.ofMillis(timeout));
        } catch (Exception e) {
            log.error("Failed to reserve funds: accountId={}", accountId, e);
            throw new RuntimeException("Ledger reserve failed: " + e.getMessage());
        }
    }

    private WebClient getWebClient() {
        return webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }
}
