package com.techpulse.pdfservice.service;

public interface StorageService {

    /**
     * Uploads PDF bytes to cloud storage and returns an accessible URL.
     *
     * @param pdfBytes PDF content as byte array
     * @param s3Key    storage key e.g. "pdf/html/uuid-invoice.pdf"
     * @return presigned or public URL string
     *
     */
    String upload(byte[] pdfBytes, String s3Key);
}
