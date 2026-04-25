package com.mark.convert.core.service.impl;

import com.mark.convert.core.exception.ConvertException;
import com.mark.convert.core.messaging.domain.FileFormats;
import com.mark.convert.core.service.ConvertService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Component
public class ImageToPdfConverter implements ConvertService {

    @Override
    public byte[] convertToPdf(byte[] fileContent) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ByteArrayInputStream bis = new ByteArrayInputStream(fileContent)) {

            BufferedImage image = ImageIO.read(bis);

            if (image == null) {
                throw new ConvertException("Cannot read image from bytes - unsupported format or corrupted file");
            }

            float imageWidth = image.getWidth();
            float imageHeight = image.getHeight();

            float a4Width = PDRectangle.A4.getWidth();
            float a4Height = PDRectangle.A4.getHeight();

            float scale = 1.0f;
            float pageWidth = imageWidth;
            float pageHeight = imageHeight;

            if (imageWidth > a4Width || imageHeight > a4Height) {
                scale = Math.min(a4Width / imageWidth, a4Height / imageHeight);
                pageWidth = imageWidth * scale;
                pageHeight = imageHeight * scale;
            }

            PDRectangle pageSize = new PDRectangle(pageWidth, pageHeight);
            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            java.io.File tempFile = java.io.File.createTempFile("image_", ".png");
            try {
                ImageIO.write(image, "png", tempFile);
                PDImageXObject pdImage = PDImageXObject.createFromFile(tempFile.getAbsolutePath(), document);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    float x = (pageSize.getWidth() - pageWidth) / 2;
                    float y = (pageSize.getHeight() - pageHeight) / 2;
                    contentStream.drawImage(pdImage, x, y, pageWidth, pageHeight);
                }
            } finally {
                tempFile.deleteOnExit();
            }

            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Failed to convert image to PDF", e);
            throw new ConvertException("Failed to convert image to PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during image to PDF conversion", e);
            throw new ConvertException("Unexpected error during image to PDF conversion", e);
        }
    }

    @Override
    public String getSupportedFormat() {
        return FileFormats.IMAGE;
    }
}
