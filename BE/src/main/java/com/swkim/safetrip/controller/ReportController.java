package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.LocationScamSummaryItem;
import com.swkim.safetrip.dto.response.LocationScamSummaryResponse;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.security.CustomUserDetails;
import com.swkim.safetrip.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final MessageSource messageSource;

    @Operation(summary = "글 등록", description = "새로운 게시글을 작성하여 서버에 등록합니다", security = @SecurityRequirement(name = "BearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/reports", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResult<Long> createReport(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestPart @Valid ReportSaveRequest request, @RequestPart(required = false) List<MultipartFile> images) {
        String email = userDetails.getUsername();
        Long id = reportService.saveReport(email, request, images);
        return ApiResult.of(HttpStatus.CREATED.value(), "User report registration successful", id);
    }

    @Operation(summary = "국가별 스캠 요약 정보 조회", description = "사이드바에 표현될 국가별 스캠 요약 정보를 조회합니다")
    @GetMapping(value = "/reports/sidebar-summary/counties")
    public ApiResult<Slice<LocationScamSummaryItem>> getSideBarCountrySummaries(Pageable pageable){
        Slice<LocationScamSummaryItem> countrySummaryPage = reportService.getCountrySummaryPage(pageable);
        return ApiResult.of(HttpStatus.OK.value(), "국가별 스캠 요약 정보를 조회했습니다.", countrySummaryPage);
    }

    @Operation(summary = "도시별 스캠 요약 정보 조회", description = "사이드바에 표현될 도시별 스캠 요약 정보를 조회합니다")
    @GetMapping(value = "/reports/sidebar-summary/cities")
    public ApiResult<Slice<LocationScamSummaryItem>> getSideBarCitySummaries(@RequestParam Long countryId, Pageable pageable){
        Slice<LocationScamSummaryItem> citySummaryPage = reportService.getCitySummaryPage(countryId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "도시별 스캠 요약 정보를 조회했습니다.", citySummaryPage);
    }

    @Operation(summary = "스캠 요약 정보 조회", description = "사이드바에 표현될 스캠 요약 정보를 조회합니다")
    @GetMapping(value = "/reports/sidebar-summary/reports")
    public ApiResult<Slice<ReportSummaryItem>> getSideBarScamSummaries(@RequestParam Long countryId, @RequestParam Long cityId, Pageable pageable) {
        Slice<ReportSummaryItem> scamSummaryItems = reportService.getReportSummaryPage(countryId, cityId, pageable);
        String message = messageSource.getMessage("report.list.get.success", null, null);

        return ApiResult.of(HttpStatus.OK.value(), message, scamSummaryItems);
    }

    @Operation(summary = "특정 리포트 조회", description = "특정 리포트를 아이디로 조회합니다")
    @GetMapping(value = "/reports/{reportId}")
    public ApiResult<ReportFindByIdResponse> getReport(@PathVariable Long reportId) {

        ReportFindByIdResponse report = reportService.getReport(reportId);
        String message = messageSource.getMessage("report.get.success", null, null);

        return ApiResult.of(HttpStatus.OK.value(), message, report);
    }

    @Operation(summary = "지도에 표시될 스캠 정보 조회", description = "zoom 정도에 따라 도시에 표시될 스캠 요약 정보를 조회합니다")
    @GetMapping(value = "/reports/map-summary")
    public ApiResult<LocationScamSummaryResponse> getLocationSummariesOfMap(@RequestParam Integer zoom){
        if(zoom < 7){
            LocationScamSummaryResponse countrySummaries = reportService.getCountrySummaries();
            return ApiResult.of(HttpStatus.OK.value(), "Country scam summaries for map display.", countrySummaries);
        }
        LocationScamSummaryResponse stateSummaries = reportService.getStateSummaries();
        return ApiResult.of(HttpStatus.OK.value(), "State scam summaries for map display.", stateSummaries);
    }
}
