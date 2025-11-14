package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.response.LocationScamSummaryItem;
import com.swkim.safetrip.dto.response.LocationScamSummaryResponse;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(
        summary = "리포트 개요 조회",
        description = "지도 zoom 레벨에 따라 적절한 집계 단위(국가/주/도시)의 리포트 개요 정보를 반환합니다."
    )
    @GetMapping("/overview")
    public ApiResult<LocationScamSummaryResponse> getReportOverview(@RequestParam Integer zoom){
        LocationScamSummaryResponse summaries;

        if (zoom < 6) {
            summaries = reportService.getCountrySummaries();
        } else if (zoom < 9) {
            summaries = reportService.getStateSummaries();
        } else {
            summaries = reportService.getCitySummaries();
        }
        return ApiResult.of(HttpStatus.OK.value(), "Report overview", summaries);
    }
}
