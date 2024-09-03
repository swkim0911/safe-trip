package com.swkim.safetrip.mapper;

import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.entity.Report;

public class ReportMapper {

    public static Report toReport(ReportRequest reportRequest) {
        return Report.builder()
                .title(reportRequest.getTitle())
                .category(reportRequest.getCategory())
                .location(reportRequest.getLocation())
                .url(reportRequest.getUrl())
                .description(reportRequest.getDescription())
                .advice(reportRequest.getAdvice())
                .build();
    }
}
