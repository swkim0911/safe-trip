package com.swkim.safetrip.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.ObjectMetadataProvider;
import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.entity.Image;
import com.swkim.safetrip.entity.Report;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    private final ReportRepository reportRepository;
    private final AmazonS3Client amazonS3Client;

    @Transactional
    public Long write(ReportRequest reportRequest, List<MultipartFile> files) {

        // 1. reportRequest -> Report Mapping
        Report report = ReportMapper.toReport(reportRequest);

        // 2. 이미지 S3에 전송
        for (MultipartFile file : files) {
            Image image = saveImage(file);
            report.addImage(image);
        }

        // 3. address에 대한 location 객체 생성
        // Json jsonResult = getLocationInfo(String lat, String lon);
        // String country = jsonResult.getCountry;
        // String city = jsonResult.getCity;
        // String cityCode = jsonResult.getCityCode;
        // Json jsonResult = getCityInfo(String cityCode);
        // String cityLat = getLat(jsonResult)
        // String cityLon = getLon(jsonResult)

        // Location location = new Location
        // Country country = new Country
        // City city = new City

        // report.setLocation(location)
        // 4. report 저장
        Report savedReport = reportRepository.save(report);
        return savedReport.getId();
    }


    private Image saveImage(MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        Image image = Image.builder()
                .originalName(originalFilename)
                .build();
        String fileName = image.getStoredName();

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());

        try {
            amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), objectMetadata);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String accessURL = amazonS3Client.getUrl(bucketName, fileName).toString();
        image.setAccessURL(accessURL);
        return image;
    }
}
