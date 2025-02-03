package com.swkim.safetrip.mapper;

import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.entity.Report;
import com.swkim.safetrip.entity.enums.Category;

public class ReportMapper {

    // todo User 객체 넣기
    public static Report toReport(ReportSaveRequest reportSaveRequest) {
        return Report.builder()
                .title(reportSaveRequest.getTitle())
                .category(reportSaveRequest.getCategory())
                .location(null)
                .likes(0)
                .description(reportSaveRequest.getDescription())
                .advice(reportSaveRequest.getAdvice())
                .build();
    }

    public static ReportFindByIdResponse toReportFindByIdResponse(Report report) {
        return ReportFindByIdResponse.builder()
                .title(report.getTitle())
                .category(report.getCategory().name())
                .URLs(report.getImages())

    }

}
