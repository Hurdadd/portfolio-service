package portfolio_service.redis;

import java.time.Duration;

public final class CacheTtl {
    private CacheTtl() {}

    public static Duration positionTtl() { return Duration.ofSeconds(30); }
    public static Duration clientListTtl() { return Duration.ofSeconds(20); }

    public static Duration cacheRebuildLockTtl() { return Duration.ofSeconds(2); }

    public static Duration positionTtlWithJitter() {
        return TtlJitter.withJitter(positionTtl(), 10);
    }
}
