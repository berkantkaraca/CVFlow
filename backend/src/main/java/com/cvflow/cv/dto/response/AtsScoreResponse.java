package com.cvflow.cv.dto.response;

import java.util.List;

public record AtsScoreResponse(int score, List<AtsCheck> checks) {
    public record AtsCheck(String code, String label, boolean passed, String detail) {}
}
