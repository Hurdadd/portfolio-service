package portfolio_service.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PositionChangedEvent(
        UUID eventId,
        PositionEventType eventType,
        Instant occurredAt,
        String tenantId,
        String traceId,
        Payload payload
) {
    public record Payload(
            UUID positionId,
            String clientId,
            String symbol,
            long quantity,
            BigDecimal avgPrice
    ) {}
}

