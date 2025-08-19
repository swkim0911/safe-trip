package com.swkim.safetrip.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalReportDetailResponse {

    private String source;

    private String sourceUrl;

    private String title;

    private String scamName;

    private String countryName;

    private String stateName;

    private String description;

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime originalCreatedAt;

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime collectedAt;
}
