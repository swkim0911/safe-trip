package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.global.response.ApiResponse;
import com.swkim.safetrip.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping(value = "/reports", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<Long> report(@RequestPart @Valid ReportRequest request, @RequestPart(required = false) List<MultipartFile> images) {

        Long id = reportService.write(request, images);

        return new ApiResponse<>(HttpStatus.CREATED.value(), "report가 등록되었습니다.", id);
    }

    @GetMapping(value = "/reports")
    public ApiResponse<Page<ReportResponse>> getReports(@RequestParam(required = false) String country, @RequestParam(required = false) String city, Pageable pageable) {

        return ApiResponse.of(HttpStatus.OK.value(), "report 조회가 완료되었습니다.", reportService.getReports(country, city, pageable));
    }
}
