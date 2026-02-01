package portfolio_service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RateLimiter {

    private static final DefaultRedisScript<Long> FIXED_WINDOW_SCRIPT = new DefaultRedisScript<>(
            "local c=redis.call('INCR', KEYS[1]); " +
                    "if c==1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; " +
                    "return c;",
            Long.class
    );

    private final StringRedisTemplate redis;

    /**
     * @return true if allowed, false if limited
     */
    public boolean allow(String key, int windowSeconds, long limit) {
        Long c = redis.execute(FIXED_WINDOW_SCRIPT, List.of(key), String.valueOf(windowSeconds));
        if (c == null) return true;
        return c <= limit;
    }

    public static long windowStartSec(int windowSeconds) {
        long now = Instant.now().getEpochSecond();
        return (now / windowSeconds) * windowSeconds;
    }
}
