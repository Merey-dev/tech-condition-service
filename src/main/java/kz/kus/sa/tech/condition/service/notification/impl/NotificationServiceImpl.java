package kz.kus.sa.tech.condition.service.notification.impl;

import kz.kus.sa.notification.dto.NotificationDto;
import kz.kus.sa.tech.condition.service.kafka.KafkaProducer;
import kz.kus.sa.tech.condition.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Value("${tech-condition.kafka.topics.notification}")
    private String notificationTopicIn;
    private final KafkaProducer kafkaProducer;

    @Override
    public void send(String to, String message) {
        NotificationDto dto = NotificationDto.builder()
                .to(to).message(message).build();
        kafkaProducer.sendMessage(notificationTopicIn, dto);
    }
}
