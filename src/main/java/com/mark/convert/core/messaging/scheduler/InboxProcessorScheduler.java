package com.mark.convert.core.messaging.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.convert.core.messaging.domain.ConvertRequestMessage;
import com.mark.convert.core.messaging.domain.ConvertResponseMessage;
import com.mark.convert.core.messaging.domain.entity.InboxMessage;
import com.mark.convert.core.messaging.domain.enumeration.EInboxStatus;
import com.mark.convert.core.messaging.producer.ConvertResponseProducer;
import com.mark.convert.core.messaging.repository.InboxMessageRepository;
import com.mark.convert.core.service.IConvertService;
import com.mark.convert.core.service.IFileReadService;
import com.mark.convert.core.service.factory.ConverterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class InboxProcessorScheduler {

    private final InboxMessageRepository inboxMessageRepository;
    private final ObjectMapper objectMapper;
    private final ConverterFactory converterFactory;
    private final IFileReadService fileReadService;
    private final ConvertResponseProducer convertResponseProducer;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void process() {
        log.info("InboxProcessor scheduler has started");
        List<InboxMessage> pendingMessages = inboxMessageRepository.findByStatus(EInboxStatus.PENDING);

        for (InboxMessage inboxMessage : pendingMessages) {
            try {
                ConvertRequestMessage request = objectMapper.readValue(
                        inboxMessage.getPayload(), ConvertRequestMessage.class
                );
                String fullPath = request.getPath() + "/" + request.getFileName() + "." + request.getTypeFormat();

                byte[] file = fileReadService.downloadFileAsBytes(request.getBucketName(), fullPath);

                IConvertService convertService = converterFactory.getConverter(request.getTypeFormat());
                byte[] convertedFile = convertService.convertToPdf(file);

                String pdfPath = request.getPath() + "/" + request.getFileName() + ".pdf";
                fileReadService.uploadBytesAsPdf(request.getBucketName(), pdfPath, convertedFile);

                convertResponseProducer.send(ConvertResponseMessage.builder()
                        .path(pdfPath)
                        .fileName(request.getFileName() + ".pdf")
                        .createdAt(Instant.now())
                        .build());

                inboxMessage.setStatus(EInboxStatus.PROCESSED);
                inboxMessage.setProcessedAt(LocalDateTime.now());
                inboxMessageRepository.save(inboxMessage);

                log.info("Successfully processed: {}", inboxMessage.getId());
            } catch (Exception e) {
                log.error("Failed processing InboxMessage {}", inboxMessage, e);
                inboxMessage.setStatus(EInboxStatus.FAILED);
                inboxMessageRepository.save(inboxMessage);
            }
        }
    }
}
