package portfolio_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "portfolio_position",
        indexes = {
                @Index(name = "idx_client_symbol", columnList = "clientId,symbol")
        })
public class PortfolioPosition {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String clientId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false)
    private long quantity;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal avgPrice;

    @Column(nullable = false)
    private Instant updatedAt;

    public PortfolioPosition() {}

    public PortfolioPosition(UUID id, String clientId, String symbol, long quantity, BigDecimal avgPrice, Instant updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.updatedAt = updatedAt;
    }

    @PrePersist @PreUpdate
    void touch() { this.updatedAt = Instant.now(); }


}
