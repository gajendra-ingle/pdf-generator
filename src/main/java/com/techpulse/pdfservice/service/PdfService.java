package com.techpulse.pdfservice.service;

import com.techpulse.pdfservice.dto.request.MergePdfByteRequest;
import com.techpulse.pdfservice.dto.request.PdfByteRequest;
import com.techpulse.pdfservice.dto.response.PdfUploadResponse;

public interface PdfService {

    PdfUploadResponse convertHtmlToPdf(PdfByteRequest request);

    PdfUploadResponse convertMarkdownToPdf(PdfByteRequest request);

    PdfUploadResponse convertOfficeToPdf(PdfByteRequest request);

    PdfUploadResponse mergePdfs(MergePdfByteRequest request);
}
