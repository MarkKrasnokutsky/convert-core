package com.mark.convert.core.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.convert.core.messaging.domain.ConvertRequestMessage;
import com.mark.convert.core.messaging.domain.ConvertResponseMessage;
import com.mark.convert.core.messaging.domain.entity.InboxMessage;
import com.mark.convert.core.messaging.domain.enumeration.EInboxStatus;
import com.mark.convert.core.messaging.producer.ConvertResponseProducer;
import com.mark.convert.core.messaging.repository.InboxMessageRepository;
import com.mark.convert.core.service.IConvertService;
import com.mark.convert.core.service.IFileReadService;
import com.mark.convert.core.service.factory.ConverterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Сервис по обработке событий о запросах на конвертацию файла (принимает запрос от топика конверта и шлёт в другой топик событие об успешной конвертации)
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class ConvertRequestConsumer {

    private final InboxMessageRepository inboxMessageRepository;

    @KafkaListener(topics = "convert-request", groupId = "request_consumer")
    public void consume(ConsumerRecord<String, String> record) {
        String messageId = record.topic() + "-" + record.partition() + "-" + record.offset();

        if (inboxMessageRepository.existsById(messageId)) {
            log.warn("Duplicate message id: {}", messageId);
            return;
        }

        InboxMessage inboxMessage = new InboxMessage();
        inboxMessage.setId(messageId);
        inboxMessage.setStatus(EInboxStatus.PENDING);
        inboxMessage.setPayload(record.value());
        inboxMessage.setCreatedAt(LocalDateTime.now());

        inboxMessageRepository.save(inboxMessage);
        log.info("InboxMessage save success {}", messageId);
    }

}
