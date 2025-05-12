package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.ReportFindAllResponse;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.dto.response.ReportMapSummaryResponse;
import com.swkim.safetrip.global.response.ApiResponse;
import com.swkim.safetrip.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final MessageSource messageSource;

    @GetMapping(value = "/reports/map-summary")
    public ApiResponse<List<ReportMapSummaryResponse>> getMapSummaryReports(@RequestParam Integer zoom){
        if(zoom <= 5){
            List<ReportMapSummaryResponse> countrySummary = reportService.getCountrySummary();
            return ApiResponse.of(HttpStatus.OK.value(), "국가별 스캠 요약 정보를 조회했습니다.", countrySummary);
        }
        List<ReportMapSummaryResponse> citySummary = reportService.getCitySummary();
        return ApiResponse.of(HttpStatus.OK.value(), "도시별 스캠 요약 정보를 조회했습니다.", citySummary);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/reports", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<Long> createReport(@RequestPart @Valid ReportSaveRequest request, @RequestPart(required = false) List<MultipartFile> images) {

        Long id = reportService.saveReport(request, images);
        String message = messageSource.getMessage("report.create.success", null, null);
        return ApiResponse.of(HttpStatus.CREATED.value(), message, id);
    }

    @GetMapping(value = "/reports")
    public ApiResponse<Page<ReportFindAllResponse>> getReports(@RequestParam(required = false) String country, @RequestParam(required = false) String city, Pageable pageable) {

        Page<ReportFindAllResponse> reports = reportService.getReports(country, city, pageable);
        String message = messageSource.getMessage("report.list.get.success", null, null);

        return ApiResponse.of(HttpStatus.OK.value(), message, reports);
    }

    @GetMapping(value = "/reports/{reportId}")
    public ApiResponse<ReportFindByIdResponse> getReport(@PathVariable Long reportId) {

        ReportFindByIdResponse report = reportService.getReport(reportId);
        String message = messageSource.getMessage("report.get.success", null, null);

        return ApiResponse.of(HttpStatus.OK.value(), message, report);
    }
}
