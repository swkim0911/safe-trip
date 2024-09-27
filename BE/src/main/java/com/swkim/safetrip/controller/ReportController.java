package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping(value = "/reports", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Long> report(@RequestPart ReportRequest request, @RequestPart(required = false) MultipartFile image) {

        Long id = reportService.enroll(request, image);

        return new ResponseEntity<>(1L, HttpStatus.CREATED);
    }

}
