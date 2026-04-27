package com.mark.convert.core.messaging.repository;

import com.mark.convert.core.messaging.domain.entity.OutboxMessage;
import com.mark.convert.core.messaging.domain.enumeration.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, String> {
    List<OutboxMessage> findByStatus(OutboxStatus status);
}
