package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.entity.Report;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public Long enroll(ReportRequest reportRequest) {
        //todo validation
        Report report = ReportMapper.toReport(reportRequest);

        Report savedReport = reportRepository.save(report);
        return savedReport.getId();
    }
}
