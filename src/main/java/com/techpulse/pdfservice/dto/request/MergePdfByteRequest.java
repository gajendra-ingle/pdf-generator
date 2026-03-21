package com.techpulse.pdfservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class MergePdfByteRequest {

    @NotNull(message = "PDF bytes list must not be null")
    @Size(min = 2, message = "At least 2 PDF files are required for merge")
    private List<byte[]> filesBytes;

    @NotNull(message = "File names list must not be null")
    private List<String> fileNames;

    private String mergedFileName = "merged.pdf";
}
