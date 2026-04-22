package com.remit.mellonsecure.payout.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Canonical ms-ledger-service integration ({@code X-Ledger-Api-Key}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LedgerClientAdapter implements LedgerClient {

    public static final String HEADER_LEDGER_PUBLIC = "X-Ledger-Api-Key";

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${payout.ledger.enabled:true}")
    private boolean enabled;

    @Value("${payout.ledger.base-url:http://localhost:8083}")
    private String baseUrl;

    @Value("${payout.ledger.public-api-key:}")
    private String publicApiKey;

    @Value("${payout.ledger.timeout-ms:15000}")
    private int timeoutMs;

    @Override
    public boolean isEnabled() {
        return enabled && publicApiKey != null && !publicApiKey.isBlank();
    }

    @Override
    public BigDecimal getBalance(UUID ledgerAccountId) {
        if (!isEnabled()) {
            return BigDecimal.ZERO;
        }
        try {
            String path = "/api/public/ledger/accounts/" + ledgerAccountId + "/balance";
            String body = getWebClient().get().uri(path).retrieve().bodyToMono(String.class)
                    .block(Duration.ofMillis(timeoutMs));
            JsonNode root = objectMapper.readTree(body);
            if (!"SUCCESS".equals(root.path("status").asText())) {
                return BigDecimal.ZERO;
            }
            JsonNode data = root.path("data");
            if (data.isMissingNode()) {
                return BigDecimal.ZERO;
            }
            if (data.has("availableBalance")) {
                return new BigDecimal(data.get("availableBalance").asText());
            }
            if (data.has("ledgerBalance")) {
                return new BigDecimal(data.get("ledgerBalance").asText());
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Ledger getBalance failed: accountId={}", ledgerAccountId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean hasSufficientBalance(UUID ledgerAccountId, BigDecimal amount) {
        if (!isEnabled()) {
            return true;
        }
        return getBalance(ledgerAccountId).compareTo(amount) >= 0;
    }

    @Override
    public void reserveFunds(UUID merchantLedgerAccountId, String paymentReference, BigDecimal amount) {
        if (!isEnabled()) {
            return;
        }
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("accountId", merchantLedgerAccountId.toString());
        req.put("amount", amount);
        req.put("reference", paymentReference);
        postJson("/api/public/ledger/float/reserve", req);
    }

    @Override
    public UUID createPayoutJournal(
            String paymentReference,
            UUID merchantLedgerAccountId,
            UUID settlementLedgerAccountId,
            BigDecimal amount,
            String currency) {
        if (!isEnabled()) {
            return null;
        }
        String ccy = currency != null ? currency.trim().toUpperCase() : "NGN";
        List<Map<String, Object>> entries = List.of(
                line(merchantLedgerAccountId, "DEBIT", amount, ccy),
                line(settlementLedgerAccountId, "CREDIT", amount, ccy)
        );
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("reference", paymentReference);
        req.put("transactionType", "PAYOUT");
        req.put("entries", entries);
        req.put("reservedAccountId", merchantLedgerAccountId.toString());
        req.put("reservedAmount", amount);

        String body = postJsonForBody("/api/public/ledger/journals", req);
        try {
            JsonNode root = objectMapper.readTree(body);
            String id = root.path("data").path("id").asText(null);
            if (id == null || id.isBlank()) {
                throw new IllegalStateException("Ledger createJournal missing data.id: " + body);
            }
            return UUID.fromString(id);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Object> line(UUID accountId, String type, BigDecimal amount, String currency) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("accountId", accountId.toString());
        m.put("type", type);
        m.put("amount", amount);
        m.put("currency", currency);
        return m;
    }

    @Override
    public void postJournal(UUID journalId) {
        if (!isEnabled() || journalId == null) {
            return;
        }
        postJson("/api/public/ledger/journals/" + journalId + "/post", Map.of());
    }

    @Override
    public void reverseJournal(UUID journalId) {
        if (!isEnabled() || journalId == null) {
            return;
        }
        postJson("/api/public/ledger/journals/" + journalId + "/reverse", Map.of());
    }

    private void postJson(String path, Map<String, Object> body) {
        postJsonForBody(path, body);
    }

    private String postJsonForBody(String path, Map<String, Object> body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            String response = getWebClient().post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(timeoutMs));
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                if (!"SUCCESS".equals(root.path("status").asText())) {
                    String err = root.path("error").path("message").asText("Ledger returned ERROR");
                    throw new IllegalStateException(err);
                }
            }
            return response;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ledger POST {} failed", path, e);
            throw new RuntimeException("Ledger request failed: " + e.getMessage(), e);
        }
    }

    private WebClient getWebClient() {
        return webClientBuilder
                .baseUrl(trimSlash(baseUrl))
                .defaultHeader(HEADER_LEDGER_PUBLIC, publicApiKey)
                .build();
    }

    private static String trimSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8083";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
