package com.mark.convert.core.messaging.repository;

import com.mark.convert.core.messaging.domain.entity.InboxMessage;
import com.mark.convert.core.messaging.domain.enumeration.EInboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InboxMessageRepository extends JpaRepository<InboxMessage, String> {
    List<InboxMessage> findByStatus(EInboxStatus status);
}
