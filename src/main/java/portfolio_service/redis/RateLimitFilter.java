package portfolio_service.redis;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import portfolio_service.config.TenantContext;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String ENV = "prod";
    private static final int WINDOW_SEC = 10;
    private static final long LIMIT = 5;

    private final RateLimiter rateLimiter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        return !path.startsWith("/positions/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String clientId = req.getHeader("X-Client-Id");
        if (clientId == null || clientId.isBlank()) clientId = "anonymous";

        String tenantNs = "asset:" + TenantContext.get();
        long windowStart = RateLimiter.windowStartSec(WINDOW_SEC);

        String key = RedisKeys.rateLimitKey(ENV, tenantNs, clientId.trim(), "getPosition", windowStart);

        boolean allowed = rateLimiter.allow(key, WINDOW_SEC, LIMIT);

        if (!allowed) {
            res.setStatus(429);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests.\"}");
            return;
        }

        chain.doFilter(req, res);
    }
}
