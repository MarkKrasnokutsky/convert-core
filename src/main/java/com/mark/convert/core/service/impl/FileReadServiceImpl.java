package com.mark.convert.core.service.impl;

import com.mark.convert.core.config.MinioConfig;
import com.mark.convert.core.exception.ManageFileException;
import com.mark.convert.core.service.FileReadService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileReadServiceImpl implements FileReadService {

    private final MinioConfig minioConfig;

    @Override
    public byte[] downloadFileAsBytes(String bucketName, String objectName) {
        MinioClient minioClient = minioConfig.createClient();
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            byte[] bytes = stream.readAllBytes();
            log.info("Файл {} загружен как поток.", objectName);
            return bytes;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ManageFileException("Failed download file: " + e.getMessage(), e);
        }
    }

    @Override
    public void uploadBytesAsPdf(String bucketName, String filePath, byte[] pdfBytes) {
        MinioClient minioClient = minioConfig.createClient();
        try {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filePath)
                                .stream(inputStream, pdfBytes.length, -1)
                                .contentType("application/pdf")
                                .build()
                );
                log.info("Uploaded PDF of {} bytes to {}", pdfBytes.length, filePath);
            }
        } catch (Exception e) {
            throw new ManageFileException("Failed to upload PDF: " + filePath, e);
        }
    }

}
