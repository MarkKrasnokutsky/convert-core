package com.mark.convert.core.messaging.domain.entity;

import com.mark.convert.core.messaging.domain.enumeration.InboxStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "inbox_messages")
@Getter
@Setter
public class InboxMessage {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InboxStatus status;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
