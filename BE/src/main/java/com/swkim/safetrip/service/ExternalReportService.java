package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.ExternalReportDetailResponse;
import com.swkim.safetrip.global.exception.custom.ReportNotFoundException;
import com.swkim.safetrip.repository.ExternalReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalReportService {

    private final ExternalReportRepository externalReportRepository;

    @Transactional(readOnly = true)
    public ExternalReportDetailResponse getExternalReport(Long id){
        return externalReportRepository.findReportDetailById(id).orElseThrow(ReportNotFoundException::new);
    }
}
