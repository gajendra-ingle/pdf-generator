package com.techpulse.pdfservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class S3KeyGeneratorTest {

    private S3KeyGenerator generator;

    @BeforeEach
    void setup() {
        generator = new S3KeyGenerator();
    }

    @Test
    @DisplayName("Key starts with pdf/{type}/ prefix")
    void generate_startsWithCorrectPrefix() {
        String key = generator.generate("html", "invoice.html");
        assertThat(key).startsWith("pdf/html/");
    }

    @Test
    @DisplayName("Key ends with .pdf extension")
    void generate_endsWithPdfExtension() {
        String key = generator.generate("markdown", "readme.md");
        assertThat(key).endsWith(".pdf");
    }

    @Test
    @DisplayName("Key contains sanitized base name")
    void generate_containsSanitizedName() {
        String key = generator.generate("html", "my invoice.html");
        assertThat(key).contains("my-invoice");
    }

    @Test
    @DisplayName("Key strips original extension from file name")
    void generate_stripsOriginalExtension() {
        String key = generator.generate("ppt", "presentation.pptx");
        assertThat(key).doesNotContain(".pptx");
        assertThat(key).endsWith(".pdf");
    }

    @Test
    @DisplayName("Two calls generate unique keys")
    void generate_producesUniqueKeys() {
        String key1 = generator.generate("html", "test.html");
        String key2 = generator.generate("html", "test.html");
        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("Special characters in file name are sanitized")
    void generate_sanitizesSpecialChars() {
        String key = generator.generate("docs", "my doc @2025!.docx");
        assertThat(key).doesNotContainAnyWhitespaces();
        assertThat(key).doesNotContain("@").doesNotContain("!");
    }
}

