package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.ReportFindAllResponse;
import com.swkim.safetrip.global.response.ApiResponse;
import com.swkim.safetrip.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/reports", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<Long> createReport(@RequestPart @Valid ReportSaveRequest request, @RequestPart(required = false) List<MultipartFile> images) {

        Long id = reportService.saveReport(request, images);

        return new ApiResponse<>(HttpStatus.CREATED.value(), "report 등록이 완료되었습니다.", id);
    }

    @GetMapping(value = "/reports")
    public ApiResponse<Page<ReportFindAllResponse>> getReports(@RequestParam(required = false) String country, @RequestParam(required = false) String city, Pageable pageable) {

        Page<ReportFindAllResponse> reports = reportService.getReports(country, city, pageable);

        return ApiResponse.of(HttpStatus.OK.value(), "report 목록 조회가 완료되었습니다.", reports);
    }

    @GetMapping(value = "/reports/{id}")
    public ApiResponse<ReportFindByIdResponse> getReport(@PathVariable Long id) {

        ReportFindByIdResponse report = reportService.getReport(id);

        return ApiResponse.of(HttpStatus.OK.value(), "report 조회가 완료되었습니다.", report);
    }
}
