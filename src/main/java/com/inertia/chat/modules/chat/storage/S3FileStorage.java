package com.inertia.chat.modules.chat.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@ConditionalOnExpression("'${spring.profiles.active:dev}'.equals('prod') or '${aws.s3.enable:false}'.equals('true')")
@RequiredArgsConstructor
public class S3FileStorage implements FileStorage {

    @Value("${aws.s3.bucket}")     private String bucket;
    @Value("${aws.s3.region}")     private String region;
    @Value("${aws.s3.access-key}") private String accessKey;
    @Value("${aws.s3.secret-key}") private String secretKey;

    private S3Client s3;

    @PostConstruct
    public void init() {
        this.s3 = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(
              StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)))
            .build();
        // ensure bucket exists
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    @Override
    public String upload(MultipartFile file) {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file", e);
        }

        s3.putObject(
          PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(file.getContentType())
            .build(),
          RequestBody.fromBytes(bytes)
        );

        // public URL pattern; adjust if you use a CDN or a different region
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed for avatars");
        }

        // Generate unique filename with original extension
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String key = "avatars/" + UUID.randomUUID() + extension;

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read avatar file", e);
        }

        s3.putObject(
          PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build(),
          RequestBody.fromBytes(bytes)
        );

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }
} 