package com.mark.convert.core.messaging.service;

import com.mark.convert.core.messaging.domain.entity.InboxMessage;
import com.mark.convert.core.messaging.domain.enumeration.InboxStatus;
import com.mark.convert.core.messaging.repository.InboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxMessageEntityService {

    private final InboxMessageRepository inboxMessageRepository;

    public void save(String messageId, String valueRecord) {
        if (inboxMessageRepository.existsById(messageId)) {
            log.warn("Duplicate message id: {}", messageId);
            return;
        }

        InboxMessage inboxMessage = new InboxMessage();
        inboxMessage.setId(messageId);
        inboxMessage.setStatus(InboxStatus.PENDING);
        inboxMessage.setPayload(valueRecord);
        inboxMessage.setCreatedAt(LocalDateTime.now());

        inboxMessageRepository.save(inboxMessage);
    }

    public void save(InboxMessage inboxMessage) {
        inboxMessageRepository.save(inboxMessage);
    }

    public List<InboxMessage> findByStatus(InboxStatus status) {
        return inboxMessageRepository.findByStatus(status);
    }

}
