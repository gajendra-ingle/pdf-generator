package com.techpulse.pdfservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PdfUploadResponse {

    private String pdfUrl; // presigned S3 URL
    private String s3Key; // e.g. pdf/html/uuid-invoice.pdf
    private String fileName; // e.g. invoice.pdf
    private long sizeBytes;
    private String contentType; // application/pdf

}

