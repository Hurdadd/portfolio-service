package portfolio_service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisLock {

    private final StringRedisTemplate redis;

    public String tryLock(String lockKey, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent(lockKey, token, ttl);
        return Boolean.TRUE.equals(ok) ? token : null;
    }

    public boolean unlock(String lockKey, String token) {
        Long r = redis.execute(RedisLuaScripts.UNLOCK_COMPARE_AND_DELETE, List.of(lockKey), token);
        return r != null && r == 1L;
    }
}
