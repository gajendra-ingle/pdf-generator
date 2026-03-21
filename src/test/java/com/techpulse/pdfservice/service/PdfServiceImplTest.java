package com.techpulse.pdfservice.service;

import com.techpulse.pdfservice.client.GotenbergClient;
import com.techpulse.pdfservice.dto.request.MergePdfByteRequest;
import com.techpulse.pdfservice.dto.request.PdfByteRequest;
import com.techpulse.pdfservice.dto.response.PdfUploadResponse;
import com.techpulse.pdfservice.exception.PdfConversionException;
import com.techpulse.pdfservice.service.impl.PdfServiceImpl;
import com.techpulse.pdfservice.util.MarkdownConverter;
import com.techpulse.pdfservice.util.S3KeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfServiceImplTest {

    @Mock
    GotenbergClient gotenbergClient;
    @Mock
    StorageService storageService;
    @Mock
    MarkdownConverter markdownConverter;
    @Mock
    S3KeyGenerator s3KeyGenerator;

    @InjectMocks
    PdfServiceImpl pdfService;

    private static final byte[] DUMMY_PDF = "%PDF-1.4 dummy".getBytes();
    private static final String DUMMY_URL = "https://s3.amazonaws.com/bucket/pdf/html/uuid.pdf";
    private static final String DUMMY_KEY = "pdf/html/uuid-test.pdf";

   
    @Test
    @DisplayName("convertHtmlToPdf — calls Gotenberg and uploads to S3")
    void convertHtmlToPdf_success() {
        when(gotenbergClient.convertHtmlToPdf(
                any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(DUMMY_PDF);
        when(s3KeyGenerator.generate(anyString(), anyString())).thenReturn(DUMMY_KEY);
        when(storageService.upload(any(), anyString())).thenReturn(DUMMY_URL);

        PdfByteRequest req = buildRequest("<h1>Test</h1>", "invoice.html", "HTML");

        PdfUploadResponse response = pdfService.convertHtmlToPdf(req);

        assertThat(response.getPdfUrl()).isEqualTo(DUMMY_URL);
        assertThat(response.getS3Key()).isEqualTo(DUMMY_KEY);
        assertThat(response.getFileName()).isEqualTo("invoice.pdf");
        assertThat(response.getSizeBytes()).isEqualTo(DUMMY_PDF.length);

        verify(gotenbergClient).convertHtmlToPdf(
                eq("<h1>Test</h1>"), any(), any(), any(), any(), any(), anyBoolean());
        verify(storageService).upload(DUMMY_PDF, DUMMY_KEY);
    }

    @Test
    @DisplayName("convertHtmlToPdf — wraps Gotenberg error as PdfConversionException")
    void convertHtmlToPdf_gotenbergThrows_wrapsException() {
        when(gotenbergClient.convertHtmlToPdf(
                any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenThrow(new RuntimeException("Gotenberg down"));

        PdfByteRequest req = buildRequest("<p>fail</p>", "test.html", "HTML");

        assertThatThrownBy(() -> pdfService.convertHtmlToPdf(req))
                .isInstanceOf(PdfConversionException.class)
                .hasMessageContaining("HTML to PDF failed");
    }

    
    @Test
    @DisplayName("convertMarkdownToPdf — converts md to HTML then calls Gotenberg")
    void convertMarkdownToPdf_success() {
        when(markdownConverter.toHtml(any(), any())).thenReturn("<h1>Hello</h1>");
        when(gotenbergClient.convertHtmlToPdf(
                any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(DUMMY_PDF);
        when(s3KeyGenerator.generate(anyString(), anyString())).thenReturn(DUMMY_KEY);
        when(storageService.upload(any(), anyString())).thenReturn(DUMMY_URL);

        PdfByteRequest req = buildRequest("# Hello", "readme.md", "MARKDOWN");

        PdfUploadResponse response = pdfService.convertMarkdownToPdf(req);

        assertThat(response.getPdfUrl()).isEqualTo(DUMMY_URL);
        verify(markdownConverter).toHtml(eq("# Hello"), isNull());
        verify(gotenbergClient).convertHtmlToPdf(
                eq("<h1>Hello</h1>"), any(), any(), any(), any(), any(), anyBoolean());
    }

  
    @Test
    @DisplayName("convertOfficeToPdf — calls Gotenberg LibreOffice route and uploads to S3")
    void convertOfficeToPdf_success() {
        when(gotenbergClient.convertOfficeToPdf(any(), anyString())).thenReturn(DUMMY_PDF);
        when(s3KeyGenerator.generate(anyString(), anyString())).thenReturn(DUMMY_KEY);
        when(storageService.upload(any(), anyString())).thenReturn(DUMMY_URL);

        PdfByteRequest req = buildRequest("pptcontent", "slides.pptx", "PPT");

        PdfUploadResponse response = pdfService.convertOfficeToPdf(req);

        assertThat(response.getPdfUrl()).isEqualTo(DUMMY_URL);
        verify(gotenbergClient).convertOfficeToPdf(any(), eq("slides.pptx"));
        verify(storageService).upload(DUMMY_PDF, DUMMY_KEY);
    }

    @Test
    @DisplayName("convertOfficeToPdf — wraps error as PdfConversionException")
    void convertOfficeToPdf_throwsWrappedException() {
        when(gotenbergClient.convertOfficeToPdf(any(), anyString()))
                .thenThrow(new RuntimeException("LibreOffice error"));

        PdfByteRequest req = buildRequest("content", "data.xlsx", "EXCEL");

        assertThatThrownBy(() -> pdfService.convertOfficeToPdf(req))
                .isInstanceOf(PdfConversionException.class)
                .hasMessageContaining("Office to PDF failed");
    }

   
    @Test
    @DisplayName("mergePdfs — calls Gotenberg merge route and uploads to S3")
    void mergePdfs_success() {
        when(gotenbergClient.mergePdfs(any(), any())).thenReturn(DUMMY_PDF);
        when(s3KeyGenerator.generate(anyString(), anyString())).thenReturn(DUMMY_KEY);
        when(storageService.upload(any(), anyString())).thenReturn(DUMMY_URL);

        MergePdfByteRequest req = new MergePdfByteRequest();
        req.setFilesBytes(List.of("pdf1".getBytes(), "pdf2".getBytes()));
        req.setFileNames(List.of("a.pdf", "b.pdf"));
        req.setMergedFileName("merged.pdf");

        PdfUploadResponse response = pdfService.mergePdfs(req);

        assertThat(response.getPdfUrl()).isEqualTo(DUMMY_URL);
        verify(gotenbergClient).mergePdfs(
                req.getFilesBytes(), req.getFileNames());
    }

    @Test
    @DisplayName("mergePdfs — wraps error as PdfConversionException")
    void mergePdfs_throwsWrappedException() {
        when(gotenbergClient.mergePdfs(any(), any()))
                .thenThrow(new RuntimeException("Merge failed"));

        MergePdfByteRequest req = new MergePdfByteRequest();
        req.setFilesBytes(List.of("pdf1".getBytes(), "pdf2".getBytes()));
        req.setFileNames(List.of("a.pdf", "b.pdf"));
        req.setMergedFileName("merged.pdf");

        assertThatThrownBy(() -> pdfService.mergePdfs(req))
                .isInstanceOf(PdfConversionException.class)
                .hasMessageContaining("PDF merge failed");
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
