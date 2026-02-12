package portfolio_service.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PositionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PositionEventPublisher.class);

    @Value("${app.kafka.topic:portfolio.position.events}")
    private String topic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener
    public void publish(PositionChangedEvent event) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, event.payload().positionId().toString(), event);
        record.headers().add("X-Tenant-Id", event.tenantId().getBytes());
        if (event.traceId() != null) {
            record.headers().add("X-Trace-Id", event.traceId().getBytes());
        }

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish position event to Kafka. eventId={}", event.eventId(), ex);
            } else {
                log.info("Published position event to Kafka. type={}, positionId={}, offset={}",
                        event.eventType(), event.payload().positionId(), result.getRecordMetadata().offset());
            }
        });
    }
}

