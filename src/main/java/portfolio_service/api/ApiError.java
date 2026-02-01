package portfolio_service.api;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ApiError(
        String code,
        String message,
        String traceId,
        Instant timestamp,
        List<FieldViolation> details
) {
    @Builder
    public record FieldViolation(String field, String issue) {}
}
