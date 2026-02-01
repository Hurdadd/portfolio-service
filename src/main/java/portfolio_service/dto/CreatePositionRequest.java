package portfolio_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePositionRequest {

    @NotBlank
    @Size(max = 64)
    private String clientId;

    @NotBlank
    @Size(max = 32)
    private String symbol;

    @PositiveOrZero
    private long quantity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal avgPrice;
}
