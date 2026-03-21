package com.techpulse.pdfservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techpulse.pdfservice.dto.request.MergePdfByteRequest;
import com.techpulse.pdfservice.dto.request.PdfByteRequest;
import com.techpulse.pdfservice.dto.response.PdfUploadResponse;
import com.techpulse.pdfservice.service.PdfService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PdfController.class)
class PdfControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    PdfService pdfService;

    private static final PdfUploadResponse DUMMY_RESPONSE = PdfUploadResponse.builder()
            .pdfUrl("https://s3.amazonaws.com/bucket/pdf/html/uuid-test.pdf")
            .s3Key("pdf/html/uuid-test.pdf")
            .fileName("test.pdf")
            .sizeBytes(1024)
            .contentType("application/pdf")
            .build();


    @Test
    @DisplayName("POST /html returns 200 with pdfUrl")
    void htmlToPdf_success() throws Exception {
        when(pdfService.convertHtmlToPdf(any())).thenReturn(DUMMY_RESPONSE);

        PdfByteRequest req = buildRequest("<h1>Hello</h1>", "invoice.html", "HTML");

        mockMvc.perform(post("/api/v1/pdf/html")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pdfUrl").exists())
                .andExpect(jsonPath("$.data.fileName").value("test.pdf"));
    }

    @Test
    @DisplayName("POST /html with null fileBytes returns 400")
    void htmlToPdf_nullBytes_returns400() throws Exception {
        PdfByteRequest req = new PdfByteRequest();
        req.setFileName("test.html");
        req.setType("HTML");
        // fileBytes intentionally null

        mockMvc.perform(post("/api/v1/pdf/html")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /markdown returns 200 with pdfUrl")
    void markdownToPdf_success() throws Exception {
        when(pdfService.convertMarkdownToPdf(any())).thenReturn(DUMMY_RESPONSE);

        PdfByteRequest req = buildRequest("# Hello World", "readme.md", "MARKDOWN");

        mockMvc.perform(post("/api/v1/pdf/markdown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pdfUrl").exists());
    }

    @Test
    @DisplayName("POST /ppt returns 200 with pdfUrl")
    void pptToPdf_success() throws Exception {
        when(pdfService.convertOfficeToPdf(any())).thenReturn(DUMMY_RESPONSE);

        PdfByteRequest req = buildRequest("pptbytes", "slides.pptx", "PPT");

        mockMvc.perform(post("/api/v1/pdf/ppt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /excel returns 200 with pdfUrl")
    void excelToPdf_success() throws Exception {
        when(pdfService.convertOfficeToPdf(any())).thenReturn(DUMMY_RESPONSE);

        PdfByteRequest req = buildRequest("xlsbytes", "data.xlsx", "EXCEL");

        mockMvc.perform(post("/api/v1/pdf/excel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /docs returns 200 with pdfUrl")
    void docsToPdf_success() throws Exception {
        when(pdfService.convertOfficeToPdf(any())).thenReturn(DUMMY_RESPONSE);

        PdfByteRequest req = buildRequest("docbytes", "contract.docx", "DOCS");

        mockMvc.perform(post("/api/v1/pdf/docs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /merge returns 200 with pdfUrl")
    void mergePdfs_success() throws Exception {
        when(pdfService.mergePdfs(any())).thenReturn(DUMMY_RESPONSE);

        MergePdfByteRequest req = new MergePdfByteRequest();
        req.setFilesBytes(List.of("pdf1".getBytes(), "pdf2".getBytes()));
        req.setFileNames(List.of("a.pdf", "b.pdf"));
        req.setMergedFileName("merged.pdf");

        mockMvc.perform(post("/api/v1/pdf/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pdfUrl").exists());
    }

    @Test
    @DisplayName("POST /merge with single file returns 400")
    void mergePdfs_singleFile_returns400() throws Exception {
        MergePdfByteRequest req = new MergePdfByteRequest();
        req.setFilesBytes(List.of("pdf1".getBytes()));
        req.setFileNames(List.of("a.pdf"));

        mockMvc.perform(post("/api/v1/pdf/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // Helper
    private PdfByteRequest buildRequest(String content, String fileName, String type) {
        PdfByteRequest req = new PdfByteRequest();
        req.setFileBytes(content.getBytes());
        req.setFileName(fileName);
        req.setType(type);
        return req;
    }
}
