package com.techpulse.pdfservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownConverterTest {

    private MarkdownConverter converter;

    @BeforeEach
    void setup() {
        converter = new MarkdownConverter();
    }

    @Test
    @DisplayName("Converts heading to h1 HTML tag")
    void toHtml_heading_convertsCorrectly() {
        String result = converter.toHtml("# Hello World", null);
        assertThat(result).contains("<h1>Hello World</h1>");
    }

    @Test
    @DisplayName("Returns a full HTML document structure")
    void toHtml_returnsFullDocument() {
        String result = converter.toHtml("# Test", null);
        assertThat(result).contains("<!DOCTYPE html>").contains("<html>").contains("<head>").contains("<body>").contains("</html>");
    }

    @Test
    @DisplayName("Injects custom CSS when provided")
    void toHtml_withCustomCss_injectsIt() {
        String customCss = "body { color: red; }";
        String result = converter.toHtml("# Test", customCss);
        assertThat(result).contains(customCss);
    }

    @Test
    @DisplayName("Uses default CSS when cssContent is null")
    void toHtml_withNullCss_usesDefault() {
        String result = converter.toHtml("# Test", null);
        assertThat(result).contains("font-family");
    }

    @Test
    @DisplayName("Converts bold markdown to strong tag")
    void toHtml_bold_convertsCorrectly() {
        String result = converter.toHtml("**bold text**", null);
        assertThat(result).contains("<strong>bold text</strong>");
    }

    @Test
    @DisplayName("Converts code block markdown correctly")
    void toHtml_codeBlock_convertsCorrectly() {
        String result = converter.toHtml("```\ncode here\n```", null);
        assertThat(result).contains("<code>");
    }

    @Test
    @DisplayName("Converts list markdown to ul/li tags")
    void toHtml_list_convertsCorrectly() {
        String result = converter.toHtml("- item one\n- item two", null);
        assertThat(result).contains("<ul>").contains("<li>");
    }
}

