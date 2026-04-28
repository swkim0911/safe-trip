package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.Image;
import com.swkim.safetrip.global.exception.custom.S3UploadException;
import com.swkim.safetrip.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${oci.storage.endpoint}")
    private String endpoint;

    @Value("${oci.storage.bucket-name}")
    private String bucketName;

    @Value("${oci.storage.base-path}")
    private String basePath;

    private final S3Client s3Client;
    private final ImageRepository imageRepository;

    public List<Image> findImagesByReportId(Long id) {
        return imageRepository.findImagesByReportId(id);
    }

    public List<Image> saveImagesInS3Bucket(List<MultipartFile> files) {
        ArrayList<Image> imageList = new ArrayList<>();

        Optional.ofNullable(files)
                .orElse(Collections.emptyList())
                .forEach(file -> {
                    Image image = saveImage(file);
                    imageList.add(image);
                });

        return imageList;
    }

    public void deleteImagesFromOci(List<Image> images) {
        images.forEach(image -> {
            String key = basePath + image.getStoredName();
            try {
                s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key).build());
            } catch (Exception e) {
                log.error("OCI delete failed: file={}, error={}", image.getStoredName(), e.getMessage());
            }
        });
    }

    public Image saveImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        Image image = Image.builder()
                .originalName(originalFilename)
                .build();
        String fileName = basePath + image.getStoredName();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            log.error("OCI upload failed: file={}, error={}", originalFilename, e.getMessage());
            throw new S3UploadException();
        }

        // path-style URL: {endpoint}/{bucket}/{key}
        String accessURL = endpoint + "/" + bucketName + "/" + fileName;
        image.setAccessURL(accessURL);
        return image;
    }

}
