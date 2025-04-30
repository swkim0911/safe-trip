package com.swkim.safetrip.mapper;

import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.entity.Report;

import java.util.List;

public class ReportMapper {

    // todo User 객체 넣기
    public static Report toReport(ReportSaveRequest reportSaveRequest) {
        return Report.builder()
                .title(reportSaveRequest.getTitle())
                .description(reportSaveRequest.getDescription())
                .advice(reportSaveRequest.getAdvice())
                .build();
    }

    public static ReportFindByIdResponse toReportFindByIdResponse(Report report, List<String> URLs) {
        return ReportFindByIdResponse.builder()
                .title(report.getTitle())
                .category(report.getScam().name())
                .URLs(URLs)
                .latitude(String.valueOf(report.getLocation().getLat()))
                .longitude(String.valueOf(report.getLocation().getLng()))
                .description(report.getDescription())
                .advice(report.getAdvice())
                .createdAt(report.getCreatedAt())
                .build();

    }

}
