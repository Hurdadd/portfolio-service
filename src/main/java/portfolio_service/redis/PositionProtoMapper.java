package portfolio_service.redis;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import portfolio_service.dto.PositionResponse;
import portfolio_service.proto.Position;
import portfolio_service.proto.PositionList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class PositionProtoMapper {

    private static final int PRICE_SCALE = 4;

    private PositionProtoMapper() {}

    public static byte[] toBytes(PositionResponse r) {
        if (r == null) return null;

        Position p = Position.newBuilder()
                .setId(safeString(r.getId()))
                .setClientId(nullSafe(r.getClientId()))
                .setSymbol(nullSafe(r.getSymbol()))
                .setQuantity(r.getQuantity())
                .setAvgPriceScaled(toScaled(r.getAvgPrice()))
                .setUpdatedAt(toTimestamp(r.getUpdatedAt()))
                .build();

        return p.toByteArray();
    }

    public static PositionResponse fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            Position p = Position.parseFrom(bytes);

            return PositionResponse.builder()
                    .id(UUID.fromString(p.getId()))
                    .clientId(p.getClientId())
                    .symbol(p.getSymbol())
                    .quantity(p.getQuantity())
                    .avgPrice(fromScaled(p.getAvgPriceScaled()))
                    .updatedAt(fromTimestamp(p.getUpdatedAt()))
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Position proto", e);
        }
    }

    public static byte[] listToBytes(List<PositionResponse> list) {
        if (list == null) return null;

        PositionList.Builder b = PositionList.newBuilder();
        for (PositionResponse r : list) {
            if (r == null) continue;

            b.addItems(Position.newBuilder()
                    .setId(safeString(r.getId()))
                    .setClientId(nullSafe(r.getClientId()))
                    .setSymbol(nullSafe(r.getSymbol()))
                    .setQuantity(r.getQuantity())
                    .setAvgPriceScaled(toScaled(r.getAvgPrice()))
                    .setUpdatedAt(toTimestamp(r.getUpdatedAt()))
                    .build());
        }
        return b.build().toByteArray();
    }

    public static List<PositionResponse> listFromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return List.of();
        try {
            PositionList pl = PositionList.parseFrom(bytes);

            return pl.getItemsList().stream()
                    .map(p -> PositionResponse.builder()
                            .id(UUID.fromString(p.getId()))
                            .clientId(p.getClientId())
                            .symbol(p.getSymbol())
                            .quantity(p.getQuantity())
                            .avgPrice(fromScaled(p.getAvgPriceScaled()))
                            .updatedAt(fromTimestamp(p.getUpdatedAt()))
                            .build())
                    .toList();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse PositionList proto", e);
        }
    }

    private static long toScaled(BigDecimal price) {
        if (price == null) return 0L;
        return price.movePointRight(PRICE_SCALE)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private static BigDecimal fromScaled(long scaled) {
        return BigDecimal.valueOf(scaled, PRICE_SCALE);
    }

    private static Timestamp toTimestamp(Instant i) {
        if (i == null) i = Instant.EPOCH;
        return Timestamps.fromMillis(i.toEpochMilli());
    }

    private static Instant fromTimestamp(Timestamp ts) {
        if (ts == null) return Instant.EPOCH;
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static String safeString(UUID id) {
        return id == null ? "" : id.toString();
    }
}
