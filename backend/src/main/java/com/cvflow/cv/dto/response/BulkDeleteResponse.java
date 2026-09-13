package com.cvflow.cv.dto.response;

import java.util.List;
import java.util.UUID;

public record BulkDeleteResponse(List<UUID> deleted, List<FailedItem> failed) {
    public record FailedItem(UUID id, String code) {}
}
