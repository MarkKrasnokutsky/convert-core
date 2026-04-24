package com.mark.convert.core.service.impl;

import com.mark.convert.core.service.IConvertService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
public class ZipToPdfConverter implements IConvertService {

    private final Map<String, IConvertService> converterMap = new HashMap<>();

    public ZipToPdfConverter(List<IConvertService> converters) {
        for (IConvertService converter : converters) {
            if (converter instanceof ZipToPdfConverter) {
                continue;
            }

            String format = converter.getSupportedFormat();
            converterMap.put(format.toLowerCase(), converter);

            if ("image".equals(format)) {
                converterMap.put("png", converter);
                converterMap.put("jpg", converter);
                converterMap.put("jpeg", converter);
            }
        }
        log.info("ZipConverter initialized with {} supported formats: {}",
                converterMap.size(), converterMap.keySet());
    }

    @Override
    public byte[] convertToPdf(byte[] fileContent) {
        File tempZipFile = null;

        try {
            tempZipFile = File.createTempFile("temp_zip_", ".zip");
            try (FileOutputStream fos = new FileOutputStream(tempZipFile)) {
                fos.write(fileContent);
            }

            PDFMergerUtility merger = new PDFMergerUtility();
            ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
            merger.setDestinationStream(resultStream);

            boolean hasEntries = false;
            int totalPages = 0;

            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(tempZipFile.toPath()))) {
                ZipEntry entry;

                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        hasEntries = true;
                        String fileName = entry.getName();
                        String extension = getFileExtension(fileName);

                        log.info("Processing ZIP entry: {} (extension: {})", fileName, extension);

                        IConvertService converter = converterMap.get(extension.toLowerCase());

                        if (converter != null) {
                            byte[] entryContent = zis.readAllBytes();

                            try {
                                byte[] pdfBytes = converter.convertToPdf(entryContent);

                                merger.addSource(new RandomAccessReadBuffer(pdfBytes));

                                try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
                                    totalPages += doc.getNumberOfPages();
                                    log.info("Queued {} pages from {}", doc.getNumberOfPages(), fileName);
                                }

                            } catch (Exception e) {
                                log.error("Failed to convert entry '{}' to PDF", fileName, e);
                            }
                        } else {
                            log.warn("No converter found for entry: {} (extension: {})", fileName, extension);
                        }
                    }
                    zis.closeEntry();
                }
            }

            if (!hasEntries) {
                throw new RuntimeException("ZIP archive is empty");
            }
            if (totalPages == 0) {
                throw new RuntimeException("No valid files found in ZIP archive for conversion");
            }

            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly().streamCache);

            byte[] result = resultStream.toByteArray();
            log.info("ZIP conversion completed. Total pages: {}", totalPages);
            return result;

        } catch (Exception e) {
            log.error("Failed to convert ZIP to PDF", e);
            throw new RuntimeException("Failed to convert ZIP to PDF: " + e.getMessage(), e);
        } finally {
            if (tempZipFile != null && tempZipFile.exists()) {
                tempZipFile.delete();
            }
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    @Override
    public String getSupportedFormat() {
        return "zip";
    }
}