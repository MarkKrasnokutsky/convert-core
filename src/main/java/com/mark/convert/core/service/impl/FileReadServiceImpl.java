package com.mark.convert.core.service.impl;

import com.mark.convert.core.config.MinioConfig;
import com.mark.convert.core.service.IFileReadService;
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
public class FileReadServiceImpl implements IFileReadService {

    private final MinioClient minioClient = MinioConfig.createClient();

    @Override
    public byte[] downloadFileAsBytes(String bucketName, String objectName) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            byte[] bytes = stream.readAllBytes();
            System.out.println("Файл '" + objectName + "' загружен как поток.");
            return bytes;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка загрузки файла: " + e.getMessage(), e);
        }
    }

    @Override
    public void uploadBytesAsPdf(String bucketName, String filePath, byte[] pdfBytes) {
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
            throw new RuntimeException("Failed to upload PDF: " + filePath, e);
        }
    }

}
