package portfolio_service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisBinaryCache {

    private final RedisTemplate<String, byte[]> redis;

    public byte[] get(String key) {
        return redis.opsForValue().get(key);
    }

    public void set(String key, byte[] value, Duration ttl) {
        redis.opsForValue().set(key, value, ttl);
    }

    public void del(String key) {
        redis.delete(key);
    }
}
