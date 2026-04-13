package com.remit.mellonsecure.payout.service;

import com.remit.mellonsecure.payout.dto.PayoutTransactionListItemDto;
import com.remit.mellonsecure.payout.dto.PayoutTransactionPageDto;
import com.remit.mellonsecure.payout.entity.MerchantEntity;
import com.remit.mellonsecure.payout.entity.PayoutTransactionEntity;
import com.remit.mellonsecure.payout.repository.MerchantJpaRepository;
import com.remit.mellonsecure.payout.repository.PayoutTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayoutTransactionListService {

    private final PayoutTransactionJpaRepository jpaRepository;
    private final MerchantJpaRepository merchantJpaRepository;
    private final PayoutBankNameResolver bankNameResolver;

    @Transactional(readOnly = true)
    public PayoutTransactionPageDto search(
            String merchantCode,
            boolean merchantCodeExact,
            String paymentReference,
            String merchantReference,
            String accountNumber,
            String bankCodeOrName,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        Specification<PayoutTransactionEntity> spec = (root, query, cb) -> cb.conjunction();

        if (merchantCode != null && !merchantCode.isBlank()) {
            String mc = merchantCode.trim();
            spec = spec.and(merchantCodeExact ? merchantCodeEquals(mc) : merchantCodeLike(mc));
        }

        if (paymentReference != null && !paymentReference.isBlank()) {
            String p = "%" + paymentReference.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("paymentReference")), p));
        }

        if (merchantReference != null && !merchantReference.isBlank()) {
            String p = "%" + merchantReference.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("merchantReference")), p));
        }

        if (accountNumber != null && !accountNumber.isBlank()) {
            String p = "%" + accountNumber.trim() + "%";
            spec = spec.and((root, q, cb) -> cb.like(root.get("accountNumber"), p));
        }

        if (bankCodeOrName != null && !bankCodeOrName.isBlank()) {
            String q = bankCodeOrName.trim().toLowerCase();
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("bankCode")), "%" + q + "%"));
        }

        if (status != null && !status.isBlank()) {
            String p = "%" + status.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("status")), p));
        }

        if (fromDate != null) {
            Instant from = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (toDate != null) {
            Instant to = toDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            spec = spec.and((root, q, cb) -> cb.lessThan(root.get("createdAt"), to));
        }

        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PayoutTransactionEntity> result = jpaRepository.findAll(spec, pageable);

        List<String> merchantIds = result.getContent().stream()
                .map(PayoutTransactionEntity::getMerchantId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, MerchantEntity> byId = merchantJpaRepository.findAllById(merchantIds).stream()
                .collect(Collectors.toMap(MerchantEntity::getId, m -> m, (a, b) -> a));

        List<PayoutTransactionListItemDto> content = result.getContent().stream()
                .map(pt -> toDto(pt, byId))
                .toList();

        return new PayoutTransactionPageDto(
                content,
                result.getTotalElements(),
                result.getTotalPages(),
                safePage);
    }

    private PayoutTransactionListItemDto toDto(PayoutTransactionEntity pt, Map<String, MerchantEntity> byId) {
        MerchantEntity m = byId.get(pt.getMerchantId());
        String merchantCode = m != null ? m.getMerchantCode() : pt.getMerchantId();
        String bankName = bankNameResolver.nameForCode(pt.getBankCode());
        return new PayoutTransactionListItemDto(
                merchantCode,
                pt.getPaymentReference(),
                pt.getCreatedAt(),
                pt.getMerchantReference(),
                pt.getAccountNumber(),
                pt.getAmount(),
                bankName,
                pt.getStatus(),
                pt.getProcessorReference(),
                pt.getMerchantPayload(),
                pt.getProcessorResponse(),
                pt.getUpdatedAt());
    }

    private static Specification<PayoutTransactionEntity> merchantCodeEquals(String code) {
        return (root, query, cb) -> {
            Subquery<String> sq = query.subquery(String.class);
            Root<MerchantEntity> mr = sq.from(MerchantEntity.class);
            sq.select(mr.get("id")).where(cb.equal(mr.get("merchantCode"), code));
            return cb.in(root.get("merchantId")).value(sq);
        };
    }

    private static Specification<PayoutTransactionEntity> merchantCodeLike(String pattern) {
        return (root, query, cb) -> {
            Subquery<String> sq = query.subquery(String.class);
            Root<MerchantEntity> mr = sq.from(MerchantEntity.class);
            sq.select(mr.get("id")).where(cb.like(cb.lower(mr.get("merchantCode")), "%" + pattern.toLowerCase() + "%"));
            return cb.in(root.get("merchantId")).value(sq);
        };
    }
}
