package com.techpulse.pdfservice.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PdfByteRequest {

    @NotNull(message = "File bytes must not be null")
    private byte[] fileBytes;

    @NotBlank(message = "File name must not be blank")
    private String fileName;

    /**
     * Conversion type: HTML, MARKDOWN, PPT, EXCEL, DOCS
     **/
    @NotBlank(message = "Conversion type must not be blank")
    private String type;

    // Optional — used for HTML and Markdown conversions
    private String cssContent;
    private String pageSize = "A4";
    private boolean landscape = false;
}
