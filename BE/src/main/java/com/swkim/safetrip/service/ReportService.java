package com.swkim.safetrip.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.entity.Report;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    private final ReportRepository reportRepository;
    private final AmazonS3Client amazonS3Client;

    @Transactional
    public Long write(ReportRequest reportRequest, MultipartFile image) {

        // 1. 이미지가 있으면 S3에 전송
        if (!image.isEmpty()) {
            // Image Url = s3.save(image);
        }

        // 3. address에 대한 location 객체 생성
        // 4. reportRequest -> Report Mapping
        Report report = ReportMapper.toReport(reportRequest);

        Report savedReport = reportRepository.save(report);
        return savedReport.getId();
    }


    private String saveImage(MultipartFile image){
        String originalFilename = image.getOriginalFilename();
    }


}
