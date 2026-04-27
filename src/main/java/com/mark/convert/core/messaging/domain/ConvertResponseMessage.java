package com.mark.convert.core.messaging.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertResponseMessage {

    private String path;
    private String bucketName;
    private String fileName;
    private Instant createdAt;

}
