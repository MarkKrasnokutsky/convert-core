package com.mark.convert.core.messaging.producer;

import com.mark.convert.core.messaging.domain.ConvertResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


/**
 * Сервис по отправке событий о конвертации файла
 **/
@Service
@RequiredArgsConstructor
public class ConvertResponseProducer {

    @Value("${spring.kafka.topic.convert-response.name}")
    private String convertResponseTopic;

    private final KafkaTemplate<String, ConvertResponseMessage> kafkaTemplate;

    public void send(ConvertResponseMessage message) {
        kafkaTemplate.send(convertResponseTopic, message);
    }
}
