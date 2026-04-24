package com.mark.convert.core.config;

import io.minio.MinioClient;

public class MinioConfig {

    public static MinioClient createClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minioadmin", "minioadmin123")
                .build();
    }

}
