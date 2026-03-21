package com.techpulse.pdfservice.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class S3KeyGenerator {

    /**
     * Generates a unique, sanitized S3 key for a PDF file.
     *
     * <p>Pattern: {@code pdf/{type}/{uuid}-{sanitized-name}.pdf}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code pdf/html/3f2a1c9d-invoice.pdf}</li>
     *   <li>{@code pdf/markdown/8b4e2f1a-readme.pdf}</li>
     *   <li>{@code pdf/ppt/c1d2e3f4-q3-deck.pdf}</li>
     *   <li>{@code pdf/merge/a9b8c7d6-final-report.pdf}</li>
     * </ul>
     *
     * @param type conversion type — html, markdown, ppt, excel, docs, merge
     * @param originalFileName original file name including extension
     * @return unique S3 key string
     */
    public String generate(String type, String originalFileName) {
        String baseName = originalFileName
                .replaceAll("\\.[^.]+$", "")
                .replaceAll("[^a-zA-Z0-9_-]", "-")
                .toLowerCase()
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        return "pdf/" + type.toLowerCase() + "/" + UUID.randomUUID() + "-" + baseName + ".pdf";
    }
}
