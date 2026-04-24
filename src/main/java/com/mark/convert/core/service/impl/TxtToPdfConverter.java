package com.mark.convert.core.service.impl;

import com.mark.convert.core.service.IConvertService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

@Component
public class TxtToPdfConverter implements IConvertService {

    @Override
    public byte[] convertToPdf(byte[] fileContent) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            String text = new String(fileContent, StandardCharsets.UTF_8);

            float margin = 50;
            float fontSize = 12;
            float leading = fontSize * 1.5f;
            PDRectangle pageSize = PDRectangle.LETTER;
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();

            PDPage currentPage = new PDPage(pageSize);
            document.addPage(currentPage);

            PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.COURIER);
            contentStream.setFont(font, fontSize);
            contentStream.beginText();

            float yPosition = pageHeight - margin;
            contentStream.newLineAtOffset(margin, yPosition);

            int maxChars = (int) ((pageWidth - margin * 2) / (fontSize * 0.5));

            try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() > maxChars) {
                        line = line.substring(0, maxChars - 3) + "...";
                    }

                    if (yPosition - leading < margin) {
                        contentStream.endText();
                        contentStream.close();

                        currentPage = new PDPage(pageSize);
                        document.addPage(currentPage);
                        contentStream = new PDPageContentStream(document, currentPage);
                        contentStream.setFont(font, fontSize);
                        contentStream.beginText();
                        yPosition = pageHeight - margin; // сброс позиции
                        contentStream.newLineAtOffset(margin, yPosition);
                    }

                    contentStream.showText(line);
                    yPosition -= leading;
                    contentStream.newLineAtOffset(0, -leading);
                }
            }

            contentStream.endText();
            contentStream.close();

            document.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert TXT to PDF", e);
        }
    }

    @Override
    public String getSupportedFormat() {
        return "txt";
    }
}
