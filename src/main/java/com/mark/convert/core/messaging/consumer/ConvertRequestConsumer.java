package com.mark.convert.core.messaging.consumer;

import com.mark.convert.core.messaging.service.InboxMessageEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Сервис по обработке событий о запросах на конвертацию файла (принимает запрос от топика конверта и шлёт в другой топик событие об успешной конвертации)
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class ConvertRequestConsumer {

    private final InboxMessageEntityService inboxMessageEntityService;

    @KafkaListener(topics = "convert-request", groupId = "request_consumer")
    public void consume(ConsumerRecord<String, String> record) {
        String messageId = record.topic() + "-" + record.partition() + "-" + record.offset();

        inboxMessageEntityService.save(messageId, record.value());

        log.info("InboxMessage save success {}", messageId);
    }

}
