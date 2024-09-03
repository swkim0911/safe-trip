package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/reports")
    public ResponseEntity<Long> report(@RequestBody ReportRequest reportRequest) {

        Long id = reportService.enroll(reportRequest);

        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }
}
