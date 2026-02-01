package portfolio_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PositionResponse {
    private UUID id;
    private String clientId;
    private String symbol;
    private long quantity;
    private BigDecimal avgPrice;
    private Instant updatedAt;
}

