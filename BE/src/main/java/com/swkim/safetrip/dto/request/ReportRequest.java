package com.swkim.safetrip.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequest {

    private Long id;
    private String title;
    private String content;
    private String advice;
    private String location;
}
