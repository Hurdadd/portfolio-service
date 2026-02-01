package portfolio_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisBinaryConfig {

    @Bean
    public RedisTemplate<String, byte[]> redisBytesTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, byte[]> t = new RedisTemplate<>();
        t.setConnectionFactory(cf);

        t.setKeySerializer(RedisSerializer.string());
        t.setValueSerializer(RedisSerializer.byteArray());

        t.setHashKeySerializer(RedisSerializer.string());
        t.setHashValueSerializer(RedisSerializer.byteArray());

        t.afterPropertiesSet();
        return t;
    }
}
