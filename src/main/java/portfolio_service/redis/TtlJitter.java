package portfolio_service.redis;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public final class TtlJitter {
    private TtlJitter() {}

    public static Duration withJitter(Duration base, int jitterSeconds) {
        int j = ThreadLocalRandom.current().nextInt(0, Math.max(1, jitterSeconds + 1));
        return base.plusSeconds(j);
    }
}
