package condition.service.kafka;

import kz.kus.commons.utils.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonObjectMapper = new JsonMapper();

    public void sendMessage(String topic, String message) {
        log.debug("Sending message to topic [{}], body=[{}]", topic, message);
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
        future.completable().whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[{}] with offset=[{}]", message, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send message=[{}] due to : {}", message, ex.getMessage());
            }
        });
    }

    public void sendMessage(String topic, Object object) {
        this.sendMessage(topic, jsonObjectMapper.writeAsString(object));
    }
}
