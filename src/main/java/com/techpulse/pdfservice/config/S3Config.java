package com.techpulse.pdfservice.config;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
public class S3Config {

    private String accessKey;
    private String secretKey;
    private String region;
    private String bucketName;
    private long presignExpirationMinutes = 60;
    private String endpoint;

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials));
        if (endpoint != null && !endpoint.isBlank()) {
            // LocalStack / MinIO — local dev
            log.info("Using custom S3 endpoint: {}", endpoint);
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region)).withPathStyleAccessEnabled(true);
        } else {
            // aws
            log.info("Using AWS S3 region: {}", region);
            builder.withRegion(region);
        }

        return builder.build();
    }
}

