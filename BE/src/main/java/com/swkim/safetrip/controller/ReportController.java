package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.LocationSummaryItem;
import com.swkim.safetrip.dto.response.LocationSummaryResponse;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.global.response.ApiResponse;
import com.swkim.safetrip.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final MessageSource messageSource;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/reports", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<Long> createReport(@AuthenticationPrincipal UserDetails user, @RequestPart @Valid ReportSaveRequest request, @RequestPart(required = false) List<MultipartFile> images) {
        String email = user.getUsername();
        Long id = reportService.saveReport(email, request, images);
        String message = messageSource.getMessage("report.create.success", null, null);
        return ApiResponse.of(HttpStatus.CREATED.value(), message, id);
    }

    @GetMapping(value = "/reports/sidebar-summary/counties")
    public ApiResponse<Slice<LocationSummaryItem>> getSideBarCountrySummaries(Pageable pageable){
        Slice<LocationSummaryItem> countrySummaryPage = reportService.getCountrySummaryPage(pageable);
        return ApiResponse.of(HttpStatus.OK.value(), "국가별 스캠 요약 정보를 조회했습니다.", countrySummaryPage);
    }

    @GetMapping(value = "/reports/sidebar-summary/cities")
    public ApiResponse<Slice<LocationSummaryItem>> getSideBarCitySummaries(@RequestParam Long countryId, Pageable pageable){
        Slice<LocationSummaryItem> citySummaryPage = reportService.getCitySummaryPage(countryId, pageable);
        return ApiResponse.of(HttpStatus.OK.value(), "도시별 스캠 요약 정보를 조회했습니다.", citySummaryPage);
    }

    @GetMapping(value = "/reports/sidebar-summary/reports")
    public ApiResponse<Slice<ReportSummaryItem>> getSideBarScamSummaries(@RequestParam Long countryId, @RequestParam Long cityId, Pageable pageable) {
        Slice<ReportSummaryItem> scamSummaryItems = reportService.getReportSummaryPage(countryId, cityId, pageable);
        String message = messageSource.getMessage("report.list.get.success", null, null);

        return ApiResponse.of(HttpStatus.OK.value(), message, scamSummaryItems);
    }

    @GetMapping(value = "/reports/{reportId}")
    public ApiResponse<ReportFindByIdResponse> getReport(@PathVariable Long reportId) {

        ReportFindByIdResponse report = reportService.getReport(reportId);
        String message = messageSource.getMessage("report.get.success", null, null);

        return ApiResponse.of(HttpStatus.OK.value(), message, report);
    }

    @GetMapping(value = "/reports/map-summary")
    public ApiResponse<LocationSummaryResponse> getMapCountrySummaries(@RequestParam Integer zoom){
        if(zoom < 7){
            LocationSummaryResponse countrySummary = reportService.getCountrySummary();
            return ApiResponse.of(HttpStatus.OK.value(), "국가별 스캠 요약 정보를 조회했습니다.", countrySummary);
        }
        LocationSummaryResponse citySummary = reportService.getCitySummary();
        return ApiResponse.of(HttpStatus.OK.value(), "도시별 스캠 요약 정보를 조회했습니다.", citySummary);
    }
}
