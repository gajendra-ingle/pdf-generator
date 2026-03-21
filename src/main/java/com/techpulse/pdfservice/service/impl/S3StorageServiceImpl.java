package com.techpulse.pdfservice.service.impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.techpulse.pdfservice.config.S3Config;
import com.techpulse.pdfservice.exception.StorageException;
import com.techpulse.pdfservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageServiceImpl implements StorageService {

    private final AmazonS3 amazonS3;
    private final S3Config s3Config;

    @Override
    public String upload(byte[] pdfBytes, String s3Key) {
        log.info("Uploading PDF to S3 — bucket: {}, key: {}", s3Config.getBucketName(), s3Key);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/pdf");
            metadata.setContentLength(pdfBytes.length);
            metadata.addUserMetadata("generated-by", "pdf-generator");

            PutObjectRequest putRequest = new PutObjectRequest(s3Config.getBucketName(), s3Key, new ByteArrayInputStream(pdfBytes), metadata);

            amazonS3.putObject(putRequest);
            log.info("PDF uploaded successfully to S3: {}", s3Key);

            return generatePresignedUrl(s3Key);
        } catch (Exception ex) {
            log.error("S3 upload failed for key: {}", s3Key, ex);
            throw new StorageException("Failed to upload PDF to S3: " + ex.getMessage(), ex);
        }
    }

    private String generatePresignedUrl(String s3Key) {
        long expiryMs = s3Config.getPresignExpirationMinutes() * 60 * 1000L;
        Date expiration = new Date(System.currentTimeMillis() + expiryMs);

        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(s3Config.getBucketName(), s3Key).withMethod(HttpMethod.GET).withExpiration(expiration);

        URL presignedUrl = amazonS3.generatePresignedUrl(urlRequest);
        log.debug("Generated presigned URL for key: {} (expires: {})", s3Key, expiration);
        return presignedUrl.toString();
    }
}

