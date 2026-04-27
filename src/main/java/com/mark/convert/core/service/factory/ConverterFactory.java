package com.mark.convert.core.service.factory;

import com.mark.convert.core.messaging.domain.FileFormats;
import com.mark.convert.core.service.ConvertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConverterFactory {

    private final List<ConvertService> converters;

    public ConvertService getConverter(String format) {
        String normalizedFormat = format.toLowerCase();
        return converters.stream()
                .filter(c -> c.getSupportedFormat().equalsIgnoreCase(normalizedFormat)
                        || isImageFormat(c, normalizedFormat))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("No converter for format: " + format));
    }

    private boolean isImageFormat(ConvertService converter, String format) {
        return FileFormats.IMAGE.equalsIgnoreCase(converter.getSupportedFormat())
                && List.of(FileFormats.PNG, FileFormats.JPG, FileFormats.JPEG).contains(format.toLowerCase());
    }
}
