package com.swkim.safetrip.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    private String title;
    private String category;
    private String location;
    private String url;
    private String description;
    private String advice;
}
