package com.mark.convert.core.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.convert.core.messaging.domain.entity.OutboxMessage;
import com.mark.convert.core.messaging.domain.enumeration.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxMessageService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxMessageEntityService outboxMessageEntityService;

    public void processMessages() {
        List<OutboxMessage> pendingMessages = outboxMessageEntityService.findByStatus(OutboxStatus.PENDING);

        for (OutboxMessage outboxMessage : pendingMessages) {
            try {
                kafkaTemplate.send(outboxMessage.getTopic(), outboxMessage.getPayload()).get();

                outboxMessage.setStatus(OutboxStatus.SENT);
                outboxMessage.setSentAt(LocalDateTime.now());
                outboxMessageEntityService.save(outboxMessage);

                log.info("Successfully sent outbox message: {}", outboxMessage.getId());

            } catch (Exception e) {
                log.error("Failed to send outbox message {}", outboxMessage.getId(), e);
                outboxMessage.setStatus(OutboxStatus.FAILED);
                outboxMessageEntityService.save(outboxMessage);
            }
        }
    }
}
