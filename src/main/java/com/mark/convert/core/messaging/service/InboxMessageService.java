package com.mark.convert.core.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.convert.core.messaging.domain.ConvertRequestMessage;
import com.mark.convert.core.messaging.domain.ConvertResponseMessage;
import com.mark.convert.core.messaging.domain.entity.InboxMessage;
import com.mark.convert.core.messaging.domain.entity.OutboxMessage;
import com.mark.convert.core.messaging.domain.enumeration.InboxStatus;
import com.mark.convert.core.messaging.domain.enumeration.OutboxStatus;
import com.mark.convert.core.service.ConvertService;
import com.mark.convert.core.service.FileReadService;
import com.mark.convert.core.service.factory.ConverterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxMessageService {

    private final ObjectMapper objectMapper;
    private final FileReadService fileReadService;
    private final ConverterFactory converterFactory;
    private final OutboxMessageEntityService outboxMessageEntityService;
    private final InboxMessageEntityService inboxMessageEntityService;

    public void processMessages() {
        List<InboxMessage> pendingMessages = inboxMessageEntityService.findByStatus(InboxStatus.PENDING);

        for (InboxMessage inboxMessage : pendingMessages) {
            try {
                ConvertRequestMessage request = objectMapper.readValue(
                        inboxMessage.getPayload(), ConvertRequestMessage.class
                );
                String fullPath = request.getPath() + "/" + request.getFileName() + "." + request.getTypeFormat();

                byte[] file = fileReadService.downloadFileAsBytes(request.getBucketName(), fullPath);

                ConvertService convertService = converterFactory.getConverter(request.getTypeFormat());
                byte[] convertedFile = convertService.convertToPdf(file);

                String pdfPath = request.getPath() + "/" + request.getFileName() + ".pdf";
                fileReadService.uploadBytesAsPdf(request.getBucketName(), pdfPath, convertedFile);

                OutboxMessage outboxMessage = new OutboxMessage();
                outboxMessage.setTopic("convert-response");
                outboxMessage.setStatus(OutboxStatus.PENDING);
                outboxMessage.setCreatedAt(LocalDateTime.now());
                outboxMessage.setPayload(objectMapper.writeValueAsString(
                        ConvertResponseMessage.builder()
                                .path(pdfPath)
                                .fileName(request.getFileName() + ".pdf")
                                .createdAt(Instant.now())
                                .build()
                ));
                outboxMessageEntityService.save(outboxMessage);

                inboxMessage.setStatus(InboxStatus.PROCESSED);
                inboxMessage.setProcessedAt(LocalDateTime.now());
                inboxMessageEntityService.save(inboxMessage);

                log.info("Successfully processed: {}", inboxMessage.getId());
            } catch (Exception e) {
                log.error("Failed processing InboxMessage {}", inboxMessage, e);
                inboxMessage.setStatus(InboxStatus.FAILED);
                inboxMessageEntityService.save(inboxMessage);
            }
        }
    }

}
