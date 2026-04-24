package com.mark.convert.core.service.factory;

import com.mark.convert.core.service.IConvertService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ConverterFactory {

    private final Map<String, IConvertService> converters = new ConcurrentHashMap<>();
    private final List<IConvertService> converterList;

    @PostConstruct
    public void init() {
        for (IConvertService converter : converterList) {
            String format = converter.getSupportedFormat();
            converters.put(format.toLowerCase(), converter);

            if ("image".equals(format)) {
                converters.put("png", converter);
                converters.put("jpg", converter);
                converters.put("jpeg", converter);
            }
        }
    }

    public IConvertService getConverter(String format) {
        String normalizedFormat = format.toLowerCase();
        IConvertService converter = converters.get(normalizedFormat);
        if (converter == null) {
            throw new UnsupportedOperationException("No converter for format: " + format);
        }
        return converter;
    }

    public boolean supportsFormat(String format) {
        return converters.containsKey(format.toLowerCase());
    }
}
