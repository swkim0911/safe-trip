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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "국가별 스캠 요약 정보 조회", description = "사이드바에 표현될 국가별 스캠 요약 정보를 조회합니다")
    @GetMapping(value = "/sidebar-summary/counties")
    public ApiResult<Slice<LocationScamSummaryItem>> getCountrySummariesForSideBar(Pageable pageable){
        Slice<LocationScamSummaryItem> countrySummaryPages = reportService.getCountrySummaryPages(pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Country scam summaries for sidebar", countrySummaryPages);
    }

    @Operation(summary = "제1 행정구역별 스캠 요약 정보 조회", description = "사이드바에 표현될 제1 행정구역(주)별 스캠 요약 정보를 조회합니다")
    @GetMapping(value = "/sidebar-summary/states")
    public ApiResult<Slice<LocationScamSummaryItem>> getStateSummariesForSidebar(@RequestParam Long countryId, Pageable pageable){
        Slice<LocationScamSummaryItem> stateSummaryPage = reportService.getStateSummaryPages(countryId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "State scam summaries for sidebar", stateSummaryPage);
    }

    @Operation(summary = "도시별 스캠 요약 정보 조회", description = "사이드바에 도시별 스캠 요약 정보를 조회합니다")
    @GetMapping(value = "/sidebar-summary/cities")
    public ApiResult<Slice<LocationScamSummaryItem>> getCitySummariesForSidebar(@RequestParam Long stateId, Pageable pageable){
        Slice<LocationScamSummaryItem> citySummaryPage = reportService.getCitySummaryPages(stateId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "City scam summaries for sidebar", citySummaryPage);
    }

    @Operation(summary = "스캠 리포트 요약 정보 조회", description = "사이드바에 표현될 스캠 리포트들의 요약 정보를 조회합니다")
    @GetMapping(value = "/sidebar-summary")
    public ApiResult<Slice<ReportSummaryItem>> getReportSummariesForSidebar(@RequestParam Long cityId, Pageable pageable) {
        Slice<ReportSummaryItem> reportSummaryPages = reportService.getReportSummaryPages(cityId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Report summaries for sidebar", reportSummaryPages);
    }

    @Operation(summary = "지도에 표시될 스캠 정보 조회", description = "zoom 정도에 따라 도시에 표시될 스캠 요약 정보를 조회합니다")
    @GetMapping(value = "/map-summary")
    public ApiResult<LocationScamSummaryResponse> getLocationSummariesForMap(@RequestParam Integer zoom){
        LocationScamSummaryResponse summaries;

        if (zoom < 6) {
            summaries = reportService.getCountrySummaries();
        } else if (zoom < 9) {
            summaries = reportService.getStateSummaries();
        } else {
            summaries = reportService.getCitySummaries();
        }
        return ApiResult.of(HttpStatus.OK.value(), "scam summaries for map view", summaries);
    }
}
