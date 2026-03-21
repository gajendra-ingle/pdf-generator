package com.techpulse.pdfservice.service.impl;

import com.techpulse.pdfservice.client.GotenbergClient;
import com.techpulse.pdfservice.dto.request.MergePdfByteRequest;
import com.techpulse.pdfservice.dto.request.PdfByteRequest;
import com.techpulse.pdfservice.dto.response.PdfUploadResponse;
import com.techpulse.pdfservice.exception.PdfConversionException;
import com.techpulse.pdfservice.service.PdfService;
import com.techpulse.pdfservice.service.StorageService;
import com.techpulse.pdfservice.util.MarkdownConverter;
import com.techpulse.pdfservice.util.S3KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final GotenbergClient gotenbergClient;
    private final StorageService storageService;
    private final MarkdownConverter markdownConverter;
    private final S3KeyGenerator s3KeyGenerator;

    @Override
    public PdfUploadResponse convertHtmlToPdf(PdfByteRequest request) {
        log.info("Converting HTML to PDF: {}", request.getFileName());
        try {
            String html = new String(request.getFileBytes());
            byte[] pdfBytes = gotenbergClient.convertHtmlToPdf(html, request.getPageSize(), "1cm", "1cm", "1cm", "1cm", request.isLandscape());
            return uploadAndBuildResponse(pdfBytes, request.getFileName(), "html");
        } catch (Exception ex) {
            log.error("HTML to PDF conversion failed", ex);
            throw new PdfConversionException("HTML to PDF failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public PdfUploadResponse convertMarkdownToPdf(PdfByteRequest request) {
        log.info("Converting Markdown to PDF: {}", request.getFileName());
        try {
            String markdown = new String(request.getFileBytes());
            String html = markdownConverter.toHtml(markdown, request.getCssContent());
            byte[] pdfBytes = gotenbergClient.convertHtmlToPdf(html, request.getPageSize(), "1cm", "1cm", "1cm", "1cm", request.isLandscape());
            return uploadAndBuildResponse(pdfBytes, request.getFileName(), "markdown");
        } catch (Exception ex) {
            log.error("Markdown to PDF conversion failed", ex);
            throw new PdfConversionException("Markdown to PDF failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public PdfUploadResponse convertOfficeToPdf(PdfByteRequest request) {
        log.info("Converting Office file to PDF: {}", request.getFileName());
        try {
            byte[] pdfBytes = gotenbergClient.convertOfficeToPdf(request.getFileBytes(), request.getFileName());
            return uploadAndBuildResponse(pdfBytes, request.getFileName(), request.getType().toLowerCase());
        } catch (Exception ex) {
            log.error("Office to PDF conversion failed", ex);
            throw new PdfConversionException("Office to PDF failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public PdfUploadResponse mergePdfs(MergePdfByteRequest request) {
        log.info("Merging {} PDF files", request.getFilesBytes().size());
        try {
            byte[] mergedPdf = gotenbergClient.mergePdfs(request.getFilesBytes(), request.getFileNames());
            return uploadAndBuildResponse(mergedPdf, request.getMergedFileName(), "merge");
        } catch (Exception ex) {
            log.error("PDF merge failed", ex);
            throw new PdfConversionException("PDF merge failed: " + ex.getMessage(), ex);
        }
    }


    private PdfUploadResponse uploadAndBuildResponse(byte[] pdfBytes, String originalName, String type) {
        String s3Key = s3KeyGenerator.generate(type, originalName);
        String pdfUrl = storageService.upload(pdfBytes, s3Key);
        String fileName = originalName.replaceAll("\\.[^.]+$", "") + ".pdf";

        return PdfUploadResponse.builder()
                .pdfUrl(pdfUrl)
                .s3Key(s3Key)
                .fileName(fileName)
                .sizeBytes(pdfBytes.length)
                .contentType("application/pdf")
                .build();
    }
}

