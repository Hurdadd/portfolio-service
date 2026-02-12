package portfolio_service.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PositionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PositionEventConsumer.class);

    private final MeterRegistry meterRegistry;

    @KafkaListener(topics = "${app.kafka.topic:portfolio.position.events}", groupId = "${spring.kafka.consumer.group-id:portfolio-service}")
    public void onMessage(@Payload PositionChangedEvent event) {
        log.info("Kafka event consumed: type={}, positionId={}, tenantId={}, traceId={}",
                event.eventType(), event.payload().positionId(), event.tenantId(), event.traceId());

        meterRegistry.counter("portfolio_kafka_events_consumed_total",
                "eventType", event.eventType().name())
                .increment();
    }
}

