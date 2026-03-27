package com.swkim.safetrip.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.swkim.safetrip.entity.Image;
import com.swkim.safetrip.global.exception.custom.S3UploadException;
import com.swkim.safetrip.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    @Value("${cloud.aws.s3.base-path}")
    private String basePath;

    private final AmazonS3Client amazonS3Client;
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

    public Image saveImage(MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        Image image = Image.builder()
                .originalName(originalFilename)
                .build();
        String fileName = basePath + image.getStoredName();

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());

        try {
            amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), objectMetadata);
        } catch (IOException e) {
            log.error("S3 upload failed: file={}, error={}", originalFilename, e.getMessage());
            throw new S3UploadException();
        }

        String accessURL = amazonS3Client.getUrl(bucketName, fileName).toString();
        image.setAccessURL(accessURL);
        return image;
    }

}
