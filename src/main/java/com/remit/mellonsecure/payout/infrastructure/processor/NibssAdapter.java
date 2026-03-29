package com.remit.mellonsecure.payout.infrastructure.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remit.mellonsecure.payout.domain.enums.TransactionStatus;
import com.remit.mellonsecure.payout.domain.model.*;
import com.remit.mellonsecure.payout.domain.port.ProcessorAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NibssAdapter implements ProcessorAdapter {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${payout.nibss.base-url}")
    private String baseUrl;

    @Value("${payout.nibss.timeout:30000}")
    private int timeout;

    @Value("${payout.nibss.name-enquiry-path:/api/nip/nameEnquiry}")
    private String nameEnquiryPath;

    @Value("${payout.nibss.transfer-path:/api/nip/transfer}")
    private String transferPath;

    @Value("${payout.nibss.query-path:/api/nip/transaction/query}")
    private String queryPath;

    @Override
    public NameEnquiryResult performNameEnquiry(String accountNumber, String bankCode) {
        log.info("NIBSS Name Enquiry: accountNumber={}, bankCode={}", mask(accountNumber), bankCode);
        try {
            Map<String, Object> request = Map.of(
                    "accountNumber", accountNumber,
                    "bankCode", bankCode
            );
            String response = getWebClient()
                    .post()
                    .uri(baseUrl + nameEnquiryPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(timeout));

            return parseNameEnquiryResponse(response, accountNumber, bankCode);
        } catch (Exception e) {
            log.error("Name enquiry failed: {}", e.getMessage());
            return NameEnquiryResult.builder()
                    .accountNumber(accountNumber)
                    .bankCode(bankCode)
                    .success(false)
                    .responseCode("99")
                    .responseMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public TransferResult performTransfer(TransferRequest request) {
        log.info("NIBSS Transfer: paymentReference={}", request.getPaymentReference());
        try {
            Map<String, Object> nibssRequest = Map.of(
                    "paymentReference", request.getPaymentReference(),
                    "sessionId", request.getNameEnquiryRef(),
                    "accountNumber", request.getAccountNumber(),
                    "bankCode", request.getBankCode(),
                    "accountName", request.getAccountName(),
                    "amount", request.getAmount().doubleValue(),
                    "narration", request.getNarration() != null ? request.getNarration() : "",
                    "sourceAccount", request.getSourceAccount()
            );

            String response = getWebClient()
                    .post()
                    .uri(baseUrl + transferPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(nibssRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(timeout));

            return parseTransferResponse(response, request.getPaymentReference(), request.getAmount());
        } catch (Exception e) {
            log.error("Transfer failed: paymentReference={}", request.getPaymentReference(), e);
            return TransferResult.builder()
                    .paymentReference(request.getPaymentReference())
                    .success(false)
                    .responseCode("99")
                    .responseMessage(e.getMessage())
                    .amount(request.getAmount())
                    .build();
        }
    }

    @Override
    public TransactionQueryResult queryTransaction(String processorReference) {
        log.info("NIBSS Query: processorReference={}", processorReference);
        try {
            String response = getWebClient()
                    .get()
                    .uri(baseUrl + queryPath + "/" + processorReference)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(timeout));

            return parseQueryResponse(response, processorReference);
        } catch (Exception e) {
            log.error("Query failed: processorReference={}", processorReference, e);
            return TransactionQueryResult.builder()
                    .processorReference(processorReference)
                    .success(false)
                    .responseCode("99")
                    .responseMessage(e.getMessage())
                    .build();
        }
    }

    private WebClient getWebClient() {
        return webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    private NameEnquiryResult parseNameEnquiryResponse(String response, String accountNumber, String bankCode) {
        try {
            JsonNode node = objectMapper.readTree(response);
            String accountName = node.path("accountName").asText("");
            String sessionId = node.path("sessionId").asText("");
            String code = node.path("responseCode").asText("99");
            String message = node.path("responseMessage").asText("");
            boolean success = "00".equals(code);
            return NameEnquiryResult.builder()
                    .accountNumber(accountNumber)
                    .bankCode(bankCode)
                    .accountName(accountName)
                    .sessionId(sessionId)
                    .success(success)
                    .responseCode(code)
                    .responseMessage(message)
                    .build();
        } catch (Exception e) {
            return NameEnquiryResult.builder()
                    .accountNumber(accountNumber)
                    .bankCode(bankCode)
                    .success(false)
                    .responseCode("99")
                    .responseMessage("Parse error: " + e.getMessage())
                    .build();
        }
    }

    private TransferResult parseTransferResponse(String response, String paymentReference, BigDecimal amount) {
        try {
            JsonNode node = objectMapper.readTree(response);
            String processorRef = node.path("processorReference").asText("");
            String code = node.path("responseCode").asText("99");
            String message = node.path("responseMessage").asText("");
            String remarks = node.path("remarks").asText("completed");
            boolean success = "00".equals(code);
            return TransferResult.builder()
                    .paymentReference(paymentReference)
                    .processorReference(processorRef)
                    .success(success)
                    .responseCode(code)
                    .responseMessage(message)
                    .amount(amount)
                    .remarks(remarks)
                    .build();
        } catch (Exception e) {
            return TransferResult.builder()
                    .paymentReference(paymentReference)
                    .success(false)
                    .responseCode("99")
                    .responseMessage("Parse error: " + e.getMessage())
                    .amount(amount)
                    .build();
        }
    }

    private TransactionQueryResult parseQueryResponse(String response, String processorReference) {
        try {
            JsonNode node = objectMapper.readTree(response);
            String statusStr = node.path("status").asText("PENDING");
            String code = node.path("responseCode").asText("99");
            String message = node.path("responseMessage").asText("");
            BigDecimal amount = node.has("amount")
                    ? BigDecimal.valueOf(node.path("amount").asDouble())
                    : BigDecimal.ZERO;
            String remarks = node.path("remarks").asText("completed");
            boolean success = "00".equals(code);
            TransactionStatus status = parseStatus(statusStr);
            return TransactionQueryResult.builder()
                    .processorReference(processorReference)
                    .paymentReference(node.path("paymentReference").asText(""))
                    .status(status)
                    .responseCode(code)
                    .responseMessage(message)
                    .amount(amount)
                    .remarks(remarks)
                    .completedAt(Instant.now())
                    .success(success)
                    .build();
        } catch (Exception e) {
            return TransactionQueryResult.builder()
                    .processorReference(processorReference)
                    .success(false)
                    .responseCode("99")
                    .responseMessage("Parse error: " + e.getMessage())
                    .build();
        }
    }

    private TransactionStatus parseStatus(String status) {
        try {
            return TransactionStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return TransactionStatus.PENDING;
        }
    }

    private String mask(String value) {
        if (value == null || value.length() < 4) return "****";
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }
}
