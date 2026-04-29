package com.mark.convert.core.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.convert.core.messaging.domain.ConvertRequestMessage;
import com.mark.convert.core.messaging.domain.ConvertResponseMessage;
import com.mark.convert.core.messaging.domain.FileFormats;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void processMessages() {
        List<InboxMessage> pendingMessages = inboxMessageEntityService.findByStatus(InboxStatus.PENDING);

        for (InboxMessage inboxMessage : pendingMessages) {
            try {
                String json = objectMapper.readValue(inboxMessage.getPayload(), String.class); // сначала разворачиваем строку

                ConvertRequestMessage request = objectMapper.readValue(json, ConvertRequestMessage.class); // потом парсим объект

                String fullPath = request.getPath();

                byte[] file = fileReadService.downloadFileAsBytes(request.getBucketName(), fullPath);

                String format = normalizeFormat(request.getTypeFormat());
                ConvertService convertService = converterFactory.getConverter(format);
                byte[] convertedFile = convertService.convertToPdf(file);

                String pdfPath = removeExtension(request.getPath()) + ".pdf";
                boolean flag = fileReadService.uploadBytesAsPdf(request.getBucketName(), pdfPath, convertedFile);

                String pdfFileName = removeExtension(request.getFileName()) + ".pdf";

                OutboxMessage outboxMessage = new OutboxMessage();
                outboxMessage.setTopic("convert-response");
                outboxMessage.setStatus(OutboxStatus.PENDING);
                outboxMessage.setCreatedAt(LocalDateTime.now());
                outboxMessage.setPayload(objectMapper.writeValueAsString(
                        ConvertResponseMessage.builder()
                                .path(pdfPath)
                                .bucketName(request.getBucketName())
                                .fileName(pdfFileName)
                                .createdAt(Instant.now())
                                .status(flag ? "SUCCESS" : "FAILURE")
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

    private static String removeExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        int lastSeparator = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

        if (lastDot > lastSeparator && lastDot > 0) {
            return filePath.substring(0, lastDot);
        }
        return filePath;
    }

    private String normalizeFormat(String typeFormat) {
        return switch (typeFormat) {
            case "application/zip" -> FileFormats.ZIP;
            case "text/plain"      -> FileFormats.TXT;
            case "image/png"       -> FileFormats.PNG;
            case "image/jpg",
                 "image/jpeg"      -> FileFormats.JPEG;
            default -> typeFormat;
        };
    }

}
