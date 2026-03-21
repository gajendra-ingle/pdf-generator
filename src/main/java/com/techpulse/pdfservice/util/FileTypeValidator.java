package com.techpulse.pdfservice.util;

import com.techpulse.pdfservice.exception.PdfConversionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
public class FileTypeValidator {

    /**
     * Validates that the file name has an allowed extension
     * and that the byte array is not null or empty.
     *
     * <p>Usage:
     * <pre>
     *   validator.validate(fileBytes, fileName, "ppt", "pptx");
     *   validator.validate(fileBytes, fileName, "xls", "xlsx");
     *   validator.validate(fileBytes, fileName, "doc", "docx");
     *   validator.validate(fileBytes, fileName, "pdf");
     * </pre>
     *
     * @param fileBytes file content
     * @param fileName original file name including extension
     * @param allowedExtensions one or more allowed extensions (without dot)
     */
    public void validate(byte[] fileBytes, String fileName, String... allowedExtensions) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new PdfConversionException("File bytes are empty or null for: " + fileName);
        }

        if (fileName == null || !fileName.contains(".")) {
            throw new PdfConversionException("Invalid file name — missing extension: " + fileName);
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        boolean valid = Arrays.stream(allowedExtensions)
                .anyMatch(ext -> ext.equalsIgnoreCase(extension));

        if (!valid) {
            throw new PdfConversionException("Unsupported file type: ." + extension + ". Allowed: " + Arrays.toString(allowedExtensions));
        }

        log.debug("File type validated — name: {}, extension: .{}", fileName, extension);
    }
}

