package com.swkim.safetrip.mapper;

import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.UserReportDetailResponse;
import com.swkim.safetrip.entity.UserReport;
import com.swkim.safetrip.entity.enums.Source;

import java.util.List;

public class ReportMapper {

    public static UserReport toReport(ReportSaveRequest reportSaveRequest) {
        return UserReport.builder()
                .source(Source.SAFETRIP)
                .title(reportSaveRequest.getTitle())
                .description(reportSaveRequest.getDescription())
                .build();
    }

    public static UserReportDetailResponse toReportFindByIdResponse(UserReport userReport, List<String> URLs) {
        return UserReportDetailResponse.builder()
                .title(userReport.getTitle())
                .scam(userReport.getScam().getName())
                .address(userReport.getLocation().getAddress())
                .lat(String.valueOf(userReport.getLocation().getLat()))
                .lng(String.valueOf(userReport.getLocation().getLng()))
                .URLs(URLs)
                .description(userReport.getDescription())
                .advice(userReport.getAdvice())
                .createdAt(userReport.getCreatedAt())
                .build();
    }

}
