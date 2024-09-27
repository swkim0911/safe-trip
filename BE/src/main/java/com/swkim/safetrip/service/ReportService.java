package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.entity.Report;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public Long write(ReportRequest reportRequest) {
        //todo validation
        Report report = ReportMapper.toReport(reportRequest);

        Report savedReport = reportRepository.save(report);
        return savedReport.getId();
    }
}
