package com.techpulse.pdfservice.controller;

import com.techpulse.pdfservice.dto.request.MergePdfByteRequest;
import com.techpulse.pdfservice.dto.request.PdfByteRequest;
import com.techpulse.pdfservice.dto.response.ApiResponse;
import com.techpulse.pdfservice.dto.response.PdfUploadResponse;
import com.techpulse.pdfservice.service.PdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    @PostMapping("/html")
    public ResponseEntity<ApiResponse<PdfUploadResponse>> htmlToPdf(@RequestBody @Valid PdfByteRequest request) {
        log.info("Received HTML to PDF request: {}", request.getFileName());
        return ResponseEntity.ok(ApiResponse.success(pdfService.convertHtmlToPdf(request)));
    }

    @PostMapping("/markdown")
    public ResponseEntity<ApiResponse<PdfUploadResponse>> markdownToPdf(@RequestBody @Valid PdfByteRequest request) {
        log.info("Received Markdown to PDF request: {}", request.getFileName());
        return ResponseEntity.ok(ApiResponse.success(pdfService.convertMarkdownToPdf(request)));
    }

    @PostMapping("/ppt")
    public ResponseEntity<ApiResponse<PdfUploadResponse>> pptToPdf(@RequestBody @Valid PdfByteRequest request) {
        log.info("Received PPT to PDF request: {}", request.getFileName());
        return ResponseEntity.ok(ApiResponse.success(pdfService.convertOfficeToPdf(request)));
    }

    @PostMapping("/excel")
    public ResponseEntity<ApiResponse<PdfUploadResponse>> excelToPdf(@RequestBody @Valid PdfByteRequest request) {
        log.info("Received Excel to PDF request: {}", request.getFileName());
        return ResponseEntity.ok(ApiResponse.success(pdfService.convertOfficeToPdf(request)));
    }

    @PostMapping("/docs")
    public ResponseEntity<ApiResponse<PdfUploadResponse>> docsToPdf(@RequestBody @Valid PdfByteRequest request) {
        log.info("Received Docs to PDF request: {}", request.getFileName());
        return ResponseEntity.ok(ApiResponse.success(pdfService.convertOfficeToPdf(request)));
    }

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<PdfUploadResponse>> mergePdfs(@RequestBody @Valid MergePdfByteRequest request) {
        log.info("Received Merge PDF request, count: {}", request.getFilesBytes().size());
        return ResponseEntity.ok(ApiResponse.success(pdfService.mergePdfs(request)));
    }
}

