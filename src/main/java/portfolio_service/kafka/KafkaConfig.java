package portfolio_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
@EnableKafka
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${app.kafka.topic:portfolio.position.events}")
    private String positionTopic;

    @Bean
    public NewTopic positionTopic() {
        return TopicBuilder.name(positionTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic positionDeadLetterTopic() {
        return TopicBuilder.name(positionTopic + ".DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> {
                    log.error("Sending record to DLT after retries exhausted. topic={}, partition={}, offset={}, reason={}",
                            record.topic(), record.partition(), record.offset(), ex.getMessage());
                    return new TopicPartition(positionTopic + ".DLT", record.partition());
                });

        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(2);
        backoff.setInitialInterval(300L);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(1000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backoff);
        errorHandler.addNotRetryableExceptions(org.springframework.kafka.support.serializer.DeserializationException.class, SerializationException.class);
        return errorHandler;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, PositionChangedEvent>> kafkaListenerContainerFactory(
            ConsumerFactory<String, PositionChangedEvent> consumerFactory,
            DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, PositionChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.setConcurrency(3);
        return factory;
    }
}
