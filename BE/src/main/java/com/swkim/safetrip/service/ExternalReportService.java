package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.ExternalReportDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalReportService {

    @Transactional(readOnly = true)
    public ExternalReportDetailResponse getExternalReport(Long id){

    }
}
