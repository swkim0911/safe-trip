package com.swkim.safetrip.mapper;

import com.swkim.safetrip.dto.request.UserReportSaveRequest;
import com.swkim.safetrip.entity.UserReport;
import com.swkim.safetrip.entity.enums.Source;

public class ReportMapper {

    public static UserReport toReport(UserReportSaveRequest userReportSaveRequest) {
        return UserReport.builder()
                .source(Source.SAFETRIP)
                .title(userReportSaveRequest.title())
                .description(userReportSaveRequest.description())
                .build();
    }
}
