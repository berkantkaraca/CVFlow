package com.cvflow.billing.service;

import com.cvflow.ai.model.AiOperation;
import com.cvflow.billing.config.BillingProperties;
import com.cvflow.billing.dto.UsageResponse;
import com.cvflow.billing.entity.AiUsage;
import com.cvflow.billing.repository.AiUsageRepository;
import com.cvflow.common.exception.LimitExceededException;
import com.cvflow.common.security.CurrentUserService;
import com.cvflow.cv.repository.MasterCvRepository;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsageService {
    private static final String PLAN = "FREE";

    private final AiUsageRepository aiUsageRepository;
    private final MasterCvRepository masterCvRepository;
    private final CurrentUserService currentUserService;
    private final BillingProperties billingProperties;

    public void checkAiQuota(UUID userId) {
        long used = aiUsageRepository.countByUserIdAndCreatedAtAfter(userId, monthStart());
        if (used >= billingProperties.freeAiCallsMonthly()) {
            throw new LimitExceededException("Monthly AI usage limit exceeded");
        }
    }

    public void recordAiUsage(UUID userId, AiOperation operation) {
        AiUsage usage = new AiUsage();
        usage.setUserId(userId);
        usage.setOperation(operation);
        aiUsageRepository.save(usage);
    }

    public void checkCvQuota(UUID userId) {
        if (masterCvRepository.countByUserId(userId) >= billingProperties.freeCvsMax()) {
            throw new LimitExceededException("CV limit exceeded for the free plan");
        }
    }

    @Transactional(readOnly = true)
    public UsageResponse usage() {
        UUID userId = currentUserService.getCurrentUserId();
        return new UsageResponse(
                PLAN,
                aiUsageRepository.countByUserIdAndCreatedAtAfter(userId, monthStart()),
                billingProperties.freeAiCallsMonthly(),
                masterCvRepository.countByUserId(userId),
                billingProperties.freeCvsMax(),
                monthStart().toString());
    }

    private Instant monthStart() {
        return YearMonth.now(ZoneOffset.UTC).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
