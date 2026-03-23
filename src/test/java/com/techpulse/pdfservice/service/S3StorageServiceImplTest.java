package com.techpulse.pdfservice.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.techpulse.pdfservice.config.*;
import com.techpulse.pdfservice.exception.*;
import  com.techpulse.pdfservice.service.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceImplTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private S3Config s3Config;

    @InjectMocks
    private S3StorageServiceImpl storageService;

    private final String bucketName = "test-bucket";
    private final String key = "test/file.pdf";
    private final byte[] pdfBytes = "dummy pdf content".getBytes();

    @BeforeEach
    void setup() {
        when(s3Config.getBucketName()).thenReturn(bucketName);
        when(s3Config.getPresignExpirationMinutes()).thenReturn(10L);
    }

    @Test
    void upload_shouldUploadAndReturnPresignedUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("http://mock-s3-url.com/file.pdf");

        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(mockUrl);

        // Act
        String result = storageService.upload(pdfBytes, key);

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl.toString(), result);

        // Verify putObject called
        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3).putObject(putCaptor.capture());

        PutObjectRequest capturedRequest = putCaptor.getValue();
        assertEquals(bucketName, capturedRequest.getBucketName());
        assertEquals(key, capturedRequest.getKey());

        ObjectMetadata metadata = capturedRequest.getMetadata();
        assertEquals("application/pdf", metadata.getContentType());
        assertEquals(pdfBytes.length, metadata.getContentLength());
        assertEquals("pdf-generator", metadata.getUserMetaDataOf("generated-by"));

        // Verify presigned URL generation
        ArgumentCaptor<GeneratePresignedUrlRequest> urlCaptor =
                ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);

        verify(amazonS3).generatePresignedUrl(urlCaptor.capture());

        GeneratePresignedUrlRequest urlRequest = urlCaptor.getValue();
        assertEquals(bucketName, urlRequest.getBucketName());
        assertEquals(key, urlRequest.getKey());
        assertEquals(HttpMethod.GET, urlRequest.getMethod());
        assertTrue(urlRequest.getExpiration().after(new Date()));
    }

    @Test
    void upload_shouldThrowStorageException_whenS3Fails() {
        // Arrange
        doThrow(new RuntimeException("S3 error"))
                .when(amazonS3).putObject(any(PutObjectRequest.class));

        // Act & Assert
        StorageException exception = assertThrows(
                StorageException.class,
                () -> storageService.upload(pdfBytes, key)
        );

        assertTrue(exception.getMessage().contains("Failed to upload PDF to S3"));
        verify(amazonS3, never()).generatePresignedUrl(any());
    }

    @Test
    void upload_shouldStillFail_ifPresignedUrlFails() throws Exception {
        // Arrange
        doNothing().when(amazonS3).putObject(any(PutObjectRequest.class));

        when(amazonS3.generatePresignedUrl(any()))
                .thenThrow(new RuntimeException("URL generation failed"));

        // Act & Assert
        assertThrows(StorageException.class,
                () -> storageService.upload(pdfBytes, key));
    }
}