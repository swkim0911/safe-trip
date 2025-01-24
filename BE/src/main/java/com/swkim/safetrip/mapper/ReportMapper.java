package com.swkim.safetrip.mapper;

import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.entity.Location;
import com.swkim.safetrip.entity.Report;

public class ReportMapper {

    // todo User 객체 넣기
    public static Report toReport(ReportRequest reportRequest) {
        return Report.builder()
                .title(reportRequest.getTitle())
                .category(reportRequest.getCategory())
                .location(null)
                .likeCnt(0)
                .description(reportRequest.getDescription())
                .advice(reportRequest.getAdvice())
                .build();
    }
}
