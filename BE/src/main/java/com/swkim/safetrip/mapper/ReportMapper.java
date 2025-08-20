package com.swkim.safetrip.mapper;

import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.entity.UserReport;
import com.swkim.safetrip.entity.enums.Source;

public class ReportMapper {

    public static UserReport toReport(ReportSaveRequest reportSaveRequest) {
        return UserReport.builder()
                .source(Source.SAFETRIP)
                .title(reportSaveRequest.getTitle())
                .description(reportSaveRequest.getDescription())
                .build();
    }
}
