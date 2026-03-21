package com.techpulse.pdfservice.client;


import java.util.List;

public interface GotenbergClient {

    /**
     * Converts HTML string to PDF via Gotenberg Chromium route.
     * Endpoint: POST /forms/chromium/convert/html
     */
    byte[] convertHtmlToPdf(String htmlContent, String pageSize, String marginTop, String marginBottom, String marginLeft, String marginRight, boolean landscape);

    /**
     * Converts Office file (ppt, xlsx, docx) to PDF via Gotenberg LibreOffice route.
     * Endpoint: POST /forms/libreoffice/convert
     */
    byte[] convertOfficeToPdf(byte[] fileBytes, String fileName);

    /**
     * Merges multiple PDFs into one via Gotenberg PDF engines route.
     * Endpoint: POST /forms/pdfengines/merge
     */
    byte[] mergePdfs(List<byte[]> pdfsBytes, List<String> fileNames);
}

