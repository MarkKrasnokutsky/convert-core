package com.mark.convert.core.messaging.service;

import com.mark.convert.core.messaging.domain.entity.OutboxMessage;
import com.mark.convert.core.messaging.domain.enumeration.OutboxStatus;
import com.mark.convert.core.messaging.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxMessageEntityService {

    private final OutboxMessageRepository outboxMessageRepository;

    public List<OutboxMessage> findByStatus(OutboxStatus status) {
        return outboxMessageRepository.findByStatus(status);
    }

    public void save(OutboxMessage outboxMessage) {
        outboxMessageRepository.save(outboxMessage);
    }
}
