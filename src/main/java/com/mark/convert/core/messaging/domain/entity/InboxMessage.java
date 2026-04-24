package com.mark.convert.core.messaging.domain.entity;

import com.mark.convert.core.messaging.domain.enumeration.EInboxStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "inbox_messages")
@Data
public class InboxMessage {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private EInboxStatus status;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
