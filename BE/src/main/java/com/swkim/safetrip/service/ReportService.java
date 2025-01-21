package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.entity.Report;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.ReportRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

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
}
