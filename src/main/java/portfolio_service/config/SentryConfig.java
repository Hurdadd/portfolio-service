package portfolio_service.config;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "sentry", name = "enabled", havingValue = "true")
public class SentryConfig {

    @Value("${sentry.dsn:}")
    private String dsn;

    @Value("${sentry.environment:local}")
    private String environment;

    @Value("${sentry.release:portfolio-service-local}")
    private String release;

    @Value("${sentry.traces-sample-rate:0.0}")
    private double tracesSampleRate;

    @PostConstruct
    public void init() {
        if (dsn == null || dsn.isBlank()) {
            return;
        }
        Sentry.init(options -> {
            options.setDsn(dsn);
            options.setEnvironment(environment);
            options.setRelease(release);
            options.setTracesSampleRate(tracesSampleRate);
        });
    }

    @PreDestroy
    public void close() {
        Sentry.close();
    }
}
