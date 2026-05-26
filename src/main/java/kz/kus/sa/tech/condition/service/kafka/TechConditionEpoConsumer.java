package kz.kus.sa.tech.condition.service.kafka;

import kz.kus.commons.utils.JsonMapper;
import kz.kus.sa.registry.dto.tc.epo.TechConditionEpoStatementDto;
import kz.kus.sa.tech.condition.service.epo.TechConditionEpoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TechConditionEpoConsumer {

    private final TechConditionEpoService service;
    private final JsonMapper jsonObjectMapper = new JsonMapper();

    @KafkaListener(topics = "${tech-condition.kafka.topics.tech-condition-epo.sa-in}")
    public void consumeEpo(@Payload String message,
                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                           @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partitionId,
                           @Header(KafkaHeaders.OFFSET) int offset) {
        log.debug("Message: {}", message);
        log.info("Received message from topic = {}, partition id = {}, offset = {}",  topic, partitionId, offset);
        try {
            TechConditionEpoStatementDto dto = jsonObjectMapper.parse(message, TechConditionEpoStatementDto.class);
            service.consume(dto);
        } catch (Exception e) {
            log.error("Error while consuming message from topic = {}, partition id = {}, offset = {}",  topic, partitionId, offset, e);
        }
        log.info("Message consumed from topic = {}, partition id = {}, offset = {}",  topic, partitionId, offset);
    }
}
