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

    @Operation(
        summary = "국가별 리포트 통계 조회", 
        description = "각 국가별 리포트 개수 및 통계 정보를 조회합니다. " +
                     "향후 스캠 유형별 분포, 위험도 점수, 트렌드 등의 통계 정보가 추가될 예정입니다."
    )
    @GetMapping(value = "/statistics/countries")
    public ApiResult<Slice<LocationScamSummaryItem>> getCountryStatistics(Pageable pageable){
        Slice<LocationScamSummaryItem> countrySummaryPages = reportService.getCountrySummaryPages(pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Report statistics by country", countrySummaryPages);
    }

    @Operation(
        summary = "주/도별 리포트 통계 조회", 
        description = "특정 국가 내 제1 행정구역(주별 리포트 개수 및 통계 정보를 조회합니다."
    )
    @GetMapping(value = "/statistics/states")
    public ApiResult<Slice<LocationScamSummaryItem>> getStateStatistics(@RequestParam Long countryId, Pageable pageable){
        Slice<LocationScamSummaryItem> stateSummaryPage = reportService.getStateSummaryPages(countryId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Report statistics by state", stateSummaryPage);
    }

    @Operation(
        summary = "도시별 리포트 통계 조회", 
        description = "특정 주/도 내 도시별 리포트 개수 및 통계 정보를 조회합니다."
    )
    @GetMapping(value = "/statistics/cities")
    public ApiResult<Slice<LocationScamSummaryItem>> getCityStatistics(@RequestParam Long stateId, Pageable pageable){
        Slice<LocationScamSummaryItem> citySummaryPage = reportService.getCitySummaryPages(stateId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Report statistics by city", citySummaryPage);
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
