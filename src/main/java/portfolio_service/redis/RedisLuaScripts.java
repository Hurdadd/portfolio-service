package portfolio_service.redis;

import org.springframework.data.redis.core.script.DefaultRedisScript;

public final class RedisLuaScripts {
    private RedisLuaScripts() {}

    public static final DefaultRedisScript<Long> UNLOCK_COMPARE_AND_DELETE =
            new DefaultRedisScript<>(
                    "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                            "  return redis.call('DEL', KEYS[1]) " +
                            "else " +
                            "  return 0 " +
                            "end",
                    Long.class
            );
}
