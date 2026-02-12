package portfolio_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-Id";
    public static final String TENANT_MDC_KEY = "tenantId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        TenantContext.set(request.getHeader(TENANT_HEADER));
        MDC.put(TENANT_MDC_KEY, TenantContext.get());
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TENANT_MDC_KEY);
            TenantContext.clear();
        }
    }
}
