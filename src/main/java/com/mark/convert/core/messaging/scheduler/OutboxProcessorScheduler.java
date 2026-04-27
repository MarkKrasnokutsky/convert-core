package com.mark.convert.core.messaging.scheduler;

import com.mark.convert.core.messaging.service.OutboxMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxProcessorScheduler {

    private final OutboxMessageService outboxMessageService;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(
            name = "outboxProcessor_processPending",
            lockAtMostFor = "30s",
            lockAtLeastFor = "5s"
    )
    public void process() {
        log.info("OutboxProcessor scheduler has started");
        outboxMessageService.processMessages();
    }
}