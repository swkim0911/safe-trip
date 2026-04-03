package com.swkim.safetrip.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class OciStorageConfig {

    @Value("${oci.storage.access-key}")
    private String accessKey;

    @Value("${oci.storage.secret-key}")
    private String secretKey;

    @Value("${oci.storage.endpoint}")
    private String endpoint;

    @Value("${oci.storage.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .forcePathStyle(true) // OCI Object Storage requires path-style access
                .build();
    }
}
