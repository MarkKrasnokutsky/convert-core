package com.mark.convert.core.messaging.scheduler;

import com.mark.convert.core.messaging.service.InboxMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class InboxProcessorScheduler {

    private final InboxMessageService inboxMessageService;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(
            name = "inboxProcessor_processPending",  // уникальное имя лока
            lockAtMostFor = "30s",   // максимальное время удержания лока (на случай падения пода)
            lockAtLeastFor = "5s"    // минимальное время удержания (чтобы другой под не схватил сразу)
    )
    public void process() {
        log.info("InboxProcessor scheduler has started");

        inboxMessageService.processMessages();
    }
}
