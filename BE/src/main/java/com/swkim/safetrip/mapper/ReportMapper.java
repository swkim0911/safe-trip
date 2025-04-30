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
                .scam(report.getScam().getName())
                .address(report.getLocation().getName())
                .lat(String.valueOf(report.getLocation().getLat()))
                .lng(String.valueOf(report.getLocation().getLng()))
                .URLs(URLs)
                .description(report.getDescription())
                .advice(report.getAdvice())
                .createdAt(report.getCreatedAt())
                .build();
    }

}
