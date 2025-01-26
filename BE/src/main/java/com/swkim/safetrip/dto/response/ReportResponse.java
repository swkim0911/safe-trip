package com.swkim.safetrip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private String title;

    private String category;

    private List<String> imageURLs;

    private Integer likes;

    private String description;

    private String advice;

}
