package com.remit.mellonsecure.payout.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remit.mellonsecure.payout.entity.TransactionStatus;
import com.remit.mellonsecure.payout.entity.*;
import com.remit.mellonsecure.payout.repository.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class NibssAdapter implements ProcessorAdapter {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final IdGenerator idGenerator;

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

    @Value("${payout.nibss.channel-code:1}")
    private int channelCode;

    @Value("${payout.nibss.source-institution-code:000017}")
    private String sourceInstitutionCode;

    @Value("${payout.nibss.originator-account-name:Mellon Nigeria Limited}")
    private String originatorAccountName;

    @Value("${payout.nibss.originator-bank-verification-number:}")
    private String originatorBankVerificationNumber;

    @Value("${payout.nibss.originator-kyc-level:1}")
    private int originatorKycLevel;

    @Value("${payout.nibss.beneficiary-kyc-level:1}")
    private int beneficiaryKycLevel;

    @Value("${payout.nibss.transaction-location:}")
    private String transactionLocation;

    @Value("${payout.nibss.biller-id:}")
    private String billerId;

    @Value("${payout.nibss.mandate-reference-number:}")
    private String mandateReferenceNumber;

    @Override
    public NameEnquiryResult performNameEnquiry(String accountNumber, String bankCode) {
        String endpoint = baseUrl + nameEnquiryPath;
        try {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("accountNumber", accountNumber);
            request.put("channelCode", String.valueOf(channelCode));
            request.put("destinationInstitutionCode", institutionCode(bankCode));
            request.put("transactionId", idGenerator.generateNipTransactionId());
            log.info("NIBSS POST {} request={}", endpoint, requestBodyForLog(request));
            String response = getWebClient()
                    .post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(timeout));

            log.info("NIBSS POST {} response={}", endpoint, response);
            return parseNameEnquiryResponse(response, accountNumber, bankCode);
        } catch (Exception e) {
            log.error("Name enquiry failed: endpoint={} {}", endpoint, e.getMessage());
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
        String endpoint = baseUrl + transferPath;
        try {
            String narration = request.getNarration() != null ? request.getNarration() : "";
            Map<String, Object> nibssRequest = new LinkedHashMap<>();
            nibssRequest.put("sourceInstitutionCode", sourceInstitutionCode);
            nibssRequest.put("amount", request.getAmount().doubleValue());
            nibssRequest.put("beneficiaryAccountName", request.getAccountName());
            nibssRequest.put("beneficiaryAccountNumber", request.getAccountNumber());
            nibssRequest.put("beneficiaryBankVerificationNumber", "");
            nibssRequest.put("beneficiaryKYCLevel", beneficiaryKycLevel);
            nibssRequest.put("channelCode", channelCode);
            nibssRequest.put("originatorAccountName", originatorAccountName);
            nibssRequest.put("originatorAccountNumber", Objects.toString(request.getSourceAccount(), ""));
            nibssRequest.put("originatorBankVerificationNumber", originatorBankVerificationNumber != null ? originatorBankVerificationNumber : "");
            nibssRequest.put("originatorKYCLevel", originatorKycLevel);
            nibssRequest.put("destinationInstitutionCode", institutionCode(request.getBankCode()));
            nibssRequest.put("mandateReferenceNumber", mandateReferenceNumber != null ? mandateReferenceNumber : "");
            nibssRequest.put("nameEnquiryRef", request.getNameEnquiryRef());
            nibssRequest.put("originatorNarration", narration);
            nibssRequest.put("paymentReference", request.getPaymentReference());
            nibssRequest.put("transactionId", request.getTransactionId());
            nibssRequest.put("transactionLocation", transactionLocation != null ? transactionLocation : "");
            nibssRequest.put("beneficiaryNarration", narration);
            if (billerId != null && !billerId.isBlank()) {
                nibssRequest.put("billerId", billerId);
            }

            log.info("NIBSS POST {} request={}", endpoint, requestBodyForLog(nibssRequest));
            String response = getWebClient()
                    .post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(nibssRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(timeout));

            log.info("NIBSS POST {} response={}", endpoint, response);
            return parseTransferResponse(response, request.getPaymentReference(), request.getAmount());
        } catch (Exception e) {
            log.error("Transfer failed: endpoint={} paymentReference={}", endpoint, request.getPaymentReference(), e);
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
        String endpoint = baseUrl + queryPath + "/" + processorReference;
        try {
            log.info("NIBSS GET {} request=(none)", endpoint);
            String response = getWebClient()
                    .get()
                    .uri(endpoint)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(timeout));

            log.info("NIBSS GET {} response={}", endpoint, response);
            return parseQueryResponse(response, processorReference);
        } catch (Exception e) {
            log.error("Query failed: endpoint={} processorReference={}", endpoint, processorReference, e);
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

    /** NIP-style: 6-digit institution code (e.g. bank code 58 → 000058). */
    static String institutionCode(String bankCode) {
        if (bankCode == null || bankCode.isBlank()) {
            return "";
        }
        String digits = bankCode.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return bankCode.trim();
        }
        if (digits.length() > 6) {
            digits = digits.substring(digits.length() - 6);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6 - digits.length(); i++) {
            sb.append('0');
        }
        sb.append(digits);
        return sb.toString();
    }

    private NameEnquiryResult parseNameEnquiryResponse(String response, String accountNumber, String bankCode) {
        try {
            JsonNode node = objectMapper.readTree(response);
            String accountName = jsonText(node, "accountName");
            String sessionId = firstNonEmpty(jsonText(node, "sessionID"), jsonText(node, "sessionId"));
            String code = jsonText(node, "responseCode");
            if (code.isEmpty()) {
                code = "99";
            }
            String message = firstNonEmpty(jsonText(node, "responseMessage"), jsonText(node, "responseDescription"));
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

    private TransferResult parseTransferResponse(String response, String paymentReference, BigDecimal requestAmount) {
        try {
            JsonNode node = objectMapper.readTree(response);
            String code = jsonText(node, "responseCode");
            if (code.isEmpty()) {
                code = "99";
            }
            String message = firstNonEmpty(jsonText(node, "responseMessage"), jsonText(node, "responseDescription"));
            String processorRef = firstNonEmpty(
                    jsonText(node, "transactionId"),
                    jsonText(node, "sessionID"),
                    jsonText(node, "sessionId"),
                    jsonText(node, "paymentReference"));
            String remarks = firstNonEmpty(jsonText(node, "narration"), jsonText(node, "remarks"), "completed");
            BigDecimal amount = requestAmount;
            if (node.has("amount") && !node.get("amount").isNull()) {
                JsonNode a = node.get("amount");
                if (a.isNumber()) {
                    amount = BigDecimal.valueOf(a.asDouble());
                } else if (a.isTextual()) {
                    try {
                        amount = new BigDecimal(a.asText());
                    } catch (NumberFormatException ignored) {
                        // keep requestAmount
                    }
                }
            }
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
                    .amount(requestAmount)
                    .build();
        }
    }

    private TransactionQueryResult parseQueryResponse(String response, String processorReference) {
        try {
            JsonNode node = objectMapper.readTree(response);
            String statusStr = jsonText(node, "status");
            if (statusStr.isEmpty()) {
                statusStr = "PENDING";
            }
            String code = jsonText(node, "responseCode");
            if (code.isEmpty()) {
                code = "99";
            }
            String message = firstNonEmpty(jsonText(node, "responseMessage"), jsonText(node, "responseDescription"));
            BigDecimal amount = BigDecimal.ZERO;
            if (node.has("amount") && !node.get("amount").isNull()) {
                amount = BigDecimal.valueOf(node.path("amount").asDouble());
            }
            String remarks = firstNonEmpty(jsonText(node, "remarks"), jsonText(node, "narration"), "completed");
            boolean success = "00".equals(code);
            TransactionStatus status = parseStatus(statusStr);
            return TransactionQueryResult.builder()
                    .processorReference(processorReference)
                    .paymentReference(jsonText(node, "paymentReference"))
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

    private static String jsonText(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull() || n.isMissingNode()) {
            return "";
        }
        return n.asText("");
    }

    private static String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "";
    }

    private String mask(String value) {
        if (value == null || value.length() < 4) {
            return "****";
        }
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

    private String requestBodyForLog(Map<String, Object> body) {
        try {
            Map<String, Object> copy = new LinkedHashMap<>(body);
            // copy.computeIfPresent("accountNumber", (k, v) -> mask(String.valueOf(v)));
            // copy.computeIfPresent("beneficiaryAccountNumber", (k, v) -> mask(String.valueOf(v)));
            // copy.computeIfPresent("originatorAccountNumber", (k, v) -> mask(String.valueOf(v)));
            // copy.computeIfPresent("nameEnquiryRef", (k, v) -> mask(String.valueOf(v)));
            // copy.computeIfPresent("paymentReference", (k, v) -> mask(String.valueOf(v)));
            // copy.computeIfPresent("transactionId", (k, v) -> mask(String.valueOf(v)));
            // copy.computeIfPresent("beneficiaryAccountName", (k, v) -> mask(String.valueOf(v)));
            // copy.computeIfPresent("originatorBankVerificationNumber", (k, v) -> mask(String.valueOf(v)));
            return objectMapper.writeValueAsString(copy);
        } catch (Exception e) {
            return String.valueOf(body);
        }
    }
}
