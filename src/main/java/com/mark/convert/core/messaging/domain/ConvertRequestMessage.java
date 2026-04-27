package com.mark.convert.core.messaging.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertRequestMessage {

    private String path;
    private String bucketName;
    private String fileName;
    private String typeFormat;

}
