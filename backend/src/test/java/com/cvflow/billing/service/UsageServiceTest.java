package com.cvflow.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cvflow.ai.model.AiOperation;
import com.cvflow.billing.config.BillingProperties;
import com.cvflow.billing.repository.AiUsageRepository;
import com.cvflow.common.exception.LimitExceededException;
import com.cvflow.common.security.CurrentUserService;
import com.cvflow.cv.repository.MasterCvRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsageServiceTest {

    @Mock
    private AiUsageRepository aiUsageRepository;

    @Mock
    private MasterCvRepository masterCvRepository;

    @Mock
    private CurrentUserService currentUserService;

    private UsageService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        service = new UsageService(aiUsageRepository, masterCvRepository, currentUserService, new BillingProperties(2, 1));
        org.mockito.Mockito.lenient().when(currentUserService.getCurrentUserId()).thenReturn(userId);
    }

    @Test
    void allowsCallsUnderQuota() {
        when(aiUsageRepository.countByUserIdAndCreatedAtAfter(any(), any())).thenReturn(1L);

        service.checkAiQuota(userId);
        service.recordAiUsage(userId, AiOperation.IMPROVE);
    }

    @Test
    void rejectsCallsAtQuota() {
        when(aiUsageRepository.countByUserIdAndCreatedAtAfter(any(), any())).thenReturn(2L);

        assertThrows(LimitExceededException.class, () -> service.checkAiQuota(userId));
    }

    @Test
    void rejectsCvCreationAtQuota() {
        when(masterCvRepository.findAllByUserIdOrderByUpdatedAtDesc(userId))
                .thenReturn(List.of(new com.cvflow.cv.entity.MasterCv()));

        assertThrows(LimitExceededException.class, () -> service.checkCvQuota(userId));
    }

    @Test
    void usageSummarizesPeriod() {
        when(aiUsageRepository.countByUserIdAndCreatedAtAfter(any(), any())).thenReturn(1L);
        when(masterCvRepository.findAllByUserIdOrderByUpdatedAtDesc(userId)).thenReturn(List.of());

        var usage = service.usage();

        assertEquals("FREE", usage.plan());
        assertEquals(1L, usage.aiCallsUsed());
        assertEquals(2, usage.aiCallsLimit());
        assertEquals(0L, usage.cvsUsed());
        assertEquals(1, usage.cvsMax());
        org.junit.jupiter.api.Assertions.assertNotNull(usage.periodStart());
    }
}
