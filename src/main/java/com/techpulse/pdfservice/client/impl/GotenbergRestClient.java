package com.techpulse.pdfservice.client.impl;

import com.techpulse.pdfservice.client.GotenbergClient;
import com.techpulse.pdfservice.config.GotenbergConfig;
import com.techpulse.pdfservice.exception.GotenbergClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GotenbergRestClient implements GotenbergClient {

    private final WebClient webClient;
    private final GotenbergConfig config;

    private static final String HTML_PATH   = "/forms/chromium/convert/html";
    private static final String OFFICE_PATH = "/forms/libreoffice/convert";
    private static final String MERGE_PATH  = "/forms/pdfengines/merge";

    @Override
    public byte[] convertHtmlToPdf(String htmlContent, String pageSize, String marginTop, String marginBottom, String marginLeft, String marginRight, boolean landscape) {
        log.debug("Calling Gotenberg Chromium convert: {}", HTML_PATH);
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("files", new ByteArrayResource(htmlContent.getBytes()) {
            @Override
            public String getFilename() {
                return "index.html";
            }
        }, MediaType.TEXT_HTML);

        builder.part("paperWidth", "A4".equalsIgnoreCase(pageSize) ? "8.27" : "8.5");
        builder.part("paperHeight", "A4".equalsIgnoreCase(pageSize) ? "11.69" : "11");
        builder.part("marginTop", marginTop);
        builder.part("marginBottom", marginBottom);
        builder.part("marginLeft", marginLeft);
        builder.part("marginRight", marginRight);
        builder.part("landscape", String.valueOf(landscape));

        return post(HTML_PATH, builder);
    }

    @Override
    public byte[] convertOfficeToPdf(byte[] fileBytes, String fileName) {
        log.debug("Calling Gotenberg LibreOffice convert for: {}", fileName);
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("files", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        }, MediaType.APPLICATION_OCTET_STREAM);

        return post(OFFICE_PATH, builder);
    }

    @Override
    public byte[] mergePdfs(List<byte[]> pdfsBytes, List<String> fileNames) {
        log.debug("Calling Gotenberg PDF engines merge, count: {}", pdfsBytes.size());
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        for (int i = 0; i < pdfsBytes.size(); i++) {
            final byte[] bytes = pdfsBytes.get(i);
            final String name = fileNames.get(i);
            builder.part("files", new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return name;
                }
            }, MediaType.APPLICATION_PDF);
        }

        return post(MERGE_PATH, builder);
    }

    private byte[] post(String path, MultipartBodyBuilder builder) {
        return webClient.post()
                .uri(config.getBaseUrl() + path)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new GotenbergClientException("Gotenberg 4xx: " + body)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new GotenbergClientException("Gotenberg 5xx: " + body)))
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .block();
    }
}
